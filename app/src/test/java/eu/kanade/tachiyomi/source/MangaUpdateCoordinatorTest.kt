package eu.kanade.tachiyomi.source

import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.SMangaUpdate
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class MangaUpdateCoordinatorTest {

    @Test
    fun `concurrent updates for the same manga are serialized`() = runBlocking {
        val firstRequestEntered = CompletableDeferred<Unit>()
        val releaseFirstRequest = CompletableDeferred<Unit>()
        val running = AtomicBoolean(false)
        val invocationCount = AtomicInteger(0)
        val source = object : Source {
            override val id = 11L
            override val name = "Guarded source"
            override val lang = "en"

            override suspend fun getMangaUpdate(
                manga: SManga,
                chapters: List<SChapter>,
                fetchDetails: Boolean,
                fetchChapters: Boolean,
            ): SMangaUpdate {
                check(running.compareAndSet(false, true)) {
                    "getMangaUpdate must not be called concurrently for the same manga"
                }
                return try {
                    if (invocationCount.incrementAndGet() == 1) {
                        firstRequestEntered.complete(Unit)
                        releaseFirstRequest.await()
                    }
                    SMangaUpdate(manga = manga, chapters = chapters)
                } finally {
                    running.set(false)
                }
            }

            override suspend fun getPageList(chapter: SChapter): List<Page> = emptyList()
        }
        val coordinator = MangaUpdateCoordinator()
        val manga = SManga.create().apply {
            url = "/same-manga"
            title = "Same manga"
        }

        val first = async {
            coordinator.update(source, manga, emptyList(), fetchDetails = true, fetchChapters = true)
        }
        firstRequestEntered.await()
        val second = async {
            coordinator.update(source, manga, emptyList(), fetchDetails = true, fetchChapters = true)
        }
        releaseFirstRequest.complete(Unit)

        val results = listOf(first, second).awaitAll()

        assertEquals(2, invocationCount.get())
        assertEquals(listOf("/same-manga", "/same-manga"), results.map { it.manga?.url })
    }

    @Test
    fun `different manga update concurrently`() = runBlocking {
        val bothRequestsEntered = CompletableDeferred<Unit>()
        val releaseRequests = CompletableDeferred<Unit>()
        val runningCount = AtomicInteger(0)
        val source = object : Source {
            override val id = 12L
            override val name = "Parallel source"
            override val lang = "en"

            override suspend fun getMangaUpdate(
                manga: SManga,
                chapters: List<SChapter>,
                fetchDetails: Boolean,
                fetchChapters: Boolean,
            ): SMangaUpdate {
                if (runningCount.incrementAndGet() == 2) {
                    bothRequestsEntered.complete(Unit)
                }
                return try {
                    releaseRequests.await()
                    SMangaUpdate(manga = manga, chapters = chapters)
                } finally {
                    runningCount.decrementAndGet()
                }
            }

            override suspend fun getPageList(chapter: SChapter): List<Page> = emptyList()
        }
        val coordinator = MangaUpdateCoordinator()
        val firstManga = SManga.create().apply { url = "/first" }
        val secondManga = SManga.create().apply { url = "/second" }

        val updates = listOf(firstManga, secondManga).map { manga ->
            async {
                coordinator.update(source, manga, emptyList(), fetchDetails = true, fetchChapters = true)
            }
        }

        withTimeout(1_000) { bothRequestsEntered.await() }
        releaseRequests.complete(Unit)

        assertEquals(listOf("/first", "/second"), updates.awaitAll().map { it.manga?.url })
    }
}
