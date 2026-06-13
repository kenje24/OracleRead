package eu.kanade.presentation.library.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import eu.kanade.domain.ui.model.AppTheme
import kotlin.math.sin

@Composable
internal fun LibraryAmbientBackground(
    appTheme: AppTheme,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "libraryAmbient")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sparkProgress",
    )

    val palette = when (appTheme) {
        AppTheme.FIRE_SPIRITS -> listOf(Color(0xFFFFB347), Color(0xFFFF6B35), Color(0xFFFFF1A8))
        AppTheme.MOON_WISPS -> listOf(Color(0xFFD8E6FF), Color(0xFFB8A7FF), Color(0xFFFFFFFF))
        AppTheme.SEA_LANTERNS -> listOf(Color(0xFF6FFFE9), Color(0xFF5BC0EB), Color(0xFFFFE66D))
        AppTheme.SAKURA_DRIFT -> listOf(Color(0xFFFFB7C5), Color(0xFFFF8FAB), Color(0xFFFFF0F4))
        AppTheme.AURORA_MOTES -> listOf(Color(0xFF86F7C8), Color(0xFF8EA7FF), Color(0xFFFFC2F2))
        else -> listOf(Color(0xFFFFB347), Color(0xFFFFE66D), Color(0xFFFFFFFF))
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        repeat(34) { index ->
            val seed = index + 1
            val cycle = (progress + (seed * 0.071f)) % 1f
            val baseX = ((seed * 73) % 100) / 100f * width
            val drift = sin((cycle * 6.28f) + seed) * width * (0.015f + (seed % 5) * 0.004f)
            val y = height * (1.05f - cycle * 1.18f)
            val radius = 1.4f + (seed % 4) * 0.85f
            val alpha = (1f - cycle).coerceIn(0f, 1f) * (0.08f + (seed % 3) * 0.035f)
            drawCircle(
                color = palette[index % palette.size].copy(alpha = alpha),
                radius = radius,
                center = Offset(baseX + drift, y),
            )
        }
    }
}
