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
        return cachedLessons!!
    }

    fun getOngoingLessons(context: Context): List<Lesson> {
        return getAllLessons(context).filter { !it.isCompleted }
    }

    fun getCompletedLessons(context: Context): List<Lesson> {
        return getAllLessons(context).filter { it.isCompleted }
    }

    fun refreshCache(context: Context) {
        cachedLessons = null
        getAllLessons(context)
    }
}
