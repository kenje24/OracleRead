package eu.kanade.tachiyomi.ui.manga

import eu.kanade.domain.manga.model.downloadedFilter
import eu.kanade.tachiyomi.data.download.model.Download
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tachiyomi.core.common.preference.TriState
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.model.StubSource

class MangaStateChaptersTest {

    // The download filter also reads the global downloaded-only setting through the app graph
    @BeforeEach
    fun setUp() {
        mockkStatic(MANGA_EXTENSIONS)
        every { any<Manga>().downloadedFilter } returns TriState.DISABLED
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(MANGA_EXTENSIONS)
    }

    @Test
    fun `duplicates stay listed unless hidden`() {
        val state = state(
            chapters = listOf(
                item(id = 1, number = 2.0, sourceOrder = 0),
                item(id = 2, number = 2.0, sourceOrder = 1),
                item(id = 3, number = 1.0, sourceOrder = 2),
            ),
        )

        assertEquals(listOf(1L, 2L, 3L), state.processedChapters.map { it.id })
        assertEquals(listOf(1L, 3L), state.copy(hideDuplicateChapters = true).processedChapters.map { it.id })
    }

    @Test
    fun `hidden duplicates keep the downloaded copy and every unknown number`() {
        val state = state(
            chapters = listOf(
                item(id = 1, number = 2.0, sourceOrder = 0),
                item(id = 2, number = 2.0, sourceOrder = 1, downloadState = Download.State.DOWNLOADED),
                item(id = 3, number = -1.0, sourceOrder = 2),
                item(id = 4, number = -1.0, sourceOrder = 3),
            ),
            hideDuplicateChapters = true,
        )

        assertEquals(listOf(2L, 3L, 4L), state.processedChapters.map { it.id })
    }

    @Test
    fun `duplicates are collapsed before the unread filter so a read chapter does not resurface`() {
        val state = state(
            chapters = listOf(
                item(id = 1, number = 2.0, sourceOrder = 0, read = true),
                item(id = 2, number = 2.0, sourceOrder = 1),
                item(id = 3, number = 1.0, sourceOrder = 2),
            ),
            chapterFlags = Manga.CHAPTER_SHOW_UNREAD,
            hideDuplicateChapters = true,
        )

        assertEquals(listOf(3L), state.processedChapters.map { it.id })
    }

    @Test
    fun `hidden duplicates follow either sort direction`() {
        val chapters = listOf(
            item(id = 1, number = 1.0, sourceOrder = 3),
            item(id = 2, number = 2.0, sourceOrder = 2),
            item(id = 3, number = 2.0, sourceOrder = 1),
            item(id = 4, number = 3.0, sourceOrder = 0),
        )
        val descending = state(
            chapters = chapters,
            chapterFlags = Manga.CHAPTER_SORTING_NUMBER or Manga.CHAPTER_SORT_DESC,
            hideDuplicateChapters = true,
        )
        val ascending = descending.copy(
            manga = descending.manga.copy(chapterFlags = Manga.CHAPTER_SORTING_NUMBER or Manga.CHAPTER_SORT_ASC),
        )

        assertEquals(listOf(4L, 3L, 1L), descending.processedChapters.map { it.id })
        assertEquals(listOf(1L, 3L, 4L), ascending.processedChapters.map { it.id })
    }

    @Test
    fun `hiding decimals and duplicates together keeps whole chapters only once`() {
        val state = state(
            chapters = listOf(
                item(id = 1, number = 2.5, sourceOrder = 0),
                item(id = 2, number = 2.0, sourceOrder = 1),
                item(id = 3, number = 2.0, sourceOrder = 2),
            ),
            hideDuplicateChapters = true,
        )

        assertEquals(listOf(1L, 2L), state.processedChapters.map { it.id })
        assertEquals(listOf(2L), state.copy(hideDecimalChapters = true).processedChapters.map { it.id })
    }

    @Test
    fun `resume picks the first unread chapter as before when duplicates are shown`() {
        val state = state(
            chapters = listOf(
                item(id = 1, number = 2.0, sourceOrder = 0),
                item(id = 2, number = 1.0, sourceOrder = 1),
                item(id = 3, number = 1.0, sourceOrder = 2, read = true),
            ),
        )

        assertEquals(2L, state.nextUnreadChapter?.id)
    }

    @Test
    fun `resume skips a chapter whose shown copy has been read`() {
        val state = state(
            chapters = listOf(
                item(id = 1, number = 2.0, sourceOrder = 0),
                item(id = 2, number = 1.0, sourceOrder = 1),
                item(id = 3, number = 1.0, sourceOrder = 2, read = true),
            ),
            hideDuplicateChapters = true,
        )

        assertEquals(1L, state.nextUnreadChapter?.id)
    }

    @Test
    fun `latest opens the newest chapter in source order whatever the sorting, filters and read state`() {
        val state = state(
            chapters = listOf(
                item(id = 1, number = 1.0, sourceOrder = 2),
                item(id = 2, number = 3.0, sourceOrder = 0, read = true),
                item(id = 3, number = 2.0, sourceOrder = 1),
            ),
            chapterFlags = Manga.CHAPTER_SORTING_NUMBER or Manga.CHAPTER_SORT_ASC or Manga.CHAPTER_SHOW_UNREAD,
        )

        assertEquals(listOf(1L, 3L), state.processedChapters.map { it.id })
        assertEquals(2L, state.latestChapter?.id)
        assertEquals(2L, state.copy(hideDecimalChapters = true).latestChapter?.id)
    }

    @Test
    fun `latest opens the shown copy of the newest chapter when duplicates are hidden`() {
        val state = state(
            chapters = listOf(
                item(id = 1, number = 3.0, sourceOrder = 0),
                item(id = 2, number = 3.0, sourceOrder = 1, downloadState = Download.State.DOWNLOADED),
                item(id = 3, number = 2.0, sourceOrder = 2),
            ),
        )

        assertEquals(1L, state.latestChapter?.id)
        assertEquals(2L, state.copy(hideDuplicateChapters = true).latestChapter?.id)
    }

    @Test
    fun `latest is absent without chapters`() {
        assertNull(state(chapters = emptyList()).latestChapter)
    }

    private fun state(
        chapters: List<ChapterList.Item>,
        chapterFlags: Long = Manga.CHAPTER_SORTING_SOURCE or Manga.CHAPTER_SORT_DESC,
        hideDuplicateChapters: Boolean = false,
    ) = MangaViewModel.State.Success(
        manga = Manga.create().copy(id = 1, source = 1, chapterFlags = chapterFlags),
        source = StubSource(id = 1, lang = "en", name = "Test"),
        isFromSource = false,
        chapters = chapters,
        availableScanlators = emptySet(),
        excludedScanlators = emptySet(),
        hideMissingChapters = true,
        hideDuplicateChapters = hideDuplicateChapters,
    )

    private fun item(
        id: Long,
        number: Double,
        sourceOrder: Long,
        read: Boolean = false,
        downloadState: Download.State = Download.State.NOT_DOWNLOADED,
    ) = ChapterList.Item(
        chapter = Chapter.create().copy(
            id = id,
            mangaId = 1,
            chapterNumber = number,
            sourceOrder = sourceOrder,
            read = read,
        ),
        downloadState = downloadState,
    )

    private companion object {
        const val MANGA_EXTENSIONS = "eu.kanade.domain.manga.model.MangaKt"
    }
}
