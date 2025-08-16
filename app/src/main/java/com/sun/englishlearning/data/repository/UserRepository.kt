package com.sun.englishlearning.data.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sun.englishlearning.data.model.User
import kotlinx.coroutines.tasks.await

interface UserRepository {
    suspend fun getUser(userId: String): Result<User?>
    suspend fun createUser(user: User): Result<Unit>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun updateUserStats(userId: String, lessonsCompleted: Int, wordsLearned: Int, totalPoints: Int): Result<Unit>
}

class UserRepositoryImpl : UserRepository {
    private val db = Firebase.firestore

    override suspend fun getUser(userId: String): Result<User?> {
        return try {
            val document = db.collection("users")
                .document(userId)
                .get()
                .await()

            val user = document.toObject(User::class.java)?.copy(id = document.id)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createUser(user: User): Result<Unit> {
        return try {
            db.collection("users")
                .document(user.id)
                .set(user)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            val userMap = mapOf(
                "email" to user.email,
                "displayName" to user.displayName,
                "photoURL" to user.photoURL,
                "provider" to user.provider,
                "isEmailVerified" to user.isEmailVerified,
                "currentLevel" to user.currentLevel,
                "totalPoints" to user.totalPoints,
                "streak" to user.streak,
                "lessonsCompleted" to user.lessonsCompleted,
                "wordsLearned" to user.wordsLearned,
                "lastActiveAt" to user.lastActiveAt,
                "preferences" to user.preferences
            )
            
            db.collection("users")
                .document(user.id)
                .update(userMap)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserStats(
        userId: String,
        lessonsCompleted: Int,
        wordsLearned: Int,
        totalPoints: Int
    ): Result<Unit> {
        return try {
            val updates = mapOf(
                "lessonsCompleted" to lessonsCompleted,
                "wordsLearned" to wordsLearned,
                "totalPoints" to totalPoints
            )
            
            db.collection("users")
                .document(userId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}