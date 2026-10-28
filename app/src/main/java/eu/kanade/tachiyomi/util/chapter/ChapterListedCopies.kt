package eu.kanade.tachiyomi.util.chapter

import eu.kanade.tachiyomi.data.download.DownloadManager
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.chapter.service.withoutDuplicateChapters
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
    val queuedChapterIds = downloadManager.queueState.value.mapTo(HashSet()) { it.chapter.id }
    return withoutDuplicateChapters(
        isDownloaded = {
            it.id in queuedChapterIds ||
                downloadManager.isChapterDownloaded(it.name, it.scanlator, it.url, manga.title, manga.source)
        },
        keepChapterId = keepChapterId,
    )
}
