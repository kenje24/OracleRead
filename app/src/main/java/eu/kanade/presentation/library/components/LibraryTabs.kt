package eu.kanade.presentation.library.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import eu.kanade.presentation.category.folderAccentColor
import eu.kanade.presentation.category.folderIconForKey
import eu.kanade.presentation.category.folderIconKey
import eu.kanade.presentation.category.visualName
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.presentation.core.components.material.TabText
import tachiyomi.presentation.core.util.collectAsState
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

@Composable
internal fun LibraryTabs(
    categories: List<Category>,
    pagerState: PagerState,
    getItemCountForCategory: (Category) -> Int?,
    onTabItemClick: (Int) -> Unit,
    onTabItemLongClick: (Category) -> Unit,
) {
    val folderStyles by remember { Injekt.get<LibraryPreferences>().folderStyles }.collectAsState()
    val currentPageIndex = pagerState.currentPage.coerceAtMost(categories.lastIndex)
    val selectedTabIndex = currentPageIndex
    Column(modifier = Modifier.zIndex(2f)) {
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 0.dp,
            // TODO: use default when width is fixed upstream
            // https://issuetracker.google.com/issues/242879624
            divider = {},
        ) {
            categories.forEachIndexed { index, category ->
                val folderColor = category.folderAccentColor(folderStyles)
                Tab(
                    modifier = Modifier.combinedClickable(
                        onClick = { onTabItemClick(index) },
                        onLongClick = { onTabItemLongClick(category) },
                    ),
                    selected = currentPageIndex == index,
                    onClick = { onTabItemClick(index) },
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = folderIconForKey(category.folderIconKey(folderStyles)),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            TabText(
                                text = category.visualName,
                                badgeCount = getItemCountForCategory(category),
                            )
                        }
                    },
                    selectedContentColor = folderColor,
                    unselectedContentColor = folderColor.copy(alpha = 0.72f),
                )
            }
        }

        HorizontalDivider()
    }
}
