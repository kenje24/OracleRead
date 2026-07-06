package mihon.data.community

import android.app.Application
import android.content.Context
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.network.NetworkHelper
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mihon.domain.community.model.Community
import mihon.domain.community.model.CommunityComment
import mihon.domain.community.model.CommunityNotification
import mihon.domain.community.model.CommunityPost
import mihon.domain.community.model.CommunityProfile
import mihon.domain.community.model.CommunityProfilePage
import mihon.domain.community.model.CommunitySession
import mihon.domain.community.model.CommunitySort
import mihon.domain.community.model.CreateComment
import mihon.domain.community.model.CreateCommunity
import mihon.domain.community.model.CreatePost
import mihon.domain.community.model.CreateReport
import mihon.domain.community.model.FriendRequestStatus
import mihon.domain.community.repository.CommunityRepository
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import tachiyomi.core.common.util.lang.withIOContext
import java.io.IOException

class SupabaseCommunityRepository(
    app: Application,
    networkHelper: NetworkHelper,
    private val json: Json,
) : CommunityRepository {

    private val client = networkHelper.client
    private val prefs = app.getSharedPreferences("oracleread_community", Context.MODE_PRIVATE)
    private val baseUrl = BuildConfig.SUPABASE_URL.trimEnd('/')
    private val anonKey = BuildConfig.SUPABASE_ANON_KEY

    override val isConfigured: Boolean
        get() = baseUrl.isNotBlank() && anonKey.isNotBlank()

    override val isSignedIn: Boolean
        get() = accessToken != null && userId != null

    override val currentSession: CommunitySession?
        get() = userId?.let { CommunitySession(it) }

    private val accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)

    private val userId: String?
        get() = prefs.getString(KEY_USER_ID, null)

    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        ensureConfigured()
        val body = json.encodeToString(PasswordAuthRequest(email = email, password = password))
        val auth = request("$baseUrl/auth/v1/token?grant_type=password") {
            post(body.toRequestBody(JSON))
            header("apikey", anonKey)
        }.let { json.decodeFromString(AuthResponse.serializer(), it) }
        saveSession(auth)
    }

    override suspend fun signUp(email: String, password: String, username: String): Result<Unit> = runCatching {
        ensureConfigured()
        val body = json.encodeToString(
            SignUpRequest(
                email = email,
                password = password,
                data = mapOf("username" to username),
            ),
        )
        val auth = request("$baseUrl/auth/v1/signup") {
            post(body.toRequestBody(JSON))
            header("apikey", anonKey)
        }.let { json.decodeFromString(AuthResponse.serializer(), it) }

        if (auth.accessToken.isNotBlank()) {
            saveSession(auth)
        }
    }

    override suspend fun saveSession(accessToken: String, userId: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_USER_ID, userId)
            .apply()
        if (isConfigured) {
            val profile = mapOf(
                "id" to userId,
                "username" to "reader_${userId.take(8)}",
            )
            request("$baseUrl/rest/v1/profiles?on_conflict=id") {
                post(json.encodeToString(profile).toRequestBody(JSON))
                header("Prefer", "resolution=ignore-duplicates")
            }
        }
    }

    override suspend fun clearSession() {
        prefs.edit().clear().apply()
    }

    override suspend fun getCommunity(mangaIdentifier: String?, title: String): Result<Community?> = runCatching {
        ensureConfigured()
        val slug = title.toCommunitySlug()
        val url = "$baseUrl/rest/v1/communities".toHttpUrl().newBuilder()
            .addQueryParameter("select", "*")
            .addQueryParameter(
                "or",
                buildString {
                    append("(slug.eq.$slug")
                    if (!mangaIdentifier.isNullOrBlank()) append(",manga_identifier.eq.$mangaIdentifier")
                    append(")")
                },
            )
            .addQueryParameter("limit", "1")
            .build()

        request(url.toString()) { get() }
            .decodeList(Community.serializer())
            .firstOrNull()
            ?.withFollowState()
    }

    override suspend fun createCommunity(input: CreateCommunity): Result<Community> = runCatching {
        ensureSignedIn()
        request("$baseUrl/rest/v1/communities") {
            post(json.encodeToString(input).toRequestBody(JSON))
            header("Prefer", "return=representation")
        }.decodeList(Community.serializer()).first()
    }

    override suspend fun getPosts(communityId: String): Result<List<CommunityPost>> = runCatching {
        ensureConfigured()
        val url = "$baseUrl/rest/v1/posts".toHttpUrl().newBuilder()
            .addQueryParameter("select", "*,profiles!posts_author_id_fkey(id,username,display_name)")
            .addQueryParameter("community_id", "eq.$communityId")
            .addQueryParameter("order", "created_at.desc")
            .build()

        request(url.toString()) { get() }
            .decodeList(CommunityPost.serializer())
            .filterHiddenPosts()
            .withPostVotes()
    }

    override suspend fun searchPosts(query: String, sort: CommunitySort): Result<List<CommunityPost>> = runCatching {
        ensureConfigured()
        val order = when (sort) {
            CommunitySort.Best -> "score.desc"
            CommunitySort.Top -> "upvotes.desc"
            CommunitySort.New -> "created_at.desc"
            CommunitySort.Controversial -> "downvotes.desc,upvotes.desc"
            CommunitySort.Old -> "created_at.asc"
            CommunitySort.QA -> "created_at.desc"
        }
        val urlBuilder = "$baseUrl/rest/v1/posts".toHttpUrl().newBuilder()
            .addQueryParameter("select", "*,communities(title,slug,cover_image),profiles!posts_author_id_fkey(id,username,display_name)")
            .addQueryParameter("order", order)
            .addQueryParameter("limit", "100")

        if (sort == CommunitySort.QA) {
            urlBuilder.addQueryParameter("type", "eq.question")
        }
        if (query.isNotBlank()) {
            val pattern = "*${query.trim()}*"
            urlBuilder.addQueryParameter("or", "(title.ilike.$pattern,content.ilike.$pattern)")
        }

        request(urlBuilder.build().toString()) { get() }
            .decodeList(CommunityPost.serializer())
            .filterHiddenPosts()
            .withPostVotes()
    }

    override suspend fun createPost(input: CreatePost): Result<CommunityPost> = runCatching {
        ensureSignedIn()
        joinCommunity(input.communityId)
        request("$baseUrl/rest/v1/posts") {
            post(json.encodeToString(input).toRequestBody(JSON))
            header("Prefer", "return=representation")
        }.decodeList(CommunityPost.serializer()).first()
    }

    override suspend fun getComments(postId: String): Result<List<CommunityComment>> = runCatching {
        ensureConfigured()
        val url = "$baseUrl/rest/v1/comments".toHttpUrl().newBuilder()
            .addQueryParameter("select", "*,profiles!comments_author_id_fkey(id,username,display_name)")
            .addQueryParameter("post_id", "eq.$postId")
            .addQueryParameter("order", "created_at.asc")
            .build()

        request(url.toString()) { get() }
            .decodeList(CommunityComment.serializer())
            .withReplyCounts()
            .withCommentVotes()
    }

    override suspend fun createComment(input: CreateComment): Result<CommunityComment> = runCatching {
        ensureSignedIn()
        joinCommunity(getPostCommunityId(input.postId))
        request("$baseUrl/rest/v1/comments") {
            post(json.encodeToString(input).toRequestBody(JSON))
            header("Prefer", "return=representation")
        }.decodeList(CommunityComment.serializer()).first().also {
            createMentionNotifications(input.content, input.postId, it.id)
        }
    }

    override suspend fun votePost(postId: String, value: Int): Result<Unit> = upsertVote(postId = postId, commentId = null, value = value)

    override suspend fun voteComment(commentId: String, value: Int): Result<Unit> = upsertVote(postId = null, commentId = commentId, value = value)

    override suspend fun softDeletePost(postId: String): Result<Unit> = runCatching {
        ensureSignedIn()
        val url = "$baseUrl/rest/v1/posts".toHttpUrl().newBuilder()
            .addQueryParameter("id", "eq.$postId")
            .build()
        request(url.toString()) {
            patch(json.encodeToString(mapOf("is_removed" to true)).toRequestBody(JSON))
            header("Prefer", "return=minimal")
        }
    }

    override suspend fun softDeleteComment(commentId: String): Result<Unit> = runCatching {
        ensureSignedIn()
        val url = "$baseUrl/rest/v1/comments".toHttpUrl().newBuilder()
            .addQueryParameter("id", "eq.$commentId")
            .build()
        request(url.toString()) {
            patch(json.encodeToString(mapOf("is_removed" to true)).toRequestBody(JSON))
            header("Prefer", "return=minimal")
        }
    }

    override suspend fun followCommunity(communityId: String): Result<Unit> = runCatching {
        ensureSignedIn()
        joinCommunity(communityId)
    }

    override suspend fun unfollowCommunity(communityId: String): Result<Unit> = runCatching {
        ensureSignedIn()
        val url = "$baseUrl/rest/v1/community_members".toHttpUrl().newBuilder()
            .addQueryParameter("community_id", "eq.$communityId")
            .addQueryParameter("user_id", "eq.$userId")
            .build()
        request(url.toString()) {
            delete()
            header("Prefer", "return=minimal")
        }
    }

    override suspend fun hidePost(postId: String): Result<Unit> = runCatching {
        ensureSignedIn()
        request("$baseUrl/rest/v1/hidden_posts?on_conflict=user_id,post_id") {
            post(json.encodeToString(HiddenPostUpsert(postId = postId, userId = userId.orEmpty())).toRequestBody(JSON))
            header("Prefer", "resolution=ignore-duplicates")
        }
    }

    override suspend fun report(input: CreateReport): Result<Unit> = runCatching {
        ensureSignedIn()
        request("$baseUrl/rest/v1/reports") {
            post(json.encodeToString(input).toRequestBody(JSON))
            header("Prefer", "return=minimal")
        }
    }

    override suspend fun getNotifications(): Result<List<CommunityNotification>> = runCatching {
        ensureSignedIn()
        val url = "$baseUrl/rest/v1/community_notifications".toHttpUrl().newBuilder()
            .addQueryParameter("select", "*")
            .addQueryParameter("user_id", "eq.$userId")
            .addQueryParameter("order", "created_at.desc")
            .addQueryParameter("limit", "100")
            .build()
        request(url.toString()) { get() }.decodeList(CommunityNotification.serializer())
    }

    override suspend fun getProfile(userId: String?): Result<CommunityProfilePage> = runCatching {
        ensureSignedIn()
        val targetUserId = userId ?: this.userId ?: throw IOException("Session expired. Please log in again.")
        val profile = getProfileById(targetUserId)
        val followed = getFollowedCommunities(targetUserId)
        val friends = getFriends(targetUserId)
        val isFriend = targetUserId != this.userId && isFriend(targetUserId)
        val friendRequest = if (targetUserId == this.userId || isFriend) null else getPendingFriendRequest(targetUserId)
        val comments = if (targetUserId == this.userId || isFriend) getRecentComments(targetUserId) else emptyList()
        CommunityProfilePage(
            profile = profile,
            followedCommunities = followed,
            friends = friends,
            recentComments = comments,
            isFriend = isFriend,
            isSelf = targetUserId == this.userId,
            friendRequestId = friendRequest?.id,
            friendRequestStatus = friendRequest?.status,
        )
    }

    override suspend fun requestFriend(userId: String): Result<Unit> = runCatching {
        ensureSignedIn()
        val currentUserId = this.userId ?: throw IOException("Session expired. Please log in again.")
        check(currentUserId != userId) { "You cannot add yourself as a friend." }

        val friendRequest = request("$baseUrl/rest/v1/friend_requests?on_conflict=requester_id,recipient_id") {
            post(json.encodeToString(FriendRequestUpsert(requesterId = currentUserId, recipientId = userId)).toRequestBody(JSON))
            header("Prefer", "resolution=merge-duplicates,return=representation")
        }.decodeList(FriendRequestLookup.serializer()).first()

        val profile = getProfileById(currentUserId)
        request("$baseUrl/rest/v1/community_notifications") {
            post(
                json.encodeToString(
                    FriendRequestNotificationInsert(
                        userId = userId,
                        actorId = currentUserId,
                        type = "friend_request",
                        title = "${profile.visibleName} sent you a friend request",
                        body = "Accept or decline the request.",
                        friendRequestId = friendRequest.id,
                    ),
                ).toRequestBody(JSON),
            )
            header("Prefer", "return=minimal")
        }
    }

    override suspend fun acceptFriendRequest(requestId: String): Result<Unit> = runCatching {
        ensureSignedIn()
        request("$baseUrl/rest/v1/rpc/accept_friend_request") {
            post(json.encodeToString(mapOf("target_request_id" to requestId)).toRequestBody(JSON))
        }
        markFriendRequestNotificationsRead(requestId)
    }

    override suspend fun declineFriendRequest(requestId: String): Result<Unit> = runCatching {
        ensureSignedIn()
        updateFriendRequest(requestId, "declined")
        markFriendRequestNotificationsRead(requestId)
    }

    private suspend fun updateFriendRequest(requestId: String, status: String): FriendRequestLookup {
        val url = "$baseUrl/rest/v1/friend_requests".toHttpUrl().newBuilder()
            .addQueryParameter("id", "eq.$requestId")
            .build()

        return request(url.toString()) {
            patch(json.encodeToString(mapOf("status" to status)).toRequestBody(JSON))
            header("Prefer", "return=representation")
        }.decodeList(FriendRequestLookup.serializer()).first()
    }

    private suspend fun getPostCommunityId(postId: String): String {
        val url = "$baseUrl/rest/v1/posts".toHttpUrl().newBuilder()
            .addQueryParameter("select", "community_id")
            .addQueryParameter("id", "eq.$postId")
            .addQueryParameter("limit", "1")
            .build()
        return request(url.toString()) { get() }
            .decodeList(PostCommunityLookup.serializer())
            .firstOrNull()
            ?.communityId
            ?: throw IOException("This discussion is no longer available.")
    }

    private suspend fun markFriendRequestNotificationsRead(requestId: String) {
        val url = "$baseUrl/rest/v1/community_notifications".toHttpUrl().newBuilder()
            .addQueryParameter("friend_request_id", "eq.$requestId")
            .build()
        request(url.toString()) {
            patch(json.encodeToString(mapOf("is_read" to true)).toRequestBody(JSON))
            header("Prefer", "return=minimal")
        }
    }

    private suspend fun upsertVote(postId: String?, commentId: String?, value: Int): Result<Unit> = runCatching {
        ensureSignedIn()
        val conflict = if (postId != null) "user_id,post_id" else "user_id,comment_id"
        request("$baseUrl/rest/v1/votes?on_conflict=$conflict") {
            post(json.encodeToString(VoteUpsert(postId = postId, commentId = commentId, value = value.coerceIn(-1, 1))).toRequestBody(JSON))
            header("Prefer", "resolution=merge-duplicates")
        }
    }

    private suspend fun joinCommunity(communityId: String) {
        val id = userId ?: throw IOException("Session expired. Please log in again.")
        request("$baseUrl/rest/v1/community_members?on_conflict=community_id,user_id") {
            post(
                json.encodeToString(
                    JoinCommunityRequest(
                        communityId = communityId,
                        userId = id,
                    ),
                ).toRequestBody(JSON),
            )
            header("Prefer", "resolution=ignore-duplicates")
        }
    }

    private suspend fun Community.withFollowState(): Community {
        if (!isSignedIn) return this
        return copy(isFollowing = isCommunityFollowed(id))
    }

    private suspend fun isCommunityFollowed(communityId: String): Boolean {
        val url = "$baseUrl/rest/v1/community_members".toHttpUrl().newBuilder()
            .addQueryParameter("select", "community_id")
            .addQueryParameter("community_id", "eq.$communityId")
            .addQueryParameter("user_id", "eq.$userId")
            .addQueryParameter("is_banned", "eq.false")
            .addQueryParameter("limit", "1")
            .build()
        return request(url.toString()) { get() }.decodeList(MemberLookup.serializer()).isNotEmpty()
    }

    private suspend fun List<CommunityPost>.filterHiddenPosts(): List<CommunityPost> {
        if (isEmpty() || !isSignedIn) return this
        val hidden = getHiddenPostIds(map { it.id })
        return filterNot { it.id in hidden }
    }

    private suspend fun getHiddenPostIds(ids: List<String>): Set<String> {
        val url = "$baseUrl/rest/v1/hidden_posts".toHttpUrl().newBuilder()
            .addQueryParameter("select", "post_id")
            .addQueryParameter("post_id", "in.(${ids.joinToString(",")})")
            .addQueryParameter("user_id", "eq.$userId")
            .build()
        return request(url.toString()) { get() }
            .decodeList(HiddenPostLookup.serializer())
            .map { it.postId }
            .toSet()
    }

    private fun List<CommunityComment>.withReplyCounts(): List<CommunityComment> {
        val counts = groupBy { it.parentCommentId }.mapValues { it.value.size }
        return map { it.copy(replyCount = counts[it.id] ?: 0) }
    }

    private suspend fun createMentionNotifications(content: String, postId: String, commentId: String) {
        val mentions = Regex("@([A-Za-z0-9_]{3,32})").findAll(content)
            .map { it.groupValues[1] }
            .distinct()
            .toList()
        if (mentions.isEmpty()) return
        val mentionedProfiles = getProfilesByUsername(mentions)
        if (mentionedProfiles.isEmpty()) return
        val notifications = mentionedProfiles.mapNotNull { profile ->
            val id = profile.id ?: return@mapNotNull null
            MentionNotificationInsert(
                userId = id,
                actorId = userId.orEmpty(),
                type = "mention",
                title = "You were mentioned",
                body = content.take(160),
                postId = postId,
                commentId = commentId,
            )
        }
        request("$baseUrl/rest/v1/community_notifications") {
            post(json.encodeToString(notifications).toRequestBody(JSON))
            header("Prefer", "return=minimal")
        }
    }

    private suspend fun getProfilesByUsername(usernames: List<String>): List<CommunityProfile> {
        val url = "$baseUrl/rest/v1/profiles".toHttpUrl().newBuilder()
            .addQueryParameter("select", "id,username,display_name")
            .addQueryParameter("username", "in.(${usernames.joinToString(",")})")
            .build()
        return request(url.toString()) { get() }.decodeList(CommunityProfile.serializer())
    }

    private suspend fun getProfileById(targetUserId: String): CommunityProfile {
        val url = "$baseUrl/rest/v1/profiles".toHttpUrl().newBuilder()
            .addQueryParameter("select", "id,username,display_name")
            .addQueryParameter("id", "eq.$targetUserId")
            .addQueryParameter("limit", "1")
            .build()
        return request(url.toString()) { get() }.decodeList(CommunityProfile.serializer()).first()
    }

    private suspend fun getFollowedCommunities(targetUserId: String): List<Community> {
        val url = "$baseUrl/rest/v1/community_members".toHttpUrl().newBuilder()
            .addQueryParameter("select", "communities(*)")
            .addQueryParameter("user_id", "eq.$targetUserId")
            .addQueryParameter("is_banned", "eq.false")
            .build()
        return request(url.toString()) { get() }
            .decodeList(MemberCommunityLookup.serializer())
            .mapNotNull { it.community }
    }

    private suspend fun getFriends(targetUserId: String): List<CommunityProfile> {
        val url = "$baseUrl/rest/v1/friends".toHttpUrl().newBuilder()
            .addQueryParameter("select", "profiles!friends_friend_id_fkey(id,username,display_name)")
            .addQueryParameter("user_id", "eq.$targetUserId")
            .build()
        return request(url.toString()) { get() }
            .decodeList(FriendProfileLookup.serializer())
            .mapNotNull { it.profile }
    }

    private suspend fun isFriend(targetUserId: String): Boolean {
        val current = userId ?: return false
        val url = "$baseUrl/rest/v1/friends".toHttpUrl().newBuilder()
            .addQueryParameter("select", "friend_id")
            .addQueryParameter("user_id", "eq.$current")
            .addQueryParameter("friend_id", "eq.$targetUserId")
            .addQueryParameter("limit", "1")
            .build()
        return request(url.toString()) { get() }.decodeList(FriendLookup.serializer()).isNotEmpty()
    }

    private suspend fun getPendingFriendRequest(targetUserId: String): FriendRequestState? {
        val current = userId ?: return null
        val url = "$baseUrl/rest/v1/friend_requests".toHttpUrl().newBuilder()
            .addQueryParameter("select", "id,requester_id,recipient_id,status")
            .addQueryParameter("status", "eq.pending")
            .addQueryParameter(
                "or",
                "(and(requester_id.eq.$current,recipient_id.eq.$targetUserId),and(requester_id.eq.$targetUserId,recipient_id.eq.$current))",
            )
            .addQueryParameter("limit", "1")
            .build()
        val request = request(url.toString()) { get() }.decodeList(FriendRequestLookup.serializer()).firstOrNull() ?: return null
        return FriendRequestState(
            id = request.id,
            status = if (request.requesterId == current) FriendRequestStatus.Outgoing else FriendRequestStatus.Incoming,
        )
    }

    private suspend fun getRecentComments(targetUserId: String): List<CommunityComment> {
        val url = "$baseUrl/rest/v1/comments".toHttpUrl().newBuilder()
            .addQueryParameter("select", "*,profiles!comments_author_id_fkey(id,username,display_name)")
            .addQueryParameter("author_id", "eq.$targetUserId")
            .addQueryParameter("is_removed", "eq.false")
            .addQueryParameter("order", "created_at.desc")
            .addQueryParameter("limit", "20")
            .build()
        return request(url.toString()) { get() }.decodeList(CommunityComment.serializer())
    }

    private fun ensureConfigured() {
        check(isConfigured) { "Set SUPABASE_URL and SUPABASE_ANON_KEY before building OracleRead." }
    }

    private fun ensureSignedIn() {
        ensureConfigured()
        check(isSignedIn) { "Sign in to OracleRead Community before posting." }
    }

    private suspend fun List<CommunityPost>.withPostVotes(): List<CommunityPost> {
        val ids = map { it.id }
        if (ids.isEmpty() || !isSignedIn) return this
        val votes = getVotes("post_id", ids)
        return map { post -> post.copy(myVote = votes[post.id] ?: 0) }
    }

    private suspend fun List<CommunityComment>.withCommentVotes(): List<CommunityComment> {
        val ids = map { it.id }
        if (ids.isEmpty() || !isSignedIn) return this
        val votes = getVotes("comment_id", ids)
        return map { comment -> comment.copy(myVote = votes[comment.id] ?: 0) }
    }

    private suspend fun getVotes(column: String, ids: List<String>): Map<String, Int> {
        val url = "$baseUrl/rest/v1/votes".toHttpUrl().newBuilder()
            .addQueryParameter("select", "$column,value")
            .addQueryParameter(column, "in.(${ids.joinToString(",")})")
            .addQueryParameter("user_id", "eq.$userId")
            .build()

        return request(url.toString()) { get() }
            .decodeList(VoteLookup.serializer())
            .associate { (it.postId ?: it.commentId.orEmpty()) to it.value }
            .filterKeys { it.isNotBlank() }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun <T> String.decodeList(serializer: KSerializer<T>): List<T> =
        json.decodeFromString(ListSerializer(serializer), this)

    private suspend fun saveSession(auth: AuthResponse) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, auth.accessToken)
            .putString(KEY_REFRESH_TOKEN, auth.refreshToken)
            .putString(KEY_USER_ID, auth.user.id)
            .putLong(KEY_EXPIRES_AT, System.currentTimeMillis() + (auth.expiresIn * 1000L))
            .apply()
        saveSession(auth.accessToken, auth.user.id)
    }

    private suspend fun refreshSessionIfNeeded() {
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        if (!isSignedIn || expiresAt == 0L || expiresAt - System.currentTimeMillis() > REFRESH_SKEW_MILLIS) return
        refreshSession()
    }

    private suspend fun refreshSession() {
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null)
        if (refreshToken.isNullOrBlank()) {
            clearSession()
            throw IOException("Session expired. Please log in again.")
        }

        val builder = Request.Builder()
            .url("$baseUrl/auth/v1/token?grant_type=refresh_token")
            .header("apikey", anonKey)
            .header("Content-Type", "application/json")
            .post(json.encodeToString(RefreshTokenRequest(refreshToken)).toRequestBody(JSON))

        client.newCall(builder.build()).execute().use { response ->
            val body = response.body.string()
            if (!response.isSuccessful) {
                clearSession()
                throw IOException("Session expired. Please log in again.")
            }
            saveSession(json.decodeFromString(AuthResponse.serializer(), body))
        }
    }

    private suspend fun request(
        url: String,
        block: Request.Builder.() -> Request.Builder,
    ): String = withIOContext {
        refreshSessionIfNeeded()
        val builder = Request.Builder()
            .url(url)
            .header("apikey", anonKey)
            .header("Authorization", "Bearer ${accessToken ?: anonKey}")
            .header("Content-Type", "application/json")

        fun execute(): Pair<Int, String> {
            client.newCall(builder.block().build()).execute().use { response ->
                return response.code to response.body.string()
            }
        }

        var (code, body) = execute()
        if (code == 401 && body.contains("JWT expired", ignoreCase = true)) {
            refreshSession()
            val retryBuilder = Request.Builder()
                .url(url)
                .header("apikey", anonKey)
                .header("Authorization", "Bearer ${accessToken ?: anonKey}")
                .header("Content-Type", "application/json")
            client.newCall(retryBuilder.block().build()).execute().use { response ->
                code = response.code
                body = response.body.string()
            }
        }
        if (code == 401 && body.contains("JWT expired", ignoreCase = true)) {
            clearSession()
            throw IOException("Session expired. Please log in again.")
        }
        if (code !in 200..299) {
            throw IOException(toFriendlyError(code, body))
        }
        body
    }

    private fun toFriendlyError(code: Int, body: String): String {
        return when {
            body.contains("42501") || body.contains("row-level security", ignoreCase = true) ->
                "You do not have permission to do that yet. Try following the community, then try again."
            body.contains("23502") && body.contains("community_notifications", ignoreCase = true) ->
                "Could not send the notification. Please run the latest Supabase SQL update, then try again."
            body.contains("duplicate key", ignoreCase = true) || body.contains("23505") ->
                "That action was already done."
            code == 401 ->
                "Your session expired. Please log in again."
            code == 403 ->
                "You do not have permission to do that."
            code in 500..599 ->
                "The community server had a problem. Please try again later."
            else ->
                "Something went wrong. Please try again."
        }
    }

    private companion object {
        val JSON = "application/json".toMediaType()
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_EXPIRES_AT = "expires_at"
        const val REFRESH_SKEW_MILLIS = 60_000L
    }
}

@Serializable
private data class PasswordAuthRequest(
    val email: String,
    val password: String,
)

@Serializable
private data class SignUpRequest(
    val email: String,
    val password: String,
    val data: Map<String, String>,
)

@Serializable
private data class AuthResponse(
    @SerialName("access_token") val accessToken: String = "",
    @SerialName("refresh_token") val refreshToken: String = "",
    @SerialName("expires_in") val expiresIn: Long = 3600L,
    val user: AuthUser,
)

@Serializable
private data class AuthUser(
    val id: String,
)

@Serializable
private data class VoteLookup(
    @SerialName("post_id") val postId: String? = null,
    @SerialName("comment_id") val commentId: String? = null,
    val value: Int,
)

@Serializable
private data class PostCommunityLookup(
    @SerialName("community_id") val communityId: String,
)

@Serializable
private data class RefreshTokenRequest(
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
private data class VoteUpsert(
    @SerialName("post_id") val postId: String? = null,
    @SerialName("comment_id") val commentId: String? = null,
    val value: Int,
)

@Serializable
private data class JoinCommunityRequest(
    @SerialName("community_id") val communityId: String,
    @SerialName("user_id") val userId: String,
)

@Serializable
private data class MemberLookup(
    @SerialName("community_id") val communityId: String,
)

@Serializable
private data class HiddenPostUpsert(
    @SerialName("user_id") val userId: String,
    @SerialName("post_id") val postId: String,
)

@Serializable
private data class HiddenPostLookup(
    @SerialName("post_id") val postId: String,
)

@Serializable
private data class MentionNotificationInsert(
    @SerialName("user_id") val userId: String,
    @SerialName("actor_id") val actorId: String,
    val type: String,
    val title: String,
    val body: String,
    @SerialName("post_id") val postId: String,
    @SerialName("comment_id") val commentId: String,
)

@Serializable
private data class MemberCommunityLookup(
    @SerialName("communities") val community: Community? = null,
)

@Serializable
private data class FriendUpsert(
    @SerialName("user_id") val userId: String,
    @SerialName("friend_id") val friendId: String,
)

@Serializable
private data class FriendRequestUpsert(
    @SerialName("requester_id") val requesterId: String,
    @SerialName("recipient_id") val recipientId: String,
    val status: String = "pending",
)

@Serializable
private data class FriendRequestLookup(
    val id: String,
    @SerialName("requester_id") val requesterId: String,
    @SerialName("recipient_id") val recipientId: String,
    val status: String,
)

private data class FriendRequestState(
    val id: String,
    val status: FriendRequestStatus,
)

@Serializable
private data class FriendRequestNotificationInsert(
    @SerialName("user_id") val userId: String,
    @SerialName("actor_id") val actorId: String,
    val type: String,
    val title: String,
    val body: String,
    @SerialName("friend_request_id") val friendRequestId: String,
)

@Serializable
private data class FriendLookup(
    @SerialName("friend_id") val friendId: String,
)

@Serializable
private data class FriendProfileLookup(
    @SerialName("profiles") val profile: CommunityProfile? = null,
)

fun String.toCommunitySlug(): String {
    return lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
        .ifBlank { "manga" }
}
