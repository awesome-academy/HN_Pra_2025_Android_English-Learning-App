package com.sun.englishlearning.data.repository

import android.content.Context
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.utils.JsonUtils
import kotlinx.coroutines.tasks.await

interface LessonRepository {
    suspend fun getAllLessons(): Result<List<Lesson>>
    suspend fun getLesson(lessonId: String): Result<Lesson?>
    suspend fun createLesson(lesson: Lesson): Result<Unit>
    suspend fun updateLesson(lesson: Lesson): Result<Unit>
    fun getAllLessonsFromAssets(context: Context): List<Lesson>
}

class LessonRepositoryImpl : LessonRepository {
    private val db = Firebase.firestore

    override suspend fun getAllLessons(): Result<List<Lesson>> {
        return try {
            val snapshot = db.collection("lessons")
                .get()
                .await()

            val lessons = snapshot.documents.mapNotNull { document ->
                document.toObject(Lesson::class.java)?.copy(id = document.id)
            }
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

    override suspend fun updateLesson(lesson: Lesson): Result<Unit> {
        return try {
            val lessonMap = mapOf(
                "name" to lesson.name,
                "description" to lesson.description,
                "image" to lesson.image,
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

    override fun getAllLessonsFromAssets(context: Context): List<Lesson> {
        return JsonUtils.loadLessonsFromAssets(context)
    }
}
