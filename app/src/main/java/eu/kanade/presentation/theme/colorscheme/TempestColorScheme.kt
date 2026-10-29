package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * OracleRead Tempest: Storm clouds over slate, electric periwinkle and ice. Its library opens on the matching folder effect. Mirrors
 * `presentation-core/src/main/res/values(-night)/colors_tempest.xml`, which the View-based screens use.
 */
internal object TempestColorScheme : BaseColorScheme() {

    override val darkScheme = darkColorScheme(
        primary = Color(0xFFAAB8FF),
        onPrimary = Color(0xFF252838),
        primaryContainer = Color(0xFF30354C),
        onPrimaryContainer = Color(0xFFDFE4FF),
        inversePrimary = Color(0xFF5E658C),
        secondary = Color(0xFF97AAC0),
        onSecondary = Color(0xFF21252A),
        secondaryContainer = Color(0xFF2B323D),
        onSecondaryContainer = Color(0xFFD7DFE7),
        tertiary = Color(0xFF8EEFFF),
        onTertiary = Color(0xFF1F3538),
        tertiaryContainer = Color(0xFF29424C),
        onTertiaryContainer = Color(0xFFD4F9FF),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF090C13),
        onBackground = Color(0xFFEEEBE9),
        surface = Color(0xFF090C13),
        onSurface = Color(0xFFEEEBE9),
        surfaceVariant = Color(0xFF191D2B),
        onSurfaceVariant = Color(0xFFA9A8A9),
        surfaceTint = Color(0xFFAAB8FF),
        inverseSurface = Color(0xFFEEEBE9),
        inverseOnSurface = Color(0xFF1D1F26),
        outline = Color(0xFF696A6D),
        outlineVariant = Color(0xFF2E3035),
        scrim = Color(0xFF000000),
        surfaceDim = Color(0xFF090C13),
        surfaceBright = Color(0xFF2B2D33),
        surfaceContainerLowest = Color(0xFF05070B),
        surfaceContainerLow = Color(0xFF11141A),
        surfaceContainer = Color(0xFF16181F),
        surfaceContainerHigh = Color(0xFF1C1F25),
        surfaceContainerHighest = Color(0xFF24272D),
    )

    override val lightScheme = lightColorScheme(
        primary = Color(0xFF374DB6),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFDBDFF2),
        onPrimaryContainer = Color(0xFF151D45),
        inversePrimary = Color(0xFFA5AFDE),
        secondary = Color(0xFF475B70),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFDEE1E5),
        onSecondaryContainer = Color(0xFF1B232B),
        tertiary = Color(0xFF006777),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFD1E4E7),
        onTertiaryContainer = Color(0xFF00272D),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFF2F4F8),
        onBackground = Color(0xFF1C1F2B),
        surface = Color(0xFFF2F4F8),
        onSurface = Color(0xFF1C1F2B),
        surfaceVariant = Color(0xFFE3E7F3),
        onSurfaceVariant = Color(0xFF585B64),
        surfaceTint = Color(0xFF374DB6),
        inverseSurface = Color(0xFF313440),
        inverseOnSurface = Color(0xFFE7E9EE),
        outline = Color(0xFF8B8E96),
        outlineVariant = Color(0xFFC7C9CF),
        scrim = Color(0xFF000000),
        surfaceDim = Color(0xFFDDDFE4),
        surfaceBright = Color(0xFFF8FAFC),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFECEEF2),
        surfaceContainer = Color(0xFFE7E9EE),
        surfaceContainerHigh = Color(0xFFE2E4E9),
        surfaceContainerHighest = Color(0xFFDDDFE4),
    )
}
