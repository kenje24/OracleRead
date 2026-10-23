package eu.kanade.presentation.reader.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindowProvider
import dev.icerock.moko.resources.StringResource
import eu.kanade.presentation.components.AdaptiveSheet
import eu.kanade.presentation.components.TabbedDialogPaddings
import eu.kanade.presentation.oracle.OraclePillTab
import eu.kanade.presentation.oracle.OraclePillTabs
import eu.kanade.tachiyomi.ui.reader.setting.ReaderSettingsViewModel
import kotlinx.coroutines.launch
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.SettingsItemsPaddings
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun ReaderSettingsDialog(
    onDismissRequest: () -> Unit,
    onShowMenus: () -> Unit,
    onHideMenus: () -> Unit,
    viewModel: ReaderSettingsViewModel,
) {
    val tabTitles = listOf(
        stringResource(MR.strings.pref_category_reading_mode),
        stringResource(MR.strings.pref_category_general),
        stringResource(MR.strings.custom_filter),
    )
    val tabs = remember(tabTitles) { tabTitles.map { OraclePillTab(label = it) } }
    val pagerState = rememberPagerState { tabTitles.size }
    val scope = rememberCoroutineScope()

    BoxWithConstraints {
        // AdaptiveSheet draws the sheet with the extraLarge shape; the tabs are cinematic pills
        AdaptiveSheet(
            modifier = Modifier.heightIn(max = maxHeight * 0.75f),
            onDismissRequest = {
                onDismissRequest()
                onShowMenus()
            },
        ) {
            Column {
                OraclePillTabs(
                    tabs = tabs,
                    selectedIndex = pagerState.currentPage,
                    onClick = { scope.launch { pagerState.animateScrollToPage(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                )

                HorizontalPager(
                    modifier = Modifier.animateContentSize(),
                    state = pagerState,
                    verticalAlignment = Alignment.Top,
                ) { page ->
                    val window = (LocalView.current.parent as? DialogWindowProvider)?.window

                    LaunchedEffect(pagerState.currentPage) {
                        if (pagerState.currentPage == 2) {
                            window?.setDimAmount(0f)
                            onHideMenus()
                        } else {
                            window?.setDimAmount(0.5f)
                            onShowMenus()
                        }
                    }

                    Column(
                        modifier = Modifier
                            .padding(vertical = TabbedDialogPaddings.Vertical)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        when (page) {
                            0 -> ReadingModePage(viewModel)
                            1 -> GeneralPage(viewModel)
                            2 -> ColorFilterPage(viewModel)
                        }
                    }
                }
            }
        }
    }
}

/** Section header for the reader settings sheet, set in the headline serif. */
@Composable
internal fun ReaderSettingsHeading(labelRes: StringResource) {
    ReaderSettingsHeading(stringResource(labelRes))
}

@Composable
internal fun ReaderSettingsHeading(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp, lineHeight = 26.sp),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = SettingsItemsPaddings.Horizontal,
                end = SettingsItemsPaddings.Horizontal,
                top = 16.dp,
                bottom = 6.dp,
            )
            .semantics { heading() },
    )
}
