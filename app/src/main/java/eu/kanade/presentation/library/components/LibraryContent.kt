package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import eu.kanade.core.preference.PreferenceMutableState
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.presentation.category.folderAmbientTheme
import eu.kanade.presentation.browse.components.SourceIcon
import eu.kanade.tachiyomi.ui.library.LibraryItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.library.model.LibraryDisplayMode
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.domain.source.model.Source
import tachiyomi.presentation.core.components.material.PullRefresh
import tachiyomi.presentation.core.util.collectAsState
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
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
    onClickSourceShortcut: (Source) -> Unit,
    onLongClickFolder: (Category) -> Unit,
    onContinueReadingClicked: ((LibraryManga) -> Unit)?,
    onToggleSelection: (Category, LibraryManga) -> Unit,
    onToggleRangeSelection: (Category, LibraryManga) -> Unit,
    onRefresh: () -> Boolean,
    onGlobalSearchClicked: () -> Unit,
    getItemCountForCategory: (Category) -> Int?,
    sourceShortcuts: List<Source>,
    getDisplayMode: (Int) -> PreferenceMutableState<LibraryDisplayMode>,
    getColumnsForOrientation: (Boolean) -> PreferenceMutableState<Int>,
    getItemsForCategory: (Category) -> List<LibraryItem>,
) {
    val appTheme by remember { Injekt.get<UiPreferences>().appTheme }.collectAsState()
    val folderStyles by remember { Injekt.get<LibraryPreferences>().folderStyles }.collectAsState()
    val ambientTheme = categories
        .getOrNull(currentPage.coerceIn(0, categories.lastIndex.coerceAtLeast(0)))
        ?.folderAmbientTheme(folderStyles)
        ?: appTheme

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = sourceShortcuts.isNotEmpty() && selection.isEmpty(),
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Extensions in your library",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                )
                sourceShortcuts.forEach { source ->
                    NavigationDrawerItem(
                        label = { Text(source.name) },
                        selected = false,
                        icon = { SourceIcon(source = source, modifier = Modifier.size(24.dp)) },
                        onClick = {
                            drawerScope.launch {
                                drawerState.close()
                                onClickSourceShortcut(source)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
            }
        },
    ) {
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
            val pagerState = rememberPagerState(currentPage) { categories.size }

            val scope = rememberCoroutineScope()
            var isRefreshing by remember(pagerState.currentPage) { mutableStateOf(false) }

            val shouldShowTabs = showPageTabs &&
                categories.isNotEmpty() &&
                (categories.size > 1 || !categories.first().isSystemCategory)
            if (shouldShowTabs) {
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
                    val started = onRefresh()
                    if (!started) return@PullRefresh
                    scope.launch {
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
                )
            }

            LaunchedEffect(pagerState.currentPage) {
                onChangeCurrentPage(pagerState.currentPage)
                }
            }
        }
        }
    }
