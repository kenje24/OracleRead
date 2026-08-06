package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.manga.components.MangaCover
import eu.kanade.tachiyomi.ui.library.LibraryItem
import tachiyomi.domain.manga.model.asMangaCover
import tachiyomi.presentation.core.components.BadgeGroup

@Composable
internal fun FeaturedMangaCard(
    item: LibraryItem,
    onClick: () -> Unit,
    onContinue: (() -> Unit)?,
) {
    val manga = item.libraryManga.manga
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box {
                MangaCover.Book(
                    data = manga.asMangaCover(),
                    modifier = Modifier
                        .width(88.dp)
                        .height(128.dp)
                        .clip(MaterialTheme.shapes.small),
                )
                // Always shown, unlike the grid badges these don't follow the badge preferences.
                val downloadCount = item.downloadCount
                val unreadCount = item.unreadCount
                if (downloadCount > 0 || unreadCount > 0) {
                    BadgeGroup(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp),
                    ) {
                        DownloadsBadge(count = downloadCount)
                        UnreadBadge(count = unreadCount)
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text("Featured", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(
                    manga.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                manga.author?.takeIf(String::isNotBlank)?.let { Text("Author: $it", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                manga.artist?.takeIf(String::isNotBlank)?.let { Text("Artist: $it", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                manga.genre?.take(3)?.takeIf { it.isNotEmpty() }?.let {
                    Text(
                        it.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(onClick = onContinue ?: onClick) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Text(if (item.libraryManga.hasStarted) "Continue" else "Start")
                    }
                }
            }
        }
    }
}
