package eu.kanade.tachiyomi.ui.manga

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MangaRefreshPolicyTest {

    @Test
    fun `uninitialized manga refreshes existing chapters to restore source metadata`() {
        assertTrue(shouldRefreshChapters(mangaInitialized = false, chapterCount = 12))
    }

    @Test
    fun `initialized manga with chapters does not refresh automatically`() {
        assertFalse(shouldRefreshChapters(mangaInitialized = true, chapterCount = 12))
    }
}
