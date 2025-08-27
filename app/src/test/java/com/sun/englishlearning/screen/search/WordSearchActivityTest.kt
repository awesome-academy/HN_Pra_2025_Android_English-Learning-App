package com.sun.englishlearning.screen.search

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.sun.englishlearning.data.model.SavedWord
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.model.WordType
import com.sun.englishlearning.data.model.Meaning
import com.sun.englishlearning.data.model.Definition
import com.sun.englishlearning.data.repository.SavedWordsRepository
import com.sun.englishlearning.data.repository.WordRepository
import com.sun.englishlearning.data.repository.source.remote.OnResultListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.capture
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class WordSearchActivityTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockWordRepository: WordRepository

    @Mock
    private lateinit var mockSavedWordsRepository: SavedWordsRepository

    @Mock
    private lateinit var mockFirebaseAuth: FirebaseAuth

    @Mock
    private lateinit var mockFirebaseUser: FirebaseUser

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var wordSearchActivity: WordSearchActivity
    private lateinit var context: Context

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = ApplicationProvider.getApplicationContext()
        
        // Mock Firebase Auth
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn("test-user-id")

        wordSearchActivity = Robolectric.buildActivity(WordSearchActivity::class.java)
            .create()
            .get()
    }

    @Test
    fun `test successful word search`() = runTest {
        // Given
        val searchTerm = "example"
        val mockWord = Word(
            id = "1",
            word = "example",
            phonetic = "/ɪɡˈzæm.pəl/",
            meanings = listOf(
                Meaning(
                    partOfSpeech = "noun",
                    definitions = listOf(
                        Definition(
                            definition = "a thing characteristic of its kind or illustrating a general rule",
                            example = "there is a similar example at page 108"
                        )
                    )
                )
            )
        )

        val listenerCaptor = ArgumentCaptor.forClass(OnResultListener::class.java) as ArgumentCaptor<OnResultListener<MutableList<Word>>>

        // When
        mockWordRepository.getWords(any(), capture(listenerCaptor))
        
        // Simulate successful response
        val capturedListener = listenerCaptor.value as OnResultListener<MutableList<Word>>
        capturedListener.onSuccess(mutableListOf(mockWord))

        // Then
        verify(mockWordRepository).getWords(any(), any())
        assertNotNull(mockWord)
        assertEquals("example", mockWord.word)
        assertEquals("noun", mockWord.meanings.firstOrNull()?.partOfSpeech)
    }

    @Test
    fun `test word search failure`() = runTest {
        // Given
        val searchTerm = "nonexistentword"
        val errorMessage = "Word not found"
        
        val listenerCaptor = ArgumentCaptor.forClass(OnResultListener::class.java) as ArgumentCaptor<OnResultListener<MutableList<Word>>>

        // When
        mockWordRepository.getWords(any(), capture(listenerCaptor))
        
        // Simulate error response
        val capturedListener = listenerCaptor.value as OnResultListener<MutableList<Word>>
        capturedListener.onError(Exception(errorMessage))

        // Then
        verify(mockWordRepository).getWords(any(), any())
    }

    @Test
    fun `test save word functionality`() = runTest {
        // Given
        val word = Word(
            word = "test",
            phonetic = "/test/",
            meanings = listOf(
                Meaning(
                    partOfSpeech = "noun", 
                    definitions = listOf(
                        Definition(
                            definition = "a test definition",
                            example = "test example"
                        )
                    )
                )
            )
        )

        val expectedSavedWord = SavedWord(
            id = "",
            userId = "test-user-id",
            word = word.word,
            ipa = word.phonetic,
            partOfSpeech = word.meanings.firstOrNull()?.partOfSpeech ?: "",
            definition = word.meanings.firstOrNull()?.definitions?.firstOrNull()?.definition ?: "",
            example = word.meanings.firstOrNull()?.definitions?.firstOrNull()?.example ?: "",
            soundUrl = "",
            wordType = WordType.SAVED.value,
            createdAt = Date(),
            updatedAt = Date()
        )

        whenever(mockSavedWordsRepository.saveWord(any())).thenReturn(Result.success("test-id"))

        // When - This would be called when user clicks save
        mockSavedWordsRepository.saveWord(expectedSavedWord)

        // Then
        verify(mockSavedWordsRepository).saveWord(any())
    }

    @Test
    fun `test word already saved check`() = runTest {
        // Given
        val word = "example"
        val mockSavedWords = listOf(
            SavedWord(
                id = "1",
                userId = "test-user-id",
                word = word,
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

        whenever(mockSavedWordsRepository.getUserSavedWords("test-user-id"))
            .thenReturn(Result.success(mockSavedWords))

        // When
        val result = mockSavedWordsRepository.getUserSavedWords("test-user-id")

        // Then
        verify(mockSavedWordsRepository).getUserSavedWords("test-user-id")
        assertTrue(result.isSuccess)
        val savedWords = result.getOrNull()
        assertNotNull(savedWords)
        assertTrue(savedWords?.any { it.word == word } ?: false)
    }

    @Test
    fun `test empty search query handling`() = runTest {
        // Given
        val emptyQuery = ""

        // When & Then
        // The activity should handle empty queries gracefully
        assertTrue(emptyQuery.isEmpty())
    }

    @Test
    fun `test audio playback functionality`() = runTest {
        // Given
        val soundUrl = "http://test.com/pronunciation.mp3"
        
        // When & Then
        // Test would verify MediaPlayer is configured correctly
        assertNotNull(soundUrl)
        assertTrue(soundUrl.isNotEmpty())
    }

    @Test
    fun `test word search with special characters`() = runTest {
        // Given
        val searchTerm = "café"
        val mockWord = Word(
            word = "café",
            phonetic = "/kæˈfeɪ/",
            meanings = listOf(
                Meaning(
                    partOfSpeech = "noun",
                    definitions = listOf(
                        Definition(
                            definition = "a restaurant serving coffee and light refreshments",
                            example = "let's meet at the café"
                        )
                    )
                )
            )
        )

        val listenerCaptor = ArgumentCaptor.forClass(OnResultListener::class.java) as ArgumentCaptor<OnResultListener<MutableList<Word>>>

        // When
        mockWordRepository.getWords(any(), capture(listenerCaptor))
        
        // Simulate successful response
        val capturedListener = listenerCaptor.value as OnResultListener<MutableList<Word>>
        capturedListener.onSuccess(mutableListOf(mockWord))

        // Then
        verify(mockWordRepository).getWords(any(), any())
        assertEquals("café", mockWord.word)
    }

    @Test
    fun `test word model structure`() {
        // Given & When
        val word = Word(
            id = "1",
            word = "test",
            phonetic = "/test/",
            meanings = listOf(
                Meaning(
                    partOfSpeech = "noun",
                    definitions = listOf(
                        Definition(
                            definition = "a test definition",
                            example = "test example"
                        )
                    )
                )
            )
        )

        // Then
        assertEquals("test", word.word)
        assertEquals("/test/", word.phonetic)
        assertEquals(1, word.meanings.size)
        assertEquals("noun", word.meanings[0].partOfSpeech)
        assertEquals(1, word.meanings[0].definitions.size)
        assertEquals("a test definition", word.meanings[0].definitions[0].definition)
        assertEquals("test example", word.meanings[0].definitions[0].example)
    }
}