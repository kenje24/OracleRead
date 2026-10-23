package eu.kanade.presentation.library.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.oracle.OracleGlass
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Add
import mihon.icons.materialsymbols.rounded.CreateNewFolder
import mihon.icons.materialsymbols.rounded.Download
import mihon.icons.materialsymbols.rounded.Forum
import mihon.icons.materialsymbols.rounded.Person
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

/**
 * The library's quick actions (new folder, downloads, profile, community) as a lantern FAB that unfolds
 * into labelled glass actions above the dock.
 */
@Composable
fun LibraryQuickActions(
    onCreateFolder: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenCommunity: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 45f else 0f, label = "quickActionsRotation")
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                QuickAction(stringResource(MR.strings.action_add_category), MaterialSymbols.Rounded.CreateNewFolder) {
                    expanded = false
                    onCreateFolder()
                }
                QuickAction(stringResource(MR.strings.label_download_queue), MaterialSymbols.Rounded.Download) {
                    expanded = false
                    onOpenDownloads()
                }
                QuickAction(stringResource(MR.strings.library_my_profile), MaterialSymbols.Rounded.Person) {
                    expanded = false
                    onOpenProfile()
                }
                QuickAction(stringResource(MR.strings.action_community), MaterialSymbols.Rounded.Forum) {
                    expanded = false
                    onOpenCommunity()
                }
            }
        }
        FloatingActionButton(
            onClick = { expanded = !expanded },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
        ) {
            Icon(
                imageVector = MaterialSymbols.Rounded.Add,
                contentDescription = stringResource(MR.strings.library_quick_actions),
                modifier = Modifier.rotate(rotation),
            )
        }
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    OracleGlass(shape = CircleShape, alpha = 0.94f, shadowElevation = 6.dp, onClick = onClick) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 18.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(20.dp),
            )
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
