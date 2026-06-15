package mihon.domain.community.repository

import mihon.domain.community.model.Community
import mihon.domain.community.model.CommunityComment
import mihon.domain.community.model.CommunityPost
import mihon.domain.community.model.CommunitySession
import mihon.domain.community.model.CommunityNotification
import mihon.domain.community.model.CommunityProfilePage
import mihon.domain.community.model.CommunitySort
import mihon.domain.community.model.CreateComment
import mihon.domain.community.model.CreateCommunity
import mihon.domain.community.model.CreatePost
import mihon.domain.community.model.CreateReport

interface CommunityRepository {
    val isConfigured: Boolean
    val isSignedIn: Boolean
    val currentSession: CommunitySession?

    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signUp(email: String, password: String, username: String): Result<Unit>
    suspend fun saveSession(accessToken: String, userId: String)
    suspend fun clearSession()

    suspend fun getCommunity(mangaIdentifier: String?, title: String): Result<Community?>
    suspend fun createCommunity(input: CreateCommunity): Result<Community>
    suspend fun getPosts(communityId: String): Result<List<CommunityPost>>
    suspend fun searchPosts(query: String, sort: CommunitySort): Result<List<CommunityPost>>
    suspend fun createPost(input: CreatePost): Result<CommunityPost>
    suspend fun getComments(postId: String): Result<List<CommunityComment>>
    suspend fun createComment(input: CreateComment): Result<CommunityComment>
    suspend fun votePost(postId: String, value: Int): Result<Unit>
    suspend fun voteComment(commentId: String, value: Int): Result<Unit>
    suspend fun softDeletePost(postId: String): Result<Unit>
    suspend fun softDeleteComment(commentId: String): Result<Unit>
    suspend fun followCommunity(communityId: String): Result<Unit>
    suspend fun unfollowCommunity(communityId: String): Result<Unit>
    suspend fun hidePost(postId: String): Result<Unit>
    suspend fun report(input: CreateReport): Result<Unit>
    suspend fun getNotifications(): Result<List<CommunityNotification>>
    suspend fun getProfile(userId: String? = null): Result<CommunityProfilePage>
    suspend fun requestFriend(userId: String): Result<Unit>
    suspend fun acceptFriendRequest(requestId: String): Result<Unit>
    suspend fun declineFriendRequest(requestId: String): Result<Unit>
}
