package eu.kanade.presentation.oracle

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OracleBackdropStateTest {

    @Test
    fun `stays calm until every request is released`() {
        val state = OracleBackdropState()
        assertFalse(state.isCalm)

        val first = state.requestCalm()
        val second = state.requestCalm()
        assertTrue(state.isCalm)

        first()
        assertTrue(state.isCalm)
        second()
        assertFalse(state.isCalm)
    }

    @Test
    fun `releasing a request twice only counts once`() {
        val state = OracleBackdropState()
        val first = state.requestCalm()
        state.requestCalm()

        first()
        first()

        assertTrue(state.isCalm)
    }
}
