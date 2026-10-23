package mihon.core.migration.migrations

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logcat.LogPriority
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext
import mihon.domain.extension.repository.ExtensionStoreRepository
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore
import tachiyomi.core.common.util.system.logcat

/**
 * Seeds OracleRead's default extension stores once, including on fresh installs. A store the user removes
 * afterwards is not added back.
 */
@Inject
@ContributesIntoSet(AppScope::class)
class DefaultExtensionStoresMigration(
    private val repository: ExtensionStoreRepository,
    private val preferenceStore: PreferenceStore,
) : Migration {
    override val version: Float = Migration.ALWAYS

    override suspend fun invoke(migrationContext: MigrationContext): Boolean = withContext(Dispatchers.IO) {
        val seeded = preferenceStore.getBoolean(Preference.appStateKey("default_extension_stores_seeded"), false)
        if (seeded.get()) return@withContext true

        val existing = repository.getAll().map { it.indexUrl }
        DEFAULT_STORES.forEach { (name, baseUrl) ->
            if (existing.any { it.startsWith(baseUrl) }) return@forEach
            val indexUrl = "$baseUrl/repo.json"
            try {
                // Falls back to a placeholder row when offline; the next store refresh fills in its metadata
                repository.insert(indexUrl).onFailure { repository.insertFromPreference(indexUrl, name) }
            } catch (e: Exception) {
                logcat(LogPriority.ERROR, e) { "Error adding default extension store $indexUrl" }
            }
        }
        seeded.set(true)
        return@withContext true
    }

    private companion object {
        val DEFAULT_STORES = listOf(
            "Keiyoushi" to "https://raw.githubusercontent.com/keiyoushi/extensions/repo",
            "Yūzōnō" to "https://raw.githubusercontent.com/yuzono/manga-repo/repo",
        )
    }
}
