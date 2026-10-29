package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import eu.kanade.presentation.category.FolderAmbience
import eu.kanade.presentation.oracle.rememberAmbientMotion
import eu.kanade.presentation.oracle.rememberAmbientSeconds
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

// The particle ambiences loop every 9 seconds; this is the still frame shown when motion is off
private const val PARTICLE_CYCLE_SECONDS = 9f
private const val STILL_SECONDS = 0.37f * PARTICLE_CYCLE_SECONDS

/**
 * A folder's ambient background. Time is only read while drawing, so the ambience redraws without recomposing,
 * and its geometry, brushes and strokes are built once per size. [FolderAmbience.NONE] draws nothing at all.
 */
@Composable
internal fun LibraryAmbientBackground(
    ambience: FolderAmbience,
    modifier: Modifier = Modifier,
) {
    if (ambience == FolderAmbience.NONE) return
    // With animations removed system-wide, or the screen hidden, the ambience holds one still frame
    val active = rememberAmbientMotion()
    val seconds = rememberAmbientSeconds(active = active, initialSeconds = STILL_SECONDS)
    Spacer(
        modifier = modifier.drawWithCache {
            val scene = ambientScene(ambience)
            onDrawBehind { with(scene) { draw(seconds.floatValue) } }
        },
    )
}

internal interface AmbientScene {
    fun DrawScope.draw(seconds: Float)
}

private fun CacheDrawScope.ambientScene(ambience: FolderAmbience): AmbientScene = when (ambience) {
    FolderAmbience.INK_TIDE -> InkTideScene(this)
    FolderAmbience.CONSTELLATIONS -> ConstellationScene(this)
    FolderAmbience.ECLIPSE -> EclipseScene(this)
    FolderAmbience.SILK_RIBBONS -> SilkRibbonScene(this)
    else -> themedAmbientScene(ambience) ?: ParticleScene(ambience)
}

private val Gold = Color(0xFFE9C987)
private val Ivory = Color(0xFFEEE7D7)
private val Slate = Color(0xFF8FAAD3)
private val Ember = Color(0xFFE79C8A)

/** The original folder ambiences: 58 drifting motes, petals, rain or stars over a soft two-tone wash. */
private class ParticleScene(private val ambience: FolderAmbience) : AmbientScene {
    private val palette = when (ambience) {
        FolderAmbience.FIRE_SPIRITS -> listOf(Color(0xFFFFB347), Color(0xFFFF6B35), Color(0xFFFFF1A8))
        FolderAmbience.MOON_WISPS -> listOf(Color(0xFFD8E6FF), Color(0xFFB8A7FF), Color(0xFFFFFFFF))
        FolderAmbience.SEA_LANTERNS -> listOf(Color(0xFF6FFFE9), Color(0xFF5BC0EB), Color(0xFFFFE66D))
        FolderAmbience.SAKURA_DRIFT -> listOf(Color(0xFFFFB7C5), Color(0xFFFF8FAB), Color(0xFFFFF0F4))
        FolderAmbience.AURORA_MOTES -> listOf(Color(0xFF86F7C8), Color(0xFF8EA7FF), Color(0xFFFFC2F2))
        FolderAmbience.INK_RAIN -> listOf(Color(0xFF44546A), Color(0xFF111827), Color(0xFFA7B0C0))
        FolderAmbience.STAR_DUST -> listOf(Color(0xFFFFF1A8), Color(0xFFB8A7FF), Color(0xFFFFFFFF))
        else -> listOf(Gold, Slate, Ivory)
    }

    private val wash = Brush.verticalGradient(
        // Starts transparent so the ambience melts into the header instead of starting at a seam
        colors = listOf(
            Color.Transparent,
            palette[0].copy(alpha = 0.06f),
            Color.Transparent,
            palette[1].copy(alpha = 0.05f),
        ),
    )

    override fun DrawScope.draw(seconds: Float) {
        val progress = (seconds / PARTICLE_CYCLE_SECONDS) % 1f
        val width = size.width
        val height = size.height
        drawRect(brush = wash)

        repeat(58) { index ->
            val seed = index + 1
            val cycle = (progress + (seed * 0.047f)) % 1f
            val baseX = ((seed * 73) % 100) / 100f * width
            val wave = sin((cycle * 6.28f) + seed)
            val color = palette[index % palette.size]
            val x: Float
            val y: Float
            val radius: Float
            val alpha: Float
            when (ambience) {
                FolderAmbience.MOON_WISPS -> {
                    x = width * ((cycle + (seed * 0.031f)) % 1f)
                    y = height * (0.18f + ((seed * 17) % 72) / 100f) + wave * 18f
                    radius = 3.5f + (seed % 4)
                    alpha = 0.10f + (seed % 3) * 0.025f
                }
                FolderAmbience.SEA_LANTERNS -> {
                    x = baseX + wave * width * 0.035f
                    y = height * (0.95f - cycle * 0.75f) + cos(cycle * 12.56f + seed) * 18f
                    radius = 4.5f + (seed % 3) * 1.4f
                    alpha = 0.14f + (seed % 3) * 0.03f
                }
                FolderAmbience.SAKURA_DRIFT -> {
                    x = baseX + wave * width * 0.09f
                    y = height * (-0.08f + cycle * 1.16f)
                    radius = 2.6f + (seed % 4) * 0.8f
                    alpha = 0.13f + (seed % 3) * 0.03f
                }
                FolderAmbience.AURORA_MOTES -> {
                    x = width * ((cycle * 0.65f + (seed * 0.043f)) % 1f)
                    y = height * (0.20f + ((seed * 13) % 58) / 100f) + wave * 34f
                    radius = 2.4f + (seed % 5)
                    alpha = 0.11f + (seed % 4) * 0.025f
                }
                FolderAmbience.INK_RAIN -> {
                    x = baseX + wave * width * 0.012f
                    y = height * (-0.04f + cycle * 1.18f)
                    radius = 1.4f + (seed % 3) * 0.55f
                    alpha = 0.10f + (seed % 4) * 0.025f
                }
                FolderAmbience.STAR_DUST -> {
                    val twinkle = ((sin(progress * 25f + seed) + 1f) / 2f)
                    x = ((seed * 59) % 100) / 100f * width
                    y = ((seed * 83) % 100) / 100f * height
                    radius = 1.7f + twinkle * 3f
                    alpha = 0.08f + twinkle * 0.24f
                }
                else -> {
                    val drift = wave * width * (0.025f + (seed % 7) * 0.006f)
                    x = baseX + drift
                    y = height * (1.08f - cycle * 1.22f)
                    radius = 2.2f + (seed % 5) * 0.9f
                    alpha = (1f - cycle).coerceIn(0f, 1f) * (0.18f + (seed % 4) * 0.04f)
                }
            }
            val center = Offset(x, y)
            if (ambience == FolderAmbience.INK_RAIN) {
                drawLine(
                    color = color.copy(alpha = alpha),
                    start = center,
                    end = Offset(x, y + radius * 8f),
                    strokeWidth = radius,
                )
                return@repeat
            }
            drawCircle(color = color.copy(alpha = alpha * 0.22f), radius = radius * 3.2f, center = center)
            drawCircle(color = color.copy(alpha = alpha), radius = radius, center = center)
        }
    }
}

/**
 * Ink Tide: three layered bands of ink roll along the bottom of the screen at different speeds, the nearest one
 * edged with a thin crest of lantern light.
 */
private class InkTideScene(scope: CacheDrawScope) : AmbientScene {
    private val bands = List(3) { Path() }
    private val crests = List(2) { Path() }
    private val bandBrushes: List<Brush>
    private val crestStrokes = listOf(
        Stroke(width = with(scope) { 1.2f * density }, cap = StrokeCap.Round),
        Stroke(width = with(scope) { 0.8f * density }, cap = StrokeCap.Round),
    )
    private val crestColors = listOf(Gold.copy(alpha = 0.24f), Ivory.copy(alpha = 0.10f))

    init {
        val height = scope.size.height
        val colors = listOf(Slate, Color(0xFF3D5A80), Color(0xFF16233A))
        val alphas = listOf(0.07f to 0.12f, 0.10f to 0.18f, 0.20f to 0.34f)
        bandBrushes = List(3) { band ->
            Brush.verticalGradient(
                0f to colors[band].copy(alpha = alphas[band].first),
                1f to colors[band].copy(alpha = alphas[band].second),
                startY = height * (0.66f + band * 0.09f),
                endY = height,
            )
        }
    }

    override fun DrawScope.draw(seconds: Float) {
        val width = size.width
        val height = size.height
        // The farthest band first, so nearer ones lie over it
        for (band in 0..2) {
            val baseY = height * (0.70f + band * 0.09f)
            val amplitude = height * (0.016f + band * 0.006f)
            val waveNumber = (2f * PI.toFloat()) / (width * (0.95f - band * 0.18f))
            val speed = (0.20f + band * 0.08f) * if (band % 2 == 0) 1f else -1f
            val path = bands[band]
            val crest = crests.getOrNull(band)
            path.rewind()
            crest?.rewind()
            path.moveTo(0f, height)
            for (step in 0..TIDE_SEGMENTS) {
                val x = width * step / TIDE_SEGMENTS
                val y = baseY +
                    amplitude * sin(x * waveNumber + seconds * speed + band) +
                    amplitude * 0.45f * sin(x * waveNumber * 2.3f - seconds * speed * 1.6f + band * 2f)
                path.lineTo(x, y)
                if (crest != null) {
                    if (step == 0) crest.moveTo(x, y) else crest.lineTo(x, y)
                }
            }
            path.lineTo(width, height)
            path.close()
            drawPath(path = path, brush = bandBrushes[band])
            if (crest != null) drawPath(path = crest, color = crestColors[band], style = crestStrokes[band])
        }
    }

    private companion object {
        const val TIDE_SEGMENTS = 48
    }
}

/**
 * Constellations: three original star figures over a field of faint stars. A point of lantern light travels each
 * figure's lines in turn, brightening the stretch it has just passed.
 */
private class ConstellationScene(scope: CacheDrawScope) : AmbientScene {
    private class Figure(val points: List<Offset>, val phase: Float) {
        val segmentLengths = points.zipWithNext { a, b -> hypot(b.x - a.x, b.y - a.y) }
        val length = segmentLengths.sum()
    }

    private val figures: List<Figure>
    private val fieldStars: List<Offset>
    private val fieldPhases: FloatArray
    private val lineWidth: Float
    private val traceWidth: Float
    private val starRadius: Float
    private val fieldRadius: Float

    init {
        val width = scope.size.width
        val height = scope.size.height
        fun figure(phase: Float, vararg points: Pair<Float, Float>) =
            Figure(points.map { (x, y) -> Offset(width * x, height * y) }, phase)
        figures = listOf(
            figure(0f, 0.10f to 0.17f, 0.20f to 0.10f, 0.31f to 0.15f, 0.27f to 0.27f, 0.15f to 0.26f, 0.10f to 0.17f),
            figure(
                1f / 3f,
                0.52f to 0.25f,
                0.63f to 0.16f,
                0.76f to 0.20f,
                0.88f to 0.11f,
                0.79f to 0.31f,
                0.66f to 0.37f,
            ),
            figure(2f / 3f, 0.18f to 0.55f, 0.31f to 0.48f, 0.44f to 0.54f, 0.39f to 0.65f, 0.53f to 0.72f),
        )
        val random = Random(0x5EED)
        fieldStars = List(40) { Offset(random.nextFloat() * width, random.nextFloat() * height * 0.85f) }
        fieldPhases = FloatArray(fieldStars.size) { random.nextFloat() * 2f * PI.toFloat() }
        lineWidth = 0.8f * scope.density
        traceWidth = 1.6f * scope.density
        starRadius = 2.2f * scope.density
        fieldRadius = 0.9f * scope.density
    }

    override fun DrawScope.draw(seconds: Float) {
        fieldStars.forEachIndexed { index, star ->
            val twinkle = (sin(seconds * 0.9f + fieldPhases[index]) + 1f) / 2f
            drawCircle(color = Ivory.copy(alpha = 0.06f + twinkle * 0.14f), radius = fieldRadius, center = star)
        }

        figures.forEachIndexed { figureIndex, figure ->
            for (index in 0..<figure.points.lastIndex) {
                drawLine(
                    color = Gold.copy(alpha = 0.09f),
                    start = figure.points[index],
                    end = figure.points[index + 1],
                    strokeWidth = lineWidth,
                )
            }

            // Each figure is traced over a third of the cycle, then rests while the others are traced
            val cycle = ((seconds / TRACE_CYCLE_SECONDS) + figure.phase) % 1f
            val traced = (cycle * 3f).coerceAtMost(1f)
            val fade = if (cycle * 3f <= 1f) 1f else (1f - (cycle * 3f - 1f) / 2f).coerceIn(0f, 1f)
            val head = figure.length * traced
            val trail = figure.length * 0.35f
            var start = 0f
            var headPoint = figure.points.first()
            figure.segmentLengths.forEachIndexed { index, segmentLength ->
                val end = start + segmentLength
                val from = maxOf(start, head - trail)
                val to = minOf(end, head)
                if (to > from && segmentLength > 0f) {
                    val a = figure.points[index]
                    val b = figure.points[index + 1]
                    val fromPoint = lerp(a, b, (from - start) / segmentLength)
                    val toPoint = lerp(a, b, (to - start) / segmentLength)
                    drawLine(
                        color = Gold.copy(alpha = 0.30f * fade),
                        start = fromPoint,
                        end = toPoint,
                        strokeWidth = traceWidth,
                        cap = StrokeCap.Round,
                    )
                    if (head in start..end) headPoint = toPoint
                }
                start = end
            }
            if (traced < 1f) {
                drawCircle(color = Gold.copy(alpha = 0.16f), radius = starRadius * 3.4f, center = headPoint)
                drawCircle(color = Ivory.copy(alpha = 0.55f), radius = starRadius * 0.9f, center = headPoint)
            }

            figure.points.forEachIndexed { index, point ->
                val twinkle = 0.75f + 0.25f * sin(seconds * 1.3f + index + figureIndex * 2f)
                drawCircle(color = Gold.copy(alpha = 0.10f * twinkle), radius = starRadius * 2.6f, center = point)
                drawCircle(color = Ivory.copy(alpha = 0.42f * twinkle), radius = starRadius, center = point)
            }
        }
    }

    private fun lerp(a: Offset, b: Offset, fraction: Float) =
        Offset(a.x + (b.x - a.x) * fraction, a.y + (b.y - a.y) * fraction)

    private companion object {
        const val TRACE_CYCLE_SECONDS = 15f
    }
}

/**
 * Eclipse Halo: an eclipsed moon on the right, its corona breathing slowly, ringed by thin halo arcs that
 * turn at different speeds and directions.
 */
private class EclipseScene(scope: CacheDrawScope) : AmbientScene {
    // Not named center: DrawScope.center would win over it inside draw() and put the moon in the middle of the screen
    private val moonCenter: Offset
    private val radius: Float
    private val corona: Brush
    private val rimStroke: Stroke
    private val crescentStroke: Stroke
    private val haloStroke: Stroke
    private val haloRadii: FloatArray
    private val satelliteRadius: Float

    init {
        val size = scope.size
        radius = min(size.width, size.height) * 0.16f
        // Right of the section title, in the gap between the continue-reading card and the grid
        moonCenter = Offset(size.width * 0.80f, size.height * 0.46f)
        corona = Brush.radialGradient(
            0f to Gold.copy(alpha = 0f),
            0.36f to Gold.copy(alpha = 0.22f),
            0.46f to Gold.copy(alpha = 0.10f),
            0.70f to Slate.copy(alpha = 0.04f),
            1f to Color.Transparent,
            center = moonCenter,
            radius = radius * 2.8f,
        )
        rimStroke = Stroke(width = 1.1f * scope.density)
        crescentStroke = Stroke(width = 2.2f * scope.density, cap = StrokeCap.Round)
        haloStroke = Stroke(width = 1f * scope.density, cap = StrokeCap.Round)
        haloRadii = floatArrayOf(radius * 1.35f, radius * 1.75f, radius * 2.2f)
        satelliteRadius = 1.8f * scope.density
    }

    override fun DrawScope.draw(seconds: Float) {
        val breath = 0.85f + 0.15f * sin(seconds * 0.45f)
        drawCircle(brush = corona, radius = radius * 2.8f, center = moonCenter, alpha = breath)
        // The moon's dark face, then its rim and the lit crescent
        drawCircle(color = Color(0xFF0B0F17).copy(alpha = 0.9f), radius = radius, center = moonCenter)
        drawCircle(color = Gold.copy(alpha = 0.30f), radius = radius, center = moonCenter, style = rimStroke)
        drawArc(
            color = Ivory.copy(alpha = 0.50f * breath),
            startAngle = 100f,
            sweepAngle = 80f,
            useCenter = false,
            topLeft = Offset(moonCenter.x - radius, moonCenter.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = crescentStroke,
        )

        for (ring in haloRadii.indices) {
            val ringRadius = haloRadii[ring]
            val speed = HALO_SPEEDS[ring]
            val color = HALO_COLORS[ring]
            val topLeft = Offset(moonCenter.x - ringRadius, moonCenter.y - ringRadius)
            val arcSize = Size(ringRadius * 2f, ringRadius * 2f)
            for (arc in 0..1) {
                val start = (seconds * speed + ring * 47f + arc * 180f) % 360f
                val sweep = 50f + ring * 22f
                drawArc(
                    color = color,
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = haloStroke,
                )
                if (arc == 0) {
                    val headAngle = Math.toRadians((start + if (speed > 0) sweep else 0f).toDouble())
                    drawCircle(
                        color = color.copy(alpha = color.alpha * 2.2f),
                        radius = satelliteRadius,
                        center = Offset(
                            moonCenter.x + ringRadius * cos(headAngle).toFloat(),
                            moonCenter.y + ringRadius * sin(headAngle).toFloat(),
                        ),
                    )
                }
            }
        }
    }

    private companion object {
        val HALO_SPEEDS = floatArrayOf(6f, -4f, 2.5f)
        val HALO_COLORS = listOf(Ivory.copy(alpha = 0.14f), Slate.copy(alpha = 0.16f), Gold.copy(alpha = 0.13f))
    }
}

/**
 * Silk Ribbons: three long ribbons of gold, slate and ember silk cross the screen on a diagonal, slowly
 * undulating and twisting as they narrow and widen.
 */
private class SilkRibbonScene(scope: CacheDrawScope) : AmbientScene {
    private class Ribbon(val startY: Float, val endY: Float, val thickness: Float, val phase: Float, val brush: Brush)

    private val ribbons: List<Ribbon>
    private val paths = List(3) { Path() }
    private val edges = List(3) { Path() }
    private val edgeStroke = Stroke(width = with(scope) { 0.9f * density }, cap = StrokeCap.Round)
    private val edgeColors = listOf(Gold.copy(alpha = 0.16f), Slate.copy(alpha = 0.12f), Ember.copy(alpha = 0.10f))

    init {
        val width = scope.size.width
        val height = scope.size.height
        fun brush(color: Color, alpha: Float) = Brush.horizontalGradient(
            0f to color.copy(alpha = 0f),
            0.25f to color.copy(alpha = alpha),
            0.6f to color.copy(alpha = alpha * 0.55f),
            1f to color.copy(alpha = 0f),
            startX = 0f,
            endX = width,
        )
        ribbons = listOf(
            Ribbon(height * 0.30f, height * 0.10f, height * 0.050f, 0f, brush(Gold, 0.13f)),
            Ribbon(height * 0.60f, height * 0.40f, height * 0.036f, 2.1f, brush(Slate, 0.12f)),
            Ribbon(height * 0.88f, height * 0.66f, height * 0.044f, 4.2f, brush(Ember, 0.09f)),
        )
    }

    override fun DrawScope.draw(seconds: Float) {
        val width = size.width
        val amplitude = size.height * 0.06f
        ribbons.forEachIndexed { index, ribbon ->
            val t = seconds * 0.22f + ribbon.phase
            val startX = -width * 0.1f
            val endX = width * 1.1f
            val c1x = width * 0.32f
            val c2x = width * 0.68f
            val c1y = ribbon.startY + amplitude * sin(t)
            val c2y = ribbon.endY + amplitude * cos(t * 0.8f)
            // The ribbon narrows and widens along its length as if it were twisting
            val half = ribbon.thickness / 2f
            val twist1 = half * (0.35f + 0.65f * abs(sin(t * 0.7f)))
            val twist2 = half * (0.35f + 0.65f * abs(cos(t * 0.6f)))

            val path = paths[index]
            path.rewind()
            path.moveTo(startX, ribbon.startY - half)
            path.cubicTo(c1x, c1y - twist1, c2x, c2y - twist2, endX, ribbon.endY - half)
            path.lineTo(endX, ribbon.endY + half)
            path.cubicTo(c2x, c2y + twist2, c1x, c1y + twist1, startX, ribbon.startY + half)
            path.close()
            drawPath(path = path, brush = ribbon.brush)

            val edge = edges[index]
            edge.rewind()
            edge.moveTo(startX, ribbon.startY - half)
            edge.cubicTo(c1x, c1y - twist1, c2x, c2y - twist2, endX, ribbon.endY - half)
            drawPath(path = edge, color = edgeColors[index], style = edgeStroke)
        }
    }
}
