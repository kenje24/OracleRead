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
    ;

    companion object {
        val DEFAULT = LANTERN

        fun fromKey(key: String?): FolderAmbience? = entries.firstOrNull { it.key == key }

        /** The ambience shown when there's no folder to take one from. */
        fun forAppTheme(appTheme: AppTheme): FolderAmbience = when (appTheme) {
            AppTheme.FIRE_SPIRITS -> FIRE_SPIRITS
            AppTheme.MOON_WISPS -> MOON_WISPS
            AppTheme.SEA_LANTERNS -> SEA_LANTERNS
            AppTheme.SAKURA_DRIFT -> SAKURA_DRIFT
            AppTheme.AURORA_MOTES -> AURORA_MOTES
            AppTheme.INK_RAIN -> INK_RAIN
            AppTheme.STAR_DUST -> STAR_DUST
            else -> LANTERN
        }
    }
}
