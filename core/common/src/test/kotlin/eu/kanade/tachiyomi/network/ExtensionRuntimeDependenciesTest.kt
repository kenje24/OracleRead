package eu.kanade.tachiyomi.network

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class ExtensionRuntimeDependenciesTest {

    @Test
    fun `host packages zstd required by extension lib 1_6`() {
        assertDoesNotThrow(
            { Class.forName("okhttp3.zstd.Zstd") },
            "TachiyomiX 1.6 extensions require okhttp-zstd from the host app",
        )
    }
}
