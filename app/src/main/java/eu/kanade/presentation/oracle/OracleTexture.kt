package eu.kanade.presentation.oracle

import android.graphics.Bitmap
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import kotlin.random.Random

/*
 * Texture and light for the OracleRead home: native Compose takes on React Bits' Aurora/Silk backgrounds,
 * Noise grain and ShinyText. Every animated piece holds still when the system turns animations off.
 */

private const val GRAIN_SIZE = 192

/** A seamless tile of monochrome film grain, generated once per process. */
private val grainBitmap: Bitmap by lazy {
    val random = Random(0x0AC1E)
    val pixels = IntArray(GRAIN_SIZE * GRAIN_SIZE) {
        val light = random.nextBoolean()
        val alpha = random.nextInt(0, 255)
        val channel = if (light) 0xFF else 0x00
        (alpha shl 24) or (channel shl 16) or (channel shl 8) or channel
    }
    Bitmap.createBitmap(pixels, GRAIN_SIZE, GRAIN_SIZE, Bitmap.Config.ARGB_8888)
}

/**
 * Film grain, like React Bits' Noise. Keep [alpha] small (0.03–0.07). [behindContent] puts the grain under
 * the content (for cards, so text stays clean) instead of over it (for backdrops).
 */
fun Modifier.oracleGrain(alpha: Float = 0.05f, behindContent: Boolean = false): Modifier = drawWithCache {
    val brush = ShaderBrush(ImageShader(grainBitmap.asImageBitmap(), TileMode.Repeated, TileMode.Repeated))
    onDrawWithContent {
        if (behindContent) drawRect(brush = brush, alpha = alpha)
        drawContent()
        if (!behindContent) drawRect(brush = brush, alpha = alpha)
    }
}

/** Seconds since first composition, frozen at a pleasant frame when [frozen]. */
@Composable
private fun rememberShaderTime(frozen: Boolean): () -> Float {
    var seconds by remember { mutableFloatStateOf(14f) }
    if (!frozen) {
        LaunchedEffect(Unit) {
            val start = withFrameNanos { it }
            while (true) {
                withFrameNanos { now -> seconds = 14f + (now - start) / 1_000_000_000f }
            }
        }
    }
    return { seconds }
}

private const val AURORA_SHADER = """
uniform float2 iResolution;
uniform float iTime;
layout(color) uniform half4 base;
layout(color) uniform half4 c1;
layout(color) uniform half4 c2;
layout(color) uniform half4 c3;

// Smooth value noise for silky, slow-moving folds
float hash(float2 p) { return fract(sin(dot(p, float2(127.1, 311.7))) * 43758.5453); }
float noise(float2 p) {
    float2 i = floor(p);
    float2 f = fract(p);
    float2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + float2(1.0, 0.0)), u.x),
               mix(hash(i + float2(0.0, 1.0)), hash(i + float2(1.0, 1.0)), u.x), u.y);
}
float fbm(float2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 4; i++) { v += a * noise(p); p *= 2.03; a *= 0.5; }
    return v;
}

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / iResolution;
    float aspect = iResolution.x / iResolution.y;
    float t = iTime * 0.045;
    float2 p = float2(uv.x * aspect, uv.y);

    // Two aurora curtains that bend with the silk noise, strongest near the top of the screen
    float warp = fbm(p * 1.6 + float2(t, -t * 0.6));
    float curtainA = 0.16 + 0.10 * sin(p.x * 2.2 + t * 3.0 + warp * 3.0);
    float curtainB = 0.30 + 0.12 * sin(p.x * 1.4 - t * 2.2 + warp * 2.4);
    float bandA = exp(-pow((uv.y - curtainA) * 7.0, 2.0));
    float bandB = exp(-pow((uv.y - curtainB) * 5.5, 2.0));
    float shimmer = 0.75 + 0.25 * fbm(float2(p.x * 6.0 + t * 4.0, uv.y * 3.0));
    float fade = smoothstep(1.0, 0.0, uv.y);

    half3 col = base.rgb;
    col = mix(col, c1.rgb, bandA * shimmer * 0.30 * fade);
    col = mix(col, c2.rgb, bandB * shimmer * 0.22 * fade);
    // A low ember glow rising from the bottom edge
    col = mix(col, c3.rgb, smoothstep(0.72, 1.0, uv.y) * 0.10 * (0.6 + 0.4 * warp));
    return half4(col, 1.0);
}
"""

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun AuroraShaderLayer(base: Color, c1: Color, c2: Color, c3: Color, frozen: Boolean) {
    val shader = remember { RuntimeShader(AURORA_SHADER) }
    val time = rememberShaderTime(frozen)
    Canvas(modifier = Modifier.fillMaxSize()) {
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iTime", time())
        shader.setColorUniform("base", base.toArgb())
        shader.setColorUniform("c1", c1.toArgb())
        shader.setColorUniform("c2", c2.toArgb())
        shader.setColorUniform("c3", c3.toArgb())
        drawRect(brush = ShaderBrush(shader))
    }
}

/**
 * The home backdrop: the night canvas with slow aurora curtains in lantern gold and logo slate, an ember glow
 * at the bottom and film grain on top. A GPU shader on Android 13+, soft static gradients before that.
 */
@Composable
fun OracleAuroraBackdrop(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val reducedMotion = rememberReducedMotion()
    Box(modifier = modifier.oracleGrain(alpha = 0.055f)) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            AuroraShaderLayer(
                base = colors.background,
                c1 = colors.primary,
                c2 = colors.secondary,
                c3 = colors.tertiary,
                frozen = reducedMotion,
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(colors.background)
                drawRect(
                    Brush.radialGradient(
                        listOf(colors.primary.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(size.width * 0.2f, size.height * 0.08f),
                        radius = size.width * 0.9f,
                    ),
                )
                drawRect(
                    Brush.radialGradient(
                        listOf(colors.secondary.copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(size.width * 0.9f, size.height * 0.22f),
                        radius = size.width * 0.8f,
                    ),
                )
                drawRect(
                    Brush.verticalGradient(
                        0.75f to Color.Transparent,
                        1f to colors.tertiary.copy(alpha = 0.10f),
                    ),
                )
            }
        }
    }
}

/**
 * Text lit by a slow sweep of lantern light, like React Bits' ShinyText / GradientText. The sweep stops,
 * leaving a still gradient, when animations are off.
 */
@Composable
fun OracleShinyText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
) {
    val colors = MaterialTheme.colorScheme
    val reducedMotion = rememberReducedMotion()
    val sweep = if (reducedMotion) {
        0.35f
    } else {
        val transition = rememberInfiniteTransition(label = "shinyText")
        val value by transition.animateFloat(
            initialValue = -0.6f,
            targetValue = 1.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 5200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "shinyTextSweep",
        )
        value
    }
    val base = colors.onBackground
    val light = colors.primary
    Text(
        text = text,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
        style = style.copy(
            brush = object : androidx.compose.ui.graphics.ShaderBrush() {
                override fun createShader(size: androidx.compose.ui.geometry.Size): android.graphics.Shader {
                    val center = size.width * sweep
                    val half = size.width * 0.35f
                    return androidx.compose.ui.graphics.LinearGradientShader(
                        from = Offset(center - half, 0f),
                        to = Offset(center + half, size.height),
                        colors = listOf(base, light, Color(0xFFFFF4DA), light, base),
                        colorStops = listOf(0f, 0.35f, 0.5f, 0.65f, 1f),
                        tileMode = TileMode.Clamp,
                    )
                }
            },
        ),
    )
}
