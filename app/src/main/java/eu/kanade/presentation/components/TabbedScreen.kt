package eu.kanade.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.zIndex
import dev.icerock.moko.resources.StringResource
import eu.kanade.presentation.oracle.OracleLargeTopBar
import eu.kanade.presentation.oracle.OraclePillTab
import eu.kanade.presentation.oracle.OraclePillTabs
import kotlinx.coroutines.launch
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Search
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource

/**
 * Cinematic tab shell: a large serif title with the current tab's actions, pill tabs under it and a
 * pager for the tab contents. A tab in search mode swaps the title for the familiar search toolbar.
 */
@Composable
fun TabbedScreen(
    titleRes: StringResource,
    tabs: List<TabContent>,
    state: PagerState = rememberPagerState { tabs.size },
    searchQuery: String? = null,
    onChangeSearchQuery: (String?) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            val tab = tabs[state.currentPage]
            val searchEnabled = tab.searchEnabled
            val activeSearchQuery = if (searchEnabled) searchQuery else null

            if (activeSearchQuery != null) {
                SearchToolbar(
                    titleContent = { AppBarTitle(stringResource(titleRes)) },
                    searchEnabled = true,
                    searchQuery = activeSearchQuery,
                    onChangeSearchQuery = onChangeSearchQuery,
                    actions = { AppBarActions(tab.actions) },
                )
            } else {
                val searchActions: List<AppBar.AppBarAction> = if (searchEnabled) {
                    listOf(
                        AppBar.Action(
                            title = stringResource(MR.strings.action_search),
                            icon = MaterialSymbols.Rounded.Search,
                            onClick = { onChangeSearchQuery("") },
                        ),
                    )
                } else {
                    emptyList()
                }
                OracleLargeTopBar(
                    title = stringResource(titleRes),
                    actions = { AppBarActions(searchActions + tab.actions) },
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(
                top = contentPadding.calculateTopPadding(),
                start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                end = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
            ),
        ) {
            OraclePillTabs(
                tabs = tabs.map { tab ->
                    OraclePillTab(
                        label = stringResource(tab.titleRes),
                        count = tab.badgeNumber,
                    )
                },
                selectedIndex = state.currentPage,
                onClick = { index -> scope.launch { state.animateScrollToPage(index) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f),
            )

            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = state,
                verticalAlignment = Alignment.Top,
            ) { page ->
                tabs[page].content(
                    PaddingValues(bottom = contentPadding.calculateBottomPadding()),
                    snackbarHostState,
                )
            }
        }
    }
}

data class TabContent(
    val titleRes: StringResource,
    val badgeNumber: Int? = null,
    val searchEnabled: Boolean = false,
    val actions: List<AppBar.AppBarAction> = listOf(),
    val content: @Composable (contentPadding: PaddingValues, snackbarHostState: SnackbarHostState) -> Unit,
)
