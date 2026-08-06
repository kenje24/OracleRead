package eu.kanade.tachiyomi.source

import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.SMangaUpdate
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

class MangaUpdateCoordinator {

    private val mangaLocks = ConcurrentHashMap<MangaKey, Mutex>()

    suspend fun update(
        source: Source,
        manga: SManga,
        chapters: List<SChapter>,
        fetchDetails: Boolean,
        fetchChapters: Boolean,
    ): SMangaUpdate {
        val key = MangaKey(source.id, manga.url)
        val lock = mangaLocks.computeIfAbsent(key) { Mutex() }
        return lock.withLock {
            source.getMangaUpdate(
                manga = manga,
                chapters = chapters,
                fetchDetails = fetchDetails,
                fetchChapters = fetchChapters,
            )
        }
    }

    private data class MangaKey(
        val sourceId: Long,
        val mangaUrl: String,
    )
}
