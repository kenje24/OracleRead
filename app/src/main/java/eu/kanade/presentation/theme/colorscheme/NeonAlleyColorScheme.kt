package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * OracleRead Neon Alley: A rain-soaked alley after midnight: magenta and cyan signs on violet black. Its library opens on the matching folder effect. Mirrors
 * `presentation-core/src/main/res/values(-night)/colors_neonalley.xml`, which the View-based screens use.
 */
internal object NeonAlleyColorScheme : BaseColorScheme() {

    override val darkScheme = darkColorScheme(
        primary = Color(0xFFFF62D0),
        onPrimary = Color(0xFF38162E),
        primaryContainer = Color(0xFF461D40),
        onPrimaryContainer = Color(0xFFFFC3ED),
        inversePrimary = Color(0xFF8C3672),
        secondary = Color(0xFF45E0FF),
        onSecondary = Color(0xFF0F3138),
        secondaryContainer = Color(0xFF193B4B),
        onSecondaryContainer = Color(0xFFB8F3FF),
        tertiary = Color(0xFFAE91FF),
        onTertiary = Color(0xFF262038),
        tertiaryContainer = Color(0xFF32284B),
        onTertiaryContainer = Color(0xFFE0D5FF),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF0B0712),
        onBackground = Color(0xFFF3E6E7),
        surface = Color(0xFF0B0712),
        onSurface = Color(0xFFF3E6E7),
        surfaceVariant = Color(0xFF231025),
        onSurfaceVariant = Color(0xFFADA3A7),
        surfaceTint = Color(0xFFFF62D0),
        inverseSurface = Color(0xFFF3E6E7),
        inverseOnSurface = Color(0xFF1F1B25),
        outline = Color(0xFF6C656B),
        outlineVariant = Color(0xFF302B34),
        scrim = Color(0xFF000000),
        surfaceDim = Color(0xFF0B0712),
        surfaceBright = Color(0xFF2E2832),
        surfaceContainerLowest = Color(0xFF07040B),
        surfaceContainerLow = Color(0xFF130F19),
        surfaceContainer = Color(0xFF18131E),
        surfaceContainerHigh = Color(0xFF1F1A24),
        surfaceContainerHighest = Color(0xFF27222C),
    )

    override val lightScheme = lightColorScheme(
        primary = Color(0xFF9C127C),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFEDD4E7),
        onPrimaryContainer = Color(0xFF3B072F),
        inversePrimary = Color(0xFFD294C4),
        secondary = Color(0xFF00667F),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD1E3E8),
        onSecondaryContainer = Color(0xFF002730),
        tertiary = Color(0xFF6044C0),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFE2DDF4),
        onTertiaryContainer = Color(0xFF241A49),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFFBF4FA),
        onBackground = Color(0xFF241A26),
        surface = Color(0xFFFBF4FA),
        onSurface = Color(0xFF241A26),
        surfaceVariant = Color(0xFFF3E2F0),
        onSurfaceVariant = Color(0xFF605761),
        surfaceTint = Color(0xFF9C127C),
        inverseSurface = Color(0xFF3A303B),
        inverseOnSurface = Color(0xFFF0E9EF),
        outline = Color(0xFF948B94),
        outlineVariant = Color(0xFFD0C8D0),
        scrim = Color(0xFF000000),
        surfaceDim = Color(0xFFE6DEE5),
        surfaceBright = Color(0xFFFDFAFC),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF5EDF4),
        surfaceContainer = Color(0xFFF0E9EF),
        surfaceContainerHigh = Color(0xFFEBE4EA),
        surfaceContainerHighest = Color(0xFFE6DEE5),
    )
}
