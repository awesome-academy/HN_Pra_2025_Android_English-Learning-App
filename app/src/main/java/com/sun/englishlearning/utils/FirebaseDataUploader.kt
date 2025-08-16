package com.sun.englishlearning.utils

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sun.englishlearning.data.model.Lesson
import kotlinx.coroutines.tasks.await

/**
 * Utility class to upload simplified lesson data to Firebase
 */
object FirebaseDataUploader {
    
    private const val TAG = "FirebaseDataUploader"
    private val db = Firebase.firestore
    
    /**
     * Upload all lessons from assets to Firebase
     */
    suspend fun uploadLessonsToFirebase(context: Context): Result<Unit> {
        return try {
            Log.d(TAG, "Starting to upload lessons to Firebase...")
            
            // Load lessons from assets
            val lessons = JsonUtils.loadLessonsFromAssets(context)
            Log.d(TAG, "Loaded ${lessons.size} lessons from assets")
            
            if (lessons.isEmpty()) {
                return Result.failure(Exception("No lessons found in assets"))
            }
            
            // Upload each lesson to Firebase
            lessons.forEach { lesson ->
                uploadSingleLesson(lesson)
                Log.d(TAG, "Uploaded lesson: ${lesson.title}")
            }
            
            Log.d(TAG, "Successfully uploaded all lessons to Firebase")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading lessons to Firebase", e)
            Result.failure(e)
        }
    }
    
    /**
     * Upload a single lesson to Firebase
     */
    private suspend fun uploadSingleLesson(lesson: Lesson) {
        db.collection("lessons")
            .document(lesson.id)
            .set(lesson)
            .await()
    }
    
    /**
     * Clear all existing lessons from Firebase (use with caution)
     */
    suspend fun clearAllLessons(): Result<Unit> {
        return try {
            Log.d(TAG, "Clearing all lessons from Firebase...")
            
            val snapshot = db.collection("lessons").get().await()
            
            snapshot.documents.forEach { document ->
                document.reference.delete().await()
                Log.d(TAG, "Deleted lesson: ${document.id}")
            }
            
            Log.d(TAG, "Successfully cleared all lessons from Firebase")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing lessons from Firebase", e)
            Result.failure(e)
        }
    }
    
    /**
     * Upload lessons with option to clear existing data first
     */
    suspend fun uploadLessonsWithClear(context: Context, clearFirst: Boolean = false): Result<Unit> {
        return try {
            if (clearFirst) {
                val clearResult = clearAllLessons()
                if (clearResult.isFailure) {
                    return clearResult
                }
            }
            
            uploadLessonsToFirebase(context)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in uploadLessonsWithClear", e)
            Result.failure(e)
        }
    }
}
