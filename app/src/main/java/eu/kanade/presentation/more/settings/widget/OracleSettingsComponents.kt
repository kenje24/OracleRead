package eu.kanade.presentation.more.settings.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.oracle.OracleGlass
import eu.kanade.presentation.oracle.oracleGlassColor
import eu.kanade.presentation.oracle.oracleHairline

/** Outer margin between glass settings cards and the screen edge. */
val OracleCardMargin = 16.dp

private val SegmentOuterCorner = 22.dp
private val SegmentInnerCorner = 6.dp
private val SegmentGap = 2.dp

/**
 * Small rounded tile holding an icon, tinted with [tint] over a soft wash of the same color. Used for
 * menu and preference icons so they read as part of the cinematic card language.
 */
@Composable
fun OracleIconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = tint.copy(alpha = 0.14f),
    size: Dp = 36.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(MaterialTheme.shapes.small)
            .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.58f),
        )
    }
}

/**
 * Turns a list row into one segment of a grouped glass card: the first and last rows of a group get the
 * large outer corners, rows in between get small corners and a hairline gap, so a run of rows reads as
 * one rounded card while each row stays its own lazy list item.
 */
@Composable
fun Modifier.oracleCardSegment(
    isFirst: Boolean,
    isLast: Boolean,
    selected: Boolean = false,
): Modifier {
    val top = if (isFirst) SegmentOuterCorner else SegmentInnerCorner
    val bottom = if (isLast) SegmentOuterCorner else SegmentInnerCorner
    val shape = RoundedCornerShape(topStart = top, topEnd = top, bottomEnd = bottom, bottomStart = bottom)
    val color = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    } else {
        oracleGlassColor()
    }
    return this
        .padding(horizontal = OracleCardMargin)
        .padding(top = if (isFirst) 0.dp else SegmentGap)
        .clip(shape)
        .background(color)
        .border(oracleHairline(), shape)
}

/** A rounded glass card with the standard outer margin, laying its rows out in a column. */
@Composable
fun OracleSettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    OracleGlass(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = OracleCardMargin, vertical = 6.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content,
        )
    }
}

/** Card heading in the headline serif, colored with the lantern primary. */
@Composable
fun OracleCardTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp, lineHeight = 26.sp),
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 6.dp)
            .semantics { heading() },
    )
}

/** A tappable row inside an [OracleSettingsCard]: icon tile, title and optional subtitle. */
@Composable
fun OracleMenuRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 60.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OracleIconTile(icon = icon)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}
