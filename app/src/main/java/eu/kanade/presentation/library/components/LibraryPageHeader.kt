package eu.kanade.presentation.library.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.category.visualName
import eu.kanade.presentation.oracle.OracleHero
import eu.kanade.presentation.oracle.OracleSectionHeader
import eu.kanade.tachiyomi.ui.library.LibraryItem
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.model.MangaCover
import tachiyomi.domain.source.model.Source
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.theme.OracleCoverShape
import tachiyomi.presentation.core.theme.OracleSpacing
import eu.kanade.presentation.manga.components.MangaCover as MangaCoverImage

/** What sits above a library page's titles: the continue-reading hero and the source shortcuts. */
@Immutable
data class LibraryPageHeader(
    val showHero: Boolean,
    val onHeroContinueReading: (LibraryManga) -> Unit,
    val sourceShortcuts: List<Source>,
    val onClickSourceShortcut: (Source) -> Unit,
)

/**
 * The title to feature: the most recently read entry that still has unread chapters, else the most recent
 * upload, so the hero always invites the next thing to read.
 */
internal fun pickHeroItem(items: List<LibraryItem>): LibraryItem? {
    return items
        .filter { it.libraryManga.lastRead != null && it.libraryManga.unreadCount > 0 }
        .maxByOrNull { it.libraryManga.lastRead!! }
        ?: items
            .filter { it.libraryManga.latestUpload != null }
            .maxByOrNull { it.libraryManga.latestUpload!! }
}

internal fun LibraryItem.coverData(): MangaCover {
    val manga = libraryManga.manga
    return MangaCover(
        mangaId = manga.id,
        sourceId = manga.source,
        isMangaFavorite = manga.favorite,
        url = manga.thumbnailUrl,
        lastModified = manga.coverLastModified,
    )
}

@Composable
internal fun LibraryPageHeaderContent(
    header: LibraryPageHeader,
    category: Category,
    hero: LibraryItem?,
    keepReading: List<LibraryItem>,
    itemCount: Int,
    onClickHero: (LibraryManga) -> Unit,
) {
    Column(modifier = Modifier.padding(bottom = 4.dp)) {
        if (hero != null) {
            val manga = hero.libraryManga
            val started = manga.hasStarted
            OracleHero(
                coverData = hero.coverData(),
                eyebrow = stringResource(
                    if (started) MR.strings.library_continue_reading else MR.strings.library_featured,
                ),
                title = manga.manga.title,
                meta = listOfNotNull(
                    manga.manga.author?.takeIf(String::isNotBlank),
                    hero.sourceName.takeIf(String::isNotBlank),
                ).joinToString(" · ").ifEmpty { null },
                progress = if (manga.totalChapters > 0) manga.readCount.toFloat() / manga.totalChapters else null,
                actionLabel = stringResource(
                    if (started) MR.strings.action_resume else MR.strings.library_start_reading,
                ),
                onAction = { header.onHeroContinueReading(manga) },
                onClick = { onClickHero(manga) },
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        KeepReadingShelf(items = keepReading, onClick = onClickHero)
        SourceShortcuts(
            sources = header.sourceShortcuts,
            onClickSource = header.onClickSourceShortcut,
        )
        OracleSectionHeader(
            title = if (category.isSystemCategory) {
                stringResource(
                    MR.strings.library_all_titles,
                )
            } else {
                category.visualName
            },
            supporting = pluralStringResource(MR.plurals.library_title_count, count = itemCount, itemCount),
        )
    }
}

internal fun LazyGridScope.libraryHeaderItem(
    header: LibraryPageHeader?,
    category: Category,
    items: List<LibraryItem>,
    onClickHero: (LibraryManga) -> Unit,
) {
    if (header == null) return
    item(span = { GridItemSpan(maxLineSpan) }, contentType = "library_header") {
        LibraryPageHeaderContent(
            header = header,
            category = category,
            hero = if (header.showHero) pickHeroItem(items) else null,
            keepReading = if (header.showHero) pickKeepReading(items) else emptyList(),
            itemCount = items.size,
            onClickHero = onClickHero,
        )
    }
}

internal fun LazyListScope.libraryHeaderItem(
    header: LibraryPageHeader?,
    category: Category,
    items: List<LibraryItem>,
    onClickHero: (LibraryManga) -> Unit,
) {
    if (header == null) return
    item(contentType = "library_header") {
        LibraryPageHeaderContent(
            header = header,
            category = category,
            hero = if (header.showHero) pickHeroItem(items) else null,
            keepReading = if (header.showHero) pickKeepReading(items) else emptyList(),
            itemCount = items.size,
            onClickHero = onClickHero,
        )
    }
}

/** Other titles in progress after the hero, most recently read first. */
internal fun pickKeepReading(items: List<LibraryItem>): List<LibraryItem> {
    val hero = pickHeroItem(items)
    return items
        .filter { it.id != hero?.id && it.libraryManga.lastRead != null && it.libraryManga.unreadCount > 0 }
        .sortedByDescending { it.libraryManga.lastRead!! }
        .take(12)
}

/** A shelf of small posters with their progress, so picking up any started title is one tap from home. */
@Composable
private fun KeepReadingShelf(
    items: List<LibraryItem>,
    onClick: (LibraryManga) -> Unit,
) {
    if (items.isEmpty()) return
    Column {
        OracleSectionHeader(title = stringResource(MR.strings.library_keep_reading))
        LazyRow(
            contentPadding = PaddingValues(horizontal = OracleSpacing.gutter),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items, key = { it.id }) { item ->
                val manga = item.libraryManga
                Column(
                    modifier = Modifier
                        .width(112.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { onClick(manga) }
                        // Inner padding keeps the text clear of the rounded corners of the touch target
                        .padding(4.dp),
                ) {
                    MangaCoverImage.Book(
                        data = item.coverData(),
                        shape = OracleCoverShape,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (manga.totalChapters > 0) {
                        LinearProgressIndicator(
                            progress = { (manga.readCount.toFloat() / manga.totalChapters).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                                .height(3.dp),
                            strokeCap = StrokeCap.Round,
                            gapSize = 0.dp,
                            drawStopIndicator = {},
                        )
                    }
                    Text(
                        text = manga.manga.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    Text(
                        text = pluralStringResource(
                            MR.plurals.library_chapters_left,
                            count = manga.unreadCount.toInt(),
                            manga.unreadCount.toInt(),
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
