package com.sun.englishlearning.data.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sun.englishlearning.data.model.UserLessonProgress
import kotlinx.coroutines.tasks.await

interface UserLessonProgressRepository {
    suspend fun getUserLessonProgress(userId: String, lessonId: String): Result<UserLessonProgress?>
    suspend fun getUserProgressByLesson(lessonId: String): Result<List<UserLessonProgress>>
    suspend fun getUserProgressByUser(userId: String): Result<List<UserLessonProgress>>
    suspend fun getCompletedLessons(userId: String): Result<List<UserLessonProgress>>
    suspend fun getInProgressLessons(userId: String): Result<List<UserLessonProgress>>
    suspend fun createProgress(progress: UserLessonProgress): Result<Unit>
    suspend fun updateProgress(progress: UserLessonProgress): Result<Unit>
    suspend fun updateWordsLearned(userId: String, lessonId: String, wordIds: List<String>): Result<Unit>
    suspend fun markLessonCompleted(userId: String, lessonId: String, finalScore: Int): Result<Unit>
}

class UserLessonProgressRepositoryImpl : UserLessonProgressRepository {
    private val db = Firebase.firestore

    override suspend fun getUserLessonProgress(userId: String, lessonId: String): Result<UserLessonProgress?> {
        return try {
            val snapshot = db.collection("userLessonProgress")
                .whereEqualTo("userId", userId)
                .whereEqualTo("lessonId", lessonId)
                .get()
                .await()

            val progress = snapshot.documents.firstOrNull()?.let { document ->
                document.toObject(UserLessonProgress::class.java)?.copy(id = document.id)
            }
            Result.success(progress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserProgressByLesson(lessonId: String): Result<List<UserLessonProgress>> {
        return try {
            val snapshot = db.collection("userLessonProgress")
                .whereEqualTo("lessonId", lessonId)
                .get()
                .await()

            val progressList = snapshot.documents.mapNotNull { document ->
                document.toObject(UserLessonProgress::class.java)?.copy(id = document.id)
            }
            Result.success(progressList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserProgressByUser(userId: String): Result<List<UserLessonProgress>> {
        return try {
            val snapshot = db.collection("userLessonProgress")
                .whereEqualTo("userId", userId)
                .orderBy("lastAccessedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val progressList = snapshot.documents.mapNotNull { document ->
                document.toObject(UserLessonProgress::class.java)?.copy(id = document.id)
            }
            Result.success(progressList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCompletedLessons(userId: String): Result<List<UserLessonProgress>> {
        return try {
            val snapshot = db.collection("userLessonProgress")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isCompleted", true)
                .orderBy("completedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val progressList = snapshot.documents.mapNotNull { document ->
                document.toObject(UserLessonProgress::class.java)?.copy(id = document.id)
            }
            Result.success(progressList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getInProgressLessons(userId: String): Result<List<UserLessonProgress>> {
        return try {
            val snapshot = db.collection("userLessonProgress")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isStarted", true)
                .whereEqualTo("isCompleted", false)
                .orderBy("lastAccessedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val progressList = snapshot.documents.mapNotNull { document ->
                document.toObject(UserLessonProgress::class.java)?.copy(id = document.id)
            }
            Result.success(progressList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createProgress(progress: UserLessonProgress): Result<Unit> {
        return try {
            db.collection("userLessonProgress")
                .document(progress.id)
                .set(progress)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProgress(progress: UserLessonProgress): Result<Unit> {
        return try {
            val progressMap = mapOf(
                "userId" to progress.userId,
                "lessonId" to progress.lessonId,
                "isStarted" to progress.isStarted,
                "isCompleted" to progress.isCompleted,
                "currentPoints" to progress.currentPoints,
                "totalPoints" to progress.totalPoints,
                "progressPercentage" to progress.progressPercentage,
                "timeSpentMinutes" to progress.timeSpentMinutes,
                "attempts" to progress.attempts,
                "bestScore" to progress.bestScore,
                "wordsLearned" to progress.wordsLearned,
                "totalWords" to progress.totalWords,
                "completedExercises" to progress.completedExercises,
                "learnedWordIds" to progress.learnedWordIds,
                "startedAt" to progress.startedAt,
                "completedAt" to progress.completedAt,
                "lastAccessedAt" to progress.lastAccessedAt
            )
            
            db.collection("userLessonProgress")
                .document(progress.id)
                .update(progressMap)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateWordsLearned(userId: String, lessonId: String, wordIds: List<String>): Result<Unit> {
        return try {
            val progressDoc = db.collection("userLessonProgress")
                .whereEqualTo("userId", userId)
                .whereEqualTo("lessonId", lessonId)
                .get()
                .await()
                .documents
                .firstOrNull()

            if (progressDoc != null) {
                val updates = mapOf(
                    "learnedWordIds" to wordIds,
                    "wordsLearned" to wordIds.size,
                    "lastAccessedAt" to java.util.Date()
                )
                
                db.collection("userLessonProgress")
                    .document(progressDoc.id)
                    .update(updates)
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markLessonCompleted(userId: String, lessonId: String, finalScore: Int): Result<Unit> {
        return try {
            val progressDoc = db.collection("userLessonProgress")
                .whereEqualTo("userId", userId)
                .whereEqualTo("lessonId", lessonId)
                .get()
                .await()
                .documents
                .firstOrNull()

            if (progressDoc != null) {
                val updates = mapOf(
                    "isCompleted" to true,
                    "bestScore" to finalScore,
                    "currentPoints" to finalScore,
                    "progressPercentage" to 100,
                    "completedAt" to java.util.Date(),
                    "lastAccessedAt" to java.util.Date()
                )
                
                db.collection("userLessonProgress")
                    .document(progressDoc.id)
                    .update(updates)
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}