package mihon.domain.community.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Community(
    val id: String,
    @SerialName("manga_identifier") val mangaIdentifier: String? = null,
    val title: String,
    val slug: String,
    val description: String? = null,
    @SerialName("cover_image") val coverImage: String? = null,
    @SerialName("creator_id") val creatorId: String,
    @SerialName("member_count") val memberCount: Int = 0,
    @SerialName("created_at") val createdAt: String,
    val isFollowing: Boolean = false,
)

@Serializable
data class CommunityPost(
    val id: String,
    @SerialName("community_id") val communityId: String,
    @SerialName("author_id") val authorId: String,
    val title: String,
    val content: String,
    val type: PostType = PostType.Discussion,
    @SerialName("is_spoiler") val isSpoiler: Boolean = false,
    @SerialName("is_removed") val isRemoved: Boolean = false,
    val score: Int = 0,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    @SerialName("comment_count") val commentCount: Int = 0,
    @SerialName("communities") val community: CommunitySummary? = null,
    @SerialName("profiles") val author: CommunityProfile? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    val myVote: Int = 0,
)

@Serializable
data class CommunitySummary(
    val title: String,
    val slug: String,
    @SerialName("cover_image") val coverImage: String? = null,
)

@Serializable
data class CommunityComment(
    val id: String,
    @SerialName("post_id") val postId: String,
    @SerialName("parent_comment_id") val parentCommentId: String? = null,
    @SerialName("author_id") val authorId: String,
    val content: String,
    val score: Int = 0,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    @SerialName("is_removed") val isRemoved: Boolean = false,
    @SerialName("profiles") val author: CommunityProfile? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    val myVote: Int = 0,
    val replyCount: Int = 0,
)

@Serializable
data class CommunityProfile(
    val id: String? = null,
    val username: String,
    @SerialName("display_name") val displayName: String? = null,
) {
    val visibleName: String
        get() = displayName?.takeIf { it.isNotBlank() } ?: username
}

@Serializable
data class CommunityNotification(
    val id: String,
    @SerialName("actor_id") val actorId: String? = null,
    val type: String,
    val title: String,
    val body: String? = null,
    @SerialName("friend_request_id") val friendRequestId: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class CommunityProfilePage(
    val profile: CommunityProfile,
    @SerialName("followed_communities") val followedCommunities: List<Community> = emptyList(),
    val friends: List<CommunityProfile> = emptyList(),
    @SerialName("recent_comments") val recentComments: List<CommunityComment> = emptyList(),
    val isFriend: Boolean = false,
    val isSelf: Boolean = false,
    val friendRequestId: String? = null,
    val friendRequestStatus: FriendRequestStatus? = null,
)

@Serializable
data class CommunitySession(
    val userId: String,
)

enum class FriendRequestStatus {
    Incoming,
    Outgoing,
}

enum class CommunitySort {
    Best,
    Top,
    New,
    Controversial,
    Old,
    QA,
}

@Serializable
enum class PostType {
    @SerialName("discussion")
    Discussion,

    @SerialName("theory")
    Theory,

    @SerialName("question")
    Question,

    @SerialName("chapter_discussion")
    ChapterDiscussion,
}

@Serializable
data class CreateCommunity(
    @SerialName("manga_identifier") val mangaIdentifier: String? = null,
    val title: String,
    val slug: String,
    val description: String? = null,
    @SerialName("cover_image") val coverImage: String? = null,
)

@Serializable
data class CreatePost(
    @SerialName("community_id") val communityId: String,
    val title: String,
    val content: String,
    val type: PostType,
    @SerialName("is_spoiler") val isSpoiler: Boolean,
)

@Serializable
data class CreateComment(
    @SerialName("post_id") val postId: String,
    @SerialName("parent_comment_id") val parentCommentId: String? = null,
    val content: String,
)

@Serializable
data class CreateReport(
    @SerialName("community_id") val communityId: String,
    @SerialName("target_type") val targetType: String,
    @SerialName("post_id") val postId: String? = null,
    @SerialName("comment_id") val commentId: String? = null,
    val reason: String,
)
