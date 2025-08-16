package com.sun.englishlearning.api

import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.data.model.LessonDifficulty
import com.sun.englishlearning.data.repository.LessonRepository
import com.sun.englishlearning.data.repository.LessonRepositoryImpl
import com.sun.englishlearning.data.repository.UserLessonProgressRepositoryImpl

/**
 * Simple API service to fetch lessons from Firebase
 * This wraps the repository pattern for easier usage
 */
class LessonApiService {
    
    private val userProgressRepository = UserLessonProgressRepositoryImpl()
    private val lessonRepository: LessonRepository = LessonRepositoryImpl(userProgressRepository)
    
    /**
     * Get all lessons from Firebase
     * @return Result containing list of lessons or error
     */
    suspend fun getAllLessons(): Result<List<Lesson>> {
        return lessonRepository.getAllLessons()
    }
    
    /**
     * Get lessons by difficulty level
     * @param difficulty The lesson difficulty (EASY, MEDIUM, ADVANCED)
     * @return Result containing filtered lessons or error
     */
    suspend fun getLessonsByDifficulty(difficulty: LessonDifficulty): Result<List<Lesson>> {
        return lessonRepository.getLessonsByDifficulty(difficulty)
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