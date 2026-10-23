package eu.kanade.presentation.oracle

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.theme.OracleSpacing

data class OraclePillTab(
    val label: String,
    val count: Int? = null,
    val icon: ImageVector? = null,
    /** Optional identity color (e.g. a folder theme). Always paired with the label and icon, never alone. */
    val accent: Color? = null,
)

/**
 * Horizontally scrolling pill tabs. The selected pill fills with its accent (or the theme primary); the
 * others are glass outlines. Keeps the selected pill in view as the pager moves.
 */
@Composable
fun OraclePillTabs(
    tabs: List<OraclePillTab>,
    selectedIndex: Int,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: ((Int) -> Unit)? = null,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(selectedIndex) {
        if (selectedIndex in tabs.indices) listState.animateScrollToItem(selectedIndex)
    }
    LazyRow(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = OracleSpacing.gutter, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(tabs) { index, tab ->
            PillTab(
                tab = tab,
                selected = index == selectedIndex,
                onClick = { onClick(index) },
                onLongClick = onLongClick?.let { { it(index) } },
            )
        }
    }
}

@Composable
private fun PillTab(
    tab: OraclePillTab,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
) {
    val accent = tab.accent ?: MaterialTheme.colorScheme.primary
    val container by animateColorAsState(
        if (selected) accent else oracleGlassColor(0.6f),
        label = "pillContainer",
    )
    val content = if (selected) {
        if (tab.accent != null) readableOn(accent) else MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Surface(
        shape = CircleShape,
        color = container,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .height(38.dp)
            .clip(CircleShape)
            .combinedClickable(role = Role.Tab, onClick = onClick, onLongClick = onLongClick)
            .semantics { this.selected = selected },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tab.icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = if (selected) content else accent,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(text = tab.label, style = MaterialTheme.typography.labelLarge, color = content, maxLines = 1)
            tab.count?.let {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = it.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = content.copy(alpha = 0.72f),
                )
            }
        }
    }
}

private fun readableOn(color: Color): Color {
    val luminance = 0.2126f * color.red + 0.7152f * color.green + 0.0722f * color.blue
    return if (luminance > 0.55f) Color(0xFF14161B) else Color(0xFFF7F3EA)
}
