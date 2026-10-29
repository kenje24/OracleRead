package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * OracleRead Screentone: A manga page: paper white on charcoal ink with one crimson accent. Its library opens on the matching folder effect. Mirrors
 * `presentation-core/src/main/res/values(-night)/colors_screentone.xml`, which the View-based screens use.
 */
internal object ScreentoneColorScheme : BaseColorScheme() {

    override val darkScheme = darkColorScheme(
        primary = Color(0xFFF2F0EC),
        onPrimary = Color(0xFF353534),
        primaryContainer = Color(0xFF454546),
        onPrimaryContainer = Color(0xFFFAF9F8),
        inversePrimary = Color(0xFF858482),
        secondary = Color(0xFFABABB1),
        onSecondary = Color(0xFF262627),
        secondaryContainer = Color(0xFF343437),
        onSecondaryContainer = Color(0xFFDFDFE1),
        tertiary = Color(0xFFFF4F6C),
        onTertiary = Color(0xFF381118),
        tertiaryContainer = Color(0xFF491E27),
        onTertiaryContainer = Color(0xFFFFBCC7),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF0F0F11),
        onBackground = Color(0xFFF2EEE8),
        surface = Color(0xFF0F0F11),
        onSurface = Color(0xFFF2EEE8),
        surfaceVariant = Color(0xFF262627),
        onSurfaceVariant = Color(0xFFAEABA8),
        surfaceTint = Color(0xFFF2F0EC),
        inverseSurface = Color(0xFFF2EEE8),
        inverseOnSurface = Color(0xFF222224),
        outline = Color(0xFF6E6D6B),
        outlineVariant = Color(0xFF333333),
        scrim = Color(0xFF000000),
        surfaceDim = Color(0xFF0F0F11),
        surfaceBright = Color(0xFF313031),
        surfaceContainerLowest = Color(0xFF09090A),
        surfaceContainerLow = Color(0xFF171719),
        surfaceContainer = Color(0xFF1B1B1D),
        surfaceContainerHigh = Color(0xFF222223),
        surfaceContainerHighest = Color(0xFF2A2A2B),
    )

    override val lightScheme = lightColorScheme(
        primary = Color(0xFF1B1B1E),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD6D6D6),
        onPrimaryContainer = Color(0xFF0A0A0B),
        inversePrimary = Color(0xFF98989A),
        secondary = Color(0xFF5B5B61),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE1E1E3),
        onSecondaryContainer = Color(0xFF232325),
        tertiary = Color(0xFFBF1E3E),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFF3D6DC),
        onTertiaryContainer = Color(0xFF490B18),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFFAF9F6),
        onBackground = Color(0xFF1A1B1F),
        surface = Color(0xFFFAF9F6),
        onSurface = Color(0xFF1A1B1F),
        surfaceVariant = Color(0xFFE8E7E5),
        onSurfaceVariant = Color(0xFF59595B),
        surfaceTint = Color(0xFF1B1B1E),
        inverseSurface = Color(0xFF303134),
        inverseOnSurface = Color(0xFFEFEEEB),
        outline = Color(0xFF8E8E8F),
        outlineVariant = Color(0xFFCDCDCB),
        scrim = Color(0xFF000000),
        surfaceDim = Color(0xFFE4E3E0),
        surfaceBright = Color(0xFFFCFCFA),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF3F2F0),
        surfaceContainer = Color(0xFFEFEEEB),
        surfaceContainerHigh = Color(0xFFE9E8E6),
        surfaceContainerHighest = Color(0xFFE4E3E0),
    )
}
