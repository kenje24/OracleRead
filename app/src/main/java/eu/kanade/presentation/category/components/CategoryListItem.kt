package eu.kanade.presentation.category.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.category.folderAccentColor
import eu.kanade.presentation.category.folderIconForKey
import eu.kanade.presentation.category.folderIconKey
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Delete
import mihon.icons.materialsymbols.rounded.DragHandle
import mihon.icons.materialsymbols.rounded.Edit
import sh.calvin.reorderable.ReorderableCollectionItemScope
import tachiyomi.domain.category.model.Category
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun ReorderableCollectionItemScope.CategoryListItem(
    category: Category,
    folderStyles: Set<String>,
    onRename: () -> Unit,
    onEditStyle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val folderColor = category.folderAccentColor(folderStyles)
    ElevatedCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onRename)
                .padding(vertical = MaterialTheme.padding.small)
                .padding(
                    start = MaterialTheme.padding.small,
                    end = MaterialTheme.padding.medium,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MaterialSymbols.Rounded.DragHandle,
                contentDescription = null,
                modifier = Modifier
                    .padding(MaterialTheme.padding.medium)
                    .draggableHandle(),
            )
            // Folder style: color dot + icon; tapping it opens the style editor
            IconButton(onClick = onEditStyle) {
                Box {
                    Icon(
                        imageVector = folderIconForKey(category.folderIconKey(folderStyles)),
                        contentDescription = stringResource(MR.strings.action_edit_folder_style),
                        tint = folderColor,
                        modifier = Modifier.size(22.dp),
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow, CircleShape)
                            .padding(1.dp)
                            .background(folderColor, CircleShape),
                    )
                }
            }
            Text(
                text = category.name,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = MaterialTheme.padding.extraSmall),
                color = folderColor,
                style = MaterialTheme.typography.bodyLarge,
            )
            IconButton(onClick = onRename) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Edit,
                    contentDescription = stringResource(MR.strings.action_rename_category),
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Delete,
                    contentDescription = stringResource(MR.strings.action_delete),
                )
            }
        }
    }
}
