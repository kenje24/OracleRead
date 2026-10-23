package eu.kanade.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.components.AppBarTitle
import eu.kanade.presentation.components.SearchToolbar
import eu.kanade.presentation.components.relativeDateText
import eu.kanade.presentation.history.components.HistoryItem
import eu.kanade.presentation.oracle.OracleLargeTopBar
import eu.kanade.presentation.oracle.OracleSectionHeader
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import eu.kanade.presentation.util.rememberIs24HourSystem
import eu.kanade.tachiyomi.ui.history.HistoryViewModel
import kotlinx.datetime.LocalDate
import mihon.app.di.appGraph
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.DeleteSweep
import mihon.icons.materialsymbols.rounded.Search
import tachiyomi.domain.history.model.HistoryWithRelations
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.presentation.core.util.collectAsState

@Composable
fun HistoryScreen(
    state: HistoryViewModel.State,
    snackbarHostState: SnackbarHostState,
    onSearchQueryChange: (String?) -> Unit,
    onClickCover: (mangaId: Long) -> Unit,
    onClickResume: (mangaId: Long, chapterId: Long) -> Unit,
    onClickFavorite: (mangaId: Long) -> Unit,
    onDialogChange: (HistoryViewModel.Dialog?) -> Unit,
) {
    Scaffold(
        topBar = { scrollBehavior ->
            HistoryToolbar(
                state = state,
                onSearchQueryChange = onSearchQueryChange,
                onClickClearHistory = { onDialogChange(HistoryViewModel.Dialog.DeleteAll) },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { contentPadding ->
        state.list.let {
            if (it == null) {
                LoadingScreen(Modifier.padding(contentPadding))
            } else if (it.isEmpty()) {
                val msg = if (!state.searchQuery.isNullOrEmpty()) {
                    MR.strings.no_results_found
                } else {
                    MR.strings.information_no_recent_manga
                }
                EmptyScreen(
                    stringRes = msg,
                    modifier = Modifier.padding(contentPadding),
                )
            } else {
                HistoryScreenContent(
                    history = it,
                    contentPadding = contentPadding,
                    onClickCover = { history -> onClickCover(history.mangaId) },
                    onClickResume = { history -> onClickResume(history.mangaId, history.chapterId) },
                    onClickDelete = { item -> onDialogChange(HistoryViewModel.Dialog.Delete(item)) },
                    onClickFavorite = { history -> onClickFavorite(history.mangaId) },
                )
            }
        }
    }
}

/**
 * Large serif "History" title with how many titles were read; the search field replaces it while
 * searching.
 */
@Composable
private fun HistoryToolbar(
    state: HistoryViewModel.State,
    onSearchQueryChange: (String?) -> Unit,
    onClickClearHistory: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val clearHistoryAction = AppBar.Action(
        title = stringResource(MR.strings.pref_clear_history),
        icon = MaterialSymbols.Rounded.DeleteSweep,
        onClick = onClickClearHistory,
    )
    if (state.searchQuery != null) {
        SearchToolbar(
            titleContent = { AppBarTitle(stringResource(MR.strings.history)) },
            searchQuery = state.searchQuery,
            onChangeSearchQuery = onSearchQueryChange,
            actions = { AppBarActions(listOf(clearHistoryAction)) },
            scrollBehavior = scrollBehavior,
        )
    } else {
        val titleCount = remember(state.list) { state.list?.count { it is HistoryUiModel.Item } }
        OracleLargeTopBar(
            // Opaque so list content scrolling underneath never shows through the title
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            title = stringResource(MR.strings.history),
            subtitle = titleCount?.takeIf { it > 0 }?.let {
                stringResource(
                    MR.strings.oracle_history_titles_read,
                    pluralStringResource(MR.plurals.library_title_count, it, it),
                )
            },
            actions = {
                AppBarActions(
                    listOf(
                        AppBar.Action(
                            title = stringResource(MR.strings.action_search),
                            icon = MaterialSymbols.Rounded.Search,
                            onClick = { onSearchQueryChange("") },
                        ),
                        clearHistoryAction,
                    ),
                )
            },
        )
    }
}

@Composable
private fun HistoryScreenContent(
    history: List<HistoryUiModel>,
    contentPadding: PaddingValues,
    onClickCover: (HistoryWithRelations) -> Unit,
    onClickResume: (HistoryWithRelations) -> Unit,
    onClickDelete: (HistoryWithRelations) -> Unit,
    onClickFavorite: (HistoryWithRelations) -> Unit,
) {
    val context = LocalContext.current
    val locale = LocalLocale.current
    val is24HourSystem = rememberIs24HourSystem()
    val timeFormat by remember { context.appGraph.uiPreferences.timeFormat }.collectAsState()
    val timeFormatter = remember(timeFormat, locale, is24HourSystem) {
        UiPreferences.timeFormat(timeFormat, locale.platformLocale, is24HourSystem)
    }

    // Number of titles read on each day, shown under the day header
    val dayCounts = remember(history) {
        val counts = HashMap<HistoryUiModel.Header, Int>()
        var currentHeader: HistoryUiModel.Header? = null
        for (model in history) {
            when (model) {
                is HistoryUiModel.Header -> currentHeader = model
                is HistoryUiModel.Item -> currentHeader?.let { counts[it] = (counts[it] ?: 0) + 1 }
            }
        }
        counts
    }

    FastScrollLazyColumn(
        contentPadding = contentPadding,
    ) {
        items(
            items = history,
            key = { "history-${it.hashCode()}" },
            contentType = {
                when (it) {
                    is HistoryUiModel.Header -> "header"
                    is HistoryUiModel.Item -> "item"
                }
            },
        ) { item ->
            when (item) {
                is HistoryUiModel.Header -> {
                    val count = dayCounts[item] ?: 0
                    OracleSectionHeader(
                        modifier = Modifier.animateItem(),
                        title = relativeDateText(item.date),
                        supporting = pluralStringResource(MR.plurals.library_title_count, count, count)
                            .takeIf { count > 0 },
                    )
                }
                is HistoryUiModel.Item -> {
                    val value = item.item
                    HistoryItem(
                        modifier = Modifier.animateItem(),
                        history = value,
                        timeFormatter = timeFormatter,
                        onClickCover = { onClickCover(value) },
                        onClickResume = { onClickResume(value) },
                        onClickDelete = { onClickDelete(value) },
                        onClickFavorite = { onClickFavorite(value) },
                    )
                }
            }
        }
    }
}

sealed interface HistoryUiModel {
    data class Header(val date: LocalDate) : HistoryUiModel
    data class Item(val item: HistoryWithRelations) : HistoryUiModel
}

@PreviewLightDark
@Composable
internal fun HistoryScreenPreviews(
    @PreviewParameter(HistoryviewModelStateProvider::class)
    historyState: HistoryViewModel.State,
) {
    TachiyomiPreviewTheme {
        HistoryScreen(
            state = historyState,
            snackbarHostState = SnackbarHostState(),
            onSearchQueryChange = {},
            onClickCover = {},
            onClickResume = { _, _ -> run {} },
            onDialogChange = {},
            onClickFavorite = {},
        )
    }
}
