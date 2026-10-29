package eu.kanade.presentation.category

import eu.kanade.domain.ui.model.AppTheme
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tachiyomi.domain.category.model.Category

class FolderStyleTest {

    private val legacyKeys = listOf(
        "lantern",
        "fire_spirits",
        "moon_wisps",
        "sea_lanterns",
        "sakura_drift",
        "aurora_motes",
        "ink_rain",
        "star_dust",
    )

    @Test
    fun `saved folders keep their ambience and round-trip unchanged`() {
        legacyKeys.forEach { key ->
            val saved = "7|${0xFFFF6B35}|star|$key"
            val style = FolderStyle.parse(saved)!!

            assertEquals(key, style.theme)
            assertEquals(key, style.ambience.key)
            assertEquals(saved, style.serialize())
        }
    }

    @Test
    fun `styles saved before ambiences existed fall back to lantern`() {
        val style = FolderStyle.parse("7|${0xFFFF6B35}|book")!!

        assertEquals(FolderAmbience.LANTERN, style.ambience)
        assertEquals("book", style.icon)
    }

    @Test
    fun `unknown ambience keys fall back to lantern`() {
        assertEquals(FolderAmbience.LANTERN, FolderStyle.parse("7|1|book|from_the_future")!!.ambience)
    }

    @Test
    fun `none is stored as its own key`() {
        val style = FolderStyle(categoryId = 7, color = 1, icon = "book", theme = FolderAmbience.NONE.key)

        assertEquals("7|1|book|none", style.serialize())
        assertEquals(FolderAmbience.NONE, FolderStyle.parse(style.serialize())!!.ambience)
    }

    @Test
    fun `ambience keys are unique and the previous ones are kept`() {
        val keys = FolderAmbience.entries.map { it.key }

        assertEquals(keys.size, keys.toSet().size)
        assertTrue(keys.containsAll(legacyKeys))
        assertTrue(FolderAmbience.entries.count { it.key !in legacyKeys && it != FolderAmbience.NONE } >= 4)
    }

    @Test
    fun `tapping the selected background again turns it off`() {
        assertEquals("none", FolderAmbience.toggledKey(currentKey = "ink_tide", tappedKey = "ink_tide"))
        assertEquals("koi_pond", FolderAmbience.toggledKey(currentKey = "ink_tide", tappedKey = "koi_pond"))
        assertEquals("ink_tide", FolderAmbience.toggledKey(currentKey = "none", tappedKey = "ink_tide"))
        assertEquals("none", FolderAmbience.toggledKey(currentKey = "none", tappedKey = "none"))
    }

    @Test
    fun `every app theme with its own effect opens on that effect`() {
        assertEquals(FolderAmbience.KOI_POND, FolderAmbience.forAppTheme(AppTheme.KOI_GARDEN))
        assertEquals(FolderAmbience.JELLYFISH, FolderAmbience.forAppTheme(AppTheme.ABYSSAL_BLOOM))
        assertEquals(FolderAmbience.PAPER_CRANES, FolderAmbience.forAppTheme(AppTheme.PAPER_CRANES))
        assertEquals(FolderAmbience.THUNDERSTORM, FolderAmbience.forAppTheme(AppTheme.TEMPEST))
        assertEquals(FolderAmbience.GLYPH_RAIN, FolderAmbience.forAppTheme(AppTheme.NEON_ALLEY))
        assertEquals(FolderAmbience.SCREENTONE, FolderAmbience.forAppTheme(AppTheme.SCREENTONE))
        assertEquals(FolderAmbience.LANTERN, FolderAmbience.forAppTheme(AppTheme.ORACLE))
    }

    @Test
    fun `folders without a saved style use the lantern ambience`() {
        val category = Category(id = 3, name = "Weekend", order = 0, flags = 0)

        assertEquals(FolderAmbience.LANTERN, category.folderAmbience(emptySet()))
        assertEquals(FolderAmbience.KOI_POND, category.folderAmbience(emptySet(), FolderAmbience.KOI_POND))
        assertEquals("koi_pond", category.folderStyle(emptySet(), FolderAmbience.KOI_POND).theme)
        assertEquals(
            FolderAmbience.NONE,
            category.folderAmbience(setOf("3|1|book|none", "4|1|book|ink_tide"), FolderAmbience.KOI_POND),
        )
        assertEquals(
            FolderAmbience.NONE,
            category.folderAmbience(setOf("3|1|book|none", "4|1|book|ink_tide")),
        )
    }
}
