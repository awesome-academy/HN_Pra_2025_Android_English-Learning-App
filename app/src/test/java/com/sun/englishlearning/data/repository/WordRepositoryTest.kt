package com.sun.englishlearning.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.model.Meaning
import com.sun.englishlearning.data.model.Definition
import com.sun.englishlearning.data.model.Phonetic
import com.sun.englishlearning.data.repository.source.local.WordLocalDataSource
import com.sun.englishlearning.data.repository.source.remote.OnResultListener
import com.sun.englishlearning.data.repository.source.remote.WordRemoteDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
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
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class WordRepositoryTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockWordRemoteDataSource: WordRemoteDataSource

    @Mock
    private lateinit var mockWordLocalDataSource: WordLocalDataSource

    private val testDispatcher = TestCoroutineDispatcher()
    private lateinit var mockWebServer: MockWebServer
    private lateinit var wordRepository: WordRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        mockWebServer = MockWebServer()
        mockWebServer.start()
        
        wordRepository = WordRepository.getInstance(
            mockWordRemoteDataSource,
            mockWordLocalDataSource
        )
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `test Word model structure`() {
        // Given & When
        val word = Word(
            id = "1",
            word = "example",
            phonetic = "/ɪɡˈzæm.pəl/",
            phonetics = listOf(
                Phonetic(
                    text = "/ɪɡˈzæm.pəl/",
                    audio = "http://test.com/audio.mp3"
                )
            ),
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
            ),
            lessonId = "lesson-1"
        )

        // Then
        assertEquals("1", word.id)
        assertEquals("example", word.word)
        assertEquals("/ɪɡˈzæm.pəl/", word.phonetic)
        assertEquals(1, word.phonetics.size)
        assertEquals(1, word.meanings.size)
        assertEquals("lesson-1", word.lessonId)
    }

    @Test
    fun `test Phonetic model structure`() {
        // Given & When
        val phonetic = Phonetic(
            text = "/ɪɡˈzæm.pəl/",
            audio = "http://test.com/audio.mp3"
        )

        // Then
        assertEquals("/ɪɡˈzæm.pəl/", phonetic.text)
        assertEquals("http://test.com/audio.mp3", phonetic.audio)
    }

    @Test
    fun `test Meaning model structure`() {
        // Given & When
        val meaning = Meaning(
            partOfSpeech = "noun",
            definitions = listOf(
                Definition(
                    definition = "a test definition",
                    example = "test example"
                )
            )
        )

        // Then
        assertEquals("noun", meaning.partOfSpeech)
        assertEquals(1, meaning.definitions.size)
        assertEquals("a test definition", meaning.definitions[0].definition)
        assertEquals("test example", meaning.definitions[0].example)
    }

    @Test
    fun `test Definition model structure`() {
        // Given & When
        val definition = Definition(
            definition = "a test definition",
            example = "test example"
        )

        // Then
        assertEquals("a test definition", definition.definition)
        assertEquals("test example", definition.example)
    }

    @Test
    fun `test Word with default values`() {
        // Given & When
        val defaultWord = Word()

        // Then
        assertEquals("", defaultWord.id)
        assertEquals("", defaultWord.word)
        assertEquals("", defaultWord.phonetic)
        assertTrue(defaultWord.phonetics.isEmpty())
        assertTrue(defaultWord.meanings.isEmpty())
        assertEquals("", defaultWord.lessonId)
    }

    @Test
    fun `test successful word search from repository`() = runTest {
        // Given
        val searchTerm = "example"
        val expectedWord = Word(
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
        wordRepository.getWords(searchTerm, capture(listenerCaptor))

        // Simulate successful API response
        val capturedListener = listenerCaptor.value as OnResultListener<MutableList<Word>>
        capturedListener.onSuccess(mutableListOf(expectedWord))

        // Then
        verify(mockWordRemoteDataSource).getWords(any(), any())
        assertEquals("example", expectedWord.word)
        assertEquals("noun", expectedWord.meanings.firstOrNull()?.partOfSpeech)
    }

    @Test
    fun `test word search failure from repository`() = runTest {
        // Given
        val searchTerm = "nonexistentword"
        val errorMessage = "No definitions found for nonexistentword"

        val listenerCaptor = ArgumentCaptor.forClass(OnResultListener::class.java) as ArgumentCaptor<OnResultListener<MutableList<Word>>>

        // When
        wordRepository.getWords(searchTerm, capture(listenerCaptor))

        // Simulate API error response
        val capturedListener = listenerCaptor.value as OnResultListener<MutableList<Word>>
        capturedListener.onError(Exception(errorMessage))

        // Then
        verify(mockWordRemoteDataSource).getWords(any(), any())
    }

    @Test
    fun `test repository singleton pattern`() {
        // Given & When
        val instance1 = WordRepository.getInstance(
            mockWordRemoteDataSource,
            mockWordLocalDataSource
        )
        val instance2 = WordRepository.getInstance(
            mockWordRemoteDataSource,
            mockWordLocalDataSource
        )

        // Then
        assertEquals(instance1, instance2)
    }

    @Test
    fun `test local word operations`() = runTest {
        // Given
        val word = Word(word = "test")
        val listenerCaptor = ArgumentCaptor.forClass(OnResultListener::class.java) as ArgumentCaptor<OnResultListener<Boolean>>

        // When - Test adding word locally
        wordRepository.addWordLocal(word, capture(listenerCaptor))

        // Then
        verify(mockWordLocalDataSource).addWordLocal(any(), any())
    }

    @Test
    fun `test get words local`() = runTest {
        // Given
        val listenerCaptor = ArgumentCaptor.forClass(OnResultListener::class.java) as ArgumentCaptor<OnResultListener<MutableList<Word>>>

        // When
        wordRepository.getWordsLocal(capture(listenerCaptor))

        // Then
        verify(mockWordLocalDataSource).getWordsLocal(any())
    }

    @Test
    fun `test complex word with multiple meanings`() {
        // Given & When
        val complexWord = Word(
            word = "bank",
            meanings = listOf(
                Meaning(
                    partOfSpeech = "noun",
                    definitions = listOf(
                        Definition(
                            definition = "a financial establishment",
                            example = "I went to the bank to deposit money"
                        )
                    )
                ),
                Meaning(
                    partOfSpeech = "noun",
                    definitions = listOf(
                        Definition(
                            definition = "the land alongside or sloping down to a river or lake",
                            example = "we sat on the bank of the river"
                        )
                    )
                )
            )
        )

        // Then
        assertEquals("bank", complexWord.word)
        assertEquals(2, complexWord.meanings.size)
        assertEquals("noun", complexWord.meanings[0].partOfSpeech)
        assertEquals("noun", complexWord.meanings[1].partOfSpeech)
        assertTrue(complexWord.meanings[0].definitions[0].definition.contains("financial"))
        assertTrue(complexWord.meanings[1].definitions[0].definition.contains("river"))
    }

    @Test
    fun `test empty phonetics and meanings`() {
        // Given & When
        val wordWithEmptyLists = Word(
            word = "test",
            phonetics = emptyList(),
            meanings = emptyList()
        )

        // Then
        assertEquals("test", wordWithEmptyLists.word)
        assertTrue(wordWithEmptyLists.phonetics.isEmpty())
        assertTrue(wordWithEmptyLists.meanings.isEmpty())
    }
}