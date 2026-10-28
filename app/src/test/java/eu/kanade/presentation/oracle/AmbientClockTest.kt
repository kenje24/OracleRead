package eu.kanade.presentation.oracle

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AmbientClockTest {

    private val frame = 16_666_667L

    @Test
    fun `publishes every frame without a cadence`() {
        val clock = AmbientClock(initialSeconds = 2f, frameIntervalNanos = 0)

        assertFalse(clock.onFrame(0))
        assertTrue(clock.onFrame(frame))
        assertTrue(clock.onFrame(2 * frame))
        assertEquals(2f + 2 * frame / 1e9f, clock.seconds, 1e-4f)
    }

    @Test
    fun `a cadence skips the frames in between`() {
        val clock = AmbientClock(initialSeconds = 0f, frameIntervalNanos = AmbientClock.intervalFor(fps = 20))
        clock.onFrame(0)

        val published = (1..60).count { clock.onFrame(it * frame) }

        // One second at 60 Hz shown at 20 updates a second
        assertEquals(20, published)
        assertEquals(1f, clock.seconds, 1e-3f)
    }

    @Test
    fun `clocks started on different frames tick on the same frames`() {
        // Every ambient effect on screen shares the cadence, so together they still redraw at most `fps` times a
        // second instead of on alternating frames
        val interval = AmbientClock.intervalFor(fps = 30)
        val first = AmbientClock(initialSeconds = 0f, frameIntervalNanos = interval)
        val second = AmbientClock(initialSeconds = 0f, frameIntervalNanos = interval)
        first.onFrame(0)
        second.onFrame(frame)

        val firstTicks = (2..61).filter { first.onFrame(it * frame) }
        val secondTicks = (2..61).filter { second.onFrame(it * frame) }

        assertEquals(firstTicks, secondTicks)
        assertEquals(30, firstTicks.size)
    }

    @Test
    fun `time stands still while paused and resumes where it stopped`() {
        val clock = AmbientClock(initialSeconds = 14f, frameIntervalNanos = 0)
        clock.onFrame(0)
        clock.onFrame(1_000_000_000L)
        assertEquals(15f, clock.seconds, 1e-4f)

        clock.pause()
        // The first frame after a pause only anchors the clock, so the hidden minute is skipped
        assertFalse(clock.onFrame(61_000_000_000L))
        assertEquals(15f, clock.seconds, 1e-4f)
        assertTrue(clock.onFrame(61_500_000_000L))
        assertEquals(15.5f, clock.seconds, 1e-4f)
    }
}
