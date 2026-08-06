package eu.kanade.tachiyomi.ui.reader.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ReaderPageRetryStateTest {

    @Test
    fun `allows five automatic retries before giving up`() {
        val retryState = ReaderPageRetryState(maxRetries = 5)

        repeat(5) { index ->
            assertTrue(retryState.recordFailure())
            assertEquals(index + 1, retryState.retryCount.value)
        }

        assertFalse(retryState.recordFailure())
        assertEquals(5, retryState.retryCount.value)
    }

    @Test
    fun `reset clears retry progress for a new manual attempt`() {
        val retryState = ReaderPageRetryState(maxRetries = 5)

        retryState.recordFailure()
        retryState.recordFailure()
        retryState.reset()

        assertEquals(0, retryState.retryCount.value)
        assertTrue(retryState.recordFailure())
        assertEquals(1, retryState.retryCount.value)
    }
}
