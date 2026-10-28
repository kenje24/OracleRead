package eu.kanade.presentation.category

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
    fun `folders without a saved style use the lantern ambience`() {
        val category = Category(id = 3, name = "Weekend", order = 0, flags = 0)

        assertEquals(FolderAmbience.LANTERN, category.folderAmbience(emptySet()))
        assertEquals(
            FolderAmbience.NONE,
            category.folderAmbience(setOf("3|1|book|none", "4|1|book|ink_tide")),
        )
    }
}
