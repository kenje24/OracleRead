package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Colors for the OracleRead cinematic theme, the app default.
 *
 * Derived from the OracleRead logo (slate #5675A0, ivory #EEE7D7) with a lantern-gold accent. Dark is the
 * primary experience: an ink-night canvas so cover art carries the color. Mirrors
 * `presentation-core/src/main/res/values(-night)/colors_oracle.xml`, which the View-based screens use.
 *
 * Key colors (dark):
 * Primary #E9C987 (lantern gold)
 * Secondary #8FAAD3 (logo slate)
 * Tertiary #E79C8A (ember)
 * Neutral #0A0D13 (ink night)
 *
 * Key colors (light):
 * Primary #7A5A12
 * Secondary #3E5F8E
 * Tertiary #9A4A3B
 * Neutral #F6F2EA (daylight paper)
 */
internal object OracleColorScheme : BaseColorScheme() {

    override val darkScheme = darkColorScheme(
        primary = Color(0xFFE9C987),
        onPrimary = Color(0xFF2A1F05),
        primaryContainer = Color(0xFF3D3218),
        onPrimaryContainer = Color(0xFFF7E2B0),
        inversePrimary = Color(0xFF7A5A12),
        secondary = Color(0xFF8FAAD3), // Unread badge
        onSecondary = Color(0xFF0F2340), // Unread badge text
        secondaryContainer = Color(0xFF24324A), // Navigation selector pill & progress indicator (remaining)
        onSecondaryContainer = Color(0xFFD6E2F7),
        tertiary = Color(0xFFE79C8A), // Volume and brightness bars, Downloaded badge
        onTertiary = Color(0xFF4A1A10),
        tertiaryContainer = Color(0xFF4A2620),
        onTertiaryContainer = Color(0xFFFFDAD2),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF0A0D13),
        onBackground = Color(0xFFECE6D8),
        surface = Color(0xFF0A0D13),
        onSurface = Color(0xFFECE6D8),
        surfaceVariant = Color(0xFF1B212C),
        onSurfaceVariant = Color(0xFFA7AEBC),
        surfaceTint = Color(0xFFE9C987),
        inverseSurface = Color(0xFFECE6D8),
        inverseOnSurface = Color(0xFF1A1C21),
        outline = Color(0xFF5C6577),
        outlineVariant = Color(0xFF262D3A),
        scrim = Color(0xFF000000),
        surfaceDim = Color(0xFF0A0D13),
        surfaceBright = Color(0xFF2A303C),
        surfaceContainerLowest = Color(0xFF06080C),
        surfaceContainerLow = Color(0xFF10141C),
        surfaceContainer = Color(0xFF151A23), // Navigation bar background
        surfaceContainerHigh = Color(0xFF1B212C),
        surfaceContainerHighest = Color(0xFF232A37),
    )

    override val lightScheme = lightColorScheme(
        primary = Color(0xFF7A5A12),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFF7E2B0),
        onPrimaryContainer = Color(0xFF2A1F05),
        inversePrimary = Color(0xFFE9C987),
        secondary = Color(0xFF3E5F8E), // Unread badge
        onSecondary = Color(0xFFFFFFFF), // Unread badge text
        secondaryContainer = Color(0xFFD6E2F7), // Navigation selector pill & progress indicator (remaining)
        onSecondaryContainer = Color(0xFF0F2340),
        tertiary = Color(0xFF9A4A3B), // Downloaded badge
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFDAD2),
        onTertiaryContainer = Color(0xFF3D0B04),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFF6F2EA),
        onBackground = Color(0xFF1A1C21),
        surface = Color(0xFFF6F2EA),
        onSurface = Color(0xFF1A1C21),
        surfaceVariant = Color(0xFFE5DFD3),
        onSurfaceVariant = Color(0xFF4A4F5A),
        surfaceTint = Color(0xFF7A5A12),
        inverseSurface = Color(0xFF2E3036),
        inverseOnSurface = Color(0xFFF1ECE2),
        outline = Color(0xFF7A7F8A),
        outlineVariant = Color(0xFFCFC8BA),
        scrim = Color(0xFF000000),
        surfaceDim = Color(0xFFDDD8CF),
        surfaceBright = Color(0xFFFBF8F2),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF1ECE2),
        surfaceContainer = Color(0xFFECE6DB), // Navigation bar background
        surfaceContainerHigh = Color(0xFFE6E0D4),
        surfaceContainerHighest = Color(0xFFE0D9CC),
    )
}
