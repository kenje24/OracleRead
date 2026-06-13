package eu.kanade.presentation.category

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.domain.category.model.Category
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

val Category.visualName: String
    @Composable
    get() = when {
        isSystemCategory -> stringResource(MR.strings.label_default)
        else -> name
    }

fun Category.visualName(context: Context): String =
    when {
        isSystemCategory -> context.stringResource(MR.strings.label_default)
        else -> name
    }

object FolderStyleDefaults {
    const val DEFAULT_ICON = "folder"

    val colorOptions = listOf(
        0xFFFF6B35,
        0xFFFFB347,
        0xFF6FFFE9,
        0xFFFF8FAB,
        0xFF8EA7FF,
        0xFF86F7C8,
        0xFFFFE66D,
        0xFFB8A7FF,
    )

    val iconOptions = listOf(
        "folder",
        "book",
        "star",
        "heart",
        "bolt",
        "flame",
        "moon",
        "bookmark",
    )
}

data class FolderStyle(
    val categoryId: Long,
    val color: Long,
    val icon: String,
) {
    fun serialize(): String = "$categoryId|$color|$icon"

    companion object {
        fun parse(value: String): FolderStyle? {
            val parts = value.split("|")
            if (parts.size != 3) return null
            return FolderStyle(
                categoryId = parts[0].toLongOrNull() ?: return null,
                color = parts[1].toLongOrNull() ?: return null,
                icon = parts[2].takeIf { it in FolderStyleDefaults.iconOptions }
                    ?: FolderStyleDefaults.DEFAULT_ICON,
            )
        }
    }
}

fun Category.folderStyle(styles: Set<String>): FolderStyle {
    val saved = styles
        .asSequence()
        .mapNotNull(FolderStyle::parse)
        .firstOrNull { it.categoryId == id }
    if (saved != null) return saved

    val color = if (isSystemCategory) {
        0xFF9AA4B2
    } else {
        FolderStyleDefaults.colorOptions[((name.hashCode() + id).mod(FolderStyleDefaults.colorOptions.size))]
    }
    return FolderStyle(
        categoryId = id,
        color = color,
        icon = FolderStyleDefaults.DEFAULT_ICON,
    )
}

fun Category.folderAccentColor(styles: Set<String>): Color = Color(folderStyle(styles).color)

fun Category.folderIconKey(styles: Set<String>): String = folderStyle(styles).icon

fun folderIconForKey(key: String): ImageVector = when (key) {
    "book" -> Icons.Outlined.AutoStories
    "star" -> Icons.Outlined.StarBorder
    "heart" -> Icons.Outlined.FavoriteBorder
    "bolt" -> Icons.Outlined.Bolt
    "flame" -> Icons.Outlined.LocalFireDepartment
    "moon" -> Icons.Outlined.Nightlight
    "bookmark" -> Icons.Outlined.BookmarkBorder
    else -> Icons.Outlined.Folder
}
