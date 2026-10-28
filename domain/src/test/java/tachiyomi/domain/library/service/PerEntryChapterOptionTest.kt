package tachiyomi.domain.library.service

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import tachiyomi.core.common.preference.InMemoryPreferenceStore

@Execution(ExecutionMode.CONCURRENT)
class PerEntryChapterOptionTest {

    @Test
    fun `hide duplicate chapters is off by default and stored apart from decimal chapters`() {
        val preferences = LibraryPreferences(InMemoryPreferenceStore())

        preferences.hideDuplicateChapters.get() shouldBe false
        preferences.hideDuplicateChapters.key() shouldBe "pref_hide_duplicate_chapters"
        preferences.duplicateChapters.isEnabled(1L) shouldBe false
        preferences.decimalChapters.isEnabled(1L) shouldBe false
    }

    @Test
    fun `follows the global setting unless the entry has an exception`() {
        fun resolve(global: Boolean, enabled: Set<String> = emptySet(), disabled: Set<String> = emptySet()) =
            PerEntryChapterOption.resolve(global, enabled, disabled, mangaId = 1L)

        resolve(global = false) shouldBe false
        resolve(global = true) shouldBe true
        resolve(global = false, enabled = setOf("1")) shouldBe true
        resolve(global = true, disabled = setOf("1")) shouldBe false
        resolve(global = true, disabled = setOf("2")) shouldBe true
    }

    @Test
    fun `toggling with the global setting off records an entry that enables it`() {
        val preferences = LibraryPreferences(InMemoryPreferenceStore())
        val option = preferences.duplicateChapters

        option.toggle(7L)
        option.isEnabled(7L) shouldBe true
        option.isEnabled(8L) shouldBe false
        preferences.hideDuplicateChaptersMangaIds.get() shouldContainExactly setOf("7")

        option.toggle(7L)
        option.isEnabled(7L) shouldBe false
        preferences.hideDuplicateChaptersMangaIds.get().shouldBeEmpty()
        preferences.showDuplicateChaptersMangaIds.get().shouldBeEmpty()
    }

    @Test
    fun `toggling with the global setting on records an entry that shows duplicates`() {
        val preferences = LibraryPreferences(InMemoryPreferenceStore())
        preferences.hideDuplicateChapters.set(true)
        val option = preferences.duplicateChapters

        option.toggle(7L)
        option.isEnabled(7L) shouldBe false
        option.isEnabled(8L) shouldBe true
        preferences.showDuplicateChaptersMangaIds.get() shouldContainExactly setOf("7")

        option.toggle(7L)
        option.isEnabled(7L) shouldBe true
        preferences.showDuplicateChaptersMangaIds.get().shouldBeEmpty()
    }

    @Test
    fun `duplicate and decimal options do not affect each other`() {
        val preferences = LibraryPreferences(InMemoryPreferenceStore())

        preferences.duplicateChapters.toggle(3L)

        preferences.duplicateChapters.isEnabled(3L) shouldBe true
        preferences.decimalChapters.isEnabled(3L) shouldBe false
        preferences.hideDecimalChaptersMangaIds.get().shouldBeEmpty()
    }
}
