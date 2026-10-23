package eu.kanade.presentation.library

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import dev.icerock.moko.resources.StringResource
import eu.kanade.presentation.components.TabbedDialog
import eu.kanade.presentation.components.TabbedDialogPaddings
import eu.kanade.tachiyomi.ui.library.LibrarySettingsViewModel
import eu.kanade.tachiyomi.util.system.isReleaseBuildType
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.ArrowDownward
import mihon.icons.materialsymbols.rounded.ArrowUpward
import mihon.icons.materialsymbols.rounded.Refresh
import tachiyomi.core.common.preference.TriState
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.library.model.LibraryDisplayMode
import tachiyomi.domain.library.model.LibrarySort
import tachiyomi.domain.library.model.sort
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.CheckboxItem
import tachiyomi.presentation.core.components.HeadingItem
import tachiyomi.presentation.core.components.SettingsChipRow
import tachiyomi.presentation.core.components.SliderItem
import tachiyomi.presentation.core.components.TriStateItem
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState

@Composable
fun LibrarySettingsDialog(
    onDismissRequest: () -> Unit,
    viewModel: LibrarySettingsViewModel,
    category: Category?,
) {
    TabbedDialog(
        onDismissRequest = onDismissRequest,
        tabTitles = listOf(
            stringResource(MR.strings.action_filter),
            stringResource(MR.strings.action_sort),
            stringResource(MR.strings.action_display),
        ),
    ) { page ->
        Column(
            modifier = Modifier
                .padding(vertical = TabbedDialogPaddings.Vertical)
                .verticalScroll(rememberScrollState()),
        ) {
            when (page) {
                0 -> FilterPage(
                    viewModel = viewModel,
                )
                1 -> SortPage(
                    category = category,
                    viewModel = viewModel,
                )
                2 -> DisplayPage(
                    viewModel = viewModel,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.FilterPage(
    viewModel: LibrarySettingsViewModel,
) {
    val filterDownloaded by viewModel.libraryPreferences.filterDownloaded.collectAsState()
    val downloadedOnly by viewModel.preferences.downloadedOnly.collectAsState()
    val autoUpdateMangaRestrictions by viewModel.libraryPreferences.autoUpdateMangaRestrictions.collectAsState()

    TriStateItem(
        label = stringResource(MR.strings.label_downloaded),
        state = if (downloadedOnly) {
            TriState.ENABLED_IS
        } else {
            filterDownloaded
        },
        enabled = !downloadedOnly,
        onClick = { viewModel.toggleFilter(LibraryPreferences::filterDownloaded) },
    )
    val filterUnread by viewModel.libraryPreferences.filterUnread.collectAsState()
    TriStateItem(
        label = stringResource(MR.strings.action_filter_unread),
        state = filterUnread,
        onClick = { viewModel.toggleFilter(LibraryPreferences::filterUnread) },
    )
    val filterStarted by viewModel.libraryPreferences.filterStarted.collectAsState()
    TriStateItem(
        label = stringResource(MR.strings.label_started),
        state = filterStarted,
        onClick = { viewModel.toggleFilter(LibraryPreferences::filterStarted) },
    )
    val filterBookmarked by viewModel.libraryPreferences.filterBookmarked.collectAsState()
    TriStateItem(
        label = stringResource(MR.strings.action_filter_bookmarked),
        state = filterBookmarked,
        onClick = { viewModel.toggleFilter(LibraryPreferences::filterBookmarked) },
    )
    val filterCompleted by viewModel.libraryPreferences.filterCompleted.collectAsState()
    TriStateItem(
        label = stringResource(MR.strings.completed),
        state = filterCompleted,
        onClick = { viewModel.toggleFilter(LibraryPreferences::filterCompleted) },
    )
    // TODO: re-enable when custom intervals are ready for stable
    if ((!isReleaseBuildType) && LibraryPreferences.MANGA_OUTSIDE_RELEASE_PERIOD in autoUpdateMangaRestrictions) {
        val filterIntervalCustom by viewModel.libraryPreferences.filterIntervalCustom.collectAsState()
        TriStateItem(
            label = stringResource(MR.strings.action_filter_interval_custom),
            state = filterIntervalCustom,
            onClick = { viewModel.toggleFilter(LibraryPreferences::filterIntervalCustom) },
        )
    }

    val trackers by viewModel.trackersFlow.collectAsState()
    when (trackers.size) {
        0 -> {
            // No trackers
        }
        1 -> {
            val service = trackers[0]
            val filterTracker by viewModel.libraryPreferences.filterTracking(service.id.toInt()).collectAsState()
            TriStateItem(
                label = stringResource(MR.strings.action_filter_tracked),
                state = filterTracker,
                onClick = { viewModel.toggleTracker(service.id.toInt()) },
            )
        }
        else -> {
            HeadingItem(MR.strings.action_filter_tracked)
            trackers.map { service ->
                val filterTracker by viewModel.libraryPreferences.filterTracking(service.id.toInt()).collectAsState()
                TriStateItem(
                    label = service.name,
                    state = filterTracker,
                    onClick = { viewModel.toggleTracker(service.id.toInt()) },
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.SortPage(
    category: Category?,
    viewModel: LibrarySettingsViewModel,
) {
    val trackers by viewModel.trackersFlow.collectAsState()
    val sortingMode = category.sort.type
    val sortAscending = category.sort.isAscending

    fun onSelect(mode: LibrarySort.Type, initialDirection: LibrarySort.Direction) {
        viewModel.setSort(
            category,
            mode,
            toggledDirection(sortingMode, sortAscending, mode, initialDirection),
        )
    }

    // OracleRead sort cards: the label itself spells out the active direction
    directionalSortOptions.forEach { option ->
        val selected = sortingMode == option.type
        val showAlternate = selected && sortAscending == option.alternateWhenAscending
        SortCard(
            title = stringResource(if (showAlternate) option.alternateLabel else option.label),
            selected = selected,
            onClick = { onSelect(option.type, option.initialDirection) },
        )
    }

    // Remaining upstream sort modes, kept so no sorting option is lost
    val extraOptions = remember(trackers.isEmpty()) {
        listOfNotNull(
            MR.strings.action_sort_unread_count to LibrarySort.Type.UnreadCount,
            MR.strings.action_sort_chapter_fetch_date to LibrarySort.Type.ChapterFetchDate,
            (MR.strings.action_sort_tracker_score to LibrarySort.Type.TrackerMean).takeIf { trackers.isNotEmpty() },
        )
    }
    extraOptions.forEach { (titleRes, mode) ->
        val selected = sortingMode == mode
        SortCard(
            title = stringResource(titleRes),
            selected = selected,
            trailingIcon = when {
                !selected -> null
                sortAscending -> MaterialSymbols.Rounded.ArrowUpward
                else -> MaterialSymbols.Rounded.ArrowDownward
            },
            onClick = { onSelect(mode, LibrarySort.Direction.Descending) },
        )
    }
    SortCard(
        title = stringResource(MR.strings.action_sort_random),
        selected = sortingMode == LibrarySort.Type.Random,
        trailingIcon = MaterialSymbols.Rounded.Refresh.takeIf { sortingMode == LibrarySort.Type.Random },
        onClick = { viewModel.setSort(category, LibrarySort.Type.Random, LibrarySort.Direction.Ascending) },
    )
}

private class DirectionalSortOption(
    val type: LibrarySort.Type,
    val label: StringResource,
    val alternateLabel: StringResource,
    // Whether alternateLabel is shown while the sort is ascending (true) or descending (false)
    val alternateWhenAscending: Boolean,
    val initialDirection: LibrarySort.Direction,
)

private val directionalSortOptions = listOf(
    DirectionalSortOption(
        type = LibrarySort.Type.Alphabetical,
        label = MR.strings.library_sort_alpha_asc,
        alternateLabel = MR.strings.library_sort_alpha_desc,
        alternateWhenAscending = false,
        initialDirection = LibrarySort.Direction.Ascending,
    ),
    DirectionalSortOption(
        type = LibrarySort.Type.LastRead,
        label = MR.strings.library_sort_last_read_newest,
        alternateLabel = MR.strings.library_sort_last_read_oldest,
        alternateWhenAscending = true,
        initialDirection = LibrarySort.Direction.Descending,
    ),
    DirectionalSortOption(
        type = LibrarySort.Type.LastUpdate,
        label = MR.strings.library_sort_last_update_newest,
        alternateLabel = MR.strings.library_sort_last_update_oldest,
        alternateWhenAscending = true,
        initialDirection = LibrarySort.Direction.Descending,
    ),
    DirectionalSortOption(
        type = LibrarySort.Type.LatestChapter,
        label = MR.strings.library_sort_latest_chapter_newest,
        alternateLabel = MR.strings.library_sort_latest_chapter_oldest,
        alternateWhenAscending = true,
        initialDirection = LibrarySort.Direction.Descending,
    ),
    DirectionalSortOption(
        type = LibrarySort.Type.TotalChapters,
        label = MR.strings.library_sort_total_chapters_most,
        alternateLabel = MR.strings.library_sort_total_chapters_least,
        alternateWhenAscending = true,
        initialDirection = LibrarySort.Direction.Descending,
    ),
    DirectionalSortOption(
        type = LibrarySort.Type.DateAdded,
        label = MR.strings.library_sort_date_added_first,
        alternateLabel = MR.strings.library_sort_date_added_last,
        alternateWhenAscending = false,
        initialDirection = LibrarySort.Direction.Ascending,
    ),
)

@Composable
private fun SortCard(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    trailingIcon: ImageVector? = null,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = MaterialTheme.padding.medium,
                vertical = MaterialTheme.padding.extraSmall,
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.padding.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
                modifier = Modifier.weight(1f, fill = false),
            )
            when {
                !selected -> {}
                trailingIcon != null -> Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = contentColor,
                )
                else -> Text(
                    text = stringResource(MR.strings.library_sort_active),
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor,
                )
            }
        }
    }
}

private fun toggledDirection(
    currentMode: LibrarySort.Type,
    currentAscending: Boolean,
    targetMode: LibrarySort.Type,
    initialDirection: LibrarySort.Direction,
): LibrarySort.Direction {
    if (currentMode != targetMode) return initialDirection
    return if (currentAscending) {
        LibrarySort.Direction.Descending
    } else {
        LibrarySort.Direction.Ascending
    }
}

private val displayModes = listOf(
    MR.strings.action_display_grid to LibraryDisplayMode.CompactGrid,
    MR.strings.action_display_comfortable_grid to LibraryDisplayMode.ComfortableGrid,
    MR.strings.action_display_cover_only_grid to LibraryDisplayMode.CoverOnlyGrid,
    MR.strings.action_display_list to LibraryDisplayMode.List,
)

@Composable
private fun ColumnScope.DisplayPage(
    viewModel: LibrarySettingsViewModel,
) {
    val displayMode by viewModel.libraryPreferences.displayMode.collectAsState()
    SettingsChipRow(MR.strings.action_display_mode) {
        displayModes.map { (titleRes, mode) ->
            FilterChip(
                selected = displayMode == mode,
                onClick = { viewModel.setDisplayMode(mode) },
                label = { Text(stringResource(titleRes)) },
            )
        }
    }

    if (displayMode != LibraryDisplayMode.List) {
        val configuration = LocalConfiguration.current
        val columnPreference = remember {
            if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                viewModel.libraryPreferences.landscapeColumns
            } else {
                viewModel.libraryPreferences.portraitColumns
            }
        }

        val columns by columnPreference.collectAsState()
        SliderItem(
            value = columns,
            valueRange = 0..10,
            label = stringResource(MR.strings.pref_library_columns),
            valueString = if (columns > 0) {
                columns.toString()
            } else {
                stringResource(MR.strings.label_auto)
            },
            onChange = columnPreference::set,
            pillColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
    }

    HeadingItem(MR.strings.overlay_header)
    CheckboxItem(
        label = stringResource(MR.strings.action_display_download_badge),
        pref = viewModel.libraryPreferences.downloadBadge,
    )
    CheckboxItem(
        label = stringResource(MR.strings.action_display_unread_badge),
        pref = viewModel.libraryPreferences.unreadBadge,
    )
    CheckboxItem(
        label = stringResource(MR.strings.action_display_local_badge),
        pref = viewModel.libraryPreferences.localBadge,
    )
    CheckboxItem(
        label = stringResource(MR.strings.action_display_language_badge),
        pref = viewModel.libraryPreferences.languageBadge,
    )
    CheckboxItem(
        label = stringResource(MR.strings.action_display_show_continue_reading_button),
        pref = viewModel.libraryPreferences.showContinueReadingButton,
    )
    CheckboxItem(
        label = stringResource(MR.strings.action_display_show_featured_manga),
        pref = viewModel.libraryPreferences.showFeaturedManga,
    )

    HeadingItem(MR.strings.tabs_header)
    CheckboxItem(
        label = stringResource(MR.strings.action_display_show_tabs),
        pref = viewModel.libraryPreferences.categoryTabs,
    )
    CheckboxItem(
        label = stringResource(MR.strings.action_display_show_number_of_items),
        pref = viewModel.libraryPreferences.categoryNumberOfItems,
    )
}
