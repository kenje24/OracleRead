package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
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
        SourceShortcuts(
            sources = header.sourceShortcuts,
            onClickSource = header.onClickSourceShortcut,
        )
        OracleSectionHeader(
            title = if (category.isSystemCategory) stringResource(MR.strings.library_all_titles) else category.visualName,
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
            itemCount = items.size,
            onClickHero = onClickHero,
        )
    }
}
