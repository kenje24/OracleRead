package eu.kanade.presentation.library.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import eu.kanade.presentation.category.FolderAmbience
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

/**
 * OracleRead 1.4.2: the folder effects each new app theme opens on, plus Meteor Shower and Fireflies. Like the other
 * ambiences, geometry, brushes and paints are made once per size and time is only read while drawing; per-frame work
 * reuses the same paths and arrays.
 */
internal fun CacheDrawScope.themedAmbientScene(ambience: FolderAmbience): AmbientScene? = when (ambience) {
    FolderAmbience.KOI_POND -> KoiPondScene(this)
    FolderAmbience.JELLYFISH -> JellyfishScene(this)
    FolderAmbience.PAPER_CRANES -> PaperCraneScene(this)
    FolderAmbience.THUNDERSTORM -> ThunderstormScene(this)
    FolderAmbience.GLYPH_RAIN -> GlyphRainScene(this)
    FolderAmbience.SCREENTONE -> ScreentoneScene(this)
    FolderAmbience.METEOR_SHOWER -> MeteorShowerScene(this)
    FolderAmbience.FIREFLIES -> FireflyScene(this)
    else -> null
}

private const val TAU = (2 * PI).toFloat()

private fun fract(x: Float): Float = x - floor(x)

/** A stable pseudo-random value in 0..1 for [n], so every frame and every device agree. */
private fun hash(n: Int): Float = fract(sin(n * 12.9898f + 78.233f) * 43758.547f)

private fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
    val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

/**
 * Koi Pond: three koi circle a moonlit pond, their bodies following their heads and their tails sweeping. Lily pads
 * drift above them and ripples spread where the water was touched.
 */
private class KoiPondScene(scope: CacheDrawScope) : AmbientScene {
    private class Koi(
        val cx: Float,
        val cy: Float,
        val rx: Float,
        val ry: Float,
        val speed: Float,
        val phase: Float,
        val scale: Float,
        val colors: List<Color>,
    )

    private val dp = scope.density
    private val pearl = Color(0xFFF4EDE1)
    private val vermilion = Color(0xFFFF7468)
    private val ink = Color(0xFF1B1D22)
    private val koi: List<Koi>
    private val tail = Path()
    private val body = Path()
    private val water: Brush
    private val moonGlow: Brush
    private val moonCenter: Offset
    private val ripple = Stroke(width = 1f * dp)
    private val padRim = Stroke(width = 1f * dp)
    private val segX = FloatArray(SEGMENTS)
    private val segY = FloatArray(SEGMENTS)

    init {
        val w = scope.size.width
        val h = scope.size.height
        val r = vermilion
        val p = pearl
        koi = listOf(
            Koi(0.50f * w, 0.58f * h, 0.33f * w, 0.19f * h, 0.24f, 0f, 1.45f, listOf(p, r, r, p, r, r, p, p, r, p)),
            Koi(0.44f * w, 0.66f * h, 0.26f * w, 0.23f * h, -0.18f, 2.1f, 1.25f, listOf(r, p, p, p, p, p, p, p, p, p)),
            Koi(
                0.58f * w,
                0.42f * h,
                0.30f * w,
                0.14f * h,
                0.20f,
                4.0f,
                1.05f,
                listOf(r, r, ink, r, r, ink, r, r, r, r),
            ),
        )
        water = Brush.verticalGradient(
            0f to Color.Transparent,
            0.45f to Color(0xFF0E3B33).copy(alpha = 0.18f),
            1f to Color(0xFF0E3B33).copy(alpha = 0.40f),
            startY = 0f,
            endY = h,
        )
        moonCenter = Offset(0.80f * w, 0.17f * h)
        moonGlow = Brush.radialGradient(
            0f to pearl.copy(alpha = 0.14f),
            0.35f to pearl.copy(alpha = 0.05f),
            1f to Color.Transparent,
            center = moonCenter,
            radius = 0.42f * w,
        )
    }

    override fun DrawScope.draw(seconds: Float) {
        val w = size.width
        val h = size.height
        drawRect(water)
        drawCircle(moonGlow, radius = 0.42f * w, center = moonCenter)
        // The moon's reflection, broken into shimmering strokes
        for (line in 0..6) {
            val half = (14f + 22f * (0.5f + 0.5f * sin(seconds * 0.8f + line * 1.7f))) * dp * (1f - line * 0.09f)
            val y = moonCenter.y + (line - 3) * 8f * dp
            drawLine(
                color = pearl.copy(alpha = 0.10f),
                start = Offset(moonCenter.x - half, y),
                end = Offset(moonCenter.x + half, y),
                strokeWidth = 1.2f * dp,
                cap = StrokeCap.Round,
            )
        }

        // Ripples, each spreading and fading in turn
        for (site in 0..3) {
            val progress = fract(seconds / 5.5f + site * 0.27f)
            val center = Offset((0.18f + 0.62f * hash(site * 7)) * w, (0.38f + 0.48f * hash(site * 11)) * h)
            val radius = (4f + progress * 52f) * dp
            drawCircle(pearl.copy(alpha = 0.20f * (1f - progress)), radius, center, style = ripple)
            drawCircle(pearl.copy(alpha = 0.10f * (1f - progress)), radius * 0.6f, center, style = ripple)
        }

        koi.forEach { drawKoi(it, seconds) }

        // Lily pads float above the koi, each turning slowly with its notch
        for (pad in 0..3) {
            val radius = (20f + 14f * hash(pad * 5 + 1)) * dp
            val center = Offset(
                (0.12f + 0.78f * hash(pad * 3 + 2)) * w + 6f * dp * sin(seconds * 0.13f + pad),
                (0.30f + 0.60f * hash(pad * 13 + 3)) * h + 4f * dp * cos(seconds * 0.11f + pad),
            )
            val notch = pad * 70f + seconds * 3f
            val topLeft = Offset(center.x - radius, center.y - radius)
            drawArc(
                Color(0xFF2F6B57).copy(alpha = 0.34f),
                notch + 18f,
                324f,
                true,
                topLeft,
                Size(radius * 2, radius * 2),
            )
            drawArc(
                Color(0xFF7FCFB4).copy(alpha = 0.12f),
                notch + 18f,
                324f,
                true,
                topLeft,
                Size(radius * 2, radius * 2),
                style = padRim,
            )
            if (pad == 1) {
                for (petal in 0..5) {
                    val angle = petal * TAU / 6f + seconds * 0.05f
                    val petalCenter = Offset(center.x + cos(angle) * 5f * dp, center.y + sin(angle) * 5f * dp)
                    drawCircle(Color(0xFFFF9FB2).copy(alpha = 0.42f), 4.2f * dp, petalCenter)
                }
                drawCircle(Color(0xFFFFF1F4).copy(alpha = 0.6f), 2.4f * dp, center)
            }
        }
    }

    private fun DrawScope.drawKoi(fish: Koi, seconds: Float) {
        val head = fish.phase + seconds * fish.speed
        val spacing = 6.5f * dp * fish.scale
        val lag = spacing / ((fish.rx + fish.ry) / 2f) * if (fish.speed < 0) -1f else 1f
        for (k in 0..<SEGMENTS) {
            val angle = head - k * lag
            segX[k] = fish.cx + fish.rx * cos(angle)
            segY[k] = fish.cy + fish.ry * sin(angle) * (1f + 0.18f * sin(2f * angle))
        }
        // A swimming wriggle that grows towards the tail
        for (k in 1..<SEGMENTS) {
            val dx = segX[k - 1] - segX[k]
            val dy = segY[k - 1] - segY[k]
            val length = max(hypot(dx, dy), 0.001f)
            val wriggle = sin(seconds * 5.5f - k * 0.7f) * k * 0.35f * dp * fish.scale
            segX[k] += -dy / length * wriggle
            segY[k] += dx / length * wriggle
        }

        val last = SEGMENTS - 1
        // The body outline: each side of the spine at that segment's width, closed round the head
        body.rewind()
        for (k in 0..last) {
            val (nx, ny) = normalAt(k)
            val radius = 7f * dp * fish.scale * PROFILE[k]
            if (k == 0) {
                body.moveTo(segX[k] + nx * radius, segY[k] + ny * radius)
            } else {
                body.lineTo(segX[k] + nx * radius, segY[k] + ny * radius)
            }
        }
        for (k in last downTo 0) {
            val (nx, ny) = normalAt(k)
            val radius = 7f * dp * fish.scale * PROFILE[k]
            body.lineTo(segX[k] - nx * radius, segY[k] - ny * radius)
        }
        body.close()

        val tdx = segX[last - 1] - segX[last]
        val tdy = segY[last - 1] - segY[last]
        val tLength = max(hypot(tdx, tdy), 0.001f)
        val bx = -tdx / tLength
        val by = -tdy / tLength
        val spread = 6.5f * dp * fish.scale * (1f + 0.35f * sin(seconds * 5.5f))
        val reach = 13f * dp * fish.scale
        tail.rewind()
        tail.moveTo(segX[last], segY[last])
        tail.quadraticTo(
            segX[last] + bx * reach * 0.6f - by * spread * 0.4f,
            segY[last] + by * reach * 0.6f + bx * spread * 0.4f,
            segX[last] + bx * reach - by * spread,
            segY[last] + by * reach + bx * spread,
        )
        tail.lineTo(segX[last] + bx * reach * 0.62f, segY[last] + by * reach * 0.62f)
        tail.lineTo(segX[last] + bx * reach + by * spread, segY[last] + by * reach - bx * spread)
        tail.quadraticTo(
            segX[last] + bx * reach * 0.6f + by * spread * 0.4f,
            segY[last] + by * reach * 0.6f - bx * spread * 0.4f,
            segX[last],
            segY[last],
        )
        tail.close()

        // Its shadow on the pond floor first, offset away from the moon
        val shadowOffset = Offset(-5f * dp * fish.scale, 9f * dp * fish.scale)
        withTransform({ translate(shadowOffset.x, shadowOffset.y) }) {
            drawPath(body, Color.Black.copy(alpha = 0.22f))
            drawPath(tail, Color.Black.copy(alpha = 0.14f))
        }

        // Pectoral fins, just behind the head
        val (px, py) = normalAt(1)
        val finReach = 9f * dp * fish.scale
        val finFlap = 1f + 0.25f * sin(seconds * 3.2f + fish.phase)
        for (side in FIN_SIDES) {
            drawCircle(
                color = pearl.copy(alpha = 0.26f),
                radius = 3.6f * dp * fish.scale,
                center = Offset(segX[2] + px * finReach * side * finFlap, segY[2] + py * finReach * side * finFlap),
            )
        }
        drawPath(tail, fish.colors.last().copy(alpha = 0.40f))
        drawPath(body, fish.colors[1].copy(alpha = 0.58f))
        // Patches inside the body, then a soft sheen along the back
        for (k in 0..last) {
            if (fish.colors[k] == fish.colors[1]) continue
            drawCircle(
                color = fish.colors[k].copy(alpha = 0.62f),
                radius = 5.6f * dp * fish.scale * PROFILE[k],
                center = Offset(segX[k], segY[k]),
            )
        }
        for (k in 0..<last - 2) {
            drawLine(
                color = Color.White.copy(alpha = 0.10f),
                start = Offset(segX[k], segY[k]),
                end = Offset(segX[k + 1], segY[k + 1]),
                strokeWidth = 2f * dp * fish.scale * PROFILE[k],
                cap = StrokeCap.Round,
            )
        }
        // Eyes
        val (ex, ey) = normalAt(0)
        for (side in FIN_SIDES) {
            drawCircle(
                color = ink.copy(alpha = 0.55f),
                radius = 1.1f * dp * fish.scale,
                center = Offset(
                    segX[0] + ex * side * 3.4f * dp * fish.scale,
                    segY[0] + ey * side * 3.4f * dp * fish.scale,
                ),
            )
        }
    }

    /** The unit normal to the spine at segment [k]. */
    private fun normalAt(k: Int): Pair<Float, Float> {
        val a = (k - 1).coerceAtLeast(0)
        val b = (k + 1).coerceAtMost(SEGMENTS - 1)
        val dx = segX[a] - segX[b]
        val dy = segY[a] - segY[b]
        val length = max(hypot(dx, dy), 0.001f)
        return -dy / length to dx / length
    }

    private companion object {
        const val SEGMENTS = 10
        val PROFILE = floatArrayOf(0.70f, 0.95f, 1f, 0.97f, 0.88f, 0.74f, 0.58f, 0.43f, 0.30f, 0.18f)
        val FIN_SIDES = floatArrayOf(-1f, 1f)
    }
}

/**
 * Jellyfish: bioluminescent jellyfish pulse their way up through the dark, trailing tentacles that wave behind each
 * stroke, while marine snow falls past them.
 */
private class JellyfishScene(scope: CacheDrawScope) : AmbientScene {
    private val dp = scope.density
    private val colors = listOf(Color(0xFF6EF2EC), Color(0xFFA991FF), Color(0xFFFF8AD4))
    private val bell = Path()
    private val bellBrushes: List<Brush>
    private val haloBrushes: List<Brush>
    private val tentacles = Path()
    private val tentacleStroke = Stroke(width = 1f * dp, cap = StrokeCap.Round)
    private val depth: Brush

    init {
        // A unit bell, 1 wide and resting on y = 0, with a scalloped rim
        bell.moveTo(-0.5f, 0f)
        bell.cubicTo(-0.52f, -0.78f, 0.52f, -0.78f, 0.5f, 0f)
        val scallops = 6
        for (i in scallops - 1 downTo 0) {
            val x = -0.5f + i / scallops.toFloat()
            val mid = x + 0.5f / scallops
            bell.quadraticTo(mid, 0.11f, x, 0f)
        }
        bell.close()
        bellBrushes = colors.map {
            Brush.radialGradient(
                0f to it.copy(alpha = 0.50f),
                0.55f to it.copy(alpha = 0.22f),
                1f to it.copy(alpha = 0.04f),
                center = Offset(0f, -0.32f),
                radius = 0.62f,
            )
        }
        haloBrushes = colors.map {
            Brush.radialGradient(
                0f to it.copy(alpha = 0.14f),
                1f to Color.Transparent,
                center = Offset.Zero,
                radius = 1f,
            )
        }
        depth = Brush.verticalGradient(
            0f to Color(0xFF071A33).copy(alpha = 0.10f),
            1f to Color(0xFF02040B).copy(alpha = 0.45f),
            startY = 0f,
            endY = scope.size.height,
        )
    }

    override fun DrawScope.draw(seconds: Float) {
        val w = size.width
        val h = size.height
        drawRect(depth)

        for (flake in 0..<SNOW) {
            val x = hash(flake * 3) * w + sin(seconds * 0.3f + flake) * 8f * dp
            val y = fract(hash(flake * 5 + 1) + seconds * 0.011f * (0.5f + hash(flake))) * h
            drawCircle(
                Color.White.copy(alpha = 0.08f + 0.16f * hash(flake * 9)),
                (0.7f + hash(flake * 2)) * dp,
                Offset(x, y),
            )
        }

        for (index in 0..<JELLIES) {
            val scale = 0.55f + 0.5f * hash(index * 17 + 4)
            val color = index % colors.size
            val travel = h * 1.5f
            val x = (0.12f + 0.78f * hash(index * 23 + 9)) * w + 10f * dp * sin(seconds * 0.2f + index)
            val pulsePhase = seconds * 1.6f + index * 1.3f
            val contraction = 0.5f + 0.5f * sin(pulsePhase)
            // Each stroke pushes the jellyfish up a little, so it rises in surges
            val rise = seconds * 0.020f * (0.7f + 0.6f * hash(index)) + 0.012f * sin(pulsePhase - 0.8f)
            val y = h * 1.2f - fract(rise + hash(index * 29)) * travel
            val bellWidth = 50f * dp * scale * (1f - 0.14f * contraction)
            val bellHeight = 38f * dp * scale * (1f + 0.10f * contraction)

            withTransform({
                translate(x, y - bellHeight * 0.25f)
                scale(bellWidth * 1.3f, bellWidth * 1.3f, pivot = Offset.Zero)
            }) {
                drawCircle(haloBrushes[color], radius = 1f, center = Offset.Zero)
            }

            tentacles.rewind()
            val tentacleLength = bellHeight * (2.4f + 0.8f * hash(index * 31))
            for (strand in 0..5) {
                val baseX = x + (strand / 5f - 0.5f) * bellWidth * 0.8f
                tentacles.moveTo(baseX, y)
                for (step in 1..TENTACLE_STEPS) {
                    val along = step / TENTACLE_STEPS.toFloat()
                    val sway = sin(seconds * 1.8f - step * 0.55f + strand * 0.9f + index) * along * 7f * dp * scale
                    tentacles.lineTo(baseX + sway, y + along * tentacleLength * (1f - 0.12f * contraction))
                }
            }
            drawPath(tentacles, colors[color].copy(alpha = 0.26f), style = tentacleStroke)

            withTransform({
                translate(x, y)
                scale(bellWidth, bellHeight, pivot = Offset.Zero)
            }) {
                drawPath(bell, bellBrushes[color])
            }
            drawCircle(
                color = Color.White.copy(alpha = 0.18f + 0.12f * contraction),
                radius = bellWidth * 0.11f,
                center = Offset(x, y - bellHeight * 0.30f),
            )
        }
    }

    private companion object {
        const val JELLIES = 6
        const val SNOW = 56
        const val TENTACLE_STEPS = 12
    }
}

/**
 * Paper Cranes: folded cranes glide across on slow wingbeats over a faint washi grain. One is folded from the red seal
 * paper.
 */
private class PaperCraneScene(scope: CacheDrawScope) : AmbientScene {
    private class Crane(
        val y: Float,
        val size: Float,
        val speed: Float,
        val phase: Float,
        val flapRate: Float,
        val paper: Color,
        val body: Path = Path(),
        val nearWing: Path = Path(),
        val farWing: Path = Path(),
        val crease: Path = Path(),
    )

    private val dp = scope.density
    private val washi = Color(0xFFE8DFD0)
    private val seal = Color(0xFFEC7480)
    private val ink = Color(0xFF121011)
    private val cranes: List<Crane>
    private val fibers = Path()
    private val fiberStroke = Stroke(width = 0.8f * dp, cap = StrokeCap.Round)
    private val creaseStrokes: List<Stroke>

    init {
        val w = scope.size.width
        val h = scope.size.height
        cranes = List(5) { i ->
            Crane(
                y = (0.14f + 0.16f * i + 0.05f * hash(i * 3)) * h,
                size = (34f + 28f * hash(i * 7 + 2)) * dp,
                speed = (0.030f + 0.030f * hash(i * 11 + 5)),
                phase = hash(i * 13 + 1),
                flapRate = 2.2f + 0.9f * hash(i * 17),
                paper = if (i == 2) seal else washi,
            )
        }
        creaseStrokes = cranes.map { Stroke(width = 0.9f * dp / it.size, cap = StrokeCap.Round) }
        val random = kotlin.random.Random(0xC4A1E)
        repeat(28) {
            val x = random.nextFloat() * w
            val y = random.nextFloat() * h
            val length = (40f + random.nextFloat() * 120f) * dp
            val angle = random.nextFloat() * TAU
            fibers.moveTo(x, y)
            fibers.quadraticTo(
                x + cos(angle + 0.6f) * length * 0.5f,
                y + sin(angle + 0.6f) * length * 0.5f,
                x + cos(angle) * length,
                y + sin(angle) * length,
            )
        }
    }

    override fun DrawScope.draw(seconds: Float) {
        val w = size.width
        drawPath(fibers, washi.copy(alpha = 0.05f), style = fiberStroke)
        cranes.forEachIndexed { index, crane ->
            val span = w + crane.size * 4f
            val x = fract(seconds * crane.speed + crane.phase) * span - crane.size * 2f
            val y = crane.y + 7f * dp * sin(seconds * 0.9f + index * 1.7f)
            val flap = cos(seconds * crane.flapRate + index)
            buildCrane(crane, flap)
            withTransform({
                translate(x, y)
                scale(crane.size, crane.size, pivot = Offset.Zero)
            }) {
                drawPath(crane.farWing, lerp(crane.paper, ink, 0.45f).copy(alpha = 0.46f))
                drawPath(crane.body, crane.paper.copy(alpha = 0.62f))
                drawPath(crane.nearWing, crane.paper.copy(alpha = 0.74f))
                drawPath(crane.crease, ink.copy(alpha = 0.30f), style = creaseStrokes[index])
            }
        }
    }

    private fun buildCrane(crane: Crane, flap: Float) {
        // Unit crane facing right: tail up and back, neck up and forward with the head folded down
        with(crane.body) {
            rewind()
            moveTo(-0.64f, -0.42f)
            lineTo(-0.12f, 0.05f)
            lineTo(0.04f, 0.13f)
            lineTo(0.18f, 0.03f)
            lineTo(0.52f, -0.42f)
            lineTo(0.62f, -0.55f)
            lineTo(0.77f, -0.46f)
            lineTo(0.58f, -0.45f)
            lineTo(0.12f, -0.08f)
            lineTo(-0.10f, -0.06f)
            close()
        }
        val nearTipY = -1.0f + (1f - flap) / 2f * 1.55f
        val farTipY = -0.86f + (1f - cos(acosSafe(flap) + 0.35f)) / 2f * 1.3f
        with(crane.farWing) {
            rewind()
            moveTo(0.12f, -0.06f)
            lineTo(-0.06f + 0.05f * flap, farTipY)
            lineTo(-0.20f, -0.05f)
            close()
        }
        with(crane.nearWing) {
            rewind()
            moveTo(0.16f, -0.04f)
            lineTo(0.03f + 0.06f * flap, nearTipY)
            lineTo(-0.18f, -0.03f)
            close()
        }
        with(crane.crease) {
            rewind()
            moveTo(-0.01f, -0.04f)
            lineTo(0.03f + 0.06f * flap, nearTipY)
            moveTo(-0.12f, 0.05f)
            lineTo(0.52f, -0.42f)
        }
    }

    private fun acosSafe(x: Float): Float = kotlin.math.acos(x.coerceIn(-1f, 1f))
}

/**
 * Thunderstorm: rain slants under rolling cloud banks and splashes along the bottom. Every eight seconds a soft fork
 * of lightning lights the clouds from inside, with a gentle flash kept well below photosensitivity limits.
 */
private class ThunderstormScene(scope: CacheDrawScope) : AmbientScene {
    private val dp = scope.density
    private val periwinkle = Color(0xFFAAB8FF)
    private val steel = Color(0xFF97AAC0)
    private val bolts: List<Path>
    private val rain = Path()
    private val rainStroke = Stroke(width = 1f * dp, cap = StrokeCap.Round)
    private val boltGlow = Stroke(width = 9f * dp, cap = StrokeCap.Round)
    private val boltCore = Stroke(width = 1.6f * dp, cap = StrokeCap.Round)
    private val splash = Stroke(width = 1f * dp)
    private val sky: Brush
    private val cloudPuff = Brush.radialGradient(
        0f to Color(0xFF1A2232).copy(alpha = 0.85f),
        0.6f to Color(0xFF1A2232).copy(alpha = 0.45f),
        1f to Color.Transparent,
        center = Offset.Zero,
        radius = 1f,
    )
    private val litPuff = Brush.radialGradient(
        0f to Color(0xFFAAB8FF).copy(alpha = 0.30f),
        1f to Color.Transparent,
        center = Offset.Zero,
        radius = 1f,
    )

    init {
        val w = scope.size.width
        val h = scope.size.height
        bolts = List(3) { variant ->
            val random = kotlin.random.Random(0xB017 + variant)
            Path().apply {
                var x = w * (0.25f + 0.25f * variant)
                var y = h * 0.11f
                moveTo(x, y)
                var branchX = x
                var branchY = y
                for (step in 0..11) {
                    x += (random.nextFloat() - 0.5f) * 36f * dp
                    y += h * 0.036f
                    lineTo(x, y)
                    if (step == 4) {
                        branchX = x
                        branchY = y
                    }
                }
                moveTo(branchX, branchY)
                val direction = if (random.nextBoolean()) 1f else -1f
                for (step in 0..4) {
                    branchX += direction * (10f + random.nextFloat() * 16f) * dp
                    branchY += h * 0.028f
                    lineTo(branchX, branchY)
                }
            }
        }
        sky = Brush.verticalGradient(
            0f to Color(0xFF05070C).copy(alpha = 0.55f),
            0.38f to Color(0xFF0B1220).copy(alpha = 0.18f),
            1f to Color.Transparent,
            startY = 0f,
            endY = h,
        )
    }

    override fun DrawScope.draw(seconds: Float) {
        val w = size.width
        val h = size.height
        val local = seconds % STRIKE_SECONDS
        val strike = floor(seconds / STRIKE_SECONDS).toInt()
        val flash = when {
            local < 0.08f -> 1f
            local < 0.16f -> 0.25f
            local < 0.24f -> 0.75f
            local < 0.55f -> 0.75f * (1f - (local - 0.24f) / 0.31f)
            else -> 0f
        }

        drawRect(sky)
        if (flash > 0f) drawRect(periwinkle.copy(alpha = 0.07f * flash))

        // Three banks of cloud drifting at different speeds
        for (band in 0..2) {
            val puffs = 9
            val drift = seconds * (6f + band * 4f) * dp
            for (puff in 0..<puffs) {
                val radius = (48f + 30f * hash(band * 31 + puff)) * dp
                val span = w + radius * 2f
                val x = ((puff / puffs.toFloat()) * span + drift) % span - radius
                val y = h * (0.02f + band * 0.065f) + 14f * dp * hash(band * 7 + puff * 3)
                withTransform({
                    translate(x, y)
                    scale(radius * 1.4f, radius, pivot = Offset.Zero)
                }) {
                    drawCircle(cloudPuff, radius = 1f, center = Offset.Zero, alpha = 0.85f - band * 0.18f)
                    if (flash > 0f) drawCircle(litPuff, radius = 0.9f, center = Offset.Zero, alpha = flash)
                }
            }
        }

        if (flash > 0f) {
            val bolt = bolts[strike.mod(bolts.size)]
            drawPath(bolt, periwinkle.copy(alpha = 0.12f * flash), style = boltGlow)
            drawPath(bolt, Color(0xFFE6ECFF).copy(alpha = 0.80f * flash), style = boltCore)
        }

        rain.rewind()
        for (drop in 0..<DROPS) {
            val length = (10f + 9f * hash(drop * 3)) * dp
            val speed = 1.1f + 0.6f * hash(drop * 5)
            val y = fract(hash(drop * 7 + 2) + seconds * speed * 0.6f) * (h + length) - length
            val x = hash(drop * 11 + 1) * w * 1.25f - y * 0.22f
            rain.moveTo(x, y)
            rain.lineTo(x - length * 0.22f, y + length)
        }
        drawPath(rain, steel.copy(alpha = 0.24f), style = rainStroke)

        for (site in 0..9) {
            val progress = fract(seconds * 1.4f + hash(site * 13))
            val width = (2f + progress * 14f) * dp
            val center = Offset(hash(site * 17 + 3) * w, h * (0.90f + 0.08f * hash(site * 19)))
            drawOval(
                color = steel.copy(alpha = 0.26f * (1f - progress)),
                topLeft = Offset(center.x - width, center.y - width * 0.3f),
                size = Size(width * 2f, width * 0.6f),
                style = splash,
            )
        }
    }

    private companion object {
        const val STRIKE_SECONDS = 8f
        const val DROPS = 110
    }
}

/**
 * Glyph Rain: streams of neon katakana fall down the screen. Each stream has a bright magenta head and a tail that
 * cools to cyan as it fades, and the glyphs it passes slowly change.
 */
private class GlyphRainScene(scope: CacheDrawScope) : AmbientScene {
    private val dp = scope.density
    private val magenta = Color(0xFFFF62D0)
    private val cyan = Color(0xFF45E0FF)
    private val glyphHeight = 17f * dp
    private val columnWidth = 21f * dp
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.MONOSPACE
        textSize = 14f * dp
        textAlign = Paint.Align.CENTER
    }
    private val horizon: Brush

    init {
        horizon = Brush.verticalGradient(
            0f to Color.Transparent,
            0.7f to magenta.copy(alpha = 0.04f),
            1f to magenta.copy(alpha = 0.14f),
            startY = 0f,
            endY = scope.size.height,
        )
    }

    override fun DrawScope.draw(seconds: Float) {
        val w = size.width
        val h = size.height
        drawRect(horizon)
        val columns = (w / columnWidth).toInt() + 1
        drawIntoCanvas { canvas ->
            val native = canvas.nativeCanvas
            for (column in 0..<columns) {
                val length = 8 + (hash(column * 3) * 12).toInt()
                val speed = (4f + 5f * hash(column * 7 + 1)) * glyphHeight
                val cycle = h + length * glyphHeight + h * 0.7f * hash(column * 11 + 2)
                val head = fract(seconds * speed / cycle + hash(column * 13 + 3)) * cycle
                val headRow = floor(head / glyphHeight).toInt()
                val x = column * columnWidth + columnWidth / 2f
                for (i in 0..<length) {
                    val row = headRow - i
                    val y = row * glyphHeight
                    if (y < -glyphHeight || y > h + glyphHeight) continue
                    val fade = (1f - i / length.toFloat()).pow(1.6f)
                    // Quieter behind the header so its text stays easy to read
                    val header = 0.25f + 0.75f * smoothstep(0.10f * h, 0.30f * h, y)
                    val color = if (i == 0) {
                        lerp(Color.White, magenta, 0.35f).copy(alpha = 0.75f * header)
                    } else {
                        lerp(magenta, cyan, i / length.toFloat()).copy(alpha = 0.45f * fade * header)
                    }
                    paint.color = color.toArgb()
                    val change = floor(seconds * 1.5f + hash(column) * 10f).toInt()
                    val glyph = (hash(column * 131 + row * 17 + change * 7) * GLYPHS.size).toInt()
                    native.drawText(GLYPHS, glyph.coerceIn(0, GLYPHS.size - 1), 1, x, y, paint)
                }
            }
        }
    }

    private companion object {
        val GLYPHS = "アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲン0123456789"
            .toCharArray()
    }
}

/**
 * Screentone: a manga halftone of ink dots, sparse at the top and dense at the bottom, swelling in slow waves that
 * spread from a corner. Faint speed lines frame the page.
 */
private class ScreentoneScene(scope: CacheDrawScope) : AmbientScene {
    private val dp = scope.density
    private val paper = Color(0xFFF2F0EC)
    private val spacing = 19f * dp
    private val columns = (scope.size.width / spacing).toInt() + 2
    private val rows = (scope.size.height / spacing).toInt() + 2
    private val points = Array(LEVELS) { FloatArray(columns * rows * 2) }
    private val counts = IntArray(LEVELS)
    private val paints = Array(LEVELS) { level ->
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 2f * (level + 1f) / LEVELS * spacing * 0.40f
            color = paper.copy(alpha = 0.13f).toArgb()
        }
    }
    private val speedLines = Path()

    // The grid never moves, so each dot's position, tone and wave phases are worked out once. Per frame, the waves
    // only need the sines and cosines of time, combined with these by angle addition.
    private val dotX: FloatArray
    private val dotY: FloatArray
    private val dotTone: FloatArray
    private val ringSin: FloatArray
    private val ringCos: FloatArray
    private val driftSin: FloatArray
    private val driftCos: FloatArray

    init {
        val w = scope.size.width
        val h = scope.size.height
        val focus = Offset(0.85f * w, 1.05f * h)
        val waveLength = 64f * dp
        val driftLength = 90f * dp
        val xs = ArrayList<Float>()
        val ys = ArrayList<Float>()
        for (row in 0..<rows) {
            val y = row * spacing
            if (smoothstep(0.28f * h, 1.0f * h, y) <= 0f) continue
            val offset = if (row % 2 == 0) 0f else spacing / 2f
            for (column in 0..<columns) {
                xs.add(column * spacing + offset)
                ys.add(y)
            }
        }
        dotX = xs.toFloatArray()
        dotY = ys.toFloatArray()
        dotTone = FloatArray(dotX.size) { smoothstep(0.28f * h, 1.0f * h, dotY[it]) }
        val ring = FloatArray(dotX.size) { hypot(dotX[it] - focus.x, dotY[it] - focus.y) / waveLength }
        ringSin = FloatArray(dotX.size) { sin(ring[it]) }
        ringCos = FloatArray(dotX.size) { cos(ring[it]) }
        driftSin = FloatArray(dotX.size) { sin(dotX[it] / driftLength) }
        driftCos = FloatArray(dotX.size) { cos(dotX[it] / driftLength) }
    }

    override fun DrawScope.draw(seconds: Float) {
        val w = size.width
        val h = size.height
        // sin(ring - 1.1t) and sin(drift + 0.45t)
        val ringTimeSin = sin(seconds * 1.1f)
        val ringTimeCos = cos(seconds * 1.1f)
        val driftTimeSin = sin(seconds * 0.45f)
        val driftTimeCos = cos(seconds * 0.45f)
        counts.fill(0)
        for (dot in dotX.indices) {
            val ringWave = ringSin[dot] * ringTimeCos - ringCos[dot] * ringTimeSin
            val driftWave = driftSin[dot] * driftTimeCos + driftCos[dot] * driftTimeSin
            val wave = 0.5f + 0.5f * ringWave + 0.18f * driftWave
            val value = (dotTone[dot] * (0.45f + 0.55f * wave)).coerceIn(0f, 1f)
            val level = (value * LEVELS).toInt().coerceAtMost(LEVELS) - 1
            if (level < 0) continue
            val index = counts[level] * 2
            points[level][index] = dotX[dot]
            points[level][index + 1] = dotY[dot]
            counts[level]++
        }
        drawIntoCanvas { canvas ->
            val native = canvas.nativeCanvas
            for (level in 0..<LEVELS) {
                if (counts[level] > 0) native.drawPoints(points[level], 0, counts[level] * 2, paints[level])
            }
        }

        // Focus lines reaching in from the edges, breathing in and out
        val center = Offset(w / 2f, h * 0.42f)
        val outer = hypot(w, h) * 0.62f
        speedLines.rewind()
        for (line in 0..<SPEED_LINES) {
            val angle = line * TAU / SPEED_LINES + hash(line) * 0.08f
            val inner = outer * (0.66f + 0.16f * hash(line * 3 + 1) + 0.05f * sin(seconds * 0.8f + line))
            val halfWidth = (1f + 2.2f * hash(line * 5 + 2)) * dp
            val dx = cos(angle)
            val dy = sin(angle)
            speedLines.moveTo(center.x + dx * inner, center.y + dy * inner)
            speedLines.lineTo(center.x + dx * outer - dy * halfWidth, center.y + dy * outer + dx * halfWidth)
            speedLines.lineTo(center.x + dx * outer + dy * halfWidth, center.y + dy * outer - dx * halfWidth)
            speedLines.close()
        }
        drawPath(speedLines, paper.copy(alpha = 0.08f))
    }

    private companion object {
        const val LEVELS = 6
        const val SPEED_LINES = 64
    }
}

/**
 * Meteor Shower: meteors streak across a quiet field of stars at their own times, each tail burning out behind its
 * head, beneath a faint band of the galaxy.
 */
private class MeteorShowerScene(scope: CacheDrawScope) : AmbientScene {
    private val dp = scope.density
    private val ice = Color(0xFFEAF2FF)
    private val tints = listOf(Color(0xFFBFD4FF), Color(0xFFD9C8FF), Color(0xFFB8F4FF))
    private val band: Brush
    private val direction = Offset(cos(2.79f), sin(2.79f))

    init {
        val w = scope.size.width
        val h = scope.size.height
        band = Brush.linearGradient(
            0f to Color.Transparent,
            0.45f to Color(0xFFB69CFF).copy(alpha = 0.05f),
            0.55f to Color(0xFF9EC8FF).copy(alpha = 0.06f),
            1f to Color.Transparent,
            start = Offset(0f, h * 0.15f),
            end = Offset(w, h * 0.65f),
        )
    }

    override fun DrawScope.draw(seconds: Float) {
        val w = size.width
        val h = size.height
        drawRect(band)
        for (star in 0..<STARS) {
            val twinkle = 0.5f + 0.5f * sin(seconds * (0.6f + hash(star * 3)) + star)
            drawCircle(
                color = ice.copy(alpha = 0.10f + 0.34f * twinkle * hash(star * 5 + 1)),
                radius = (0.7f + 1.1f * hash(star * 7 + 2)) * dp,
                center = Offset(hash(star * 11 + 3) * w, hash(star * 13 + 4) * h * 0.9f),
            )
        }

        for (slot in 0..<METEORS) {
            val period = 3f + 3f * hash(slot * 17)
            val shifted = seconds + hash(slot * 19 + 1) * period
            val cycle = floor(shifted / period).toInt()
            val local = shifted - cycle * period
            if (local > DURATION) continue
            val progress = local / DURATION
            val envelope = sin(PI.toFloat() * progress)
            val start = Offset(
                w * (0.30f + 0.80f * hash(cycle * 7 + slot * 31)),
                h * (-0.05f + 0.50f * hash(cycle * 13 + slot * 37)),
            )
            val travel = w * (0.45f + 0.25f * hash(slot * 23 + cycle))
            val head = start + direction * (travel * progress)
            val tailLength = (130f + 110f * hash(slot * 29)) * dp * (0.4f + 0.6f * envelope)
            val tail = head - direction * tailLength
            val tint = tints[slot % tints.size]
            drawLine(
                brush = Brush.linearGradient(
                    0f to tint.copy(alpha = 0.95f * envelope),
                    1f to Color.Transparent,
                    start = head,
                    end = tail,
                ),
                start = head,
                end = tail,
                strokeWidth = 2.2f * dp,
                cap = StrokeCap.Round,
            )
            drawCircle(tint.copy(alpha = 0.28f * envelope), 6f * dp, head)
            drawCircle(Color.White.copy(alpha = 0.85f * envelope), 1.3f * dp, head)
        }
    }

    private companion object {
        const val STARS = 80
        const val METEORS = 10
        const val DURATION = 1.1f
    }
}

/** Fireflies: fireflies wander and blink over a dark meadow whose grass sways in layers. */
private class FireflyScene(scope: CacheDrawScope) : AmbientScene {
    private val dp = scope.density
    private val glow = Color(0xFFC8FF9E)
    private val grass = List(3) { Path() }
    private val grassColors = listOf(
        Color(0xFF14301F).copy(alpha = 0.45f),
        Color(0xFF10291A).copy(alpha = 0.65f),
        Color(0xFF0A1C12).copy(alpha = 0.85f),
    )
    private val mist: Brush

    init {
        val h = scope.size.height
        mist = Brush.verticalGradient(
            0f to Color.Transparent,
            0.62f to Color.Transparent,
            0.85f to Color(0xFF7FCFB4).copy(alpha = 0.06f),
            1f to Color(0xFF7FCFB4).copy(alpha = 0.10f),
            startY = 0f,
            endY = h,
        )
    }

    override fun DrawScope.draw(seconds: Float) {
        val w = size.width
        val h = size.height
        drawRect(mist)

        for (fly in 0..<FIREFLIES) {
            val rate = 0.6f + 0.8f * hash(fly * 3)
            val x = hash(fly * 5 + 1) * w + sin(seconds * 0.21f * rate + fly) * 30f * dp +
                sin(seconds * 0.53f + fly * 2.3f) * 12f * dp
            val y = (0.30f + 0.62f * hash(fly * 7 + 2)) * h + cos(seconds * 0.17f * rate + fly * 1.7f) * 24f * dp
            val blink = max(0f, sin(seconds * (0.9f + 0.9f * hash(fly * 11)) + fly * 2.1f)).pow(6)
            val center = Offset(x, y)
            if (blink > 0.02f) {
                drawCircle(glow.copy(alpha = 0.10f * blink), (14f + 8f * blink) * dp, center)
                drawCircle(glow.copy(alpha = 0.28f * blink), (5f + 4f * blink) * dp, center)
            }
            drawCircle(glow.copy(alpha = 0.16f + 0.80f * blink), 2f * dp, center)
        }

        // Grass in three layers, the nearest swaying most
        for (layer in 0..2) {
            val path = grass[layer]
            path.rewind()
            val spacing = (8f - layer * 1.5f) * dp
            val blades = (w / spacing).toInt() + 2
            for (blade in 0..<blades) {
                val seed = layer * 1000 + blade
                val x = (blade + hash(seed)) * spacing
                val height = (30f + 46f * hash(seed * 3 + 1)) * dp * (1f + layer * 0.25f)
                val sway = sin(seconds * 0.9f + x * 0.012f + layer) * (3f + layer * 1.5f) * dp
                val half = (1.2f + layer * 0.3f) * dp
                path.moveTo(x - half, h)
                path.quadraticTo(x + sway * 0.3f, h - height * 0.6f, x + sway, h - height)
                path.quadraticTo(x + sway * 0.3f + half, h - height * 0.6f, x + half, h)
                path.close()
            }
            drawPath(path, grassColors[layer])
        }
    }

    private companion object {
        const val FIREFLIES = 44
    }
}
