package tachiyomi.domain.chapter.service

import tachiyomi.domain.chapter.model.Chapter

/**
 * OracleRead: whether chapters with this number can be told apart as copies of one another. Unknown numbers (-1),
 * 0 (which sources use for specials, extras and announcements alike) and non-finite numbers are left alone.
 */
fun isDeduplicableChapterNumber(chapterNumber: Double): Boolean {
    return chapterNumber > 0.0 && chapterNumber.isFinite()
}

/**
 * OracleRead: the list with one entry per known chapter number, in the original order. This only changes what is
 * listed, so nothing about the hidden copies is lost.
 *
 * Each number keeps, in order: [keepChapterId], a copy being read, a read copy, a bookmarked copy, a downloaded copy,
 * then the copy first in source order (lowest id on ties). Reading progress comes first so the list, Resume and the
 * reader agree on what has been read. Fractional numbers are their own number, so 12.5 is never folded into 12.
 */
fun <T> List<T>.withoutDuplicateChapters(
    chapterOf: (T) -> Chapter,
    isDownloaded: (T) -> Boolean = { false },
    keepChapterId: Long? = null,
): List<T> {
    val representativeComparator = compareByDescending<T> { chapterOf(it).id == keepChapterId }
        .thenByDescending { chapterOf(it).let { chapter -> !chapter.read && chapter.lastPageRead > 0 } }
        .thenByDescending { chapterOf(it).read }
        .thenByDescending { chapterOf(it).bookmark }
        .thenByDescending { isDownloaded(it) }
        .thenBy { chapterOf(it).sourceOrder }
        .thenBy { chapterOf(it).id }

    val representatives = HashMap<Double, T>()
    for (item in this) {
        val number = chapterOf(item).chapterNumber
        if (!isDeduplicableChapterNumber(number)) continue
        val current = representatives[number]
        if (current == null || representativeComparator.compare(item, current) < 0) {
            representatives[number] = item
        }
    }
    if (representatives.size == count { isDeduplicableChapterNumber(chapterOf(it).chapterNumber) }) return this

    return filter { item ->
        val number = chapterOf(item).chapterNumber
        !isDeduplicableChapterNumber(number) || representatives[number] === item
    }
}

/**
 * OracleRead: see the generic [withoutDuplicateChapters].
 */
fun List<Chapter>.withoutDuplicateChapters(
    isDownloaded: (Chapter) -> Boolean = { false },
    keepChapterId: Long? = null,
): List<Chapter> {
    return withoutDuplicateChapters(chapterOf = { it }, isDownloaded = isDownloaded, keepChapterId = keepChapterId)
}

/**
 * OracleRead: the newest chapter as the source lists it (sources list newest first, so the lowest
 * [Chapter.sourceOrder]), regardless of how the chapters are sorted, filtered or read. Ties go to the highest number.
 */
fun List<Chapter>.latestBySourceOrder(): Chapter? {
    return minWithOrNull(
        compareBy<Chapter> { it.sourceOrder }
            .thenByDescending { it.chapterNumber }
            .thenByDescending { it.id },
    )
}
