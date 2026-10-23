package eu.kanade.tachiyomi.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import eu.kanade.domain.ui.model.NavigationBarMode
import eu.kanade.presentation.oracle.OracleDock
import eu.kanade.presentation.oracle.OracleDockItem
import eu.kanade.presentation.oracle.OracleDockSpace
import eu.kanade.presentation.oracle.OracleRail
import eu.kanade.presentation.oracle.OracleRailItem
import eu.kanade.presentation.util.Screen
import eu.kanade.presentation.util.isTabletUi
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.browse.BrowseTab
import eu.kanade.tachiyomi.ui.download.DownloadQueueScreen
import eu.kanade.tachiyomi.ui.history.HistoryTab
import eu.kanade.tachiyomi.ui.library.LibraryTab
import eu.kanade.tachiyomi.ui.manga.MangaScreen
import eu.kanade.tachiyomi.ui.more.MoreTab
import eu.kanade.tachiyomi.ui.updates.UpdatesTab
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import mihon.app.di.appGraph
import soup.compose.material.motion.animation.materialFadeThroughIn
import soup.compose.material.motion.animation.materialFadeThroughOut
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.util.collectAsState

object HomeScreen : Screen() {

    private val librarySearchEvent = Channel<String>()
    private val openTabEvent = Channel<Tab>()
    private val showBottomNavEvent = Channel<Boolean>()

    @Suppress("ConstPropertyName")
    private const val TabFadeDuration = 200

    @Suppress("ConstPropertyName")
    private const val TabNavigatorKey = "HomeTabs"

    private val TABS = listOf(
        LibraryTab,
        UpdatesTab,
        HistoryTab,
        BrowseTab,
        MoreTab,
    )

    // Compact navigation keeps the dock to three destinations until Browse is opened
    private val COMPACT_TABS = listOf(
        LibraryTab,
        UpdatesTab,
        BrowseTab,
    )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val uiPreferences = remember { context.appGraph.uiPreferences }
        val navigationBarMode by uiPreferences.navigationBarMode.collectAsState()
        TabNavigator(
            tab = LibraryTab,
            key = TabNavigatorKey,
        ) { tabNavigator ->
            var expandedNav by rememberSaveable { mutableStateOf(false) }
            val tabs = if (navigationBarMode == NavigationBarMode.FULL || expandedNav) TABS else COMPACT_TABS
            val onTabSelected: (eu.kanade.presentation.util.Tab) -> Unit = { selectedTab ->
                if (navigationBarMode == NavigationBarMode.COMPACT) {
                    when (selectedTab) {
                        LibraryTab -> expandedNav = false
                        BrowseTab -> expandedNav = true
                        else -> Unit
                    }
                }
            }

            // Provide usable navigator to content screen
            CompositionLocalProvider(LocalNavigator provides navigator) {
                val tabletUi = isTabletUi()
                val bottomNavVisible by produceState(initialValue = true) {
                    showBottomNavEvent.receiveAsFlow().collectLatest { value = it }
                }
                val dockVisible = !tabletUi && bottomNavVisible
                val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                val contentBottom by animateDpAsState(
                    if (dockVisible) OracleDockSpace + navBarInset else 0.dp,
                    label = "dockSpace",
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        if (tabletUi) {
                            OracleRail(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .windowInsetsPadding(
                                        WindowInsets.safeDrawing.only(
                                            WindowInsetsSides.Start + WindowInsetsSides.Vertical,
                                        ),
                                    )
                                    .padding(12.dp),
                                header = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_mihon),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(bottom = 12.dp)
                                            .size(32.dp),
                                    )
                                },
                            ) {
                                tabs.forEach { tab -> RailItem(tab, onTabSelected) }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(bottom = contentBottom)
                                .consumeWindowInsets(PaddingValues(bottom = contentBottom)),
                        ) {
                            AnimatedContent(
                                targetState = tabNavigator.current,
                                transitionSpec = {
                                    materialFadeThroughIn(
                                        initialScale = 0.98f,
                                        durationMillis = TabFadeDuration,
                                    ) togetherWith materialFadeThroughOut(durationMillis = TabFadeDuration)
                                },
                                label = "tabContent",
                            ) {
                                tabNavigator.saveableState(key = "currentTab", it) {
                                    it.Content()
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = dockVisible,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(bottom = 12.dp),
                    ) {
                        AnimatedContent(
                            targetState = tabs,
                            label = "dockTabs",
                        ) { dockTabs ->
                            OracleDock {
                                dockTabs.forEach { tab -> DockItem(tab, onTabSelected) }
                            }
                        }
                    }
                }
            }

            val goToLibraryTab = { tabNavigator.current = LibraryTab }

            BackHandler(enabled = tabNavigator.current != LibraryTab, onBack = goToLibraryTab)

            // Keep the compact dock's expanded state in sync when the tab changes programmatically
            // (app shortcuts, deep links, search) instead of via a dock tap
            LaunchedEffect(tabNavigator.current, navigationBarMode) {
                if (navigationBarMode == NavigationBarMode.COMPACT) {
                    when (tabNavigator.current) {
                        LibraryTab -> expandedNav = false
                        HistoryTab, MoreTab -> expandedNav = true
                        else -> Unit
                    }
                }
            }

            LaunchedEffect(Unit) {
                launch {
                    librarySearchEvent.receiveAsFlow().collectLatest {
                        goToLibraryTab()
                        LibraryTab.search(it)
                    }
                }
                launch {
                    openTabEvent.receiveAsFlow().collectLatest {
                        tabNavigator.current = when (it) {
                            is Tab.Library -> LibraryTab
                            Tab.Updates -> UpdatesTab
                            Tab.History -> HistoryTab
                            is Tab.Browse -> {
                                if (it.toExtensions) {
                                    BrowseTab.showExtension()
                                }
                                BrowseTab
                            }
                            is Tab.More -> MoreTab
                        }

                        if (it is Tab.Library && it.mangaIdToOpen != null) {
                            navigator.push(MangaScreen(it.mangaIdToOpen))
                        }
                        if (it is Tab.More && it.toDownloads) {
                            navigator.push(DownloadQueueScreen)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun androidx.compose.foundation.layout.RowScope.DockItem(
        tab: eu.kanade.presentation.util.Tab,
        onTabSelected: (eu.kanade.presentation.util.Tab) -> Unit,
    ) {
        val tabNavigator = LocalTabNavigator.current
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val selected = tabNavigator.current::class == tab::class
        val (badge, badgeDescription) = tabBadge(tab)
        OracleDockItem(
            selected = selected,
            icon = tab.options.icon!!,
            label = tab.options.title,
            badge = badge,
            badgeDescription = badgeDescription,
            onClick = {
                if (!selected) {
                    tabNavigator.current = tab
                    onTabSelected(tab)
                } else {
                    scope.launch { tab.onReselect(navigator) }
                }
            },
        )
    }

    @Composable
    private fun androidx.compose.foundation.layout.ColumnScope.RailItem(
        tab: eu.kanade.presentation.util.Tab,
        onTabSelected: (eu.kanade.presentation.util.Tab) -> Unit,
    ) {
        val tabNavigator = LocalTabNavigator.current
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val selected = tabNavigator.current::class == tab::class
        val (badge, badgeDescription) = tabBadge(tab)
        OracleRailItem(
            selected = selected,
            icon = tab.options.icon!!,
            label = tab.options.title,
            badge = badge,
            badgeDescription = badgeDescription,
            onClick = {
                if (!selected) {
                    tabNavigator.current = tab
                    onTabSelected(tab)
                } else {
                    scope.launch { tab.onReselect(navigator) }
                }
            },
        )
    }

    /** Badge text and its spoken description for a tab, or nulls when there is nothing to show. */
    @Composable
    private fun tabBadge(tab: eu.kanade.presentation.util.Tab): Pair<String?, String?> {
        val context = LocalContext.current
        val count by produceState(initialValue = 0, tab) {
            val graph = context.appGraph
            when (tab) {
                is UpdatesTab -> {
                    combine(
                        graph.libraryPreferences.newShowUpdatesCount.changes(),
                        graph.libraryPreferences.newUpdatesCount.changes(),
                    ) { show, count ->
                        if (show) count else 0
                    }
                        .collectLatest { value = it }
                }

                is BrowseTab -> {
                    graph.sourcePreferences.extensionUpdatesCount.changes()
                        .collectLatest { value = it }
                }

                else -> value = 0
            }
        }
        if (count <= 0) return null to null
        val description = when (tab) {
            is UpdatesTab -> pluralStringResource(MR.plurals.notification_chapters_generic, count = count, count)
            is BrowseTab -> pluralStringResource(MR.plurals.update_check_notification_ext_updates, count = count, count)
            else -> null
        }
        return count.toString() to description
    }

    suspend fun search(query: String) {
        librarySearchEvent.send(query)
    }

    suspend fun openTab(tab: Tab) {
        openTabEvent.send(tab)
    }

    suspend fun showBottomNav(show: Boolean) {
        showBottomNavEvent.send(show)
    }

    sealed interface Tab {
        data class Library(val mangaIdToOpen: Long? = null) : Tab
        data object Updates : Tab
        data object History : Tab
        data class Browse(val toExtensions: Boolean = false) : Tab
        data class More(val toDownloads: Boolean) : Tab
    }
}
