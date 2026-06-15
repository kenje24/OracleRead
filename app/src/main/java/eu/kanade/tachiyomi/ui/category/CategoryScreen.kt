package eu.kanade.tachiyomi.ui.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.util.fastMap
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.category.CategoryScreen
import eu.kanade.presentation.category.components.CategoryCreateDialog
import eu.kanade.presentation.category.components.CategoryDeleteDialog
import eu.kanade.presentation.category.components.CategoryRenameDialog
import eu.kanade.presentation.category.folderStyle
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.coroutines.flow.collectLatest
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.presentation.core.util.collectAsState
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class CategoryScreen(
    private val openCreateDialog: Boolean = false,
) : Screen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { CategoryScreenModel() }

        val state by screenModel.state.collectAsState()
        val folderStyles by remember { Injekt.get<LibraryPreferences>().folderStyles }.collectAsState()

        if (state is CategoryScreenState.Loading) {
            LoadingScreen()
            return
        }

        val successState = state as CategoryScreenState.Success

        LaunchedEffect(openCreateDialog) {
            if (openCreateDialog) {
                screenModel.showDialog(CategoryDialog.Create)
            }
        }

        CategoryScreen(
            state = successState,
            onClickCreate = { screenModel.showDialog(CategoryDialog.Create) },
            onClickRename = { screenModel.showDialog(CategoryDialog.Rename(it)) },
            onClickDelete = { screenModel.showDialog(CategoryDialog.Delete(it)) },
            onChangeOrder = screenModel::changeOrder,
            navigateUp = navigator::pop,
        )

        when (val dialog = successState.dialog) {
            null -> {}
            CategoryDialog.Create -> {
                CategoryCreateDialog(
                    onDismissRequest = screenModel::dismissDialog,
                    onCreate = screenModel::createCategory,
                    categories = successState.categories.fastMap { it.name },
                )
            }
            is CategoryDialog.Rename -> {
                CategoryRenameDialog(
                    onDismissRequest = screenModel::dismissDialog,
                    onRename = { name, color, icon, theme ->
                        screenModel.renameCategory(dialog.category, name, color, icon, theme)
                    },
                    categories = successState.categories.fastMap { it.name },
                    category = dialog.category.name,
                    initialColor = dialog.category.folderStyle(folderStyles).color,
                    initialIcon = dialog.category.folderStyle(folderStyles).icon,
                    initialTheme = dialog.category.folderStyle(folderStyles).theme,
                )
            }
            is CategoryDialog.Delete -> {
                CategoryDeleteDialog(
                    onDismissRequest = screenModel::dismissDialog,
                    onDelete = { screenModel.deleteCategory(dialog.category.id) },
                    category = dialog.category.name,
                )
            }
        }

        LaunchedEffect(Unit) {
            screenModel.events.collectLatest { event ->
                if (event is CategoryEvent.LocalizedMessage) {
                    context.toast(event.stringRes)
                }
            }
        }
    }
}
