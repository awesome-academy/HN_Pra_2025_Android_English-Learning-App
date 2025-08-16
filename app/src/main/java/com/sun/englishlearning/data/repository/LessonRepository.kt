package com.sun.englishlearning.data.repository

import android.content.Context
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.data.model.UserLessonProgress
import com.sun.englishlearning.utils.JsonUtils
import kotlinx.coroutines.tasks.await

interface LessonRepository {
    suspend fun getAllLessons(): Result<List<Lesson>>
    suspend fun getLessonsByCourse(courseId: String): Result<List<Lesson>>
    suspend fun getLesson(lessonId: String): Result<Lesson?>
    suspend fun getLessonsForUser(userId: String): Result<List<Lesson>>
    suspend fun getSuggestedLessons(userId: String): Result<List<Lesson>>
    suspend fun getRecentlyLearnedLessons(userId: String, limit: Int = 2): Result<List<Pair<Lesson, UserLessonProgress>>>
    suspend fun createLesson(lesson: Lesson): Result<Unit>
    suspend fun updateLesson(lesson: Lesson): Result<Unit>
}

class LessonRepositoryImpl(
    private val context: Context,
    private val userLessonProgressRepository: UserLessonProgressRepository
) : LessonRepository {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    override suspend fun getAllLessons(): Result<List<Lesson>> {
        return try {
            // Load lessons from JSON file in assets
            val lessons = JsonUtils.loadLessonsFromAssets(context)
            Result.success(lessons)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLessonsByCourse(courseId: String): Result<List<Lesson>> {
        return try {
            // Load all lessons and filter by courseId if needed
            val allLessons = JsonUtils.loadLessonsFromAssets(context)
            // Since our JSON doesn't have courseId, return all lessons
            Result.success(allLessons)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLesson(lessonId: String): Result<Lesson?> {
        return try {
            // Load all lessons and find the one with matching ID
            val allLessons = JsonUtils.loadLessonsFromAssets(context)
            val lesson = allLessons.find { it.id == lessonId }
            Result.success(lesson)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLessonsForUser(userId: String): Result<List<Lesson>> {
        return try {
            // Get all lessons from JSON
            val allLessons = JsonUtils.loadLessonsFromAssets(context)

            // Get user's lesson progress
            val userProgressResult = userLessonProgressRepository.getUserProgressByUser(userId)
            if (userProgressResult.isFailure) {
                // If we can't get progress, return lessons as is
                return Result.success(allLessons)
            }

            // Return lessons (progress tracking is handled separately)
            Result.success(allLessons)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSuggestedLessons(userId: String): Result<List<Lesson>> {
        return try {
            // Get all lessons from JSON
            val allLessons = JsonUtils.loadLessonsFromAssets(context)

            // Get user's lesson progress to filter out started lessons
            val userProgressResult = userLessonProgressRepository.getUserProgressByUser(userId)
            val startedLessonIds = if (userProgressResult.isSuccess) {
                userProgressResult.getOrNull()?.map { it.lessonId }?.toSet() ?: emptySet()
            } else {
                emptySet()
            }

            // Filter to only show lessons that haven't been started as suggestions
            val suggestedLessons = allLessons.filter { !startedLessonIds.contains(it.id) }

            Result.success(suggestedLessons)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecentlyLearnedLessons(userId: String, limit: Int): Result<List<Pair<Lesson, UserLessonProgress>>> {
        return try {
            // Get user progress ordered by lastAccessedAt (most recent first)
            val progressResult = userLessonProgressRepository.getUserProgressByUser(userId)
            if (progressResult.isFailure) {
                return Result.success(emptyList()) // Return empty list if no progress found
            }

            val userProgress = progressResult.getOrNull() ?: emptyList()

            // Get the most recent progress records (limit to specified number)
            val recentProgress = userProgress
                .filter { it.isStarted } // Only lessons that have been started
                .take(limit)

            if (recentProgress.isEmpty()) {
                return Result.success(emptyList())
            }

            // Get all lessons
            val allLessonsResult = getAllLessons()
            if (allLessonsResult.isFailure) {
                return Result.success(emptyList())
            }

            val allLessons = allLessonsResult.getOrNull() ?: emptyList()

            // Combine lessons with their progress data
            val recentLessonsWithProgress = recentProgress.mapNotNull { progress ->
                val lesson = allLessons.find { it.id == progress.lessonId }
                if (lesson != null) {
                    Pair(lesson, progress)
                } else {
                    null
                }
            }

            Result.success(recentLessonsWithProgress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLesson(lesson: Lesson): Result<Unit> {
        return try {
            val lessonMap = mapOf(
                "title" to lesson.title,
                "description" to lesson.description,
                "imageUrl" to lesson.imageUrl,
                "vocabulary" to lesson.vocabulary
            )

            db.collection("lessons")
                .document(lesson.id)
                .update(lessonMap)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createLesson(lesson: Lesson): Result<Unit> {
        return try {
            db.collection("lessons")
                .document(lesson.id)
                .set(lesson)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
