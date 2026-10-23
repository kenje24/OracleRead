package eu.kanade.presentation.oracle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Glass container color: translucent so artwork and ambience show through, opaque enough for text. */
@Composable
fun oracleGlassColor(alpha: Float = 0.86f): Color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = alpha)

/** Hairline lit from the top-left in lantern light, fading into the outline: the edge of a glass pane. */
@Composable
fun oracleHairline(): BorderStroke = BorderStroke(
    width = 1.dp,
    brush = Brush.linearGradient(
        0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
        0.45f to MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f),
        1f to MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
    ),
)

/**
 * Translucent elevated container with a hairline border: the cinematic replacement for flat surfaces
 * (bars, chips, cards and sheets that float over artwork).
 */
@Composable
fun OracleGlass(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    alpha: Float = 0.86f,
    shadowElevation: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = oracleGlassColor(alpha),
            border = oracleHairline(),
            shadowElevation = shadowElevation,
        ) {
            Box(modifier = Modifier.oracleGrain(alpha = 0.04f, behindContent = true), content = content)
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = oracleGlassColor(alpha),
            border = oracleHairline(),
            shadowElevation = shadowElevation,
        ) {
            Box(modifier = Modifier.oracleGrain(alpha = 0.04f, behindContent = true), content = content)
        }
    }
}
