package eu.kanade.presentation.oracle

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

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
