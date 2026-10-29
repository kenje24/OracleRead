package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * OracleRead Abyssal Bloom: The deep sea at night, lit by bioluminescent cyan, violet and coral pink. Its library opens on the matching folder effect. Mirrors
 * `presentation-core/src/main/res/values(-night)/colors_abyssalbloom.xml`, which the View-based screens use.
 */
internal object AbyssalBloomColorScheme : BaseColorScheme() {

    override val darkScheme = darkColorScheme(
        primary = Color(0xFF6EF2EC),
        onPrimary = Color(0xFF183534),
        primaryContainer = Color(0xFF1D4048),
        onPrimaryContainer = Color(0xFFC8FAF8),
        inversePrimary = Color(0xFF3C8582),
        secondary = Color(0xFFA991FF),
        onSecondary = Color(0xFF252038),
        secondaryContainer = Color(0xFF2C294C),
        onSecondaryContainer = Color(0xFFDED5FF),
        tertiary = Color(0xFFFF8AD4),
        onTertiary = Color(0xFF381E2F),
        tertiaryContainer = Color(0xFF402742),
        onTertiaryContainer = Color(0xFFFFD3EF),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF040814),
        onBackground = Color(0xFFEAEEE8),
        surface = Color(0xFF040814),
        onSurface = Color(0xFFEAEEE8),
        surfaceVariant = Color(0xFF0F1F2A),
        onSurfaceVariant = Color(0xFFA5A9A8),
        surfaceTint = Color(0xFF6EF2EC),
        inverseSurface = Color(0xFFEAEEE8),
        inverseOnSurface = Color(0xFF181C27),
        outline = Color(0xFF65696D),
        outlineVariant = Color(0xFF292D36),
        scrim = Color(0xFF000000),
        surfaceDim = Color(0xFF040814),
        surfaceBright = Color(0xFF262A34),
        surfaceContainerLowest = Color(0xFF02050C),
        surfaceContainerLow = Color(0xFF0C101B),
        surfaceContainer = Color(0xFF111520),
        surfaceContainerHigh = Color(0xFF181C26),
        surfaceContainerHighest = Color(0xFF20242D),
    )

    override val lightScheme = lightColorScheme(
        primary = Color(0xFF006A67),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD1E4E4),
        onPrimaryContainer = Color(0xFF002827),
        inversePrimary = Color(0xFF8CBCBB),
        secondary = Color(0xFF5A44B5),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE1DDF2),
        onSecondaryContainer = Color(0xFF221A45),
        tertiary = Color(0xFFA2317C),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFEEDAE7),
        onTertiaryContainer = Color(0xFF3E132F),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFF1F6FB),
        onBackground = Color(0xFF182125),
        surface = Color(0xFFF1F6FB),
        onSurface = Color(0xFF182125),
        surfaceVariant = Color(0xFFDEEBEF),
        onSurfaceVariant = Color(0xFF555D61),
        surfaceTint = Color(0xFF006A67),
        inverseSurface = Color(0xFF2E363A),
        inverseOnSurface = Color(0xFFE6EBF0),
        outline = Color(0xFF899094),
        outlineVariant = Color(0xFFC6CBD0),
        scrim = Color(0xFF000000),
        surfaceDim = Color(0xFFDBE1E6),
        surfaceBright = Color(0xFFF8FAFD),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFEAF0F5),
        surfaceContainer = Color(0xFFE6EBF0),
        surfaceContainerHigh = Color(0xFFE1E6EB),
        surfaceContainerHighest = Color(0xFFDBE1E6),
    )
}
