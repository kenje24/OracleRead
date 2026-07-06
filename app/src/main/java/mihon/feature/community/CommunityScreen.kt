package mihon.feature.community

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.util.Screen
import kotlinx.coroutines.flow.update
import mihon.domain.community.model.CommunityComment
import mihon.domain.community.model.CommunityNotification
import mihon.domain.community.model.CommunityPost
import mihon.domain.community.model.CommunityProfilePage
import mihon.domain.community.model.CommunitySort
import mihon.domain.community.model.CreateComment
import mihon.domain.community.model.CreateReport
import mihon.domain.community.model.CreatePost
import mihon.domain.community.model.FriendRequestStatus
import mihon.domain.community.model.PostType
import mihon.domain.community.repository.CommunityRepository
import tachiyomi.core.common.util.lang.launchIO
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.screens.LoadingScreen
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class CommunityScreen(
    private val mangaId: Long,
) : Screen() {

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { CommunityScreenModel(mangaId) }
        val state by screenModel.state.collectAsState()

        when (val value = state) {
            CommunityScreenState.Loading -> LoadingScreen()
            is CommunityScreenState.Error -> CommunityScaffold("Community", navigator::pop, screenModel::refresh) {
                item("error") { ErrorText(value.message) }
            }
            is CommunityScreenState.CreateCommunity -> CreateCommunityContent(
                state = value,
                navigateUp = navigator::pop,
                onRefresh = screenModel::refresh,
                onSignIn = screenModel::signIn,
                onSignUp = screenModel::signUp,
                onCreate = screenModel::createCommunity,
            )
            is CommunityScreenState.Feed -> CommunityFeedContent(
                state = value,
                navigateUp = navigator::pop,
                onRefresh = screenModel::refresh,
                onSignIn = screenModel::signIn,
                onSignUp = screenModel::signUp,
                onNewPost = { navigator.push(CommunityNewPostScreen(mangaId, value.community.id, value.community.slug)) },
                onOpenPost = { navigator.push(CommunityDiscussionScreen(mangaId, it)) },
                onOpenProfile = { navigator.push(CommunityProfileScreen(it)) },
                onVotePost = screenModel::votePost,
                onDeletePost = screenModel::softDeletePost,
                onToggleFollow = screenModel::toggleFollow,
            )
        }
    }
}

class CommunityHubScreen : Screen() {

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { CommunityHubScreenModel() }
        val state by screenModel.state.collectAsState()
        var showSortSheet by remember { mutableStateOf(false) }

        CommunityScaffold("Oracle discussions", navigator::pop, screenModel::refresh) {
            if (!state.signedIn) {
                item("auth-only") {
                    AuthPanel(
                        signedIn = false,
                        working = state.working,
                        message = state.message ?: "Please log in to OracleRead to browse community discussions.",
                        onSignIn = screenModel::signIn,
                        onSignUp = screenModel::signUp,
                    )
                }
                return@CommunityScaffold
            }
            item("search") {
                Row(
                    modifier = Modifier.padding(MaterialTheme.padding.medium),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = screenModel::search,
                        label = { Text("Search discussions") },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { showSortSheet = true }) {
                        Icon(Icons.Outlined.Sort, contentDescription = "Sort discussions")
                    }
                }
                state.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
            if (state.loading) {
                item("loading") { Text("Loading...", modifier = Modifier.padding(MaterialTheme.padding.medium)) }
            } else if (state.posts.isEmpty()) {
                item("empty") {
                    Text(
                        text = "No discussions found.",
                        modifier = Modifier.padding(MaterialTheme.padding.medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(state.posts, key = { it.id }) { post ->
                PostItem(
                    post = post,
                    currentUserId = state.currentUserId,
                    onOpen = { navigator.push(CommunityDiscussionScreen(-1L, post)) },
                    onOpenProfile = { userId -> navigator.push(CommunityProfileScreen(userId)) },
                    onVote = { screenModel.votePost(post.id, it) },
                    onDelete = { screenModel.softDeletePost(post.id) },
                    onHide = { screenModel.hidePost(post.id) },
                    onReport = { screenModel.reportPost(post) },
                )
            }
        }
        if (showSortSheet) {
            ModalBottomSheet(onDismissRequest = { showSortSheet = false }) {
                Text(
                    text = "Sort discussions",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium),
                )
                CommunitySort.entries.forEach { sort ->
                    TextButton(
                        onClick = {
                            screenModel.setSort(sort)
                            showSortSheet = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = if (sort == CommunitySort.QA) "Q&A" else sort.name,
                            modifier = Modifier.weight(1f),
                        )
                        if (state.sort == sort) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = "Selected")
                        }
                    }
                }
            }
        }
    }
}

class CommunityNewPostScreen(
    private val mangaId: Long,
    private val communityId: String,
    private val slug: String,
) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { NewPostScreenModel(communityId) }
        val state by screenModel.state.collectAsState()

        var title by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        var type by remember { mutableStateOf(PostType.Discussion) }
        var spoiler by remember { mutableStateOf(false) }

        CommunityScaffold("New post", navigator::pop, {}) {
            item("form") {
                Column(
                    modifier = Modifier.padding(MaterialTheme.padding.medium),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium),
                ) {
                    Text("oracle/$slug", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = MaterialTheme.padding.small),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (title.isBlank()) {
                                    Text(
                                        text = "Title",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )
                    PostTypePicker(type = type, onTypeChange = { type = it })
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Content") },
                        minLines = 8,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = spoiler, onCheckedChange = { spoiler = it })
                        Text("Spoiler")
                    }
                    state.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(
                        enabled = !state.working && title.isNotBlank() && content.isNotBlank(),
                        onClick = {
                            screenModel.createPost(title, content, type, spoiler) {
                                navigator.replace(CommunityScreen(mangaId))
                            }
                        },
                    ) {
                        Text(if (state.working) "Posting..." else "Post discussion")
                    }
                }
            }
        }
    }
}

class CommunityDiscussionScreen(
    private val mangaId: Long,
    private val initialPost: CommunityPost,
) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { DiscussionScreenModel(initialPost) }
        val state by screenModel.state.collectAsState()

        CommunityScaffold("Discussion", navigator::pop, screenModel::refresh) {
            item("post") {
                DiscussionHeader(
                    post = state.post,
                    onVote = screenModel::votePost,
                    onDelete = screenModel::softDeletePost,
                    onReport = screenModel::reportPost,
                )
            }
            item("reply") {
                ReplyBox(
                    signedIn = state.signedIn,
                    working = state.working,
                    message = state.message,
                    replyTarget = state.replyTarget,
                    onCancelReply = { screenModel.setReplyTarget(null) },
                    onReply = { screenModel.createComment(it) },
                )
            }
            if (state.comments.isEmpty()) {
                item("empty") {
                    Text(
                        text = "No comments yet.",
                        modifier = Modifier.padding(MaterialTheme.padding.medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            val topLevelComments = state.comments.filter { it.parentCommentId == null }
            items(topLevelComments, key = { it.id }) { comment ->
                CommentThreadPreview(
                    comment = comment,
                    allComments = state.comments,
                    onVote = { commentId, value -> screenModel.voteComment(commentId, value) },
                    onDelete = screenModel::softDeleteComment,
                    onReply = screenModel::setReplyTarget,
                    onOpenProfile = { navigator.push(CommunityProfileScreen(it)) },
                    onOpenThread = { navigator.push(CommunityCommentThreadScreen(initialPost, comment)) },
                    onReport = screenModel::reportComment,
                )
            }
        }
    }
}

class CommunityCommentThreadScreen(
    private val post: CommunityPost,
    private val rootComment: CommunityComment,
) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { DiscussionScreenModel(post) }
        val state by screenModel.state.collectAsState()
        val comments = state.comments.threadFor(rootComment.id)

        CommunityScaffold("Replies", navigator::pop, screenModel::refresh) {
            item("root") {
                CommentItem(
                    comment = rootComment,
                    indentLevel = 0,
                    onVote = { screenModel.voteComment(rootComment.id, it) },
                    onDelete = { screenModel.softDeleteComment(rootComment.id) },
                    onReply = { screenModel.setReplyTarget(rootComment) },
                    onOpenProfile = { navigator.push(CommunityProfileScreen(it)) },
                    onReport = { screenModel.reportComment(rootComment) },
                )
            }
            item("reply-box") {
                ReplyBox(
                    signedIn = state.signedIn,
                    working = state.working,
                    message = state.message,
                    replyTarget = state.replyTarget ?: rootComment,
                    onCancelReply = { screenModel.setReplyTarget(null) },
                    onReply = { screenModel.createComment(it) },
                )
            }
            items(comments, key = { it.id }) { comment ->
                CommentItem(
                    comment = comment,
                    indentLevel = 1,
                    onVote = { screenModel.voteComment(comment.id, it) },
                    onDelete = { screenModel.softDeleteComment(comment.id) },
                    onReply = { screenModel.setReplyTarget(comment) },
                    onOpenProfile = { navigator.push(CommunityProfileScreen(it)) },
                    onReport = { screenModel.reportComment(comment) },
                )
            }
        }
    }
}

@Composable
private fun CreateCommunityContent(
    state: CommunityScreenState.CreateCommunity,
    navigateUp: () -> Unit,
    onRefresh: () -> Unit,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String, String) -> Unit,
    onCreate: (String, String, String) -> Unit,
) {
    var title by remember(state.title) { mutableStateOf(state.title) }
    var slug by remember(state.slug) { mutableStateOf(state.slug) }
    var description by remember(state.description) { mutableStateOf(state.description) }

    CommunityScaffold("Create community", navigateUp, onRefresh) {
        item("auth") {
            if (!state.signedIn) {
                AuthPanel(
                    signedIn = false,
                    working = state.working,
                    message = state.message ?: "Please log in to OracleRead to create this community.",
                    onSignIn = onSignIn,
                    onSignUp = onSignUp,
                )
            }
        }
        if (state.signedIn) {
            item("form") {
                Column(
                    modifier = Modifier.padding(MaterialTheme.padding.medium),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                ) {
                    Text(text = state.manga.title, style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Community title") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = slug,
                        onValueChange = { slug = it },
                        label = { Text("Slug") },
                        prefix = { Text("oracle/") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = { onCreate(title, slug, description) },
                        enabled = !state.working && title.isNotBlank() && slug.isNotBlank(),
                    ) {
                        Text(if (state.working) "Creating..." else "Create community")
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityFeedContent(
    state: CommunityScreenState.Feed,
    navigateUp: () -> Unit,
    onRefresh: () -> Unit,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String, String) -> Unit,
    onNewPost: () -> Unit,
    onOpenPost: (CommunityPost) -> Unit,
    onOpenProfile: (String) -> Unit,
    onVotePost: (String, Int) -> Unit,
    onDeletePost: (String) -> Unit,
    onToggleFollow: () -> Unit,
) {
    var showDescription by remember { mutableStateOf(true) }

    CommunityScaffold(
        title = "oracle/${state.community.slug}",
        navigateUp = navigateUp,
        onRefresh = onRefresh,
        floatingActionButton = {
            if (state.signedIn) {
                ExtendedFloatingActionButton(
                    text = { Text("New post") },
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    onClick = onNewPost,
                )
            }
        },
    ) {
        if (!state.signedIn) {
            item("auth-only") {
                AuthPanel(
                    signedIn = false,
                    working = state.working,
                    message = state.message ?: "Please log in to OracleRead to view this community.",
                    onSignIn = onSignIn,
                    onSignUp = onSignUp,
                )
            }
            return@CommunityScaffold
        }
        item("header") {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.padding.medium),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Column(
                    modifier = Modifier.padding(MaterialTheme.padding.medium),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                ) {
                    Text(text = state.community.title, style = MaterialTheme.typography.headlineSmall)
                    Text("oracle/${state.community.slug}", color = MaterialTheme.colorScheme.primary)
                    state.community.description?.takeIf { it.isNotBlank() }?.let {
                        TextButton(onClick = { showDescription = !showDescription }) {
                            Icon(
                                imageVector = if (showDescription) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                contentDescription = null,
                            )
                            Text(if (showDescription) "Hide description" else "About this community")
                        }
                        AnimatedVisibility(showDescription) { Text(it) }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CommunityMetric("${state.community.memberCount}", "Members", Modifier.weight(1f))
                        CommunityMetric("${state.posts.size}", "Posts", Modifier.weight(1f))
                    }
                    Button(onClick = onToggleFollow, modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            imageVector = if (state.community.isFollowing) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                        )
                        Text(if (state.community.isFollowing) "Following community" else "Follow community")
                    }
                    if (!state.message.isNullOrBlank()) {
                        Text(state.message, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        if (state.posts.isEmpty()) {
            item("empty") {
                Text(
                    text = "No posts yet. Start the discussion.",
                    modifier = Modifier.padding(MaterialTheme.padding.medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(state.posts, key = { it.id }) { post ->
            PostItem(
                post = post,
                coverImage = state.community.coverImage,
                currentUserId = state.currentUserId,
                onOpen = { onOpenPost(post) },
                onOpenProfile = onOpenProfile,
                onVote = { onVotePost(post.id, it) },
                onDelete = { onDeletePost(post.id) },
                onHide = {},
                onReport = {},
            )
        }
    }
}

@Composable
private fun CommunityMetric(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.padding.small)) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CommunityScaffold(
    title: String,
    navigateUp: () -> Unit,
    onRefresh: () -> Unit,
    floatingActionButton: @Composable () -> Unit = {},
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    Scaffold(
        topBar = {
            AppBar(
                title = title,
                navigateUp = navigateUp,
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(imageVector = Icons.Outlined.Refresh, contentDescription = "Refresh community")
                    }
                },
            )
        },
        floatingActionButton = floatingActionButton,
    ) { paddingValues ->
        FastScrollLazyColumn(contentPadding = paddingValues, content = content)
    }
}

@Composable
internal fun AuthPanel(
    signedIn: Boolean,
    working: Boolean,
    message: String?,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String, String) -> Unit,
) {
    if (signedIn && message.isNullOrBlank()) return
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var register by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(false) }

    val feedback = message?.toAuthFeedback()

    Column(
        modifier = Modifier.padding(MaterialTheme.padding.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium),
    ) {
        Text(if (register) "Create OracleRead account" else "Log in to OracleRead", style = MaterialTheme.typography.titleLarge)
        Text(
            text = if (register) {
                "Create an account to post, vote, and join manga discussions."
            } else {
                "Log in to post, vote, and read Oracle discussions."
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        feedback?.let {
            AuthFeedbackRow(it)
        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (register) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { acceptedTerms = !acceptedTerms },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = acceptedTerms,
                    onCheckedChange = { acceptedTerms = it },
                )
                Text(
                    text = "I agree to the Terms and Conditions and Community Guidelines.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Button(
            onClick = {
                if (register) onSignUp(email, password, username) else onSignIn(email, password)
            },
            enabled = !working && email.isNotBlank() && password.length >= 6 &&
                (!register || username.length >= 3 && acceptedTerms),
        ) {
            Text(
                when {
                    working && register -> "Creating account..."
                    working -> "Logging in..."
                    register -> "Register"
                    else -> "Log in"
                },
            )
        }
        TextButton(enabled = !working, onClick = { register = !register }) {
            Text(if (register) "Already have an account? Log in" else "No account? Register")
        }
    }
}

@Composable
private fun PostItem(
    post: CommunityPost,
    coverImage: String? = post.community?.coverImage,
    currentUserId: String?,
    onOpen: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onVote: (Int) -> Unit,
    onDelete: () -> Unit,
    onHide: () -> Unit,
    onReport: () -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MaterialTheme.padding.medium,
                    top = MaterialTheme.padding.small,
                    end = MaterialTheme.padding.medium,
                ),
            verticalAlignment = Alignment.Top,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
            ) {
                if (!coverImage.isNullOrBlank()) {
                    AsyncImage(
                        model = coverImage,
                        contentDescription = post.community?.title?.let { "$it cover" } ?: "Manga cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 64.dp, height = 88.dp)
                            .clip(MaterialTheme.shapes.small),
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(width = 64.dp, height = 88.dp),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Forum, contentDescription = null)
                        }
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
                ) {
                    Text(
                        text = if (post.isRemoved) "User has deleted this discussion" else post.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (post.isRemoved) {
                        Text("Deleted discussion", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        post.community?.slug?.let { Text("oracle/$it", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
                        ) {
                            Text(
                                text = post.author?.visibleName ?: "Unknown user",
                                color = if (post.authorId == ORACLEREAD_CREATOR_ID) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.clickable { onOpenProfile(post.authorId) },
                            )
                            CreatorAwareProfileIcon(post.authorId)
                        }
                        Text(
                            text = "${post.type.name} - ${post.commentCount} comments",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "Discussion options")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Hide this topic") },
                        onClick = {
                            menuExpanded = false
                            onHide()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Report this topic") },
                        onClick = {
                            menuExpanded = false
                            onReport()
                        },
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = MaterialTheme.padding.extraLarge, end = MaterialTheme.padding.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
        ) {
            TextButton(onClick = onOpen) {
                Text("Open discussion")
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
            VoteButton(value = 1, selected = post.myVote == 1, count = post.upvotes, onVote = onVote)
            VoteButton(value = -1, selected = post.myVote == -1, count = post.downvotes, onVote = onVote)
            if (!post.isRemoved && currentUserId == post.authorId) {
                IconButton(onClick = { confirmDelete = true }) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete discussion")
                }
            }
        }
        HorizontalDivider()
    }
    if (confirmDelete) {
        ConfirmDeleteDialog(
            title = "Delete discussion?",
            message = "This will hide your discussion and show that the user deleted it.",
            onConfirm = {
                confirmDelete = false
                onDelete()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun DiscussionHeader(post: CommunityPost, onVote: (Int) -> Unit, onDelete: () -> Unit, onReport: () -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(MaterialTheme.padding.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                if (post.isRemoved) "User has deleted this discussion" else post.title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "Discussion options")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Report this topic") },
                        onClick = {
                            menuExpanded = false
                            onReport()
                        },
                    )
                }
            }
        }
        if (!post.isRemoved) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small)) {
                Text(
                    post.author?.visibleName ?: "Unknown user",
                    color = if (post.authorId == ORACLEREAD_CREATOR_ID) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
                CreatorAwareProfileIcon(post.authorId)
            }
            Text(post.content)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            VoteButton(value = 1, selected = post.myVote == 1, count = post.upvotes, onVote = onVote)
            VoteButton(value = -1, selected = post.myVote == -1, count = post.downvotes, onVote = onVote)
            if (!post.isRemoved) {
                TextButton(onClick = { confirmDelete = true }) {
                    Icon(Icons.Outlined.Delete, contentDescription = null)
                    Text("Delete")
                }
            }
        }
    }
    if (confirmDelete) {
        ConfirmDeleteDialog(
            title = "Delete discussion?",
            message = "This will hide your discussion and show that the user deleted it.",
            onConfirm = {
                confirmDelete = false
                onDelete()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun ReplyBox(
    signedIn: Boolean,
    working: Boolean,
    message: String?,
    replyTarget: CommunityComment? = null,
    onCancelReply: () -> Unit = {},
    onReply: (String) -> Unit,
) {
    var comment by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MaterialTheme.padding.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
    ) {
        replyTarget?.author?.visibleName?.let { name ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Replying to @$name", color = MaterialTheme.colorScheme.primary)
                TextButton(onClick = onCancelReply) { Text("Cancel") }
            }
        }
        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Comment or @username") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )
        message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = {
                onReply(comment)
                comment = ""
            },
            enabled = signedIn && !working && comment.isNotBlank(),
        ) {
            Text(if (working) "Posting..." else "Reply")
        }
    }
}

@Composable
private fun CommentThreadPreview(
    comment: CommunityComment,
    allComments: List<CommunityComment>,
    onVote: (String, Int) -> Unit,
    onDelete: (String) -> Unit,
    onReply: (CommunityComment) -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenThread: () -> Unit,
    onReport: (CommunityComment) -> Unit,
) {
    val replies = allComments.filter { it.parentCommentId == comment.id }
    CommentItem(
        comment = comment,
        indentLevel = 0,
        onVote = { onVote(comment.id, it) },
        onDelete = { onDelete(comment.id) },
        onReply = { onReply(comment) },
        onOpenProfile = onOpenProfile,
        onReport = { onReport(comment) },
    )
    replies.take(2).forEach { reply ->
        CommentItem(
            comment = reply,
            indentLevel = 1,
            onVote = { onVote(reply.id, it) },
            onDelete = { onDelete(reply.id) },
            onReply = { onReply(reply) },
            onOpenProfile = onOpenProfile,
            onReport = { onReport(reply) },
        )
    }
    if (replies.size >= 3) {
        TextButton(
            onClick = onOpenThread,
            modifier = Modifier.padding(start = MaterialTheme.padding.extraLarge),
        ) {
            Text("View ${replies.size} replies")
        }
        HorizontalDivider()
    }
}

@Composable
private fun CommentItem(
    comment: CommunityComment,
    indentLevel: Int,
    onVote: (Int) -> Unit,
    onDelete: () -> Unit,
    onReply: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onReport: () -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(
            start = if (indentLevel == 0) MaterialTheme.padding.medium else MaterialTheme.padding.extraLarge,
            top = MaterialTheme.padding.small,
            end = MaterialTheme.padding.medium,
            bottom = MaterialTheme.padding.small,
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
            ) {
                if (!comment.isRemoved) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small)) {
                        Text(
                            comment.author?.visibleName ?: "Unknown user",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (comment.authorId == ORACLEREAD_CREATOR_ID) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onOpenProfile(comment.authorId) },
                        )
                        CreatorAwareProfileIcon(comment.authorId)
                    }
                }
                Text(if (comment.isRemoved) "User has deleted this comment" else comment.content)
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "Comment options")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Report comment") },
                        onClick = {
                            menuExpanded = false
                            onReport()
                        },
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            VoteButton(value = 1, selected = comment.myVote == 1, count = comment.upvotes, onVote = onVote)
            VoteButton(value = -1, selected = comment.myVote == -1, count = comment.downvotes, onVote = onVote)
            if (!comment.isRemoved) {
                TextButton(onClick = onReply) { Text("Reply") }
                TextButton(onClick = { confirmDelete = true }) { Text("Delete") }
            }
        }
        HorizontalDivider()
    }
    if (confirmDelete) {
        ConfirmDeleteDialog(
            title = "Delete comment?",
            message = "This will hide your comment and show that the user deleted it.",
            onConfirm = {
                confirmDelete = false
                onDelete()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun CreatorAwareProfileIcon(authorId: String) {
    if (authorId == ORACLEREAD_CREATOR_ID) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.WorkspacePremium,
                contentDescription = "OracleRead creator",
                tint = MaterialTheme.colorScheme.tertiary,
            )
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
            )
        }
    } else {
        Icon(
            imageVector = Icons.Outlined.AccountCircle,
            contentDescription = "Profile",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AuthFeedbackRow(feedback: AuthFeedback) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
    ) {
        Icon(
            imageVector = if (feedback.success) Icons.Filled.CheckCircle else Icons.Filled.Error,
            contentDescription = null,
            tint = feedback.color,
        )
        Text(text = feedback.message, color = feedback.color, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun VoteButton(value: Int, selected: Boolean, count: Int, onVote: (Int) -> Unit) {
    val color by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "voteColor",
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        label = "voteScale",
    )
    TextButton(onClick = { onVote(value) }) {
        Icon(
            imageVector = if (value > 0) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
            contentDescription = if (value > 0) "Upvote" else "Downvote",
            tint = color,
            modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale),
        )
        Text("$count", color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostTypePicker(type: PostType, onTypeChange: (PostType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = type.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Post type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            PostType.entries.forEach {
                DropdownMenuItem(
                    text = { Text(it.name) },
                    onClick = {
                        onTypeChange(it)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(
        text = message,
        modifier = Modifier.padding(MaterialTheme.padding.medium),
        color = MaterialTheme.colorScheme.error,
    )
}

class NewPostScreenModel(
    private val communityId: String,
    private val repository: CommunityRepository = Injekt.get(),
) : StateScreenModel<NewPostScreenModel.State>(State()) {

    fun createPost(title: String, content: String, type: PostType, spoiler: Boolean, onSuccess: () -> Unit) {
        screenModelScope.launchIO {
            mutableState.value = state.value.copy(working = true, message = null)
            repository.createPost(CreatePost(communityId, title.trim(), content.trim(), type, spoiler))
                .onSuccess { onSuccess() }
                .onFailure { mutableState.value = state.value.copy(working = false, message = it.message ?: "Unable to create post") }
        }
    }

    data class State(val working: Boolean = false, val message: String? = null)
}

class CommunityHubScreenModel(
    private val repository: CommunityRepository = Injekt.get(),
) : StateScreenModel<CommunityHubScreenModel.State>(
    State(currentUserId = repository.currentSession?.userId, signedIn = repository.isSignedIn),
) {

    init {
        refresh()
    }

    fun search(query: String) {
        mutableState.update { it.copy(query = query) }
        refresh()
    }

    fun setSort(sort: CommunitySort) {
        mutableState.update { it.copy(sort = sort) }
        refresh()
    }

    fun refresh() {
        screenModelScope.launchIO {
            mutableState.update { it.copy(loading = true, message = null) }
            val state = state.value
            repository.searchPosts(state.query, state.sort)
                .onSuccess { posts ->
                    mutableState.update {
                        it.copy(
                            loading = false,
                            posts = posts,
                            currentUserId = repository.currentSession?.userId,
                            signedIn = repository.isSignedIn,
                        )
                    }
                }
                .onFailure { error ->
                    mutableState.update {
                        it.copy(
                            loading = false,
                            signedIn = repository.isSignedIn,
                            message = error.message ?: "Unable to load discussions",
                        )
                    }
                }
        }
    }

    fun signIn(email: String, password: String) {
        screenModelScope.launchIO {
            mutableState.update { it.copy(working = true, message = null) }
            repository.signIn(email, password)
                .onSuccess {
                    mutableState.update { it.copy(working = false, signedIn = true, message = "Logged in successfully!") }
                    refresh()
                }
                .onFailure { error ->
                    mutableState.update { it.copy(working = false, signedIn = false, message = friendlyAuthError(error.message, "Unable to log in")) }
                }
        }
    }

    fun signUp(email: String, password: String, username: String) {
        screenModelScope.launchIO {
            mutableState.update { it.copy(working = true, message = null) }
            repository.signUp(email, password, username)
                .onSuccess {
                    mutableState.update {
                        it.copy(
                            working = false,
                            signedIn = repository.isSignedIn,
                            message = if (repository.isSignedIn) "Successfully registered!" else "Successfully registered. Log in to continue.",
                        )
                    }
                    if (repository.isSignedIn) refresh()
                }
                .onFailure { error ->
                    mutableState.update { it.copy(working = false, signedIn = false, message = friendlyAuthError(error.message, "Unable to register")) }
                }
        }
    }

    fun votePost(postId: String, value: Int) {
        screenModelScope.launchIO {
            repository.votePost(postId, value)
                .onSuccess {
                    mutableState.update { state ->
                        state.copy(posts = state.posts.map { if (it.id == postId) it.applyVote(value) else it })
                    }
                }
                .onFailure { error -> mutableState.update { it.copy(message = error.message ?: "Unable to vote") } }
        }
    }

    fun softDeletePost(postId: String) {
        screenModelScope.launchIO {
            repository.softDeletePost(postId)
                .onSuccess {
                    mutableState.update { state ->
                        state.copy(posts = state.posts.map { if (it.id == postId) it.copy(isRemoved = true) else it })
                    }
                }
                .onFailure { error -> mutableState.update { it.copy(message = error.message ?: "Unable to delete discussion") } }
        }
    }

    fun hidePost(postId: String) {
        screenModelScope.launchIO {
            repository.hidePost(postId)
                .onSuccess { mutableState.update { state -> state.copy(posts = state.posts.filterNot { it.id == postId }) } }
                .onFailure { error -> mutableState.update { it.copy(message = error.message ?: "Unable to hide discussion") } }
        }
    }

    fun reportPost(post: CommunityPost) {
        screenModelScope.launchIO {
            repository.report(
                CreateReport(
                    communityId = post.communityId,
                    targetType = "post",
                    postId = post.id,
                    reason = "Reported from app",
                ),
            )
                .onSuccess { mutableState.update { it.copy(message = "Report sent.") } }
                .onFailure { error -> mutableState.update { it.copy(message = error.message ?: "Unable to report discussion") } }
        }
    }

    data class State(
        val query: String = "",
        val sort: CommunitySort = CommunitySort.New,
        val posts: List<CommunityPost> = emptyList(),
        val currentUserId: String? = null,
        val signedIn: Boolean = false,
        val working: Boolean = false,
        val loading: Boolean = false,
        val message: String? = null,
    )
}

private data class AuthFeedback(
    val message: String,
    val success: Boolean,
    val color: Color,
)

@Composable
private fun String.toAuthFeedback(): AuthFeedback {
    val success = contains("success", ignoreCase = true) ||
        contains("logged in", ignoreCase = true) ||
        contains("created", ignoreCase = true) ||
        contains("registered", ignoreCase = true)
    return AuthFeedback(
        message = this,
        success = success,
        color = if (success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
    )
}

private fun friendlyAuthError(message: String?, fallback: String): String {
    val text = message.orEmpty()
    return when {
        text.contains("invalid", ignoreCase = true) && text.contains("credential", ignoreCase = true) ->
            "Wrong password or account does not exist."
        text.contains("already", ignoreCase = true) || text.contains("registered", ignoreCase = true) ->
            "That email is already registered. Log in instead."
        text.contains("expired", ignoreCase = true) ->
            "Session expired. Please log in again."
        text.isNotBlank() -> text
        else -> fallback
    }
}

private const val ORACLEREAD_CREATOR_ID = "9b01c237-695d-429d-9812-780edc73b6c6"

class DiscussionScreenModel(
    initialPost: CommunityPost,
    private val repository: CommunityRepository = Injekt.get(),
) : StateScreenModel<DiscussionScreenModel.State>(State(post = initialPost, signedIn = repository.isSignedIn)) {

    init {
        refresh()
    }

    fun refresh() {
        screenModelScope.launchIO {
            repository.getComments(state.value.post.id)
                .onSuccess { comments -> mutableState.update { it.copy(comments = comments, signedIn = repository.isSignedIn) } }
                .onFailure { mutableState.update { it.copy(message = it.message ?: "Unable to load comments") } }
        }
    }

    fun setReplyTarget(comment: CommunityComment?) {
        mutableState.update { it.copy(replyTarget = comment) }
    }

    fun createComment(content: String) {
        screenModelScope.launchIO {
            mutableState.update { it.copy(working = true, message = null) }
            val current = state.value
            val replyTarget = current.replyTarget
            val cleanedContent = content.trim()
            val parentCommentId = replyTarget?.parentCommentId ?: replyTarget?.id
            val finalContent = if (replyTarget?.parentCommentId != null) {
                val username = replyTarget.author?.username ?: replyTarget.author?.visibleName ?: "user"
                "@$username $cleanedContent"
            } else {
                cleanedContent
            }
            repository.createComment(
                CreateComment(
                    postId = current.post.id,
                    parentCommentId = parentCommentId,
                    content = finalContent,
                ),
            )
                .onSuccess {
                    mutableState.update { current -> current.copy(working = false, message = null, replyTarget = null) }
                    refresh()
                }
                .onFailure { error -> mutableState.update { it.copy(working = false, message = error.message ?: "Unable to comment") } }
        }
    }

    fun votePost(value: Int) {
        screenModelScope.launchIO {
            repository.votePost(state.value.post.id, value)
                .onSuccess { mutableState.update { it.copy(post = it.post.applyVote(value)) } }
                .onFailure { error -> mutableState.update { it.copy(message = error.message ?: "Unable to vote") } }
        }
    }

    fun voteComment(commentId: String, value: Int) {
        screenModelScope.launchIO {
            repository.voteComment(commentId, value)
                .onSuccess {
                    mutableState.update { state ->
                        state.copy(comments = state.comments.map { if (it.id == commentId) it.applyVote(value) else it })
                    }
                }
                .onFailure { error -> mutableState.update { it.copy(message = error.message ?: "Unable to vote") } }
        }
    }

    fun softDeletePost() {
        screenModelScope.launchIO {
            repository.softDeletePost(state.value.post.id)
                .onSuccess { mutableState.update { it.copy(post = it.post.copy(isRemoved = true)) } }
                .onFailure { error -> mutableState.update { it.copy(message = error.message ?: "Unable to delete discussion") } }
        }
    }

    fun reportPost() {
        screenModelScope.launchIO {
            val post = state.value.post
            repository.report(
                CreateReport(
                    communityId = post.communityId,
                    targetType = "post",
                    postId = post.id,
                    reason = "Reported from app",
                ),
            )
                .onSuccess { mutableState.update { it.copy(message = "Report sent.") } }
                .onFailure { error -> mutableState.update { it.copy(message = error.message ?: "Unable to report discussion") } }
        }
    }

    fun softDeleteComment(commentId: String) {
        screenModelScope.launchIO {
            repository.softDeleteComment(commentId)
                .onSuccess {
                    mutableState.update { state ->
                        state.copy(comments = state.comments.map { if (it.id == commentId) it.copy(isRemoved = true) else it })
                    }
                }
                .onFailure { error -> mutableState.update { it.copy(message = error.message ?: "Unable to delete comment") } }
        }
    }

    fun reportComment(comment: CommunityComment) {
        screenModelScope.launchIO {
            repository.report(
                CreateReport(
                    communityId = state.value.post.communityId,
                    targetType = "comment",
                    commentId = comment.id,
                    reason = "Reported from app",
                ),
            )
                .onSuccess { mutableState.update { it.copy(message = "Report sent.") } }
                .onFailure { error -> mutableState.update { it.copy(message = error.message ?: "Unable to report comment") } }
        }
    }

    data class State(
        val post: CommunityPost,
        val comments: List<CommunityComment> = emptyList(),
        val signedIn: Boolean,
        val working: Boolean = false,
        val message: String? = null,
        val replyTarget: CommunityComment? = null,
    )
}

class CommunityProfileScreen(
    private val userId: String? = null,
) : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { CommunityProfileScreenModel(userId) }
        val state by screenModel.state.collectAsState()

        CommunityScaffold("Profile", navigator::pop, screenModel::refresh) {
            when (val value = state) {
                CommunityProfileScreenModel.State.Loading -> item("loading") {
                    Text("Loading...", modifier = Modifier.padding(MaterialTheme.padding.medium))
                }
                is CommunityProfileScreenModel.State.Error -> item("error") { ErrorText(value.message) }
                is CommunityProfileScreenModel.State.Ready -> item("profile") {
                    Column(
                        modifier = Modifier.padding(MaterialTheme.padding.medium),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.medium),
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Column(
                                modifier = Modifier.padding(MaterialTheme.padding.medium),
                                verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
                            ) {
                                Icon(Icons.Outlined.AccountCircle, contentDescription = null, modifier = Modifier.size(52.dp))
                                Text(value.page.profile.visibleName, style = MaterialTheme.typography.headlineSmall)
                                Text("@${value.page.profile.username}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        if (!value.page.isSelf && !value.page.isFriend) {
                            when (value.page.friendRequestStatus) {
                                FriendRequestStatus.Outgoing -> AssistChip(onClick = {}, label = { Text("Friend request sent") })
                                FriendRequestStatus.Incoming -> Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small)) {
                                    Button(onClick = screenModel::acceptFriendRequest) {
                                        Text("Accept")
                                    }
                                    TextButton(onClick = screenModel::declineFriendRequest) {
                                        Text("Decline")
                                    }
                                }
                                null -> Button(onClick = screenModel::requestFriend) {
                                    Icon(Icons.Outlined.PersonAdd, contentDescription = null)
                                    Text("Add friend")
                                }
                            }
                        } else if (!value.page.isSelf) {
                            AssistChip(onClick = {}, label = { Text("Friends") })
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small)) {
                            CommunityMetric("${value.page.followedCommunities.size}", "Following", Modifier.weight(1f))
                            CommunityMetric("${value.page.friends.size}", "Friends", Modifier.weight(1f))
                        }
                        ProfileBentoSection(
                            title = "Following",
                            emptyText = "No followed communities yet.",
                            lines = value.page.followedCommunities.map { "oracle/${it.slug}" },
                        )
                        ProfileBentoSection(
                            title = "Friends",
                            emptyText = "No friends yet.",
                            lines = value.page.friends.map { it.visibleName },
                        )
                        ProfileBentoSection(
                            title = "Recent replies",
                            emptyText = "Only friends can see recent replies.",
                            lines = value.page.recentComments.map { it.content },
                        )
                        value.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileBentoSection(title: String, emptyText: String, lines: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.padding.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (lines.isEmpty()) {
                Text(emptyText, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                lines.take(5).forEach { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

class CommunityNotificationsScreen : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { CommunityNotificationsScreenModel() }
        val state by screenModel.state.collectAsState()
        CommunityScaffold("Notifications", navigator::pop, screenModel::refresh) {
            if (state.loading) {
                item("loading") { Text("Loading...", modifier = Modifier.padding(MaterialTheme.padding.medium)) }
            } else if (state.notifications.isEmpty()) {
                item("empty") { Text("No notifications yet.", modifier = Modifier.padding(MaterialTheme.padding.medium)) }
            }
            items(state.notifications, key = { it.id }) { notification ->
                Column(
                    modifier = Modifier.padding(MaterialTheme.padding.medium),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.extraSmall),
                ) {
                    Text(notification.title, fontWeight = FontWeight.SemiBold)
                    notification.body?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    if (notification.type == "friend_request" && notification.friendRequestId != null && !notification.isRead) {
                        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small)) {
                            Button(onClick = { screenModel.acceptFriendRequest(notification.friendRequestId) }) {
                                Text("Accept")
                            }
                            TextButton(onClick = { screenModel.declineFriendRequest(notification.friendRequestId) }) {
                                Text("Decline")
                            }
                        }
                    }
                }
                HorizontalDivider()
            }
            state.message?.let { item("message") { ErrorText(it) } }
        }
    }
}

class CommunityProfileScreenModel(
    private val userId: String?,
    private val repository: CommunityRepository = Injekt.get(),
) : StateScreenModel<CommunityProfileScreenModel.State>(State.Loading) {

    init {
        refresh()
    }

    fun refresh() {
        screenModelScope.launchIO {
            mutableState.value = State.Loading
            repository.getProfile(userId)
                .onSuccess { mutableState.value = State.Ready(it) }
                .onFailure { mutableState.value = State.Error(it.message ?: "Unable to load profile") }
        }
    }

    fun requestFriend() {
        val current = state.value as? State.Ready ?: return
        val target = current.page.profile.id ?: return
        screenModelScope.launchIO {
            repository.requestFriend(target)
                .onSuccess {
                    repository.getProfile(userId)
                        .onSuccess { mutableState.value = State.Ready(it, message = "Friend request sent.") }
                }
                .onFailure { mutableState.value = current.copy(message = it.message ?: "Unable to send friend request") }
        }
    }

    fun acceptFriendRequest() {
        val current = state.value as? State.Ready ?: return
        val requestId = current.page.friendRequestId ?: return
        screenModelScope.launchIO {
            repository.acceptFriendRequest(requestId)
                .onSuccess {
                    repository.getProfile(userId)
                        .onSuccess { mutableState.value = State.Ready(it, message = "Friend request accepted.") }
                }
                .onFailure { mutableState.value = current.copy(message = it.message ?: "Unable to accept friend request") }
        }
    }

    fun declineFriendRequest() {
        val current = state.value as? State.Ready ?: return
        val requestId = current.page.friendRequestId ?: return
        screenModelScope.launchIO {
            repository.declineFriendRequest(requestId)
                .onSuccess {
                    repository.getProfile(userId)
                        .onSuccess { mutableState.value = State.Ready(it, message = "Friend request declined.") }
                }
                .onFailure { mutableState.value = current.copy(message = it.message ?: "Unable to decline friend request") }
        }
    }

    sealed class State {
        data object Loading : State()
        data class Error(val message: String) : State()
        data class Ready(val page: CommunityProfilePage, val message: String? = null) : State()
    }
}

class CommunityNotificationsScreenModel(
    private val repository: CommunityRepository = Injekt.get(),
) : StateScreenModel<CommunityNotificationsScreenModel.State>(State()) {

    init {
        refresh()
    }

    fun refresh() {
        screenModelScope.launchIO {
            mutableState.update { it.copy(loading = true, message = null) }
            repository.getNotifications()
                .onSuccess { notifications -> mutableState.value = State(notifications = notifications) }
                .onFailure { error -> mutableState.value = State(message = error.message ?: "Unable to load notifications") }
        }
    }

    fun acceptFriendRequest(requestId: String) {
        screenModelScope.launchIO {
            repository.acceptFriendRequest(requestId)
                .onSuccess {
                    mutableState.update { it.copy(message = "Friend request accepted.") }
                    refresh()
                }
                .onFailure { error -> mutableState.update { it.copy(message = error.message ?: "Unable to accept friend request") } }
        }
    }

    fun declineFriendRequest(requestId: String) {
        screenModelScope.launchIO {
            repository.declineFriendRequest(requestId)
                .onSuccess {
                    mutableState.update { it.copy(message = "Friend request declined.") }
                    refresh()
                }
                .onFailure { error -> mutableState.update { it.copy(message = error.message ?: "Unable to decline friend request") } }
        }
    }

    data class State(
        val loading: Boolean = false,
        val notifications: List<CommunityNotification> = emptyList(),
        val message: String? = null,
    )
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

private fun CommunityComment.applyVote(value: Int): CommunityComment {
    val old = myVote
    val next = value
    return copy(
        myVote = next,
        upvotes = upvotes + voteDelta(old, next, 1),
        downvotes = downvotes + voteDelta(old, next, -1),
    )
}

private fun List<CommunityComment>.threadFor(rootCommentId: String): List<CommunityComment> {
    val direct = filter { it.parentCommentId == rootCommentId }
    return direct + direct.flatMap { threadFor(it.id) }
}

private fun voteDelta(old: Int, next: Int, target: Int): Int {
    return (if (next == target) 1 else 0) - (if (old == target) 1 else 0)
}
