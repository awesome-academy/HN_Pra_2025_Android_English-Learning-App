package com.sun.englishlearning.utils

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for JsonUtils to verify lesson parsing with simplified structure
 */
class JsonUtilsTest {

    @Test
    fun testParseLessonsFromJson_withValidJson_returnsCorrectLessons() {
        // Given
        val jsonString = """
        {
          "lessons": [
            {
              "id": "1",
              "title": "Schools",
              "description": "Learn vocabulary about schools and educational institutions",
              "imageUrl": "https://example.com/image1.png",
              "vocabulary": ["school", "teacher", "student", "classroom", "homework"]
            },
            {
              "id": "2",
              "title": "Examination",
              "description": "Vocabulary and phrases related to exams and testing",
              "imageUrl": "https://example.com/image2.png",
              "vocabulary": ["exam", "test", "quiz", "grade", "score"]
            }
          ]
        }
        """.trimIndent()

        // When
        val lessons = JsonUtils.parseLessonsFromJson(jsonString)

        // Then
        assertEquals(2, lessons.size)
        
        val firstLesson = lessons[0]
        assertEquals("1", firstLesson.id)
        assertEquals("Schools", firstLesson.title)
        assertEquals("Learn vocabulary about schools and educational institutions", firstLesson.description)
        assertEquals("https://example.com/image1.png", firstLesson.imageUrl)
        assertEquals(5, firstLesson.vocabulary.size)
        assertTrue(firstLesson.vocabulary.contains("school"))
        assertTrue(firstLesson.vocabulary.contains("teacher"))
        
        val secondLesson = lessons[1]
        assertEquals("2", secondLesson.id)
        assertEquals("Examination", secondLesson.title)
        assertEquals("Vocabulary and phrases related to exams and testing", secondLesson.description)
        assertEquals("https://example.com/image2.png", secondLesson.imageUrl)
        assertEquals(5, secondLesson.vocabulary.size)
        assertTrue(secondLesson.vocabulary.contains("exam"))
        assertTrue(secondLesson.vocabulary.contains("test"))
    }

    @Test
    fun testParseLessonsFromJson_withEmptyJson_returnsEmptyList() {
        // Given
        val jsonString = """{"lessons": []}"""

        // When
        val lessons = JsonUtils.parseLessonsFromJson(jsonString)

        // Then
        assertTrue(lessons.isEmpty())
    }

    @Test
    fun testParseLessonsFromJson_withInvalidJson_returnsEmptyList() {
        // Given
        val jsonString = "invalid json"

        // When
        val lessons = JsonUtils.parseLessonsFromJson(jsonString)

        // Then
        assertTrue(lessons.isEmpty())
    }

    @Test
    fun testParseLessonsFromJson_withMissingVocabulary_handlesGracefully() {
        // Given
        val jsonString = """
        {
          "lessons": [
            {
              "id": "1",
              "title": "Test Lesson",
              "description": "Test description",
              "imageUrl": "https://example.com/image.png"
            }
          ]
        }
        """.trimIndent()

        // When
        val lessons = JsonUtils.parseLessonsFromJson(jsonString)

        // Then
        assertEquals(1, lessons.size)
        val lesson = lessons[0]
        assertEquals("1", lesson.id)
        assertEquals("Test Lesson", lesson.title)
        assertTrue(lesson.vocabulary.isEmpty())
    }
}
