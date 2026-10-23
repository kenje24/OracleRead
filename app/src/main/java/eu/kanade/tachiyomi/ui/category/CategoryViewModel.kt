package eu.kanade.tachiyomi.ui.category

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.icerock.moko.resources.StringResource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import eu.kanade.presentation.category.FolderStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mihon.core.metro.AppCoroutineScope
import tachiyomi.core.common.preference.getAndSet
import tachiyomi.domain.category.interactor.CreateCategoryWithName
import tachiyomi.domain.category.interactor.DeleteCategory
import tachiyomi.domain.category.interactor.GetCategories
import tachiyomi.domain.category.interactor.RenameCategory
import tachiyomi.domain.category.interactor.ReorderCategory
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.i18n.MR
import kotlin.time.Duration.Companion.seconds

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding = binding<ViewModel>())
class CategoryViewModel(
    @AppCoroutineScope private val appScope: CoroutineScope,
    private val getCategories: GetCategories,
    private val createCategoryWithName: CreateCategoryWithName,
    private val deleteCategory: DeleteCategory,
    private val reorderCategory: ReorderCategory,
    private val renameCategory: RenameCategory,
    private val libraryPreferences: LibraryPreferences,
) : ViewModel() {

    private val _events: Channel<CategoryEvent> = Channel()
    val events = _events.receiveAsFlow()

    private val dialog = MutableStateFlow<CategoryDialog?>(null)

    val state: StateFlow<CategoryScreenState> = combine(
        getCategories.subscribe(),
        libraryPreferences.folderStyles.changes(),
        dialog,
    ) { categories, folderStyles, dialog ->
        CategoryScreenState.Success(
            categories = categories.filterNot(Category::isSystemCategory),
            folderStyles = folderStyles,
            dialog = dialog,
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), CategoryScreenState.Loading)

    fun createCategory(name: String, color: Long, icon: String, theme: String) {
        viewModelScope.launch {
            when (createCategoryWithName.await(name)) {
                is CreateCategoryWithName.Result.InternalError -> _events.send(CategoryEvent.InternalError)
                is CreateCategoryWithName.Result.Success -> {
                    // Upstream's interactor does not return the new id; folder names are unique, so look it up
                    getCategories.await()
                        .firstOrNull { !it.isSystemCategory && it.name == name }
                        ?.let { setFolderStyle(it.id, color, icon, theme) }
                }
            }
        }
    }

    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            // The deletion finishes even if the screen is left while it runs
            when (appScope.async { deleteCategory.await(categoryId = categoryId) }.await()) {
                is DeleteCategory.Result.InternalError -> _events.send(CategoryEvent.InternalError)
                else -> {}
            }
        }
    }

    fun changeOrder(category: Category, newIndex: Int) {
        viewModelScope.launch {
            when (reorderCategory.await(category, newIndex)) {
                is ReorderCategory.Result.InternalError -> _events.send(CategoryEvent.InternalError)
                else -> {}
            }
        }
    }

    fun renameCategory(category: Category, name: String, color: Long, icon: String, theme: String) {
        viewModelScope.launch {
            if (name != category.name) {
                when (renameCategory.await(category, name)) {
                    is RenameCategory.Result.InternalError -> {
                        _events.send(CategoryEvent.InternalError)
                        return@launch
                    }
                    else -> {}
                }
            }
            setFolderStyle(category.id, color, icon, theme)
        }
    }

    private fun setFolderStyle(categoryId: Long, color: Long, icon: String, theme: String) {
        libraryPreferences.folderStyles.getAndSet { styles ->
            styles
                .filterNot { FolderStyle.parse(it)?.categoryId == categoryId }
                .plus(FolderStyle(categoryId, color, icon, theme).serialize())
                .toSet()
        }
    }

    fun showDialog(dialog: CategoryDialog) {
        this.dialog.update { dialog }
    }

    fun dismissDialog() {
        dialog.update { null }
    }
}

sealed interface CategoryDialog {
    data object Create : CategoryDialog
    data class Rename(val category: Category) : CategoryDialog
    data class Delete(val category: Category) : CategoryDialog
}

sealed interface CategoryEvent {
    sealed class LocalizedMessage(val stringRes: StringResource) : CategoryEvent
    data object InternalError : LocalizedMessage(MR.strings.internal_error)
}

sealed interface CategoryScreenState {

    @Immutable
    data object Loading : CategoryScreenState

    @Immutable
    data class Success(
        val categories: List<Category>,
        val folderStyles: Set<String> = emptySet(),
        val dialog: CategoryDialog? = null,
    ) : CategoryScreenState {

        val isEmpty: Boolean
            get() = categories.isEmpty()
    }
}
