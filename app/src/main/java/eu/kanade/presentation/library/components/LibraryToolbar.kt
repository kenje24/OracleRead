package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.components.SearchToolbar
import eu.kanade.presentation.oracle.OracleLargeTopBar
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.FilterList
import mihon.icons.materialsymbols.rounded.FlipToBack
import mihon.icons.materialsymbols.rounded.Search
import mihon.icons.materialsymbols.rounded.SelectAll
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.theme.active

@Composable
fun LibraryToolbar(
    hasActiveFilters: Boolean,
    selectedCount: Int,
    title: LibraryToolbarTitle,
    onClickUnselectAll: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
    onClickFilter: () -> Unit,
    onClickRefresh: () -> Unit,
    onClickGlobalUpdate: () -> Unit,
    onClickOpenRandomManga: () -> Unit,
    onClickNotifications: () -> Unit,
    onClickChangeTheme: () -> Unit,
    searchQuery: String?,
    onSearchQueryChange: (String?) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?,
) = when {
    selectedCount > 0 -> LibrarySelectionToolbar(
        selectedCount = selectedCount,
        onClickUnselectAll = onClickUnselectAll,
        onClickSelectAll = onClickSelectAll,
        onClickInvertSelection = onClickInvertSelection,
    )
    // Search keeps the familiar search field
    searchQuery != null -> SearchToolbar(
        titleContent = { Text(text = title.text) },
        searchQuery = searchQuery,
        onChangeSearchQuery = onSearchQueryChange,
        actions = {
            LibraryActions(
                hasFilters = hasActiveFilters,
                onClickFilter = onClickFilter,
                onClickRefresh = onClickRefresh,
                onClickGlobalUpdate = onClickGlobalUpdate,
                onClickOpenRandomManga = onClickOpenRandomManga,
                onClickChangeTheme = onClickChangeTheme,
            )
        },
        scrollBehavior = scrollBehavior,
    )
    else -> LibraryTitleBar(
        title = title,
        hasFilters = hasActiveFilters,
        onClickSearch = { onSearchQueryChange("") },
        onClickNotifications = onClickNotifications,
        onClickChangeTheme = onClickChangeTheme,
        onClickFilter = onClickFilter,
        onClickRefresh = onClickRefresh,
        onClickGlobalUpdate = onClickGlobalUpdate,
        onClickOpenRandomManga = onClickOpenRandomManga,
    )
}

@Composable
private fun LibraryTitleBar(
    title: LibraryToolbarTitle,
    hasFilters: Boolean,
    onClickSearch: () -> Unit,
    onClickNotifications: () -> Unit,
    onClickChangeTheme: () -> Unit,
    onClickFilter: () -> Unit,
    onClickRefresh: () -> Unit,
    onClickGlobalUpdate: () -> Unit,
    onClickOpenRandomManga: () -> Unit,
) {
    OracleLargeTopBar(
        title = title.text,
        subtitle = title.numberOfManga?.let {
            pluralStringResource(MR.plurals.library_title_count, count = it, it)
        },
        actions = {
            IncognitoToggle()
            Spacer(Modifier.width(6.dp))
            ReadingStreak()
            AppBarActions(
                listOf(
                    AppBar.Action(
                        title = stringResource(MR.strings.action_search),
                        icon = MaterialSymbols.Rounded.Search,
                        onClick = onClickSearch,
                    ),
                    AppBar.OverflowAction(
                        title = stringResource(MR.strings.library_notifications),
                        onClick = onClickNotifications,
                    ),
                ),
            )
            LibraryActions(
                hasFilters = hasFilters,
                onClickFilter = onClickFilter,
                onClickRefresh = onClickRefresh,
                onClickGlobalUpdate = onClickGlobalUpdate,
                onClickOpenRandomManga = onClickOpenRandomManga,
                onClickChangeTheme = onClickChangeTheme,
            )
        },
    )
}

@Composable
private fun LibraryActions(
    hasFilters: Boolean,
    onClickFilter: () -> Unit,
    onClickRefresh: () -> Unit,
    onClickGlobalUpdate: () -> Unit,
    onClickOpenRandomManga: () -> Unit,
    onClickChangeTheme: () -> Unit,
) {
    val filterTint = if (hasFilters) MaterialTheme.colorScheme.active else LocalContentColor.current
    AppBarActions(
        listOf(
            AppBar.Action(
                title = stringResource(MR.strings.action_filter),
                icon = MaterialSymbols.Rounded.FilterList,
                iconTint = filterTint,
                onClick = onClickFilter,
            ),
            AppBar.OverflowAction(
                title = stringResource(MR.strings.action_update_library),
                onClick = onClickGlobalUpdate,
            ),
            AppBar.OverflowAction(
                title = stringResource(MR.strings.action_update_category),
                onClick = onClickRefresh,
            ),
            AppBar.OverflowAction(
                title = stringResource(MR.strings.action_open_random_manga),
                onClick = onClickOpenRandomManga,
            ),
            AppBar.OverflowAction(
                title = stringResource(MR.strings.library_change_theme),
                onClick = onClickChangeTheme,
            ),
        ),
    )
}

@Composable
private fun LibrarySelectionToolbar(
    selectedCount: Int,
    onClickUnselectAll: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
) {
    AppBar(
        titleContent = { Text(text = "$selectedCount") },
        actions = {
            AppBarActions(
                listOf(
                    AppBar.Action(
                        title = stringResource(MR.strings.action_select_all),
                        icon = MaterialSymbols.Rounded.SelectAll,
                        onClick = onClickSelectAll,
                    ),
                    AppBar.Action(
                        title = stringResource(MR.strings.action_select_inverse),
                        icon = MaterialSymbols.Rounded.FlipToBack,
                        onClick = onClickInvertSelection,
                    ),
                ),
            )
        },
        isActionMode = true,
        onCancelActionMode = onClickUnselectAll,
    )
}

@Immutable
data class LibraryToolbarTitle(
    val text: String,
    val numberOfManga: Int? = null,
)
