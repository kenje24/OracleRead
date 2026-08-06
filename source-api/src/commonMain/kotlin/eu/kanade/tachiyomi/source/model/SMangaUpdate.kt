package eu.kanade.tachiyomi.source.model

@Suppress("UNUSED")
data class SMangaUpdate(
    val manga: SManga? = null,
    val chapters: List<SChapter>? = null,
)
