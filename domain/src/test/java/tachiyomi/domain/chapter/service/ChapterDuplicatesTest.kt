package tachiyomi.domain.chapter.service

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import tachiyomi.domain.chapter.model.Chapter

@Execution(ExecutionMode.CONCURRENT)
class ChapterDuplicatesTest {

    @Test
    fun `keeps one entry per known chapter number`() {
        val chapters = listOf(
            chapter(id = 1, number = 3.0, sourceOrder = 0),
            chapter(id = 2, number = 3.0, sourceOrder = 1),
            chapter(id = 3, number = 2.0, sourceOrder = 2),
            chapter(id = 4, number = 2.0, sourceOrder = 3),
            chapter(id = 5, number = 1.0, sourceOrder = 4),
        )

        chapters.withoutDuplicateChapters().map { it.id } shouldContainExactly listOf(1L, 3L, 5L)
    }

    @Test
    fun `never groups unknown or special chapter numbers`() {
        val chapters = listOf(
            chapter(id = 1, number = -1.0),
            chapter(id = 2, number = -1.0),
            chapter(id = 3, number = 0.0),
            chapter(id = 4, number = 0.0),
            chapter(id = 5, number = Double.NaN),
            chapter(id = 6, number = Double.NaN),
            chapter(id = 7, number = Double.POSITIVE_INFINITY),
            chapter(id = 8, number = Double.POSITIVE_INFINITY),
        )

        chapters.withoutDuplicateChapters().map { it.id } shouldContainExactly (1L..8L).toList()
    }

    @Test
    fun `keeps fractional chapters apart from whole chapters`() {
        val chapters = listOf(
            chapter(id = 1, number = 12.0),
            chapter(id = 2, number = 12.5),
            chapter(id = 3, number = 12.5),
            chapter(id = 4, number = 13.0),
        )

        chapters.withoutDuplicateChapters().map { it.id } shouldContainExactly listOf(1L, 2L, 4L)
    }

    @Test
    fun `prefers the copy in progress, then read, bookmarked and downloaded`() {
        val plain = chapter(id = 1, number = 5.0, sourceOrder = 0)
        val downloaded = chapter(id = 2, number = 5.0, sourceOrder = 1)
        val bookmarked = chapter(id = 3, number = 5.0, sourceOrder = 2, bookmark = true)
        val read = chapter(id = 4, number = 5.0, sourceOrder = 3, read = true)
        val inProgress = chapter(id = 5, number = 5.0, sourceOrder = 4, lastPageRead = 7)
        val isDownloaded: (Chapter) -> Boolean = { it.id == downloaded.id }

        fun best(vararg chapters: Chapter) = chapters.toList()
            .withoutDuplicateChapters(isDownloaded = isDownloaded)
            .single()
            .id

        best(plain, downloaded, bookmarked, read, inProgress) shouldBe inProgress.id
        best(plain, downloaded, bookmarked, read) shouldBe read.id
        best(plain, downloaded, bookmarked) shouldBe bookmarked.id
        best(plain, downloaded) shouldBe downloaded.id
        best(plain) shouldBe plain.id
    }

    @Test
    fun `a read copy with a leftover page count is not in progress`() {
        val readWithPage = chapter(id = 1, number = 5.0, sourceOrder = 1, read = true, lastPageRead = 3)
        val inProgress = chapter(id = 2, number = 5.0, sourceOrder = 2, lastPageRead = 3)

        listOf(readWithPage, inProgress).withoutDuplicateChapters().single().id shouldBe inProgress.id
    }

    @Test
    fun `ties fall back to source order, then id, whatever the input order`() {
        val first = chapter(id = 9, number = 5.0, sourceOrder = 0)
        val second = chapter(id = 3, number = 5.0, sourceOrder = 1)
        val sameOrder = chapter(id = 2, number = 5.0, sourceOrder = 1)

        listOf(second, first).withoutDuplicateChapters().single().id shouldBe first.id
        listOf(first, second).withoutDuplicateChapters().single().id shouldBe first.id
        listOf(second, sameOrder).withoutDuplicateChapters().single().id shouldBe sameOrder.id
        listOf(sameOrder, second).withoutDuplicateChapters().single().id shouldBe sameOrder.id
    }

    @Test
    fun `the kept chapter always wins its group`() {
        val inProgress = chapter(id = 1, number = 5.0, lastPageRead = 4)
        val opened = chapter(id = 2, number = 5.0)

        listOf(inProgress, opened).withoutDuplicateChapters(keepChapterId = opened.id).single().id shouldBe opened.id
    }

    @Test
    fun `keeps the input order so either sort direction is preserved`() {
        val ascending = listOf(
            chapter(id = 1, number = 1.0, sourceOrder = 4),
            chapter(id = 2, number = 2.0, sourceOrder = 3),
            chapter(id = 3, number = 2.0, sourceOrder = 2),
            chapter(id = 4, number = 3.0, sourceOrder = 1),
        )

        ascending.withoutDuplicateChapters().map { it.id } shouldContainExactly listOf(1L, 3L, 4L)
        ascending.asReversed().withoutDuplicateChapters().map { it.id } shouldContainExactly listOf(4L, 3L, 1L)
    }

    @Test
    fun `works on wrapped items`() {
        data class Item(val chapter: Chapter, val downloaded: Boolean)
        val items = listOf(
            Item(chapter(id = 1, number = 5.0, sourceOrder = 0), downloaded = false),
            Item(chapter(id = 2, number = 5.0, sourceOrder = 1), downloaded = true),
        )

        items.withoutDuplicateChapters(chapterOf = { it.chapter }, isDownloaded = { it.downloaded })
            .single().chapter.id shouldBe 2L
    }

    @Test
    fun `latest chapter is the first one in source order, whatever is read or listed first`() {
        val chapters = listOf(
            chapter(id = 1, number = 1.0, sourceOrder = 2, read = true),
            chapter(id = 2, number = 3.0, sourceOrder = 0, read = true),
            chapter(id = 3, number = 2.0, sourceOrder = 1),
        )

        chapters.latestBySourceOrder()?.id shouldBe 2L
        chapters.asReversed().latestBySourceOrder()?.id shouldBe 2L
        emptyList<Chapter>().latestBySourceOrder() shouldBe null
    }

    @Test
    fun `latest chapter ties on source order resolve to the highest number`() {
        val chapters = listOf(
            chapter(id = 1, number = 4.0, sourceOrder = 0),
            chapter(id = 2, number = 5.0, sourceOrder = 0),
            chapter(id = 3, number = -1.0, sourceOrder = 0),
        )

        chapters.latestBySourceOrder()?.id shouldBe 2L
    }

    private fun chapter(
        id: Long,
        number: Double,
        sourceOrder: Long = 0,
        read: Boolean = false,
        bookmark: Boolean = false,
        lastPageRead: Int = 0,
    ) = Chapter.create().copy(
        id = id,
        chapterNumber = number,
        sourceOrder = sourceOrder,
        read = read,
        bookmark = bookmark,
        lastPageRead = lastPageRead,
    )
}
