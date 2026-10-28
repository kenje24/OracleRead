package tachiyomi.domain.library.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.getAndSet

/**
 * OracleRead: a chapter list option with a global setting that each entry can override in either direction. With
 * the global setting on, [disabledIds] holds the entries it is turned off for; with it off, [enabledIds] holds the
 * entries it is turned on for.
 */
class PerEntryChapterOption(
    val global: Preference<Boolean>,
    private val enabledIds: Preference<Set<String>>,
    private val disabledIds: Preference<Set<String>>,
) {
    fun isEnabled(mangaId: Long): Boolean {
        return resolve(global.get(), enabledIds.get(), disabledIds.get(), mangaId)
    }

    fun changes(mangaId: Long): Flow<Boolean> {
        return combine(global.changes(), enabledIds.changes(), disabledIds.changes()) { global, enabled, disabled ->
            resolve(global, enabled, disabled, mangaId)
        }
            .distinctUntilChanged()
    }

    fun toggle(mangaId: Long) {
        val enabled = isEnabled(mangaId)
        val key = mangaId.toString()
        if (global.get()) {
            disabledIds.getAndSet { ids -> if (enabled) ids + key else ids - key }
        } else {
            enabledIds.getAndSet { ids -> if (enabled) ids - key else ids + key }
        }
    }

    companion object {
        fun resolve(globalEnabled: Boolean, enabledIds: Set<String>, disabledIds: Set<String>, mangaId: Long): Boolean {
            val key = mangaId.toString()
            return if (globalEnabled) key !in disabledIds else key in enabledIds
        }
    }
}
