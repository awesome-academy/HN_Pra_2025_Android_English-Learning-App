package com.sun.englishlearning.screen.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.data.model.UserLessonProgress
import com.sun.englishlearning.data.repository.LessonRepository
import com.sun.englishlearning.data.repository.UserLessonProgressRepository
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
class HomeFragmentTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockLessonRepository: LessonRepository

    @Mock
    private lateinit var mockUserProgressRepository: UserLessonProgressRepository

    private val testDispatcher = TestCoroutineDispatcher()

    private lateinit var homeFragment: HomeFragment

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        homeFragment = HomeFragment()
    }

    @Test
    fun `test fragment initialization`() {
        // Given & When & Then
        assertNotNull(homeFragment)
    }

    @Test
    fun `test lesson data model`() {
        // Given & When
        val lesson = Lesson(
            id = "1",
            title = "Test Lesson",
            description = "Test Description",
            imageUrl = "http://test.com/image.jpg",
            vocabulary = listOf("word1", "word2", "word3")
        )

        // Then
        assertEquals("1", lesson.id)
        assertEquals("Test Lesson", lesson.title)
        assertEquals("Test Description", lesson.description)
        assertEquals("http://test.com/image.jpg", lesson.imageUrl)
        assertEquals(3, lesson.vocabulary.size)
        assertTrue(lesson.vocabulary.contains("word1"))
    }

    @Test
    fun `test user lesson progress data model`() {
        // Given & When
        val progress = UserLessonProgress(
            id = "1",
            userId = "user123",
            lessonId = "lesson1",
            isStarted = true,
            isCompleted = false,
            currentPoints = 75,
            totalPoints = 100,
            progressPercentage = 75,
            timeSpentMinutes = 30,
            attempts = 2,
            bestScore = 85,
            wordsLearned = 15,
            totalWords = 20,
            completedExercises = listOf("ex1", "ex2"),
            learnedWordIds = listOf("word1", "word2", "word3"),
            completedAt = null
        )

        // Then
        assertEquals("1", progress.id)
        assertEquals("user123", progress.userId)
        assertEquals("lesson1", progress.lessonId)
        assertTrue(progress.isStarted)
        assertFalse(progress.isCompleted)
        assertEquals(75, progress.currentPoints)
        assertEquals(100, progress.totalPoints)
        assertEquals(75, progress.progressPercentage)
        assertEquals(30, progress.timeSpentMinutes)
        assertEquals(2, progress.attempts)
        assertEquals(85, progress.bestScore)
        assertEquals(15, progress.wordsLearned)
        assertEquals(20, progress.totalWords)
        assertEquals(2, progress.completedExercises.size)
        assertEquals(3, progress.learnedWordIds.size)
        assertNull(progress.completedAt)
    }

    @Test
    fun `test lesson with default values`() {
        // Given & When
        val defaultLesson = Lesson()

        // Then
        assertEquals("", defaultLesson.id)
        assertEquals("", defaultLesson.title)
        assertEquals("", defaultLesson.description)
        assertEquals("", defaultLesson.imageUrl)
        assertTrue(defaultLesson.vocabulary.isEmpty())
    }

    @Test
    fun `test lesson with vocabulary`() = runTest {
        // Given & When
        val lessonWithVocabulary = Lesson(
            id = "1",
            title = "Vocabulary Lesson",
            vocabulary = listOf("word1", "word2", "word3")
        )

        // Then
        assertEquals("Vocabulary Lesson", lessonWithVocabulary.title)
        assertEquals(3, lessonWithVocabulary.vocabulary.size)
        assertTrue(lessonWithVocabulary.vocabulary.contains("word1"))
    }

    @Test
    fun `test lesson vocabulary count`() = runTest {
        // Given
        val lessons = listOf(
            Lesson(id = "1", vocabulary = listOf("word1", "word2")),
            Lesson(id = "2", vocabulary = listOf("word3")),
            Lesson(id = "3", vocabulary = emptyList())
        )

        // When
        val totalVocabularyCount = lessons.sumOf { it.vocabulary.size }
        val lessonsWithVocab = lessons.filter { it.vocabulary.isNotEmpty() }

        // Then
        assertEquals(3, totalVocabularyCount)
        assertEquals(2, lessonsWithVocab.size)
    }

    @Test
    fun `test course category data`() {
        // Given & When
        val categories = listOf("Travel", "Business", "Academic", "Practice")
        val selectedCategory = "Travel"

        // Then
        assertEquals(4, categories.size)
        assertTrue(categories.contains(selectedCategory))
        assertEquals("Travel", selectedCategory)
    }

    @Test
    fun `test lesson filtering by title keyword`() = runTest {
        // Given
        val allLessons = listOf(
            Lesson(id = "1", title = "Travel Lesson"),
            Lesson(id = "2", title = "Business Lesson"),
            Lesson(id = "3", title = "Travel Guide")
        )

        // When
        val travelLessons = allLessons.filter { it.title.contains("Travel") }
        val businessLessons = allLessons.filter { it.title.contains("Business") }

        // Then
        assertEquals(2, travelLessons.size)
        assertEquals(1, businessLessons.size)
        assertTrue(travelLessons.all { it.title.contains("Travel") })
        assertTrue(businessLessons.all { it.title.contains("Business") })
    }

    @Test
    fun `test lesson search by description`() = runTest {
        // Given
        val lessons = listOf(
            Lesson(id = "1", description = "Learn basic vocabulary"),
            Lesson(id = "2", description = "Intermediate grammar"),
            Lesson(id = "3", description = "Advanced conversation"),
            Lesson(id = "4", description = "Basic grammar rules")
        )

        // When
        val basicLessons = lessons.filter { it.description.contains("basic", ignoreCase = true) }
        val grammarLessons = lessons.filter { it.description.contains("grammar", ignoreCase = true) }
        val advancedLessons = lessons.filter { it.description.contains("advanced", ignoreCase = true) }

        // Then
        assertEquals(2, basicLessons.size)
        assertEquals(2, grammarLessons.size)
        assertEquals(1, advancedLessons.size)
    }
}