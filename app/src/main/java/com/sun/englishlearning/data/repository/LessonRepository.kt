package com.sun.englishlearning.data.repository

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.data.model.LessonDifficulty
import com.sun.englishlearning.data.model.UserLessonProgress
import kotlinx.coroutines.tasks.await

interface LessonRepository {
    suspend fun getAllLessons(): Result<List<Lesson>>
    suspend fun getLessonsByCourse(courseId: String): Result<List<Lesson>>
    suspend fun getLessonsByDifficulty(difficulty: LessonDifficulty): Result<List<Lesson>>
    suspend fun getLesson(lessonId: String): Result<Lesson?>
    suspend fun getLessonsForUser(userId: String): Result<List<Lesson>>
    suspend fun getSuggestedLessons(userId: String): Result<List<Lesson>>
    suspend fun getRecentlyLearnedLessons(userId: String, limit: Int = 2): Result<List<Pair<Lesson, UserLessonProgress>>>
    suspend fun createLesson(lesson: Lesson): Result<Unit>
    suspend fun updateLesson(lesson: Lesson): Result<Unit>
}

class LessonRepositoryImpl(
    private val userLessonProgressRepository: UserLessonProgressRepository
) : LessonRepository {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    override suspend fun getAllLessons(): Result<List<Lesson>> {
        return try {
            // Check if user is authenticated
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated. Please sign in first."))
            }
            
            // Try simple query without orderBy to avoid index issues
            val snapshot = db.collection("lessons")
                .get()
                .await()

            val lessons = snapshot.documents.mapNotNull { document ->
                document.toObject(Lesson::class.java)?.copy(id = document.id)
            }.filter { it.isActive } // Filter active lessons in code instead of query
             .sortedBy { it.lessonNumber } // Sort in code instead of database
            
            Result.success(lessons)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLessonsByCourse(courseId: String): Result<List<Lesson>> {
        return try {
            val snapshot = db.collection("lessons")
                .whereEqualTo("courseId", courseId)
                .get()
                .await()

            val lessons = snapshot.documents.mapNotNull { document ->
                document.toObject(Lesson::class.java)?.copy(id = document.id)
            }.filter { it.isActive }
             .sortedBy { it.lessonNumber }
             
            Result.success(lessons)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLessonsByDifficulty(difficulty: LessonDifficulty): Result<List<Lesson>> {
        return try {
            val snapshot = db.collection("lessons")
                .whereEqualTo("difficulty", difficulty.name)
                .get()
                .await()

            val lessons = snapshot.documents.mapNotNull { document ->
                document.toObject(Lesson::class.java)?.copy(id = document.id)
            }.filter { it.isActive }
             .sortedBy { it.lessonNumber }
             
            Result.success(lessons)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLesson(lessonId: String): Result<Lesson?> {
        return try {
            val document = db.collection("lessons")
                .document(lessonId)
                .get()
                .await()

            val lesson = document.toObject(Lesson::class.java)?.copy(id = document.id)
            Result.success(lesson)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getLessonsForUser(userId: String): Result<List<Lesson>> {
        return try {
            // Get all lessons
            val allLessonsResult = getAllLessons()
            if (allLessonsResult.isFailure) {
                return allLessonsResult
            }
            
            val allLessons = allLessonsResult.getOrNull() ?: emptyList()
            
            // Get user's lesson progress
            val userProgressResult = userLessonProgressRepository.getUserProgressByUser(userId)
            if (userProgressResult.isFailure) {
                // If we can't get progress, return lessons without started status
                return Result.success(allLessons)
            }
            
            val userProgress = userProgressResult.getOrNull() ?: emptyList()
            val startedLessonIds = userProgress.map { it.lessonId }.toSet()
            
            // Mark lessons as started if user has progress for them
            val lessonsWithStatus = allLessons.map { lesson ->
                lesson.copy(isStarted = startedLessonIds.contains(lesson.id))
            }
            
            Result.success(lessonsWithStatus)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSuggestedLessons(userId: String): Result<List<Lesson>> {
        return try {
            val lessonsResult = getLessonsForUser(userId)
            if (lessonsResult.isFailure) {
                return lessonsResult
            }
            
            val allLessons = lessonsResult.getOrNull() ?: emptyList()
            
            // Filter to only show lessons that haven't been started as suggestions
            val suggestedLessons = allLessons.filter { !it.isStarted }
            
            Result.success(suggestedLessons)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecentlyLearnedLessons(userId: String, limit: Int): Result<List<Pair<Lesson, UserLessonProgress>>> {
        return try {
            println("=== GETTING RECENT LESSONS ===")
            println("Looking for userId: $userId")
            
            // Get user progress ordered by lastAccessedAt (most recent first)
            val progressResult = userLessonProgressRepository.getUserProgressByUser(userId)
            if (progressResult.isFailure) {
                println("Failed to get user progress: ${progressResult.exceptionOrNull()?.message}")
                return Result.success(emptyList()) // Return empty list if no progress found
            }
            println("Found progressResult total progress records ${progressResult}")
            
            val userProgress = progressResult.getOrNull() ?: emptyList()
            println("Found ${userProgress.size} total progress records")

            userProgress.forEach { progress ->
                println("Progress: lessonId=${progress.lessonId}, isStarted=${progress.isStarted}, lastAccessed=${progress.lastAccessedAt}")
            }
            
            // Get the most recent progress records (limit to specified number)
            val recentProgress = userProgress
                .filter { it.isStarted } // Only lessons that have been started
                .take(limit)
            
            println("Filtered to ${recentProgress.size} started lessons")
            
            if (recentProgress.isEmpty()) {
                println("No started lessons found")
                return Result.success(emptyList())
            }
            
            // Get all lessons
            val allLessonsResult = getAllLessons()
            if (allLessonsResult.isFailure) {
                println("Failed to get all lessons")
                return Result.success(emptyList())
            }
            
            val allLessons = allLessonsResult.getOrNull() ?: emptyList()
            println("Found ${allLessons.size} total lessons")
            
            // Debug: Show all lesson IDs for comparison
            println("Available lesson IDs: ${allLessons.map { it.id }}")
            println("Progress lesson IDs: ${recentProgress.map { it.lessonId }}")
            
            // Combine lessons with their progress data
            val recentLessonsWithProgress = recentProgress.mapNotNull { progress ->
                val lesson = allLessons.find { it.id == progress.lessonId }
                if (lesson != null) {
                    println("✓ Matched lesson: ${lesson.title} (${lesson.id}) with progress")
                    Pair(lesson, progress)
                } else {
                    println("✗ No lesson found for lessonId: ${progress.lessonId}")
                    println("  Available lessons: ${allLessons.map { "${it.id} (${it.title})" }}")
                    null
                }
            }
            
            println("Returning ${recentLessonsWithProgress.size} recent lessons")
            println("==============================")
            Result.success(recentLessonsWithProgress)
        } catch (e: Exception) {
            println("Exception in getRecentlyLearnedLessons: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateLesson(lesson: Lesson): Result<Unit> {
        return try {
            val lessonMap = mapOf(
                "courseId" to lesson.courseId,
                "title" to lesson.title,
                "lessonNumber" to lesson.lessonNumber,
                "description" to lesson.description,
                "duration" to lesson.duration,
                "difficulty" to lesson.difficulty.name,
                "totalPoints" to lesson.totalPoints,
                "wordIds" to lesson.wordIds,
                "exercises" to lesson.exercises,
                "videoUrl" to lesson.videoUrl,
                "audioUrl" to lesson.audioUrl,
                "imageRes" to lesson.imageRes,
                "imageUrl" to lesson.imageUrl,
                "isActive" to lesson.isActive,
                "createdAt" to lesson.createdAt
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
                .set(lesson.copy(isStarted = false)) // Don't store isStarted status
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
