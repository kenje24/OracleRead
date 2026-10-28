package eu.kanade.presentation.oracle

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState

/**
 * Whether the user turned animations off (Developer options or Accessibility "Remove animations"), which
 * sets [Settings.Global.ANIMATOR_DURATION_SCALE] to 0. Ambient and looping motion must stay still then.
 */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

/**
 * OracleRead: whether ambient motion may run, which needs animations on and the screen at least started. Hidden
 * screens then hold still instead of asking for frames.
 */
@Composable
fun rememberAmbientMotion(): Boolean {
    val reducedMotion = rememberReducedMotion()
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    return !reducedMotion && lifecycleState.isAtLeast(Lifecycle.State.STARTED)
}

/**
 * OracleRead: how often ambient motion moves. Slow drifts look continuous at this rate, and because every ambient
 * clock shares it, an idle screen redraws at most this many times a second instead of on every display frame.
 */
const val AMBIENT_FPS = 30

/**
 * OracleRead: seconds of ambient time, starting at [initialSeconds] and advancing only while [active], at most
 * [fps] times a second (0 for every frame). Read it in the draw phase, so each tick only redraws.
 */
@Composable
fun rememberAmbientSeconds(active: Boolean, initialSeconds: Float, fps: Int = AMBIENT_FPS): FloatState {
    val clock = remember { AmbientClock(initialSeconds, AmbientClock.intervalFor(fps)) }
    val seconds = remember { mutableFloatStateOf(clock.seconds) }
    if (active) {
        LaunchedEffect(clock) {
            try {
                while (true) {
                    withFrameNanos { frameNanos ->
                        if (clock.onFrame(frameNanos)) seconds.floatValue = clock.seconds
                    }
                }
            } finally {
                clock.pause()
            }
        }
    }
    return seconds
}

/**
 * OracleRead: the time behind [rememberAmbientSeconds]. It holds still while paused and resumes where it stopped. With
 * a cadence it changes only when the frame time enters a new [frameIntervalNanos] slot. The slots are counted from the
 * same zero for every clock, so all ambient effects on screen change on the same frames and the screen redraws at
 * most once per slot however many of them are running.
 */
internal class AmbientClock(initialSeconds: Float, private val frameIntervalNanos: Long) {
    var seconds: Float = initialSeconds
        private set

    // The frame time at which the clock read [anchorSeconds], or -1 until the first frame after starting or pausing
    private var anchorNanos = -1L
    private var anchorSeconds = initialSeconds
    private var lastSlot = Long.MIN_VALUE

    fun pause() {
        anchorNanos = -1L
    }

    /** Moves the clock to [frameNanos] and returns whether it changed. */
    fun onFrame(frameNanos: Long): Boolean {
        val slot = if (frameIntervalNanos > 0L) frameNanos / frameIntervalNanos else frameNanos
        if (anchorNanos < 0L) {
            anchorNanos = frameNanos
            anchorSeconds = seconds
            lastSlot = slot
            return false
        }
        if (slot == lastSlot) return false
        lastSlot = slot
        seconds = anchorSeconds + (frameNanos - anchorNanos) / 1_000_000_000f
        return true
    }

    companion object {
        fun intervalFor(fps: Int): Long {
            if (fps <= 0) return 0L
            return 1_000_000_000L / fps
        }
    }
}
