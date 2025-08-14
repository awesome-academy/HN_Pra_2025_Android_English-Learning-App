package com.sun.englishlearning.data.repository

import android.content.Context
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.utils.JsonUtils

object LessonRepository {

    private var cachedLessons: List<Lesson>? = null

    fun getAllLessons(context: Context): List<Lesson> {
        // Use cached data if available
        if (cachedLessons != null) {
            return cachedLessons!!
        }

        // Load from JSON and cache
        cachedLessons = JsonUtils.loadLessonsFromAssets(context)
        return cachedLessons ?: getFallbackLessons()
    }

    fun getOngoingLessons(context: Context): List<Lesson> {
        return getAllLessons(context).filter { !it.isCompleted }
    }

    fun getCompletedLessons(context: Context): List<Lesson> {
        return getAllLessons(context).filter { it.isCompleted }
    }

    fun getLessonById(context: Context, lessonId: String): Lesson? {
        return getAllLessons(context).find { it.id == lessonId }
    }

    fun refreshCache(context: Context) {
        cachedLessons = null
        getAllLessons(context)
    }

    /**
     * Fallback data in case JSON loading fails
     */
    private fun getFallbackLessons(): List<Lesson> {
        return listOf(
            Lesson(
                id = "31",
                title = "School Stationery",
                lessonNumber = 4,
                advancedLevel = 20,
                currentPoints = 100,
                totalPoints = 100,
                progressPercentage = 100,
                description = "Essential school supplies and stationery items",
                imageUrl = "https://learn.mochidemy.com/_next/image?url=https%3A%2F%2Fmochien3.1-api.mochidemy.com%2Fpublic%2Fimages%2Flesson%2FTACB_lesson_31.webp&w=256&q=70",
                isCompleted = true
            ),
            Lesson(
                id = "33",
                title = "Classroom",
                lessonNumber = 6,
                advancedLevel = 25,
                currentPoints = 100,
                totalPoints = 100,
                progressPercentage = 100,
                description = "Classroom objects and learning environment",
                imageUrl = "https://learn.mochidemy.com/_next/image?url=https%3A%2F%2Fmochien3.1-api.mochidemy.com%2Fpublic%2Fimages%2Flesson%2FTACB_lesson_33.webp&w=256&q=70",
                isCompleted = true
            ),
            Lesson(
                id = "8",
                title = "Body",
                lessonNumber = 8,
                advancedLevel = 15,
                currentPoints = 100,
                totalPoints = 100,
                progressPercentage = 100,
                description = "Parts of the human body and related vocabulary",
                imageUrl = "https://learn.mochidemy.com/_next/image?url=https%3A%2F%2Fmochien3.1-api.mochidemy.com%2Fpublic%2Fimages%2Flesson%2FTACB_lesson_8.webp&w=256&q=70",
                isCompleted = true
            )
        )
    }
}
