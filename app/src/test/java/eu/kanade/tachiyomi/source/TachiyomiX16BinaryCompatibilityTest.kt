package eu.kanade.tachiyomi.source

import eu.kanade.domain.chapter.model.toSChapter
import eu.kanade.domain.manga.model.toSManga
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.SMangaUpdate
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import mihon.domain.manga.model.toDomainManga
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import tachiyomi.data.MemoColumnAdapter
import tachiyomi.domain.chapter.model.Chapter

class TachiyomiX16BinaryCompatibilityTest {

    @Test
    fun `SManga exposes the TachiyomiX 1_6 memo getter`() {
        val getter = SManga::class.java.getMethod("getMemo")
        val setter = SManga::class.java.getMethod("setMemo", JsonObject::class.java)

        assertEquals(JsonObject::class.java, getter.returnType)
        assertEquals(Void.TYPE, setter.returnType)
        assertNotNull(getter.invoke(SManga.create()))
    }

    @Test
    fun `SChapter exposes the TachiyomiX 1_6 memo getter`() {
        val getter = SChapter::class.java.getMethod("getMemo")
        val setter = SChapter::class.java.getMethod("setMemo", JsonObject::class.java)

        assertEquals(JsonObject::class.java, getter.returnType)
        assertEquals(Void.TYPE, setter.returnType)
        assertNotNull(getter.invoke(SChapter.create()))
    }

    @Test
    fun `Source exposes the TachiyomiX 1_6 combined update method`() {
        val method = Source::class.java.getMethod(
            "getMangaUpdate",
            SManga::class.java,
            List::class.java,
            Boolean::class.javaPrimitiveType,
            Boolean::class.javaPrimitiveType,
            kotlin.coroutines.Continuation::class.java,
        )

        assertEquals(Object::class.java, method.returnType)
    }

    @Test
    fun `SMangaUpdate supports partial updates through its default constructor`() {
        val update = SMangaUpdate::class.java.getDeclaredConstructor().newInstance()

        assertNull(update.manga)
        assertNull(update.chapters)
    }

    @Test
    fun `combined update falls back to legacy split source methods`() = runBlocking {
        val manga = SManga.create().apply {
            url = "/series"
            title = "Before"
        }
        val source = object : Source {
            override val id = 1L
            override val name = "Legacy"

            override suspend fun getMangaDetails(manga: SManga): SManga = manga.apply { title = "After" }

            override suspend fun getChapterList(manga: SManga): List<SChapter> = listOf(
                SChapter.create().apply {
                    url = "/chapter-1"
                    name = "Chapter 1"
                },
            )

            override suspend fun getPageList(chapter: SChapter): List<Page> = emptyList()
        }

        val update = source.getMangaUpdate(manga, emptyList(), fetchDetails = true, fetchChapters = true)

        assertEquals("After", update.manga?.title)
        assertEquals(listOf("Chapter 1"), update.chapters?.map { it.name })
    }

    @Test
    fun `combined update dispatches to a modern extension override`() = runBlocking {
        var invoked = false
        val source = object : Source {
            override val id = 2L
            override val name = "Modern"

            override suspend fun getMangaUpdate(
                manga: SManga,
                chapters: List<SChapter>,
                fetchDetails: Boolean,
                fetchChapters: Boolean,
            ): SMangaUpdate {
                invoked = true
                return SMangaUpdate(manga, chapters)
            }

            override suspend fun getPageList(chapter: SChapter): List<Page> = emptyList()
        }

        source.getMangaUpdate(SManga.create(), emptyList(), fetchDetails = true, fetchChapters = true)

        assertEquals(true, invoked)
    }

    @Test
    fun `manga memo survives source domain database and source round trips`() {
        val memo = buildJsonObject { put("slug", "asura-series") }
        val sourceManga = SManga.create().apply {
            url = "/series"
            title = "Series"
            this.memo = memo
        }

        val domainManga = sourceManga.toDomainManga(sourceId = 42L)
        val persistedMemo = MemoColumnAdapter.decode(MemoColumnAdapter.encode(domainManga.memo))

        assertEquals(memo, domainManga.memo)
        assertEquals(memo, persistedMemo)
        assertEquals(memo, domainManga.toSManga().memo)
    }

    @Test
    fun `chapter memo reaches reader and downloader source models`() {
        val memo = buildJsonObject { put("token", "chapter-token") }
        val chapter = Chapter.create().copy(
            url = "/chapter-1",
            name = "Chapter 1",
            memo = memo,
        )

        assertEquals(memo, chapter.toSChapter().memo)
        assertEquals(memo, MemoColumnAdapter.decode(MemoColumnAdapter.encode(chapter.memo)))
    }
}
