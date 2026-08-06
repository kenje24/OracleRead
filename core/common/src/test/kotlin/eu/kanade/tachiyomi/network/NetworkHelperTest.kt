package eu.kanade.tachiyomi.network

import android.content.Context
import android.webkit.CookieManager
import androidx.core.content.ContextCompat
import eu.kanade.tachiyomi.network.interceptor.IgnoreGzipInterceptor
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class NetworkHelperTest {

    @TempDir
    lateinit var cacheDir: File

    @Test
    fun `default client does not expose legacy gzip interceptor to extensions`() {
        mockkStatic(CookieManager::class)
        mockkStatic(ContextCompat::class)
        try {
            every { CookieManager.getInstance() } returns mockk(relaxed = true)
            val context = mockk<Context> {
                every { this@mockk.cacheDir } returns this@NetworkHelperTest.cacheDir
            }
            every { ContextCompat.getMainExecutor(context) } returns java.util.concurrent.Executor(Runnable::run)
            val preferences = mockk<NetworkPreferences> {
                every { verboseLogging.get() } returns false
                every { dohProvider.get() } returns -1
                every { defaultUserAgent.get() } returns "OracleRead test"
            }
            val helper = NetworkHelper(
                context = context,
                preferences = preferences,
                scope = CoroutineScope(SupervisorJob()),
            )

            assertFalse(
                helper.client.networkInterceptors.any { it is IgnoreGzipInterceptor },
                "IgnoreGzipInterceptor must not be present in the default client",
            )
        } finally {
            unmockkStatic(ContextCompat::class)
            unmockkStatic(CookieManager::class)
        }
    }
}
