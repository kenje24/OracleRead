package tachiyomi.domain.history.interactor

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.history.model.History
import tachiyomi.domain.history.model.HistoryWithRelations
import tachiyomi.domain.history.repository.HistoryRepository

@Inject
class GetHistory(
    private val repository: HistoryRepository,
) {

    suspend fun await(mangaId: Long): List<History> {
        return repository.getHistoryByMangaId(mangaId)
    }

    fun subscribe(query: String): Flow<List<HistoryWithRelations>> {
        return repository.getHistory(query)
    }

    /** OracleRead: the chapter of [mangaId] read most recently, or null before anything is read. */
    fun subscribeLastReadChapterId(mangaId: Long): Flow<Long?> {
        return repository.getLastReadChapterId(mangaId)
    }
}
