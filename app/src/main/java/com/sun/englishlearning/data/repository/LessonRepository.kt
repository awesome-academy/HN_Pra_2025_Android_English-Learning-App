package com.sun.englishlearning.data.repository

import android.content.Context
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
    suspend fun getInProgressLessons(userId: String): Result<List<Pair<Lesson, UserLessonProgress>>>
    suspend fun getCompletedLessons(userId: String): Result<List<Pair<Lesson, UserLessonProgress>>>
    suspend fun createLesson(lesson: Lesson): Result<Unit>
    suspend fun updateLesson(lesson: Lesson): Result<Unit>
    suspend fun updateLessonProgressForFlashcard(userId: String, lessonId: String, wordId: String): Result<Unit>
}

class LessonRepositoryImpl(
    private val context: Context,
    private val userLessonProgressRepository: UserLessonProgressRepository
) : LessonRepository {

    override suspend fun getAllLessons(): Result<List<Lesson>> {
        return try {
            val lessons = JsonUtils.loadLessonsFromAssets(context)
            Result.success(lessons)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLessonsByCourse(courseId: String): Result<List<Lesson>> {
        return try {
            val allLessons = JsonUtils.loadLessonsFromAssets(context)
            val lessons = allLessons.filter { it.id == courseId }
            Result.success(lessons)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLesson(lessonId: String): Result<Lesson?> {
        return try {
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
    
    override suspend fun getInProgressLessons(userId: String): Result<List<Pair<Lesson, UserLessonProgress>>> {
        return try {
            // Get user progress
            val progressResult = userLessonProgressRepository.getUserProgressByUser(userId)
            if (progressResult.isFailure) {
                return Result.success(emptyList())
            }

            val userProgress = progressResult.getOrNull() ?: emptyList()

            // Filter for lessons that are in progress (started but not completed)
            val inProgressProgress = userProgress.filter { 
                it.isStarted && it.progressPercentage < 100 
            }

            if (inProgressProgress.isEmpty()) {
                return Result.success(emptyList())
            }

            // Get all lessons
            val allLessonsResult = getAllLessons()
            if (allLessonsResult.isFailure) {
                return Result.success(emptyList())
            }

            val allLessons = allLessonsResult.getOrNull() ?: emptyList()

            // Combine lessons with their progress data
            val inProgressLessonsWithProgress = inProgressProgress.mapNotNull { progress ->
                val lesson = allLessons.find { it.id == progress.lessonId }
                if (lesson != null) {
                    Pair(lesson, progress)
                } else {
                    null
                }
            }

            // Sort by last accessed time (most recent first)
            val sortedLessons = inProgressLessonsWithProgress.sortedByDescending { 
                it.second.lastAccessedAt 
            }

            Result.success(sortedLessons)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCompletedLessons(userId: String): Result<List<Pair<Lesson, UserLessonProgress>>> {
        return try {
            // Get user progress
            val progressResult = userLessonProgressRepository.getUserProgressByUser(userId)
            if (progressResult.isFailure) {
                return Result.success(emptyList())
            }

            val userProgress = progressResult.getOrNull() ?: emptyList()

            // Filter for lessons that are completed (100% progress)
            val completedProgress = userProgress.filter { 
                it.isStarted && it.progressPercentage >= 100 
            }

            if (completedProgress.isEmpty()) {
                return Result.success(emptyList())
            }

            // Get all lessons
            val allLessonsResult = getAllLessons()
            if (allLessonsResult.isFailure) {
                return Result.success(emptyList())
            }

            val allLessons = allLessonsResult.getOrNull() ?: emptyList()

            // Combine lessons with their progress data
            val completedLessonsWithProgress = completedProgress.mapNotNull { progress ->
                val lesson = allLessons.find { it.id == progress.lessonId }
                if (lesson != null) {
                    Pair(lesson, progress)
                } else {
                    null
                }
            }

            Result.success(completedLessonsWithProgress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLesson(lesson: Lesson): Result<Unit> {
        // No-op: lessons are loaded from JSON, not updated in Firestore
        return Result.success(Unit)
    }

    override suspend fun createLesson(lesson: Lesson): Result<Unit> {
        // No-op: lessons are loaded from JSON, not created in Firestore
        return Result.success(Unit)
    }

    override suspend fun updateLessonProgressForFlashcard(userId: String, lessonId: String, wordId: String): Result<Unit> {
        return try {
            val progressResult = userLessonProgressRepository.getUserLessonProgress(userId, lessonId)
            
            val lessonResult = getLesson(lessonId)
            if (lessonResult.isFailure) return Result.failure(lessonResult.exceptionOrNull()!!)
            val lesson = lessonResult.getOrNull() ?: return Result.failure(Exception("Lesson not found"))
            val totalWords = lesson.vocabulary.size
            
            // Handle case where progress doesn't exist yet
            val progress = if (progressResult.isSuccess) {
                progressResult.getOrNull()
            } else {
                null
            }
            
            val learnedWords = progress?.learnedWordIds?.toMutableList() ?: mutableListOf()
            if (!learnedWords.contains(wordId)) learnedWords.add(wordId)
            
            // Update progress fields
            val updatedProgress = if (progress != null) {
                // Update existing progress
                progress.copy(
                    learnedWordIds = learnedWords,
                    wordsLearned = learnedWords.size,
                    totalWords = totalWords,
                    progressPercentage = if (totalWords > 0) (learnedWords.size * 100 / totalWords) else 0,
                    bestScore = learnedWords.size,
                    lastAccessedAt = java.util.Date()
                )
            } else {
                // Create new progress record if it doesn't exist
                UserLessonProgress(
                    userId = userId,
                    lessonId = lessonId,
                    isStarted = true,
                    learnedWordIds = learnedWords,
                    wordsLearned = learnedWords.size,
                    totalWords = totalWords,
                    progressPercentage = if (totalWords > 0) (learnedWords.size * 100 / totalWords) else 0,
                    bestScore = learnedWords.size,
                    lastAccessedAt = java.util.Date(),
                    startedAt = java.util.Date()
                )
            }
            
            // Create progress record if it doesn't exist yet
            if (progress == null) {
                val createResult = userLessonProgressRepository.createProgress(updatedProgress)
                if (createResult.isFailure) {
                    return Result.failure(Exception("Failed to create progress record: ${createResult.exceptionOrNull()?.message}"))
                }
            }
            val updateResult = userLessonProgressRepository.updateProgress(updatedProgress)
            if (updateResult.isFailure) return updateResult
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
