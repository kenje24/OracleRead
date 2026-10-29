package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * OracleRead Paper Cranes: Washi paper on sumi ink, folded indigo and a crimson seal. Its library opens on the matching folder effect. Mirrors
 * `presentation-core/src/main/res/values(-night)/colors_papercranes.xml`, which the View-based screens use.
 */
internal object PaperCranesColorScheme : BaseColorScheme() {

    override val darkScheme = darkColorScheme(
        primary = Color(0xFFE8DFD0),
        onPrimary = Color(0xFF33312E),
        primaryContainer = Color(0xFF45423F),
        onPrimaryContainer = Color(0xFFF6F3ED),
        inversePrimary = Color(0xFF807B72),
        secondary = Color(0xFF93A3DC),
        onSecondary = Color(0xFF202430),
        secondaryContainer = Color(0xFF313342),
        onSecondaryContainer = Color(0xFFD6DCF2),
        tertiary = Color(0xFFEC7480),
        onTertiary = Color(0xFF341A1C),
        tertiaryContainer = Color(0xFF46282C),
        onTertiaryContainer = Color(0xFFF8CACF),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF121011),
        onBackground = Color(0xFFF1EDE7),
        surface = Color(0xFF121011),
        onSurface = Color(0xFFF1EDE7),
        surfaceVariant = Color(0xFF272524),
        onSurfaceVariant = Color(0xFFAEABA7),
        surfaceTint = Color(0xFFE8DFD0),
        inverseSurface = Color(0xFFF1EDE7),
        inverseOnSurface = Color(0xFF252324),
        outline = Color(0xFF706D6B),
        outlineVariant = Color(0xFF363333),
        scrim = Color(0xFF000000),
        surfaceDim = Color(0xFF121011),
        surfaceBright = Color(0xFF333131),
        surfaceContainerLowest = Color(0xFF0B0A0A),
        surfaceContainerLow = Color(0xFF1A1818),
        surfaceContainer = Color(0xFF1E1C1D),
        surfaceContainerHigh = Color(0xFF252323),
        surfaceContainerHighest = Color(0xFF2D2B2B),
    )

    override val lightScheme = lightColorScheme(
        primary = Color(0xFF36437A),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFDBDDE7),
        onPrimaryContainer = Color(0xFF15192E),
        inversePrimary = Color(0xFFA5AAC3),
        secondary = Color(0xFF6B5F55),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE4E2E0),
        onSecondaryContainer = Color(0xFF292420),
        tertiary = Color(0xFFA42F3D),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFEFDADC),
        onTertiaryContainer = Color(0xFF3E1217),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFF7F3EC),
        onBackground = Color(0xFF1C1E26),
        surface = Color(0xFFF7F3EC),
        onSurface = Color(0xFF1C1E26),
        surfaceVariant = Color(0xFFE8E5E3),
        onSurfaceVariant = Color(0xFF595A5D),
        surfaceTint = Color(0xFF36437A),
        inverseSurface = Color(0xFF32333A),
        inverseOnSurface = Color(0xFFECE8E2),
        outline = Color(0xFF8E8D8D),
        outlineVariant = Color(0xFFCBC8C4),
        scrim = Color(0xFF000000),
        surfaceDim = Color(0xFFE1DED8),
        surfaceBright = Color(0xFFFBF9F6),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF0EDE6),
        surfaceContainer = Color(0xFFECE8E2),
        surfaceContainerHigh = Color(0xFFE7E3DD),
        surfaceContainerHighest = Color(0xFFE1DED8),
    )
}
