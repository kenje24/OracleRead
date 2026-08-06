package eu.kanade.tachiyomi.ui.manga

internal fun shouldRefreshChapters(
    mangaInitialized: Boolean,
    chapterCount: Int,
): Boolean {
    return !mangaInitialized || chapterCount == 0
}
