package mihon.feature.community

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mihon.data.community.toCommunitySlug
import mihon.domain.community.model.Community
import mihon.domain.community.model.CommunityComment
import mihon.domain.community.model.CommunityPost
import mihon.domain.community.model.CreateComment
import mihon.domain.community.model.CreateCommunity
import mihon.domain.community.model.CreatePost
import mihon.domain.community.model.PostType
import mihon.domain.community.repository.CommunityRepository
import tachiyomi.domain.manga.interactor.GetManga
import tachiyomi.domain.manga.model.Manga

@AssistedInject
class CommunityViewModel(
    @Assisted private val mangaId: Long,
    private val getManga: GetManga,
    private val repository: CommunityRepository,
) : ViewModel() {

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(mangaId: Long): CommunityViewModel
    }

    val state: StateFlow<CommunityScreenState>
        field = MutableStateFlow<CommunityScreenState>(CommunityScreenState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val manga = getManga.await(mangaId)
            if (manga == null) {
                state.value = CommunityScreenState.Error("Entry not found")
                return@launch
            }

            if (!repository.isConfigured) {
                state.value = CommunityScreenState.Error(
                    "Community is not configured. Rebuild with SUPABASE_URL and SUPABASE_ANON_KEY.",
                )
                return@launch
            }

            val mangaIdentifier = manga.communityIdentifier()
            repository.getCommunity(mangaIdentifier, manga.title)
                .onSuccess { community ->
                    if (community == null) {
                        state.value = CommunityScreenState.CreateCommunity(
                            manga = manga,
                            mangaIdentifier = mangaIdentifier,
                            title = manga.title,
                            slug = manga.title.toCommunitySlug(),
                            description = manga.description.orEmpty(),
                            coverImage = manga.thumbnailUrl,
                            signedIn = repository.isSignedIn,
                        )
                    } else {
                        loadFeed(manga, mangaIdentifier, community)
                    }
                }
                .onFailure { state.value = CommunityScreenState.Error(it.message ?: "Unable to load community") }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            setWorkingMessage(true, null)
            repository.signIn(email, password)
                .onSuccess { refresh() }
                .onFailure {
                    setWorkingMessage(false, friendlyAuthError(it.message, "Unable to sign in"))
                }
        }
    }

    fun signUp(email: String, password: String, username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            setWorkingMessage(true, null)
            repository.signUp(email, password, username)
                .onSuccess {
                    if (repository.isSignedIn) {
                        refresh()
                    } else {
                        setWorkingMessage(false, "Account created. You can log in now.")
                    }
                }
                .onFailure {
                    setWorkingMessage(false, friendlyAuthError(it.message, "Unable to register"))
                }
        }
    }

    fun createCommunity(title: String, slug: String, description: String) {
        val current = state.value as? CommunityScreenState.CreateCommunity ?: return
        viewModelScope.launch(Dispatchers.IO) {
            state.value = current.copy(working = true, message = null)
            repository.createCommunity(
                CreateCommunity(
                    mangaIdentifier = current.mangaIdentifier,
                    title = title.trim(),
                    slug = slug.toCommunitySlug(),
                    description = description.trim().ifBlank { null },
                    coverImage = current.coverImage,
                ),
            )
                .onSuccess { loadFeed(current.manga, current.mangaIdentifier, it) }
                .onFailure { state.value = current.copy(working = false, message = it.message) }
        }
    }

    fun createPost(title: String, content: String, type: PostType, spoiler: Boolean) {
        val current = state.value as? CommunityScreenState.Feed ?: return
        viewModelScope.launch(Dispatchers.IO) {
            state.value = current.copy(working = true, message = null)
            repository.createPost(
                CreatePost(
                    communityId = current.community.id,
                    title = title.trim(),
                    content = content.trim(),
                    type = type,
                    isSpoiler = spoiler,
                ),
            )
                .onSuccess { refresh() }
                .onFailure { state.value = current.copy(working = false, message = it.message) }
        }
    }

    fun selectPost(post: CommunityPost?) {
        val current = state.value as? CommunityScreenState.Feed ?: return
        state.value = current.copy(selectedPost = post, comments = emptyList(), message = null)
        if (post != null) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.getComments(post.id)
                    .onSuccess { comments ->
                        state.update {
                            (it as? CommunityScreenState.Feed)?.copy(comments = comments) ?: it
                        }
                    }
                    .onFailure { updateMessage(it.message ?: "Unable to load comments") }
            }
        }
    }

    fun createComment(postId: String, parentCommentId: String?, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createComment(
                CreateComment(postId = postId, parentCommentId = parentCommentId, content = content.trim()),
            )
                .onSuccess { selectPost((state.value as? CommunityScreenState.Feed)?.selectedPost) }
                .onFailure { updateMessage(it.message ?: "Unable to add comment") }
        }
    }

    private suspend fun loadFeed(manga: Manga, mangaIdentifier: String, community: Community) {
        repository.getPosts(community.id)
            .onSuccess {
                state.value = CommunityScreenState.Feed(
                    manga = manga,
                    mangaIdentifier = mangaIdentifier,
                    community = community,
                    posts = it,
                    signedIn = repository.isSignedIn,
                    currentUserId = repository.currentSession?.userId,
                )
            }
            .onFailure { state.value = CommunityScreenState.Error(it.message ?: "Unable to load posts") }
    }

    fun votePost(postId: String, value: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.votePost(postId, value)
                .onSuccess {
                    state.update { state ->
                        (state as? CommunityScreenState.Feed)?.copy(
                            posts = state.posts.map { if (it.id == postId) it.applyVote(value) else it },
                        ) ?: state
                    }
                }
                .onFailure { updateMessage(it.message ?: "Unable to vote") }
        }
    }

    fun softDeletePost(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.softDeletePost(postId)
                .onSuccess {
                    state.update { state ->
                        (state as? CommunityScreenState.Feed)?.copy(
                            posts = state.posts.map { if (it.id == postId) it.copy(isRemoved = true) else it },
                        ) ?: state
                    }
                }
                .onFailure { updateMessage(it.message ?: "Unable to delete discussion") }
        }
    }

    fun toggleFollow() {
        val current = state.value as? CommunityScreenState.Feed ?: return
        val nextFollowing = !current.community.isFollowing
        state.value = current.copy(
            community = current.community.copy(
                isFollowing = nextFollowing,
                memberCount = (current.community.memberCount + if (nextFollowing) 1 else -1).coerceAtLeast(0),
            ),
            message = if (nextFollowing) "Following this community." else "Unfollowed this community.",
        )
        viewModelScope.launch(Dispatchers.IO) {
            val result = if (current.community.isFollowing) {
                repository.unfollowCommunity(current.community.id)
            } else {
                repository.followCommunity(current.community.id)
            }
            result
                .onSuccess { refresh() }
                .onFailure {
                    state.value = current.copy(message = it.message ?: "Unable to update follow")
                }
        }
    }

    private fun updateMessage(message: String) {
        state.update {
            when (it) {
                is CommunityScreenState.CreateCommunity -> it.copy(message = message)
                is CommunityScreenState.Feed -> it.copy(message = message)
                else -> it
            }
        }
    }

    private fun setWorkingMessage(working: Boolean, message: String?) {
        state.update {
            when (it) {
                is CommunityScreenState.CreateCommunity -> it.copy(working = working, message = message)
                is CommunityScreenState.Feed -> it.copy(working = working, message = message)
                else -> it
            }
        }
    }
}

sealed class CommunityScreenState {
    data object Loading : CommunityScreenState()
    data class Error(val message: String) : CommunityScreenState()

    @Immutable
    data class CreateCommunity(
        val manga: Manga,
        val mangaIdentifier: String,
        val title: String,
        val slug: String,
        val description: String,
        val coverImage: String?,
        val signedIn: Boolean,
        val working: Boolean = false,
        val message: String? = null,
    ) : CommunityScreenState()

    @Immutable
    data class Feed(
        val manga: Manga,
        val mangaIdentifier: String,
        val community: Community,
        val posts: List<CommunityPost>,
        val signedIn: Boolean,
        val currentUserId: String?,
        val selectedPost: CommunityPost? = null,
        val comments: List<CommunityComment> = emptyList(),
        val working: Boolean = false,
        val message: String? = null,
    ) : CommunityScreenState()
}

private fun Manga.communityIdentifier(): String {
    return "source:$source:${url.trim()}"
}

private fun friendlyAuthError(message: String?, fallback: String): String {
    val text = message.orEmpty()
    return when {
        text.contains("already", ignoreCase = true) || text.contains("registered", ignoreCase = true) ->
            "That email is already registered. Log in instead."
        text.contains("invalid", ignoreCase = true) || text.contains("credentials", ignoreCase = true) ->
            "Email or password is incorrect."
        text.isNotBlank() -> text
        else -> fallback
    }
}

private fun CommunityPost.applyVote(value: Int): CommunityPost {
    val old = myVote
    val next = value
    return copy(
        myVote = next,
        upvotes = upvotes + voteDelta(old, next, 1),
        downvotes = downvotes + voteDelta(old, next, -1),
    )
}

private fun voteDelta(old: Int, next: Int, target: Int): Int {
    return (if (next == target) 1 else 0) - (if (old == target) 1 else 0)
}
