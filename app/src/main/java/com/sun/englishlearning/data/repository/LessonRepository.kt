package com.sun.englishlearning.data.repository

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.data.model.LessonDifficulty
import kotlinx.coroutines.tasks.await

interface LessonRepository {
    suspend fun getAllLessons(): Result<List<Lesson>>
    suspend fun getLessonsByCourse(courseId: String): Result<List<Lesson>>
    suspend fun getLessonsByDifficulty(difficulty: LessonDifficulty): Result<List<Lesson>>
    suspend fun getLesson(lessonId: String): Result<Lesson?>
    suspend fun getLessonsForUser(userId: String): Result<List<Lesson>>
    suspend fun getSuggestedLessons(userId: String): Result<List<Lesson>>
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
