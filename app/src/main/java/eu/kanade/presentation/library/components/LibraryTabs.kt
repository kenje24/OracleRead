package eu.kanade.presentation.library.components

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import eu.kanade.presentation.category.folderAccentColor
import eu.kanade.presentation.category.folderIconForKey
import eu.kanade.presentation.category.folderIconKey
import eu.kanade.presentation.category.visualName
import eu.kanade.presentation.oracle.OraclePillTab
import eu.kanade.presentation.oracle.OraclePillTabs
import mihon.app.di.appGraph
import tachiyomi.domain.category.model.Category
import tachiyomi.presentation.core.util.collectAsState

/** Category pills: each folder's own accent color and icon, with its title count. */
@Composable
internal fun LibraryTabs(
    categories: List<Category>,
    pagerState: PagerState,
    getItemCountForCategory: (Category) -> Int?,
    onTabItemClick: (Int) -> Unit,
    onTabItemLongClick: (Category) -> Unit,
) {
    val context = LocalContext.current
    val folderStyles by remember { context.appGraph.libraryPreferences.folderStyles }.collectAsState()
    val currentPageIndex = pagerState.currentPage.coerceAtMost(categories.lastIndex)
    OraclePillTabs(
        tabs = categories.map { category ->
            OraclePillTab(
                label = category.visualName,
                count = getItemCountForCategory(category),
                icon = folderIconForKey(category.folderIconKey(folderStyles)),
                accent = category.folderAccentColor(folderStyles),
            )
        },
        selectedIndex = currentPageIndex,
        onClick = onTabItemClick,
        onLongClick = { onTabItemLongClick(categories[it]) },
    )
}
