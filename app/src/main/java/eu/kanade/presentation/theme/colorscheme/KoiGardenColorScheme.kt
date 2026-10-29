package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * OracleRead Koi Garden: A moonlit pond: deep jade water, koi vermilion and pearl. Its library opens on the matching folder effect. Mirrors
 * `presentation-core/src/main/res/values(-night)/colors_koigarden.xml`, which the View-based screens use.
 */
internal object KoiGardenColorScheme : BaseColorScheme() {

    override val darkScheme = darkColorScheme(
        primary = Color(0xFFFF8C82),
        onPrimary = Color(0xFF381F1D),
        primaryContainer = Color(0xFF422F2B),
        onPrimaryContainer = Color(0xFFFFD3D0),
        inversePrimary = Color(0xFF8C4D48),
        secondary = Color(0xFF7FCFB4),
        onSecondary = Color(0xFF1C2E28),
        secondaryContainer = Color(0xFF233F37),
        onSecondaryContainer = Color(0xFFCEEDE2),
        tertiary = Color(0xFFE9DFF5),
        onTertiary = Color(0xFF333136),
        tertiaryContainer = Color(0xFF3C4246),
        onTertiaryContainer = Color(0xFFF7F3FB),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF06110F),
        onBackground = Color(0xFFF3E8E2),
        surface = Color(0xFF06110F),
        onSurface = Color(0xFFF3E8E2),
        surfaceVariant = Color(0xFF1F1D1A),
        onSurfaceVariant = Color(0xFFACA8A3),
        surfaceTint = Color(0xFFFF8C82),
        inverseSurface = Color(0xFFF3E8E2),
        inverseOnSurface = Color(0xFF1A2422),
        outline = Color(0xFF6A6B68),
        outlineVariant = Color(0xFF2C3331),
        scrim = Color(0xFF000000),
        surfaceDim = Color(0xFF06110F),
        surfaceBright = Color(0xFF2A312F),
        surfaceContainerLowest = Color(0xFF040A09),
        surfaceContainerLow = Color(0xFF0E1916),
        surfaceContainer = Color(0xFF131D1B),
        surfaceContainerHigh = Color(0xFF1A2321),
        surfaceContainerHighest = Color(0xFF222B28),
    )

    override val lightScheme = lightColorScheme(
        primary = Color(0xFFA8352F),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFEFDBDA),
        onPrimaryContainer = Color(0xFF401412),
        inversePrimary = Color(0xFFD8A4A1),
        secondary = Color(0xFF2C6A57),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD9E4E1),
        onSecondaryContainer = Color(0xFF112821),
        tertiary = Color(0xFF6A5A86),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFE4E1E9),
        onTertiaryContainer = Color(0xFF282233),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFF2F7F4),
        onBackground = Color(0xFF251D20),
        surface = Color(0xFFF2F7F4),
        onSurface = Color(0xFF251D20),
        surfaceVariant = Color(0xFFECE7E4),
        onSurfaceVariant = Color(0xFF5E5A5B),
        surfaceTint = Color(0xFFA8352F),
        inverseSurface = Color(0xFF3A3335),
        inverseOnSurface = Color(0xFFE8ECE9),
        outline = Color(0xFF908E8E),
        outlineVariant = Color(0xFFC9CBCA),
        scrim = Color(0xFF000000),
        surfaceDim = Color(0xFFDEE1DF),
        surfaceBright = Color(0xFFF8FBFA),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFECF0EE),
        surfaceContainer = Color(0xFFE8ECE9),
        surfaceContainerHigh = Color(0xFFE3E7E4),
        surfaceContainerHighest = Color(0xFFDEE1DF),
    )
}
