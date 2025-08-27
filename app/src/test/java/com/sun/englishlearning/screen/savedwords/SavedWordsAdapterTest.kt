package com.sun.englishlearning.screen.savedwords

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.sun.englishlearning.data.model.SavedWord
import com.sun.englishlearning.data.model.WordType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SavedWordsAdapterTest {

    @Mock
    private lateinit var mockOnAction: (SavedWord, SavedWordsAdapter.Action) -> Unit

    private lateinit var context: Context
    private lateinit var adapter: SavedWordsAdapter
    private lateinit var testWords: List<SavedWord>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = ApplicationProvider.getApplicationContext()
        
        testWords = listOf(
            SavedWord(
                id = "1",
                userId = "user123",
                word = "example",
                ipa = "/ɪɡˈzæm.pəl/",
                partOfSpeech = "noun",
                definition = "a thing characteristic of its kind or illustrating a general rule",
                example = "there is a similar example at page 108",
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
                definition = "a procedure intended to establish the quality, performance, or reliability of something",
                example = "both types of test are essential",
                soundUrl = "http://test.com/sound2.mp3",
                wordType = WordType.WEAK.value,
                createdAt = Date(),
                updatedAt = Date()
            )
        )

        adapter = SavedWordsAdapter(testWords, mockOnAction)
    }

    @Test
    fun `test adapter item count`() {
        // Given & When & Then
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `test create view holder`() {
        // Given
        val parent = RecyclerView(context)

        // When
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        // Then
        assertNotNull(viewHolder)
    }

    @Test
    fun `test adapter with empty list`() {
        // Given
        val emptyAdapter = SavedWordsAdapter(emptyList(), mockOnAction)

        // When & Then
        assertEquals(0, emptyAdapter.itemCount)
    }

    @Test
    fun `test adapter with different word types`() {
        // Given
        val mixedWords = listOf(
            SavedWord(
                id = "1",
                userId = "user123",
                word = "weak_word",
                ipa = "/weak/",
                partOfSpeech = "adjective",
                definition = "weak definition",
                example = "weak example",
                soundUrl = "http://test.com/weak.mp3",
                wordType = WordType.WEAK.value,
                createdAt = Date(),
                updatedAt = Date()
            ),
            SavedWord(
                id = "2", 
                userId = "user123",
                word = "strong_word",
                ipa = "/strong/",
                partOfSpeech = "adjective", 
                definition = "strong definition",
                example = "strong example",
                soundUrl = "http://test.com/strong.mp3",
                wordType = WordType.STRONG.value,
                createdAt = Date(),
                updatedAt = Date()
            )
        )

        val mixedAdapter = SavedWordsAdapter(mixedWords, mockOnAction)

        // When & Then
        assertEquals(2, mixedAdapter.itemCount)
    }

    @Test
    fun `test word with empty example`() {
        // Given
        val wordWithoutExample = SavedWord(
            id = "1",
            userId = "user123", 
            word = "minimal",
            ipa = "/minimal/",
            partOfSpeech = "adjective",
            definition = "minimal definition",
            example = "", // Empty example
            soundUrl = "http://test.com/minimal.mp3",
            wordType = WordType.SAVED.value,
            createdAt = Date(),
            updatedAt = Date()
        )

        val adapterWithEmptyExample = SavedWordsAdapter(listOf(wordWithoutExample), mockOnAction)

        // When & Then
        assertEquals(1, adapterWithEmptyExample.itemCount)
    }

    @Test
    fun `test action enum values`() {
        // Given & When & Then
        val actions = SavedWordsAdapter.Action.values()
        assertEquals(3, actions.size)
        assertEquals(SavedWordsAdapter.Action.PLAY_SOUND, actions[0])
        assertEquals(SavedWordsAdapter.Action.REMOVE_WORD, actions[1])
        assertEquals(SavedWordsAdapter.Action.VIEW_DETAILS, actions[2])
    }

    @Test
    fun `test word data integrity`() {
        // Given & When
        val testWord = testWords[0]

        // Then
        assertEquals("example", testWord.word)
        assertEquals("noun", testWord.partOfSpeech)
        assertEquals("/ɪɡˈzæm.pəl/", testWord.ipa)
        assertTrue(testWord.definition.isNotEmpty())
        assertTrue(testWord.example.isNotEmpty())
        assertTrue(testWord.soundUrl.isNotEmpty())
    }
}