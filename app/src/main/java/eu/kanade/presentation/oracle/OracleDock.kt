package eu.kanade.presentation.oracle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Height of the floating dock itself, without its margins. */
val OracleDockHeight = 64.dp

/** Space the dock occupies above the system navigation bar: its height plus its floating margin. */
val OracleDockSpace = OracleDockHeight + 20.dp

/**
 * Floating pill navigation for phones: a glass capsule hovering above content. The selected destination
 * grows into a lantern-colored pill with its label; the rest stay as icons with their labels for screen
 * readers.
 */
@Composable
fun OracleDock(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    OracleGlass(
        modifier = modifier.height(OracleDockHeight),
        shape = CircleShape,
        alpha = 0.92f,
        shadowElevation = 14.dp,
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

/** Vertical counterpart of [OracleDock] for tablets and wide windows. */
@Composable
fun OracleRail(
    modifier: Modifier = Modifier,
    header: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    OracleGlass(
        modifier = modifier.width(88.dp),
        shape = MaterialTheme.shapes.extraLarge,
        alpha = 0.92f,
        shadowElevation = 10.dp,
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            header()
            content()
        }
    }
}

@Composable
fun RowScope.OracleDockItem(
    selected: Boolean,
    icon: Painter,
    label: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    badge: String? = null,
    badgeDescription: String? = null,
) {
    val container by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        spring(stiffness = 600f),
        label = "dockItemContainer",
    )
    val tint by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "dockItemTint",
    )
    Row(
        modifier = Modifier
            .height(48.dp)
            .clip(CircleShape)
            .background(container)
            .combinedClickable(
                role = Role.Tab,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .semantics(mergeDescendants = true) {
                this.selected = selected
                contentDescription = listOfNotNull(label, badgeDescription).joinToString(", ")
            }
            .padding(horizontal = if (selected) 18.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DockIcon(icon = icon, tint = tint, badge = badge)
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally(),
        ) {
            Row {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = tint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun ColumnScope.OracleRailItem(
    selected: Boolean,
    icon: Painter,
    label: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    badge: String? = null,
    badgeDescription: String? = null,
) {
    val container by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "railItemContainer",
    )
    val tint by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "railItemTint",
    )
    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.large)
            .combinedClickable(role = Role.Tab, onClick = onClick, onLongClick = onLongClick)
            .semantics(mergeDescendants = true) {
                this.selected = selected
                contentDescription = listOfNotNull(label, badgeDescription).joinToString(", ")
            }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(container)
                .padding(horizontal = 18.dp, vertical = 8.dp),
        ) {
            DockIcon(icon = icon, tint = tint, badge = badge)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DockIcon(icon: Painter, tint: Color, badge: String?) {
    Box {
        Icon(painter = icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        if (badge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(start = 14.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
                    .padding(horizontal = 5.dp),
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiary,
                    maxLines = 1,
                )
            }
        }
    }
}
