package eu.kanade.tachiyomi.ui.reader.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class ReaderPageRetryState(
    val maxRetries: Int = DEFAULT_MAX_RETRIES,
) {
    private val _retryCount = MutableStateFlow(0)
    val retryCount = _retryCount.asStateFlow()

    fun recordFailure(): Boolean {
        if (_retryCount.value >= maxRetries) return false
        _retryCount.value += 1
        return true
    }

    fun reset() {
        _retryCount.value = 0
    }

    companion object {
        const val DEFAULT_MAX_RETRIES = 5
    }
}
