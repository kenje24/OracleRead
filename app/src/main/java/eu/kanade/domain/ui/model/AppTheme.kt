package eu.kanade.domain.ui.model

import dev.icerock.moko.resources.StringResource
import tachiyomi.i18n.MR

enum class AppTheme(val titleRes: StringResource?) {
    ORACLE(MR.strings.theme_oracle),
    DEFAULT(MR.strings.label_default),
    MONET(MR.strings.theme_monet),
    CATPPUCCIN(MR.strings.theme_catppuccin),
    GREEN_APPLE(MR.strings.theme_greenapple),
    LAVENDER(MR.strings.theme_lavender),
    MIDNIGHT_DUSK(MR.strings.theme_midnightdusk),
    NORD(MR.strings.theme_nord),
    STRAWBERRY_DAIQUIRI(MR.strings.theme_strawberrydaiquiri),
    TAKO(MR.strings.theme_tako),
    TEALTURQUOISE(MR.strings.theme_tealturquoise),
    TIDAL_WAVE(MR.strings.theme_tidalwave),
    YINYANG(MR.strings.theme_yinyang),
    YOTSUBA(MR.strings.theme_yotsuba),
    TOKYONIGHT(MR.strings.theme_tokyonight),
    MONOCHROME(MR.strings.theme_monochrome),
    FIRE_SPIRITS(MR.strings.theme_fire_spirits),
    MOON_WISPS(MR.strings.theme_moon_wisps),
    SEA_LANTERNS(MR.strings.theme_sea_lanterns),
    SAKURA_DRIFT(MR.strings.theme_sakura_drift),
    AURORA_MOTES(MR.strings.theme_aurora_motes),
    INK_RAIN(MR.strings.theme_ink_rain),
    STAR_DUST(MR.strings.theme_star_dust),

    // OracleRead 1.4.2: themes with a folder effect of their own
    KOI_GARDEN(MR.strings.theme_koigarden),
    ABYSSAL_BLOOM(MR.strings.theme_abyssalbloom),
    PAPER_CRANES(MR.strings.theme_papercranes),
    TEMPEST(MR.strings.theme_tempest),
    NEON_ALLEY(MR.strings.theme_neonalley),
    SCREENTONE(MR.strings.theme_screentone),

    // Deprecated
    DARK_BLUE(null),
    HOT_PINK(null),
    BLUE(null),
}
