package eu.kanade.tachiyomi.util.chapter

import eu.kanade.tachiyomi.data.download.DownloadManager
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.chapter.service.withoutDuplicateChapters
import tachiyomi.domain.chapter.service.withoutHiddenChapters
import tachiyomi.domain.manga.model.Manga

/**
 * OracleRead: the copies the entry's chapter list shows when duplicates are hidden, so reading from elsewhere picks
 * the same ones. A queued or downloaded copy counts as downloaded, as it does in the chapter list.
 */
fun List<Chapter>.withoutDuplicateChapters(
    manga: Manga,
    downloadManager: DownloadManager,
    keepChapterId: Long? = null,
): List<Chapter> {
    return withoutDuplicateChapters(
        isDownloaded = downloadedCheck(manga, downloadManager),
        keepChapterId = keepChapterId,
    )
}

/**
 * OracleRead: the chapters the entry's chapter list shows with "hide decimal chapters" and "hide duplicate
 * chapters" applied, so the reader moves only between chapters you can see in the list.
 */
fun List<Chapter>.withoutHiddenChapters(
    manga: Manga,
    downloadManager: DownloadManager,
    hideDecimals: Boolean,
    hideDuplicates: Boolean,
    keepChapterId: Long? = null,
): List<Chapter> {
    if (!hideDecimals && !hideDuplicates) return this
    return withoutHiddenChapters(
        hideDecimals = hideDecimals,
        hideDuplicates = hideDuplicates,
        isDownloaded = if (hideDuplicates) downloadedCheck(manga, downloadManager) else { _ -> false },
        keepChapterId = keepChapterId,
    )
}

private fun downloadedCheck(manga: Manga, downloadManager: DownloadManager): (Chapter) -> Boolean {
    val queuedChapterIds = downloadManager.queueState.value.mapTo(HashSet()) { it.chapter.id }
    return {
        it.id in queuedChapterIds ||
            downloadManager.isChapterDownloaded(it.name, it.scanlator, it.url, manga.title, manga.source)
    }
}
