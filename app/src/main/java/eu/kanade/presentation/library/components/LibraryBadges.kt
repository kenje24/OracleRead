package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import eu.kanade.presentation.oracle.OraclePill
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Folder
import mihon.icons.materialsymbols.rounded.Keep
import tachiyomi.presentation.core.components.Badge

@Composable
internal fun DownloadsBadge(count: Int) {
    if (count > 0) {
        OraclePill(
            text = "$count",
            container = MaterialTheme.colorScheme.tertiary,
            content = MaterialTheme.colorScheme.onTertiary,
        )
    }
}

@Composable
internal fun UnreadBadge(count: Long) {
    if (count > 0) {
        OraclePill(text = "$count")
    }
}

@Composable
internal fun PinnedBadge(isPinned: Boolean) {
    if (isPinned) {
        Badge(
            imageVector = MaterialSymbols.Rounded.Keep,
            color = MaterialTheme.colorScheme.secondary,
            iconColor = MaterialTheme.colorScheme.onSecondary,
        )
    }
}

@Composable
internal fun LanguageBadge(
    isLocal: Boolean,
    sourceLanguage: String,
) {
    if (isLocal) {
        Badge(
            imageVector = MaterialSymbols.Rounded.Folder,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            iconColor = MaterialTheme.colorScheme.onSurface,
        )
    } else if (sourceLanguage.isNotEmpty()) {
        OraclePill(
            text = sourceLanguage.uppercase(),
            container = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.9f),
            content = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@PreviewLightDark
@Composable
private fun BadgePreview() {
    TachiyomiPreviewTheme {
        Column {
            PinnedBadge(isPinned = true)
            DownloadsBadge(count = 10)
            UnreadBadge(count = 10)
            LanguageBadge(isLocal = true, sourceLanguage = "EN")
            LanguageBadge(isLocal = false, sourceLanguage = "EN")
        }
    }
}
