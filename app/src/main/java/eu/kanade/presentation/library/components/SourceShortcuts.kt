package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.browse.components.SourceIcon
import eu.kanade.presentation.oracle.OracleGlass
import eu.kanade.presentation.oracle.OracleSectionHeader
import tachiyomi.domain.source.model.Source
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.theme.OracleSpacing

/**
 * Pinned and favourite sources as glass chips under the library hero, so jumping from the library into a
 * source is one tap. Hidden when there is nothing to show.
 */
@Composable
fun SourceShortcuts(
    sources: List<Source>,
    onClickSource: (Source) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (sources.isEmpty()) return
    Column(modifier = modifier) {
        OracleSectionHeader(title = stringResource(MR.strings.library_source_shortcuts))
        LazyRow(
            contentPadding = PaddingValues(horizontal = OracleSpacing.gutter),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(sources, key = { it.id }) { source ->
                OracleGlass(
                    shape = CircleShape,
                    alpha = 0.72f,
                    onClick = { onClickSource(source) },
                ) {
                    Row(
                        modifier = Modifier.padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SourceIcon(
                            source = source,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = source.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
