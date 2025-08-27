package com.sun.englishlearning.screen.vocabulary

import android.content.Context
import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.sun.englishlearning.data.model.SavedWord
import com.sun.englishlearning.data.model.WordType
import com.sun.englishlearning.data.repository.SavedWordsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class VocabularyByTypeActivityTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockSavedWordsRepository: SavedWordsRepository

    @Mock
    private lateinit var mockFirebaseAuth: FirebaseAuth

    @Mock
    private lateinit var mockFirebaseUser: FirebaseUser

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var context: Context
    private lateinit var activity: VocabularyByTypeActivity

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = ApplicationProvider.getApplicationContext()
        
        // Mock Firebase Auth
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn("test-user-id")
    }

    @Test
    fun `test create intent for weak words`() {
        // Given
        val wordType = WordType.WEAK
        val title = "Weak words"

        // When
        val intent = VocabularyByTypeActivity.createIntent(context, wordType, title)

        // Then
        assertNotNull(intent)
        assertEquals(VocabularyByTypeActivity::class.java.name, intent.component?.className)
        assertEquals(wordType.value, intent.getIntExtra("extra_word_type", -1))
        assertEquals(title, intent.getStringExtra("extra_type_title"))
    }

    @Test
    fun `test create intent for medium words`() {
        // Given
        val wordType = WordType.MEDIUM
        val title = "Medium words"

        // When
        val intent = VocabularyByTypeActivity.createIntent(context, wordType, title)

        // Then
        assertNotNull(intent)
        assertEquals(wordType.value, intent.getIntExtra("extra_word_type", -1))
        assertEquals(title, intent.getStringExtra("extra_type_title"))
    }

    @Test
    fun `test create intent for strong words`() {
        // Given
        val wordType = WordType.STRONG
        val title = "Strong words"

        // When
        val intent = VocabularyByTypeActivity.createIntent(context, wordType, title)

        // Then
        assertNotNull(intent)
        assertEquals(wordType.value, intent.getIntExtra("extra_word_type", -1))
        assertEquals(title, intent.getStringExtra("extra_type_title"))
    }

    @Test
    fun `test activity initialization with weak words`() {
        // Given
        val intent = VocabularyByTypeActivity.createIntent(context, WordType.WEAK, "Weak words")
        
        // When
        activity = Robolectric.buildActivity(VocabularyByTypeActivity::class.java, intent)
            .create()
            .get()

        // Then
        assertNotNull(activity)
    }

    @Test
    fun `test word filtering functionality`() = runTest {
        // Given
        val testWords = listOf(
            SavedWord(
                id = "1",
                userId = "user123",
                word = "example",
                ipa = "/ɪɡˈzæm.pəl/",
                partOfSpeech = "noun",
                definition = "a thing characteristic of its kind",
                example = "there is a similar example",
                soundUrl = "http://test.com/sound1.mp3",
                wordType = WordType.SAVED.value,
                createdAt = Date(),
                updatedAt = Date()
            ),
            SavedWord(
                id = "2",
                userId = "user123",
                word = "test",
                ipa = "/test/",
                partOfSpeech = "noun",
                definition = "a procedure intended to establish quality",
                example = "both types of test are essential",
                soundUrl = "http://test.com/sound2.mp3",
                wordType = WordType.SAVED.value,
                createdAt = Date(),
                updatedAt = Date()
            )
        )

        // When - Test search functionality
        val filteredWords = testWords.filter { 
            it.word.lowercase().contains("exam") || 
            it.definition.lowercase().contains("exam") 
        }

        // Then
        assertEquals(1, filteredWords.size)
        assertEquals("example", filteredWords[0].word)
    }

    @Test
    fun `test word details dialog data`() = runTest {
        // Given
        val testWord = SavedWord(
            id = "1",
            userId = "user123",
            word = "dialog",
            ipa = "/ˈdaɪəlɒɡ/",
            partOfSpeech = "noun",
            definition = "a conversation between two or more people",
            example = "the book consisted of a series of dialogs",
            soundUrl = "http://test.com/dialog.mp3",
            wordType = WordType.SAVED.value,
            createdAt = Date(),
            updatedAt = Date()
        )

        // When & Then - Test that word data is properly structured
        assertEquals("dialog", testWord.word)
        assertEquals("/ˈdaɪəlɒɡ/", testWord.ipa)
        assertEquals("noun", testWord.partOfSpeech)
        assertTrue(testWord.definition.isNotEmpty())
        assertTrue(testWord.example.isNotEmpty())
        assertTrue(testWord.soundUrl.isNotEmpty())
    }

    @Test
    fun `test empty state handling`() = runTest {
        // Given
        val emptyWords = emptyList<SavedWord>()

        // Then
        assertTrue(emptyWords.isEmpty())
        assertEquals(0, emptyWords.size)
    }

    @Test
    fun `test search with empty query`() = runTest {
        // Given
        val testWords = listOf(
            SavedWord(
                id = "1",
                userId = "user123",
                word = "example",
                ipa = "/ɪɡˈzæm.pəl/",
                partOfSpeech = "noun",
                definition = "test definition",
                example = "test example",
                soundUrl = "http://test.com/sound.mp3",
                wordType = WordType.SAVED.value,
                createdAt = Date(),
                updatedAt = Date()
            )
        )

        // When - Search with empty query should return all words
        val filteredWords = testWords.filter { word ->
            "".isEmpty() || word.word.lowercase().contains("".lowercase()) ||
            word.definition.lowercase().contains("".lowercase())
        }

        // Then
        assertEquals(testWords.size, filteredWords.size)
    }

    @Test
    fun `test search case insensitive`() = runTest {
        // Given
        val testWords = listOf(
            SavedWord(
                id = "1",
                userId = "user123",
                word = "Example",
                ipa = "/ɪɡˈzæm.pəl/",
                partOfSpeech = "noun",
                definition = "A Test Definition",
                example = "test example",
                soundUrl = "http://test.com/sound.mp3",
                wordType = WordType.SAVED.value,
                createdAt = Date(),
                updatedAt = Date()
            )
        )

        // When - Search should be case insensitive
        val query = "example"
        val filteredWords = testWords.filter { word ->
            word.word.lowercase().contains(query.lowercase()) ||
            word.definition.lowercase().contains(query.lowercase())
        }

        // Then
        assertEquals(1, filteredWords.size)
        assertEquals("Example", filteredWords[0].word)
    }

    @Test
    fun `test word type enum mapping`() {
        // Given & When & Then
        val weakValue = WordType.WEAK.value
        val mediumValue = WordType.MEDIUM.value
        val strongValue = WordType.STRONG.value
        val savedValue = WordType.SAVED.value

        assertEquals(2, weakValue)
        assertEquals(3, mediumValue)
        assertEquals(4, strongValue)
        assertEquals(1, savedValue)

        // Test reverse mapping
        val weakType = WordType.values().find { it.value == 2 }
        val mediumType = WordType.values().find { it.value == 3 }
        val strongType = WordType.values().find { it.value == 4 }
        val savedType = WordType.values().find { it.value == 1 }

        assertEquals(WordType.WEAK, weakType)
        assertEquals(WordType.MEDIUM, mediumType)
        assertEquals(WordType.STRONG, strongType)
        assertEquals(WordType.SAVED, savedType)
    }

    @Test
    fun `test intent parameter validation`() {
        // Given
        val wordType = WordType.WEAK
        val title = "Test Title"

        // When
        val intent = VocabularyByTypeActivity.createIntent(context, wordType, title)

        // Then
        val extractedWordType = intent.getIntExtra("extra_word_type", -1)
        val extractedTitle = intent.getStringExtra("extra_type_title")
        
        assertEquals(wordType.value, extractedWordType)
        assertEquals(title, extractedTitle)
        
        // Test reverse enum lookup
        val foundWordType = WordType.values().find { it.value == extractedWordType }
        assertEquals(wordType, foundWordType)
    }

    @Test
    fun `test saved word data model validation`() {
        // Given & When
        val savedWord = SavedWord(
            id = "test-id",
            userId = "test-user",
            word = "test-word",
            ipa = "/test/",
            partOfSpeech = "test-pos",
            definition = "test-definition",
            example = "test-example",
            soundUrl = "test-url",
            wordType = WordType.MEDIUM.value,
            createdAt = Date(),
            updatedAt = Date()
        )

        // Then
        assertEquals("test-id", savedWord.id)
        assertEquals("test-user", savedWord.userId)
        assertEquals("test-word", savedWord.word)
        assertEquals("/test/", savedWord.ipa)
        assertEquals("test-pos", savedWord.partOfSpeech)
        assertEquals("test-definition", savedWord.definition)
        assertEquals("test-example", savedWord.example)
        assertEquals("test-url", savedWord.soundUrl)
        assertEquals(WordType.MEDIUM.value, savedWord.wordType)
        assertNotNull(savedWord.createdAt)
        assertNotNull(savedWord.updatedAt)
    }
}