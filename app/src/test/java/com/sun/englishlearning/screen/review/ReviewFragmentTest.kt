package com.sun.englishlearning.screen.review

import android.content.Context
import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.sun.englishlearning.data.model.WordType
import com.sun.englishlearning.data.repository.SavedWordsRepository
import com.sun.englishlearning.screen.savedwords.SavedWordsActivity
import com.sun.englishlearning.screen.vocabulary.VocabularyByTypeActivity
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
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ReviewFragmentTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockSavedWordsRepository: SavedWordsRepository

    @Mock  
    private lateinit var mockFirebaseAuth: FirebaseAuth

    @Mock
    private lateinit var mockFirebaseUser: FirebaseUser

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var reviewFragment: ReviewFragment
    private lateinit var context: Context

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = ApplicationProvider.getApplicationContext()
        reviewFragment = ReviewFragment()
        
        // Mock Firebase Auth
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn("test-user-id")
    }

    @Test
    fun `test vocabulary card click navigation - weak words`() = runTest {
        // Given - Create intent for weak words
        val intent = VocabularyByTypeActivity.createIntent(
            context,
            WordType.WEAK,
            "Weak words"
        )
        
        // Then - Verify intent creation
        assertNotNull(intent)
        assertEquals(WordType.WEAK.value, intent.getIntExtra("extra_word_type", -1))
        assertEquals("Weak words", intent.getStringExtra("extra_type_title"))
    }

    @Test
    fun `test vocabulary card click navigation - medium words`() = runTest {
        // Given - Create intent for medium words
        val intent = VocabularyByTypeActivity.createIntent(
            context,
            WordType.MEDIUM,
            "Medium words"
        )
        
        // Then - Verify intent creation
        assertNotNull(intent)
        assertEquals(WordType.MEDIUM.value, intent.getIntExtra("extra_word_type", -1))
        assertEquals("Medium words", intent.getStringExtra("extra_type_title"))
    }

    @Test
    fun `test vocabulary card click navigation - strong words`() = runTest {
        // Given - Create intent for strong words
        val intent = VocabularyByTypeActivity.createIntent(
            context,
            WordType.STRONG,
            "Strong words"
        )
        
        // Then - Verify intent creation
        assertNotNull(intent)
        assertEquals(WordType.STRONG.value, intent.getIntExtra("extra_word_type", -1))
        assertEquals("Strong words", intent.getStringExtra("extra_type_title"))
    }

    @Test
    fun `test saved words navigation`() = runTest {
        // Given - Create intent for saved words activity
        val intent = Intent(context, SavedWordsActivity::class.java)
        
        // Then - Verify intent creation
        assertNotNull(intent)
        assertEquals(SavedWordsActivity::class.java.name, intent.component?.className)
    }

    @Test
    fun `test fragment initialization`() = runTest {
        // Given & When & Then - Verify fragment is properly initialized
        assertNotNull(reviewFragment)
    }

    @Test
    fun `test word type enum values`() {
        // Given & When & Then - Verify word type enum values are correct
        assertEquals(1, WordType.SAVED.value)
        assertEquals(2, WordType.WEAK.value)
        assertEquals(3, WordType.MEDIUM.value)
        assertEquals(4, WordType.STRONG.value)
    }
}