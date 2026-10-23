package eu.kanade.presentation.oracle

import android.graphics.Bitmap
import android.os.Build
import androidx.collection.LruCache
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import eu.kanade.presentation.theme.colorscheme.MonetCompatColorScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

/**
 * Full-bleed, blurred and slightly enlarged cover art that fades into the background: the stage the
 * cinematic surfaces sit on. Android 12+ blurs the image; older versions use a heavier scrim instead.
 */
@Composable
fun CinematicBackdrop(
    data: Any?,
    modifier: Modifier = Modifier,
) {
    val background = MaterialTheme.colorScheme.background
    val canBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    Box(modifier = modifier) {
        AsyncImage(
            model = data,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1.18f
                    scaleY = 1.18f
                    alpha = if (canBlur) 0.9f else 0.55f
                }
                .then(if (canBlur) Modifier.blur(36.dp) else Modifier),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to background.copy(alpha = if (canBlur) 0.30f else 0.55f),
                        0.45f to background.copy(alpha = if (canBlur) 0.62f else 0.78f),
                        1f to background,
                    ),
                ),
        )
    }
}

private val seedCache = LruCache<Any, Int>(128)

/**
 * A color scheme tinted by the dominant color of a cover. Accents (primary/secondary/tertiary and their
 * containers) come from the cover; surfaces stay those of [fallback], so the page keeps the app's night
 * canvas. Falls back to [fallback] until, or unless, the cover loads.
 */
@Composable
fun rememberCoverColorScheme(
    data: Any?,
    fallback: ColorScheme = MaterialTheme.colorScheme,
): ColorScheme {
    val context = LocalContext.current
    val isDark = fallback.background.luminance() < 0.5f
    val seed by produceState(initialValue = data?.let { seedCache[it] }, data) {
        if (data == null || value != null) return@produceState
        value = withContext(Dispatchers.IO) {
            val request = ImageRequest.Builder(context)
                .data(data)
                .size(64)
                .allowHardware(false)
                .build()
            val result = context.imageLoader.execute(request) as? SuccessResult ?: return@withContext null
            dominantColor(result.image.toBitmap())
        }
        value?.let { seedCache.put(data, it) }
    }
    val tinted = seed?.let { argb ->
        val generated = MonetCompatColorScheme.generateColorSchemeFromSeed(Color(argb), dark = isDark)
        fallback.copy(
            primary = generated.primary,
            onPrimary = generated.onPrimary,
            primaryContainer = generated.primaryContainer,
            onPrimaryContainer = generated.onPrimaryContainer,
            secondary = generated.secondary,
            onSecondary = generated.onSecondary,
            secondaryContainer = generated.secondaryContainer,
            onSecondaryContainer = generated.onSecondaryContainer,
            tertiary = generated.tertiary,
            onTertiary = generated.onTertiary,
            tertiaryContainer = generated.tertiaryContainer,
            onTertiaryContainer = generated.onTertiaryContainer,
            surfaceTint = generated.primary,
        )
    } ?: fallback
    // Ease into the cover's accent instead of snapping when it resolves
    val primary by animateColorAsState(tinted.primary, tween(450), label = "coverPrimary")
    val primaryContainer by animateColorAsState(tinted.primaryContainer, tween(450), label = "coverPrimaryContainer")
    return tinted.copy(primary = primary, primaryContainer = primaryContainer)
}

/** Wraps [content] in the [rememberCoverColorScheme] of [data], keeping the current type and shapes. */
@Composable
fun CoverColorTheme(
    data: Any?,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = rememberCoverColorScheme(data),
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}

/**
 * Picks a representative accent from a small bitmap: averages pixels weighted by saturation so colorful
 * art wins over paper-white or ink-black backgrounds. Returns null for effectively grayscale covers.
 */
private fun dominantColor(bitmap: Bitmap): Int? {
    val width = bitmap.width
    val height = bitmap.height
    if (width == 0 || height == 0) return null
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
    var r = 0.0
    var g = 0.0
    var b = 0.0
    var total = 0.0
    for (pixel in pixels) {
        if ((pixel ushr 24) < 128) continue
        val pr = (pixel shr 16) and 0xFF
        val pg = (pixel shr 8) and 0xFF
        val pb = pixel and 0xFF
        val hi = max(pr, max(pg, pb))
        val lo = min(pr, min(pg, pb))
        val saturation = if (hi == 0) 0.0 else (hi - lo) / hi.toDouble()
        // Ignore near-black and near-white pixels, they say nothing about the cover's mood
        if (hi < 28 || lo > 235) continue
        val weight = saturation * saturation + 0.02
        r += pr * weight
        g += pg * weight
        b += pb * weight
        total += weight
    }
    if (total < pixels.size * 0.01) return null
    return (0xFF shl 24) or ((r / total).toInt() shl 16) or ((g / total).toInt() shl 8) or (b / total).toInt()
}
