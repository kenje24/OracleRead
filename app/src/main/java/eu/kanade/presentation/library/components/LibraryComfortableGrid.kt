package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.oracle.OraclePillRow
import eu.kanade.presentation.oracle.OraclePoster
import eu.kanade.tachiyomi.ui.library.LibraryItem
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.roundedfilled.PlayArrow
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
internal fun LibraryComfortableGrid(
    items: List<LibraryItem>,
    category: Category,
    header: LibraryPageHeader?,
    columns: Int,
    contentPadding: PaddingValues,
    selection: Set<Long>,
    onClick: (LibraryManga) -> Unit,
    onLongClick: (LibraryManga) -> Unit,
    onClickContinueReading: ((LibraryManga) -> Unit)?,
    searchQuery: String?,
    onGlobalSearchClicked: () -> Unit,
) {
    LazyLibraryGrid(
        modifier = Modifier.fillMaxSize(),
        columns = columns,
        contentPadding = contentPadding,
    ) {
        globalSearchItem(searchQuery, onGlobalSearchClicked)
        libraryHeaderItem(header, category, items, onClick)

        items(
            items = items,
            contentType = { "library_comfortable_grid_item" },
        ) { libraryItem ->
            LibraryPoster(
                libraryItem = libraryItem,
                selection = selection,
                onClick = onClick,
                onLongClick = onLongClick,
                onClickContinueReading = onClickContinueReading,
                titleInside = false,
                showTitle = true,
            )
        }
    }
}

/** One library entry as a cinematic poster, shared by the comfortable, compact and cover-only grids. */
@Composable
internal fun LibraryPoster(
    libraryItem: LibraryItem,
    selection: Set<Long>,
    onClick: (LibraryManga) -> Unit,
    onLongClick: (LibraryManga) -> Unit,
    onClickContinueReading: ((LibraryManga) -> Unit)?,
    titleInside: Boolean,
    showTitle: Boolean,
) {
    val manga = libraryItem.libraryManga.manga
    OraclePoster(
        coverData = libraryItem.coverData(),
        title = if (showTitle) manga.title else "",
        titleInside = titleInside || !showTitle,
        isSelected = manga.id in selection,
        onClick = { onClick(libraryItem.libraryManga) },
        onLongClick = { onLongClick(libraryItem.libraryManga) },
        badges = { modifier ->
            Row(modifier = modifier) {
                OraclePillRow {
                    PinnedBadge(isPinned = libraryItem.isPinned)
                    DownloadsBadge(count = libraryItem.badges.downloadCount)
                    UnreadBadge(count = libraryItem.badges.unreadCount)
                    LanguageBadge(
                        isLocal = libraryItem.badges.isLocal,
                        sourceLanguage = libraryItem.badges.sourceLanguage,
                    )
                }
            }
        },
        overlay = {
            if (onClickContinueReading != null && libraryItem.unreadCount > 0) {
                ContinueReadingFab { onClickContinueReading(libraryItem.libraryManga) }
            }
        },
    )
}

@Composable
private fun BoxScope.ContinueReadingFab(onClick: () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(8.dp)
            .size(34.dp),
    ) {
        Icon(
            imageVector = MaterialSymbols.RoundedFilled.PlayArrow,
            contentDescription = stringResource(MR.strings.action_resume),
            modifier = Modifier.size(20.dp),
        )
    }
}
