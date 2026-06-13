package eu.kanade.presentation.category

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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

fun Category.folderAccentColor(): Color {
    if (isSystemCategory) return Color(0xFF9AA4B2)
    val palette = listOf(
        Color(0xFFFFB347),
        Color(0xFF6FFFE9),
        Color(0xFFFF8FAB),
        Color(0xFF8EA7FF),
        Color(0xFF86F7C8),
        Color(0xFFFFE66D),
    )
    val index = ((name.hashCode() + id).mod(palette.size))
    return palette[index]
}
