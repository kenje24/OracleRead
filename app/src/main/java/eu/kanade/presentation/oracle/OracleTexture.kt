package eu.kanade.presentation.oracle

import android.graphics.Bitmap
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
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

/**
 * OracleRead: lets the screen over the home backdrop ask it for a calm, static frame, as the library does for a
 * folder whose ambience is None. Each request lasts until it is released.
 */
@Stable
class OracleBackdropState {
    private var calmRequests by mutableIntStateOf(0)

    val isCalm: Boolean
        get() = calmRequests > 0

    fun requestCalm(): () -> Unit {
        calmRequests++
        var released = false
        return {
            if (!released) {
                released = true
                calmRequests--
            }
        }
    }
}

val LocalOracleBackdrop = staticCompositionLocalOf { OracleBackdropState() }

/** OracleRead: keeps the home backdrop calm and static for as long as this is composed with [enabled]. */
@Composable
fun CalmBackdropRequest(enabled: Boolean) {
    val backdrop = LocalOracleBackdrop.current
    DisposableEffect(backdrop, enabled) {
        val release = if (enabled) backdrop.requestCalm() else null
        onDispose { release?.invoke() }
    }
}

private const val AURORA_STILL_SECONDS = 14f

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
// Three octaves, scaled to the range four had: the fourth only added detail too fine to see in the soft folds
float fbm(float2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 3; i++) { v += a * noise(p); p *= 2.03; a *= 0.5; }
    return v * 1.0714;
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

/**
 * OracleRead: draws the content at half the width and height into its own layer, scaled up to fill the space. The
 * layer is kept between the content's own updates, so the rest of the screen redrawing doesn't draw it again.
 */
private fun Modifier.halfResolutionLayer(): Modifier = layout { measurable, constraints ->
    val width = constraints.maxWidth
    val height = constraints.maxHeight
    val placeable = measurable.measure(Constraints.fixed((width + 1) / 2, (height + 1) / 2))
    layout(width, height) {
        placeable.placeWithLayer(0, 0) {
            scaleX = 2f
            scaleY = 2f
            transformOrigin = TransformOrigin(0f, 0f)
            compositingStrategy = CompositingStrategy.Offscreen
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun AuroraShaderLayer(base: Color, c1: Color, c2: Color, c3: Color, frozen: Boolean) {
    val shader = remember { RuntimeShader(AURORA_SHADER) }
    val brush = remember(shader) { ShaderBrush(shader) }
    val time = rememberAmbientSeconds(active = !frozen, initialSeconds = AURORA_STILL_SECONDS)
    // The curtains are soft gradients, so a quarter of the pixels draws the same picture; the grain over it stays
    // at full resolution
    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .halfResolutionLayer()
            .drawBehind {
                shader.setFloatUniform("iResolution", size.width, size.height)
                shader.setFloatUniform("iTime", time.floatValue)
                shader.setColorUniform("base", base.toArgb())
                shader.setColorUniform("c1", c1.toArgb())
                shader.setColorUniform("c2", c2.toArgb())
                shader.setColorUniform("c3", c3.toArgb())
                drawRect(brush = brush)
            },
    )
}

/** The aurora's still counterpart: soft lantern and slate glows over the ember floor, without a shader. */
@Composable
private fun StaticAurora(base: Color, c1: Color, c2: Color, c3: Color) {
    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val glowA = Brush.radialGradient(
                    listOf(c1.copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.08f),
                    radius = size.width * 0.9f,
                )
                val glowB = Brush.radialGradient(
                    listOf(c2.copy(alpha = 0.14f), Color.Transparent),
                    center = Offset(size.width * 0.9f, size.height * 0.22f),
                    radius = size.width * 0.8f,
                )
                val ember = Brush.verticalGradient(
                    0.75f to Color.Transparent,
                    1f to c3.copy(alpha = 0.10f),
                )
                onDrawBehind {
                    drawRect(base)
                    drawRect(glowA)
                    drawRect(glowB)
                    drawRect(ember)
                }
            },
    )
}

/**
 * The home backdrop: the night canvas with slow aurora curtains in lantern gold and logo slate, an ember glow
 * at the bottom and film grain on top. A GPU shader on Android 13+, soft static gradients before that and while
 * [state] asks for calm.
 */
@Composable
fun OracleAuroraBackdrop(
    modifier: Modifier = Modifier,
    state: OracleBackdropState = LocalOracleBackdrop.current,
) {
    val colors = MaterialTheme.colorScheme
    val motion = rememberAmbientMotion()
    Box(modifier = modifier.oracleGrain(alpha = 0.055f)) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !state.isCalm) {
            AuroraShaderLayer(
                base = colors.background,
                c1 = colors.primary,
                c2 = colors.secondary,
                c3 = colors.tertiary,
                frozen = !motion,
            )
        } else {
            StaticAurora(
                base = colors.background,
                c1 = colors.primary,
                c2 = colors.secondary,
                c3 = colors.tertiary,
            )
        }
    }
}

// The sweep crosses from -0.6 to 1.6 of the width every 5.2 s; with motion off it holds at 0.35
private const val SHINY_SWEEP_SECONDS = 5.2f
private const val SHINY_STILL_SECONDS = (0.35f + 0.6f) / 2.2f * SHINY_SWEEP_SECONDS
private val ShinyHighlight = Color(0xFFFFF4DA)

/**
 * Text lit by a slow sweep of lantern light, like React Bits' ShinyText / GradientText. The sweep stops,
 * leaving a still gradient, when animations are off.
 *
 * OracleRead: the text is laid out once in the plain color and the light is painted over its glyphs in the draw
 * phase, so the sweep neither recomposes the text nor builds a new shader every frame.
 */
@Composable
fun OracleShinyText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
) {
    val colors = MaterialTheme.colorScheme
    val seconds = rememberAmbientSeconds(active = rememberAmbientMotion(), initialSeconds = SHINY_STILL_SECONDS)
    val light = colors.primary
    Text(
        text = text,
        color = colors.onBackground,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = style,
        modifier = modifier
            // Its own layer, so the light only lands on the glyphs
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithCache {
                val half = size.width * 0.35f
                val sweepLight = Brush.linearGradient(
                    0f to light.copy(alpha = 0f),
                    0.35f to light,
                    0.5f to ShinyHighlight,
                    0.65f to light,
                    1f to light.copy(alpha = 0f),
                    start = Offset(-half, 0f),
                    end = Offset(half, size.height),
                )
                onDrawWithContent {
                    drawContent()
                    val sweep = -0.6f + 2.2f * ((seconds.floatValue % SHINY_SWEEP_SECONDS) / SHINY_SWEEP_SECONDS)
                    val center = size.width * sweep
                    translate(left = center) {
                        drawRect(
                            brush = sweepLight,
                            topLeft = Offset(-center, 0f),
                            size = size,
                            blendMode = BlendMode.SrcAtop,
                        )
                    }
                }
            },
    )
}
