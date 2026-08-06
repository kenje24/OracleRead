package mihon.data.extension.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class NetworkLegacyExtensionRepoTest {

    @Test
    fun `repo metadata redirects to the v2 catalog when index v2 is present`() {
        val repository = NetworkLegacyExtensionRepo(
            indexV2 = "https://example.invalid/index.pb",
            meta = NetworkLegacyExtensionRepo.Meta(
                name = "Example",
                shortName = null,
                website = "https://example.invalid",
                signingKeyFingerprint = "fingerprint",
            ),
        )

        val store = repository.toExtensionStore("https://example.invalid/index.min.json")

        assertEquals("https://example.invalid/index.pb", store.indexUrl)
        assertFalse(store.isLegacy)
    }
}
