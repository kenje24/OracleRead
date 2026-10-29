package eu.kanade.presentation.category

import dev.icerock.moko.resources.StringResource
import eu.kanade.domain.ui.model.AppTheme
import tachiyomi.i18n.MR

/**
 * OracleRead: the ambient background a library folder shows behind its titles. [key] is what [FolderStyle] stores,
 * so keys never change once released. The first eight match the ambient app themes they were named after, but are
 * kept apart from [AppTheme] so folders can have designs of their own.
 */
enum class FolderAmbience(val key: String, val label: StringResource) {
    // Nothing is drawn and the home aurora behind the library holds still
    NONE("none", MR.strings.folder_ambience_none),
    LANTERN("lantern", MR.strings.theme_oracle),
    FIRE_SPIRITS("fire_spirits", MR.strings.theme_fire_spirits),
    MOON_WISPS("moon_wisps", MR.strings.theme_moon_wisps),
    SEA_LANTERNS("sea_lanterns", MR.strings.theme_sea_lanterns),
    SAKURA_DRIFT("sakura_drift", MR.strings.theme_sakura_drift),
    AURORA_MOTES("aurora_motes", MR.strings.theme_aurora_motes),
    INK_RAIN("ink_rain", MR.strings.theme_ink_rain),
    STAR_DUST("star_dust", MR.strings.theme_star_dust),

    // Layered ink waves rolling along the bottom of the screen
    INK_TIDE("ink_tide", MR.strings.folder_ambience_ink_tide),

    // Star figures whose lines are traced by a travelling point of lantern light
    CONSTELLATIONS("constellations", MR.strings.folder_ambience_constellations),

    // An eclipsed moon with a soft corona and slowly turning halo arcs
    ECLIPSE("eclipse", MR.strings.folder_ambience_eclipse),

    // Long ribbons of gold and slate silk flowing across the screen
    SILK_RIBBONS("silk_ribbons", MR.strings.folder_ambience_silk_ribbons),

    // Koi circling a moonlit pond under drifting lily pads, ripples spreading where they surface
    KOI_POND("koi_pond", MR.strings.folder_ambience_koi_pond),

    // Bioluminescent jellyfish pulsing upward through falling marine snow
    JELLYFISH("jellyfish", MR.strings.folder_ambience_jellyfish),

    // Folded paper cranes gliding across on slow wingbeats, casting soft shadows
    PAPER_CRANES("paper_cranes", MR.strings.folder_ambience_paper_cranes),

    // Slanting rain under rolling cloud banks, with a soft, rare fork of lightning
    THUNDERSTORM("thunderstorm", MR.strings.folder_ambience_thunderstorm),

    // Streams of neon katakana raining down, magenta heads fading to cyan
    GLYPH_RAIN("glyph_rain", MR.strings.folder_ambience_glyph_rain),

    // A manga halftone of ink dots swelling in slow waves, framed by faint speed lines
    SCREENTONE("screentone", MR.strings.folder_ambience_screentone),

    // Meteors streaking across a quiet star field, each tail burning out behind it
    METEOR_SHOWER("meteor_shower", MR.strings.folder_ambience_meteor_shower),

    // Fireflies blinking over a dark meadow of swaying grass
    FIREFLIES("fireflies", MR.strings.folder_ambience_fireflies),
    ;

    companion object {
        val DEFAULT = LANTERN

        fun fromKey(key: String?): FolderAmbience? = entries.firstOrNull { it.key == key }

        /** The key a folder gets when [tappedKey] is tapped in the picker: tapping the selected one turns it off. */
        fun toggledKey(currentKey: String, tappedKey: String): String =
            if (tappedKey == currentKey) NONE.key else tappedKey

        /** The ambience shown when there's no folder to take one from. */
        fun forAppTheme(appTheme: AppTheme): FolderAmbience = when (appTheme) {
            AppTheme.FIRE_SPIRITS -> FIRE_SPIRITS
            AppTheme.MOON_WISPS -> MOON_WISPS
            AppTheme.SEA_LANTERNS -> SEA_LANTERNS
            AppTheme.SAKURA_DRIFT -> SAKURA_DRIFT
            AppTheme.AURORA_MOTES -> AURORA_MOTES
            AppTheme.INK_RAIN -> INK_RAIN
            AppTheme.STAR_DUST -> STAR_DUST
            AppTheme.KOI_GARDEN -> KOI_POND
            AppTheme.ABYSSAL_BLOOM -> JELLYFISH
            AppTheme.PAPER_CRANES -> PAPER_CRANES
            AppTheme.TEMPEST -> THUNDERSTORM
            AppTheme.NEON_ALLEY -> GLYPH_RAIN
            AppTheme.SCREENTONE -> SCREENTONE
            else -> LANTERN
        }
    }
}
