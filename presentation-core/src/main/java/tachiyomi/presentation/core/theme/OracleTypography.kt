package tachiyomi.presentation.core.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import tachiyomi.presentation.core.R

/*
 * OracleRead cinematic type system.
 *
 * Display and headline roles use Fraunces, a soft literary serif (variable opsz/wght/SOFT), so titles read
 * like a film card. Every other role uses Manrope, a clean geometric sans (variable wght). Both are bundled
 * under the SIL Open Font License (see assets/licenses).
 */

@OptIn(ExperimentalTextApi::class)
private fun fraunces(weight: FontWeight, opticalSize: Float) = Font(
    resId = R.font.fraunces,
    weight = weight,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight.weight),
        FontVariation.Setting("opsz", opticalSize),
        FontVariation.Setting("SOFT", 60f),
        FontVariation.Setting("WONK", 0f),
    ),
)

@OptIn(ExperimentalTextApi::class)
private fun manrope(weight: FontWeight) = Font(
    resId = R.font.manrope,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val OracleDisplayFontFamily = FontFamily(
    fraunces(FontWeight.Normal, 72f),
    fraunces(FontWeight.Medium, 72f),
    fraunces(FontWeight.SemiBold, 72f),
    fraunces(FontWeight.Bold, 72f),
)

// Headlines sit closer to body size, so they use a smaller optical size for sturdier strokes
val OracleHeadlineFontFamily = FontFamily(
    fraunces(FontWeight.Medium, 28f),
    fraunces(FontWeight.SemiBold, 28f),
    fraunces(FontWeight.Bold, 28f),
)

val OracleTextFontFamily = FontFamily(
    manrope(FontWeight.Normal),
    manrope(FontWeight.Medium),
    manrope(FontWeight.SemiBold),
    manrope(FontWeight.Bold),
    manrope(FontWeight.ExtraBold),
)

private fun TextStyle.display(weight: FontWeight = FontWeight.SemiBold) = copy(
    fontFamily = OracleDisplayFontFamily,
    fontWeight = weight,
    letterSpacing = (-0.01).em,
)

private fun TextStyle.headline(weight: FontWeight = FontWeight.SemiBold) = copy(
    fontFamily = OracleHeadlineFontFamily,
    fontWeight = weight,
    letterSpacing = (-0.005).em,
)

private fun TextStyle.text(weight: FontWeight, tracking: Float = 0f) = copy(
    fontFamily = OracleTextFontFamily,
    fontWeight = weight,
    letterSpacing = tracking.em,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val OracleTypography: Typography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.display(),
        displayMedium = base.displayMedium.display(),
        displaySmall = base.displaySmall.display(),
        headlineLarge = base.headlineLarge.headline(),
        headlineMedium = base.headlineMedium.headline(),
        headlineSmall = base.headlineSmall.headline(),
        titleLarge = base.titleLarge.text(FontWeight.Bold, -0.005f),
        titleMedium = base.titleMedium.text(FontWeight.SemiBold),
        titleSmall = base.titleSmall.text(FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.text(FontWeight.Normal),
        bodyMedium = base.bodyMedium.text(FontWeight.Normal),
        bodySmall = base.bodySmall.text(FontWeight.Medium),
        labelLarge = base.labelLarge.text(FontWeight.SemiBold, 0.01f),
        labelMedium = base.labelMedium.text(FontWeight.SemiBold, 0.02f),
        labelSmall = base.labelSmall.text(FontWeight.Bold, 0.03f),
        displayLargeEmphasized = base.displayLargeEmphasized.display(FontWeight.Bold),
        displayMediumEmphasized = base.displayMediumEmphasized.display(FontWeight.Bold),
        displaySmallEmphasized = base.displaySmallEmphasized.display(FontWeight.Bold),
        headlineLargeEmphasized = base.headlineLargeEmphasized.headline(FontWeight.Bold),
        headlineMediumEmphasized = base.headlineMediumEmphasized.headline(FontWeight.Bold),
        headlineSmallEmphasized = base.headlineSmallEmphasized.headline(FontWeight.Bold),
        titleLargeEmphasized = base.titleLargeEmphasized.text(FontWeight.ExtraBold, -0.005f),
        titleMediumEmphasized = base.titleMediumEmphasized.text(FontWeight.Bold),
        titleSmallEmphasized = base.titleSmallEmphasized.text(FontWeight.Bold),
        bodyLargeEmphasized = base.bodyLargeEmphasized.text(FontWeight.SemiBold),
        bodyMediumEmphasized = base.bodyMediumEmphasized.text(FontWeight.SemiBold),
        bodySmallEmphasized = base.bodySmallEmphasized.text(FontWeight.Bold),
        labelLargeEmphasized = base.labelLargeEmphasized.text(FontWeight.Bold, 0.01f),
        labelMediumEmphasized = base.labelMediumEmphasized.text(FontWeight.Bold, 0.02f),
        labelSmallEmphasized = base.labelSmallEmphasized.text(FontWeight.ExtraBold, 0.03f),
    )
}
