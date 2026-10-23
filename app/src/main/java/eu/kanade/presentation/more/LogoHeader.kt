package eu.kanade.presentation.more

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.oracle.rememberReducedMotion
import eu.kanade.tachiyomi.R
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

/**
 * The OracleRead logo in the lantern primary, sitting on a soft radial glow. The glow breathes slowly,
 * and stays still when the system asks for reduced motion.
 */
@Composable
fun OracleLogoGlow(
    modifier: Modifier = Modifier,
    glowSize: Dp = 176.dp,
    logoSize: Dp = 68.dp,
) {
    val primary = MaterialTheme.colorScheme.primary
    val reducedMotion = rememberReducedMotion()
    val pulse: State<Float> = if (reducedMotion) {
        remember { mutableFloatStateOf(1f) }
    } else {
        rememberInfiniteTransition(label = "logoGlow").animateFloat(
            initialValue = 0.78f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "logoGlowPulse",
        )
    }
    Box(
        modifier = modifier
            .size(glowSize)
            .drawBehind {
                // Read in the draw phase so the pulse only redraws, never recomposes
                val strength = pulse.value
                val radius = size.minDimension / 2f
                drawCircle(
                    brush = Brush.radialGradient(
                        0f to primary.copy(alpha = 0.34f * strength),
                        0.45f to primary.copy(alpha = 0.12f * strength),
                        1f to Color.Transparent,
                        center = center,
                        radius = radius * (0.88f + 0.12f * strength),
                    ),
                    radius = radius,
                    center = center,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_mihon),
            contentDescription = null,
            tint = primary,
            modifier = Modifier.size(logoSize),
        )
    }
}

/**
 * Cinematic hero for the More and About screens: glowing logo, the app name in the display serif and an
 * optional tagline and version line.
 */
@Composable
fun LogoHeader(
    modifier: Modifier = Modifier,
    tagline: String? = null,
    supporting: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OracleLogoGlow()
        Text(
            text = stringResource(MR.strings.app_name),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                // Tuck the title into the lower part of the glow
                .offset(y = (-20).dp)
                .semantics { heading() },
        )
        if (!tagline.isNullOrBlank()) {
            Text(
                text = tagline,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(y = (-16).dp)
                    .padding(horizontal = 24.dp),
            )
        }
        if (!supporting.isNullOrBlank()) {
            Text(
                text = supporting,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(y = (-12).dp)
                    .padding(horizontal = 24.dp),
            )
        }
    }
}
