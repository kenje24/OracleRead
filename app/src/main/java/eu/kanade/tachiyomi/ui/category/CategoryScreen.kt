package eu.kanade.tachiyomi.ui.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.util.fastMap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.zacsweers.metrox.viewmodel.metroViewModel
import eu.kanade.presentation.category.CategoryScreen
import eu.kanade.presentation.category.FolderAmbience
import eu.kanade.presentation.category.components.CategoryCreateDialog
import eu.kanade.presentation.category.components.CategoryDeleteDialog
import eu.kanade.presentation.category.components.CategoryRenameDialog
import eu.kanade.presentation.category.folderStyle
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.coroutines.flow.collectLatest
import mihon.app.di.appGraph
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.presentation.core.util.collectAsState

class CategoryScreen(
    private val openCreateDialog: Boolean = false,
) : Screen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = metroViewModel<CategoryViewModel>()

        val state by viewModel.state.collectAsStateWithLifecycle()
        // OracleRead: folders never restyled show the app theme's own effect, so their dialogs start on it
        val appTheme by context.appGraph.uiPreferences.appTheme.collectAsState()
        val themeAmbience = FolderAmbience.forAppTheme(appTheme)

        if (state is CategoryScreenState.Loading) {
            LoadingScreen()
            return
        }

        val successState = state as CategoryScreenState.Success

        var createDialogConsumed by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(openCreateDialog) {
            if (openCreateDialog && !createDialogConsumed) {
                createDialogConsumed = true
                viewModel.showDialog(CategoryDialog.Create)
            }
        }

        CategoryScreen(
            state = successState,
            onClickCreate = { viewModel.showDialog(CategoryDialog.Create) },
            onClickRename = { viewModel.showDialog(CategoryDialog.Rename(it)) },
            onClickEditStyle = { viewModel.showDialog(CategoryDialog.Rename(it)) },
            onClickDelete = { viewModel.showDialog(CategoryDialog.Delete(it)) },
            onChangeOrder = viewModel::changeOrder,
            navigateUp = navigator::pop,
        )

        when (val dialog = successState.dialog) {
            null -> {}
            CategoryDialog.Create -> {
                CategoryCreateDialog(
                    onDismissRequest = viewModel::dismissDialog,
                    onCreate = viewModel::createCategory,
                    defaultTheme = themeAmbience.key,
                    categories = successState.categories.fastMap { it.name },
                )
            }
            is CategoryDialog.Rename -> {
                val folderStyle = dialog.category.folderStyle(successState.folderStyles, themeAmbience)
                CategoryRenameDialog(
                    onDismissRequest = viewModel::dismissDialog,
                    onRename = { name, color, icon, theme ->
                        viewModel.renameCategory(dialog.category, name, color, icon, theme)
                    },
                    categories = successState.categories.fastMap { it.name },
                    category = dialog.category.name,
                    initialColor = folderStyle.color,
                    initialIcon = folderStyle.icon,
                    initialTheme = folderStyle.theme,
                )
            }
            is CategoryDialog.Delete -> {
                CategoryDeleteDialog(
                    onDismissRequest = viewModel::dismissDialog,
                    onDelete = { viewModel.deleteCategory(dialog.category.id) },
                    category = dialog.category.name,
                )
            }
        }

        LaunchedEffect(Unit) {
            viewModel.events.collectLatest { event ->
                if (event is CategoryEvent.LocalizedMessage) {
                    context.toast(event.stringRes)
                }
            }
        }
    }
}
