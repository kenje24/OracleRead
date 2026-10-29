package eu.kanade.presentation.theme

import eu.kanade.domain.ui.model.AppTheme
import eu.kanade.presentation.theme.colorscheme.OracleColorScheme
import eu.kanade.tachiyomi.ui.base.delegate.themeResources
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class AppThemeCoverageTest {

    // Monet takes its colors from the wallpaper, and the deprecated themes are hidden from the picker
    private val selectableThemes = AppTheme.entries.filter { it.titleRes != null && it != AppTheme.MONET }

    private val ownPaletteThemes = listOf(
        AppTheme.KOI_GARDEN,
        AppTheme.ABYSSAL_BLOOM,
        AppTheme.PAPER_CRANES,
        AppTheme.TEMPEST,
        AppTheme.NEON_ALLEY,
        AppTheme.SCREENTONE,
    )

    @Test
    fun `every selectable theme has Compose colors and a View style`() {
        assertEquals(emptyList<AppTheme>(), selectableThemes.filterNot { it in colorSchemes })
        // Default uses the base Theme.Tachiyomi style itself
        assertEquals(
            emptyList<AppTheme>(),
            selectableThemes.filterNot { it in themeResources || it == AppTheme.DEFAULT },
        )
    }

    @Test
    fun `the new themes have palettes of their own`() {
        val schemes = ownPaletteThemes.map { colorSchemes.getValue(it) }
        assertEquals(schemes.size, schemes.toSet().size)
        schemes.forEach { assertNotEquals(OracleColorScheme, it) }
        val styles = ownPaletteThemes.map { themeResources.getValue(it) }
        assertEquals(styles.size, styles.toSet().size)
    }
}
