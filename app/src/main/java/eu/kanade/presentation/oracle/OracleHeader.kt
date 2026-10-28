package eu.kanade.presentation.oracle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.theme.OracleSpacing

/**
 * Large, transparent title block for top-level tabs: the screen name set in the display serif with an
 * optional mood line under it, and actions on the right. It scrolls away with content, like a film
 * title card, instead of sitting in a solid bar. [eyebrowContent] sits at the end of the mood line's row, where the
 * mood line gives way to it on narrow screens or with large fonts.
 */
@Composable
fun OracleLargeTopBar(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    subtitle: String? = null,
    applyStatusBarInsets: Boolean = true,
    supportingContent: (@Composable RowScope.() -> Unit)? = null,
    eyebrowContent: (@Composable RowScope.() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (applyStatusBarInsets) {
                    Modifier.windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                } else {
                    Modifier
                },
            )
            .padding(start = OracleSpacing.gutter, end = 8.dp, top = 18.dp, bottom = 10.dp),
    ) {
        if (eyebrowContent == null) {
            eyebrow?.let { Eyebrow(text = it) }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(
                    text = eyebrow.orEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                )
                Row(
                    // Lines the pills up with the visible edge of the action icons below
                    modifier = Modifier.padding(end = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    content = eyebrowContent,
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Lit by a slow sweep of lantern light
            OracleShinyText(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        supportingContent?.let {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = it,
            )
        }
    }
}

@Composable
private fun Eyebrow(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

/** Section title in the headline serif, with an optional trailing text action. */
@Composable
fun OracleSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = OracleSpacing.gutter, end = 8.dp, top = 20.dp, bottom = 6.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { heading() },
            )
            supporting?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
