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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import eu.kanade.domain.ui.model.AppTheme
import kotlin.math.cos
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
        AppTheme.INK_RAIN -> listOf(Color(0xFF44546A), Color(0xFF111827), Color(0xFFA7B0C0))
        AppTheme.STAR_DUST -> listOf(Color(0xFFFFF1A8), Color(0xFFB8A7FF), Color(0xFFFFFFFF))
        else -> listOf(Color(0xFFFFB347), Color(0xFFFFE66D), Color(0xFFFFFFFF))
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    palette[0].copy(alpha = 0.10f),
                    Color.Transparent,
                    palette[1].copy(alpha = 0.05f),
                ),
            ),
        )

        repeat(58) { index ->
            val seed = index + 1
            val cycle = (progress + (seed * 0.047f)) % 1f
            val baseX = ((seed * 73) % 100) / 100f * width
            val wave = sin((cycle * 6.28f) + seed)
            val color = palette[index % palette.size]
            val (center, radius, alpha) = when (appTheme) {
                AppTheme.MOON_WISPS -> {
                    val x = width * ((cycle + (seed * 0.031f)) % 1f)
                    val y = height * (0.18f + ((seed * 17) % 72) / 100f) + wave * 18f
                    Triple(Offset(x, y), 3.5f + (seed % 4), 0.10f + (seed % 3) * 0.025f)
                }
                AppTheme.SEA_LANTERNS -> {
                    val x = baseX + wave * width * 0.035f
                    val y = height * (0.95f - cycle * 0.75f) + cos(cycle * 12.56f + seed) * 18f
                    Triple(Offset(x, y), 4.5f + (seed % 3) * 1.4f, 0.14f + (seed % 3) * 0.03f)
                }
                AppTheme.SAKURA_DRIFT -> {
                    val x = baseX + wave * width * 0.09f
                    val y = height * (-0.08f + cycle * 1.16f)
                    Triple(Offset(x, y), 2.6f + (seed % 4) * 0.8f, 0.13f + (seed % 3) * 0.03f)
                }
                AppTheme.AURORA_MOTES -> {
                    val x = width * ((cycle * 0.65f + (seed * 0.043f)) % 1f)
                    val y = height * (0.20f + ((seed * 13) % 58) / 100f) + wave * 34f
                    Triple(Offset(x, y), 2.4f + (seed % 5), 0.11f + (seed % 4) * 0.025f)
                }
                AppTheme.INK_RAIN -> {
                    val x = baseX + wave * width * 0.012f
                    val y = height * (-0.04f + cycle * 1.18f)
                    Triple(Offset(x, y), 1.4f + (seed % 3) * 0.55f, 0.10f + (seed % 4) * 0.025f)
                }
                AppTheme.STAR_DUST -> {
                    val twinkle = ((sin(progress * 25f + seed) + 1f) / 2f)
                    val x = ((seed * 59) % 100) / 100f * width
                    val y = ((seed * 83) % 100) / 100f * height
                    Triple(Offset(x, y), 1.7f + twinkle * 3f, 0.08f + twinkle * 0.24f)
                }
                else -> {
                    val drift = wave * width * (0.025f + (seed % 7) * 0.006f)
                    val y = height * (1.08f - cycle * 1.22f)
                    val radius = 2.2f + (seed % 5) * 0.9f
                    val alpha = (1f - cycle).coerceIn(0f, 1f) * (0.18f + (seed % 4) * 0.04f)
                    Triple(Offset(baseX + drift, y), radius, alpha)
                }
            }
            if (appTheme == AppTheme.INK_RAIN) {
                drawLine(
                    color = color.copy(alpha = alpha),
                    start = center,
                    end = center.copy(y = center.y + radius * 8f),
                    strokeWidth = radius,
                )
                return@repeat
            }
            drawCircle(
                color = color.copy(alpha = alpha * 0.22f),
                radius = radius * 3.2f,
                center = center,
            )
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = radius,
                center = center,
            )
        }
    }
}
