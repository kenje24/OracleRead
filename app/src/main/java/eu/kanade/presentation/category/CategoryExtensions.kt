package eu.kanade.presentation.category

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Bedtime
import mihon.icons.materialsymbols.rounded.Bolt
import mihon.icons.materialsymbols.rounded.Book
import mihon.icons.materialsymbols.rounded.Bookmark
import mihon.icons.materialsymbols.rounded.Favorite
import mihon.icons.materialsymbols.rounded.Folder
import mihon.icons.materialsymbols.rounded.LocalFireDepartment
import mihon.icons.materialsymbols.rounded.Star
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
    val DEFAULT_THEME = FolderAmbience.DEFAULT.key

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

    // OracleRead: every folder ambience in picker order, see FolderAmbience
    val themeOptions = FolderAmbience.entries
}

data class FolderStyle(
    val categoryId: Long,
    val color: Long,
    val icon: String,
    val theme: String,
) {
    val ambience: FolderAmbience
        get() = FolderAmbience.fromKey(theme) ?: FolderAmbience.DEFAULT

    fun serialize(): String = "$categoryId|$color|$icon|$theme"

    companion object {
        fun parse(value: String): FolderStyle? {
            val parts = value.split("|")
            if (parts.size !in 3..4) return null
            return FolderStyle(
                categoryId = parts[0].toLongOrNull() ?: return null,
                color = parts[1].toLongOrNull() ?: return null,
                icon = parts[2].takeIf { it in FolderStyleDefaults.iconOptions }
                    ?: FolderStyleDefaults.DEFAULT_ICON,
                theme = parts.getOrNull(3)
                    ?.takeIf { FolderAmbience.fromKey(it) != null }
                    ?: FolderStyleDefaults.DEFAULT_THEME,
            )
        }
    }
}

/**
 * OracleRead: this folder's saved style, or a default one using [defaultAmbience] (the app theme's own effect) for
 * folders whose style was never changed.
 */
fun Category.folderStyle(
    styles: Set<String>,
    defaultAmbience: FolderAmbience = FolderAmbience.DEFAULT,
): FolderStyle {
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
        theme = defaultAmbience.key,
    )
}

fun Category.folderAccentColor(styles: Set<String>): Color = Color(folderStyle(styles).color)

fun Category.folderIconKey(styles: Set<String>): String = folderStyle(styles).icon

fun Category.folderThemeKey(styles: Set<String>): String = folderStyle(styles).theme

fun Category.folderAmbience(
    styles: Set<String>,
    defaultAmbience: FolderAmbience = FolderAmbience.DEFAULT,
): FolderAmbience = folderStyle(styles, defaultAmbience).ambience

fun folderIconForKey(key: String): ImageVector = when (key) {
    "book" -> MaterialSymbols.Rounded.Book
    "star" -> MaterialSymbols.Rounded.Star
    "heart" -> MaterialSymbols.Rounded.Favorite
    "bolt" -> MaterialSymbols.Rounded.Bolt
    "flame" -> MaterialSymbols.Rounded.LocalFireDepartment
    "moon" -> MaterialSymbols.Rounded.Bedtime
    "bookmark" -> MaterialSymbols.Rounded.Bookmark
    else -> MaterialSymbols.Rounded.Folder
}
