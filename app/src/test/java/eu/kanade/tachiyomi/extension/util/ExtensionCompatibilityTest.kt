package eu.kanade.tachiyomi.extension.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ExtensionCompatibilityTest {

    @Test
    fun `supports current Mihon and TachiyomiX extension libraries`() {
        assertTrue(ExtensionCompatibility.isSupportedLibVersion(1.4))
        assertTrue(ExtensionCompatibility.isSupportedLibVersion(1.5))
        assertTrue(ExtensionCompatibility.isSupportedLibVersion(1.6))
        assertFalse(ExtensionCompatibility.isSupportedLibVersion(1.3))
    }

    @Test
    fun `prefers manifest extension library metadata over legacy version name`() {
        assertEquals(
            1.6,
            ExtensionCompatibility.resolveLibVersion(metadataVersion = 1.6f, versionName = "1.4.99"),
        )
    }

    @Test
    fun `falls back to the legacy version name when metadata is absent`() {
        assertEquals(
            1.4,
            ExtensionCompatibility.resolveLibVersion(metadataVersion = null, versionName = "1.4.42"),
        )
    }
}
