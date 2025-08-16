package com.sun.englishlearning.api

import android.content.Context
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.data.repository.LessonRepository
import com.sun.englishlearning.data.repository.LessonRepositoryImpl
import com.sun.englishlearning.data.repository.UserLessonProgressRepositoryImpl

/**
 * Simple API service to fetch lessons from JSON assets
 * This wraps the repository pattern for easier usage
 */
class LessonApiService(context: Context) {

    private val userProgressRepository = UserLessonProgressRepositoryImpl()
    private val lessonRepository: LessonRepository = LessonRepositoryImpl(context, userProgressRepository)

    /**
     * Get all lessons from JSON assets
     * @return Result containing list of lessons or error
     */
    suspend fun getAllLessons(): Result<List<Lesson>> {
        return lessonRepository.getAllLessons()
    }


    /**
     * Get a specific lesson by ID
     * @param lessonId The lesson ID to fetch
     * @return Result containing the lesson or error
     */
    suspend fun getLesson(lessonId: String): Result<Lesson?> {
        return lessonRepository.getLesson(lessonId)
    }

    /**
     * Get lessons for a specific user (includes progress status)
     * @param userId The user ID
     * @return Result containing lessons with user progress or error
     */
    suspend fun getLessonsForUser(userId: String): Result<List<Lesson>> {
        return lessonRepository.getLessonsForUser(userId)
    }

    /**
     * Get suggested lessons for a user (lessons they haven't started)
     * @param userId The user ID
     * @return Result containing suggested lessons or error
     */
    suspend fun getSuggestedLessons(userId: String): Result<List<Lesson>> {
        return lessonRepository.getSuggestedLessons(userId)
    }
}