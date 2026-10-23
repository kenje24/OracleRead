package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import eu.kanade.core.preference.PreferenceMutableState
import eu.kanade.presentation.category.folderAmbientTheme
import eu.kanade.tachiyomi.ui.library.LibraryItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mihon.app.di.appGraph
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.library.model.LibraryDisplayMode
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.source.model.Source
import tachiyomi.presentation.core.components.material.PullRefresh
import tachiyomi.presentation.core.util.collectAsState
import kotlin.time.Duration.Companion.seconds

@Composable
fun LibraryContent(
    categories: List<Category>,
    searchQuery: String?,
    selection: Set<Long>,
    contentPadding: PaddingValues,
    currentPage: Int,
    hasActiveFilters: Boolean,
    showPageTabs: Boolean,
    onChangeCurrentPage: (Int) -> Unit,
    onClickManga: (Long) -> Unit,
    onLongClickFolder: (Category) -> Unit,
    onContinueReadingClicked: ((LibraryManga) -> Unit)?,
    onHeroContinueReading: (LibraryManga) -> Unit,
    onToggleSelection: (Category, LibraryManga) -> Unit,
    onToggleRangeSelection: (Category, LibraryManga) -> Unit,
    onRefresh: suspend () -> Boolean,
    onGlobalSearchClicked: () -> Unit,
    sourceShortcuts: List<Source>,
    onClickSourceShortcut: (Source) -> Unit,
    getItemCountForCategory: (Category) -> Int?,
    getDisplayMode: (Int) -> PreferenceMutableState<LibraryDisplayMode>,
    getColumnsForOrientation: (Boolean) -> PreferenceMutableState<Int>,
    getItemsForCategory: (Category) -> List<LibraryItem>,
) {
    val context = LocalContext.current
    val uiPreferences = remember { context.appGraph.uiPreferences }
    val libraryPreferences = remember { context.appGraph.libraryPreferences }
    val appTheme by uiPreferences.appTheme.collectAsState()
    val folderStyles by libraryPreferences.folderStyles.collectAsState()
    val showFeaturedManga by libraryPreferences.showFeaturedManga.collectAsState()

    val pagerState = rememberPagerState(currentPage) { categories.size }
    // Each folder carries its own ambience; the library falls back to the app theme's
    val ambientTheme = categories
        .getOrNull(pagerState.currentPage.coerceIn(0, categories.lastIndex.coerceAtLeast(0)))
        ?.folderAmbientTheme(folderStyles)
        ?: appTheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding(),
                start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                end = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
            ),
    ) {
        LibraryAmbientBackground(
            appTheme = ambientTheme,
            modifier = Modifier.fillMaxSize(),
        )
        Column {
            val scope = rememberCoroutineScope()
            var isRefreshing by remember(pagerState.currentPage) { mutableStateOf(false) }

            if (showPageTabs && categories.isNotEmpty() &&
                (categories.size > 1 || !categories.first().isSystemCategory)
            ) {
                LaunchedEffect(categories) {
                    if (categories.size <= pagerState.currentPage) {
                        pagerState.scrollToPage(categories.size - 1)
                    }
                }
                LibraryTabs(
                    categories = categories,
                    pagerState = pagerState,
                    getItemCountForCategory = getItemCountForCategory,
                    onTabItemClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(it)
                        }
                    },
                    onTabItemLongClick = onLongClickFolder,
                )
            }

            PullRefresh(
                refreshing = isRefreshing,
                enabled = selection.isEmpty(),
                onRefresh = {
                    scope.launch {
                        if (!onRefresh()) return@launch
                        // Fake refresh status but hide it after a second as it's a long running task
                        isRefreshing = true
                        delay(1.seconds)
                        isRefreshing = false
                    }
                },
            ) {
                LibraryPager(
                    state = pagerState,
                    contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding()),
                    hasActiveFilters = hasActiveFilters,
                    selection = selection,
                    searchQuery = searchQuery,
                    onGlobalSearchClicked = onGlobalSearchClicked,
                    getCategoryForPage = { page -> categories[page] },
                    getDisplayMode = getDisplayMode,
                    getColumnsForOrientation = getColumnsForOrientation,
                    getItemsForCategory = getItemsForCategory,
                    onClickManga = { category, manga ->
                        if (selection.isNotEmpty()) {
                            onToggleSelection(category, manga)
                        } else {
                            onClickManga(manga.manga.id)
                        }
                    },
                    onLongClickManga = onToggleRangeSelection,
                    onClickContinueReading = onContinueReadingClicked,
                    header = LibraryPageHeader(
                        showHero = showFeaturedManga && searchQuery.isNullOrEmpty() && selection.isEmpty(),
                        onHeroContinueReading = onHeroContinueReading,
                        sourceShortcuts = if (searchQuery.isNullOrEmpty()) sourceShortcuts else emptyList(),
                        onClickSourceShortcut = onClickSourceShortcut,
                    ),
                )
            }

            LaunchedEffect(pagerState.currentPage) {
                onChangeCurrentPage(pagerState.currentPage)
            }
        }
    }
}
