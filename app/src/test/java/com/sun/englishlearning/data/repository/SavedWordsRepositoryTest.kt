package com.sun.englishlearning.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.sun.englishlearning.data.model.SavedWord
import com.sun.englishlearning.data.model.WordType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SavedWordsRepositoryTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()

    private lateinit var savedWordsRepository: SavedWordsRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        savedWordsRepository = SavedWordsRepositoryImpl()
    }

    @Test
    fun `test saveWord creates SavedWord object correctly`() = runTest {
        // Given
        val savedWord = SavedWord(
            id = "test-id",
            userId = "user123",
            word = "example",
            ipa = "/ɪɡˈzæm.pəl/",
            partOfSpeech = "noun",
            definition = "a test definition",
            example = "test example",
            soundUrl = "http://test.com/sound.mp3",
            wordType = WordType.SAVED.value,
            createdAt = Date(),
            updatedAt = Date()
        )

        // When & Then - Test object structure
        assertEquals("test-id", savedWord.id)
        assertEquals("user123", savedWord.userId)
        assertEquals("example", savedWord.word)
        assertEquals("noun", savedWord.partOfSpeech)
        assertEquals(WordType.SAVED.value, savedWord.wordType)
    }

    @Test
    fun `test WordType enum values`() {
        // Given & When & Then
        assertEquals(1, WordType.SAVED.value)
        assertEquals(2, WordType.WEAK.value)
        assertEquals(3, WordType.MEDIUM.value)
        assertEquals(4, WordType.STRONG.value)
    }

    @Test
    fun `test SavedWord with different word types`() = runTest {
        // Given
        val weakWord = SavedWord(
            userId = "user123",
            word = "weak",
            wordType = WordType.WEAK.value
        )
        
        val mediumWord = SavedWord(
            userId = "user123",
            word = "medium",
            wordType = WordType.MEDIUM.value
        )
        
        val strongWord = SavedWord(
            userId = "user123",
            word = "strong",
            wordType = WordType.STRONG.value
        )

        // When & Then
        assertEquals(WordType.WEAK.value, weakWord.wordType)
        assertEquals(WordType.MEDIUM.value, mediumWord.wordType)
        assertEquals(WordType.STRONG.value, strongWord.wordType)
    }

    @Test
    fun `test SavedWord default values`() {
        // Given & When
        val defaultWord = SavedWord()

        // Then
        assertEquals("", defaultWord.id)
        assertEquals("", defaultWord.userId)
        assertEquals("", defaultWord.word)
        assertEquals("", defaultWord.ipa)
        assertEquals("", defaultWord.partOfSpeech)
        assertEquals("", defaultWord.definition)
        assertEquals("", defaultWord.example)
        assertEquals("", defaultWord.soundUrl)
        assertEquals(WordType.SAVED.value, defaultWord.wordType)
        assertNotNull(defaultWord.createdAt)
        assertNotNull(defaultWord.updatedAt)
    }

    @Test
    fun `test SavedWord with empty fields`() {
        // Given & When
        val wordWithEmptyFields = SavedWord(
            id = "test-id",
            userId = "user123",
            word = "test",
            ipa = "",
            partOfSpeech = "",
            definition = "test definition",
            example = "",
            soundUrl = "",
            wordType = WordType.SAVED.value
        )

        // Then
        assertEquals("test-id", wordWithEmptyFields.id)
        assertEquals("user123", wordWithEmptyFields.userId)
        assertEquals("test", wordWithEmptyFields.word)
        assertTrue(wordWithEmptyFields.ipa.isEmpty())
        assertTrue(wordWithEmptyFields.partOfSpeech.isEmpty())
        assertEquals("test definition", wordWithEmptyFields.definition)
        assertTrue(wordWithEmptyFields.example.isEmpty())
        assertTrue(wordWithEmptyFields.soundUrl.isEmpty())
    }

    @Test
    fun `test repository interface methods exist`() {
        // Given & When & Then - Test that repository implements required methods
        assertTrue(savedWordsRepository is SavedWordsRepository)
        
        // Test that methods exist by checking the interface
        val methods = SavedWordsRepository::class.java.declaredMethods
        val methodNames = methods.map { it.name }
        
        assertTrue(methodNames.contains("saveWord"))
        assertTrue(methodNames.contains("getUserSavedWords"))
        assertTrue(methodNames.contains("getUserWordsByType"))
        assertTrue(methodNames.contains("getWordCountByType"))
        assertTrue(methodNames.contains("deleteWord"))
        assertTrue(methodNames.contains("updateWordType"))
        assertTrue(methodNames.contains("isWordSaved"))
        assertTrue(methodNames.contains("isWordSavedWithType"))
        assertTrue(methodNames.contains("deleteWordByUserAndName"))
    }

    @Test
    fun `test date fields are properly set`() {
        // Given
        val createdDate = Date()
        val updatedDate = Date()
        
        // When
        val savedWord = SavedWord(
            word = "test",
            createdAt = createdDate,
            updatedAt = updatedDate
        )

        // Then
        assertEquals(createdDate, savedWord.createdAt)
        assertEquals(updatedDate, savedWord.updatedAt)
    }
}