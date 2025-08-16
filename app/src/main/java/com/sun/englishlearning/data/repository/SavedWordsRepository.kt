package com.sun.englishlearning.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sun.englishlearning.data.model.SavedWord
import com.sun.englishlearning.data.model.WordType
import kotlinx.coroutines.tasks.await
import java.util.Date

interface SavedWordsRepository {
    suspend fun saveWord(savedWord: SavedWord): Result<String>
    suspend fun getUserSavedWords(userId: String): Result<List<SavedWord>>
    suspend fun deleteWord(wordId: String): Result<Unit>
    suspend fun updateWordType(wordId: String, wordType: WordType): Result<Unit>
    suspend fun isWordSaved(userId: String, word: String): Result<Boolean>
    suspend fun isWordSavedWithType(userId: String, word: String, wordType: WordType): Result<SavedWord?>
    suspend fun deleteWordByUserAndName(userId: String, word: String, wordType: WordType): Result<Unit>
}

class SavedWordsRepositoryImpl : SavedWordsRepository {
    
    private val db: FirebaseFirestore = Firebase.firestore
    
    companion object {
        private const val TAG = "SavedWordsRepository"
        private const val COLLECTION_NAME = "savedWords"
    }
    
    override suspend fun saveWord(savedWord: SavedWord): Result<String> {
        return try {
            Log.d(TAG, "Saving word: ${savedWord.word} for user: ${savedWord.userId}")
            
            val document = db.collection(COLLECTION_NAME).document()
            val wordToSave = savedWord.copy(
                id = document.id,
                createdAt = Date(),
                updatedAt = Date()
            )
            
            document.set(wordToSave).await()
            
            Log.d(TAG, "Successfully saved word: ${savedWord.word}")
            Result.success(document.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving word: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getUserSavedWords(userId: String): Result<List<SavedWord>> {
        return try {
            Log.d(TAG, "Getting saved words for user: $userId")
            
            val snapshot = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val savedWords = snapshot.documents.mapNotNull { document ->
                document.toObject(SavedWord::class.java)?.copy(id = document.id)
            }.sortedByDescending { it.createdAt }
            
            Log.d(TAG, "Found ${savedWords.size} saved words for user: $userId")
            Result.success(savedWords)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting saved words: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun deleteWord(wordId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting word: $wordId")
            
            db.collection(COLLECTION_NAME)
                .document(wordId)
                .delete()
                .await()
            
            Log.d(TAG, "Successfully deleted word: $wordId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting word: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun updateWordType(wordId: String, wordType: WordType): Result<Unit> {
        return try {
            Log.d(TAG, "Updating word type: $wordId to ${wordType.name}")
            
            db.collection(COLLECTION_NAME)
                .document(wordId)
                .update(
                    mapOf(
                        "wordType" to wordType.value,
                        "updatedAt" to Date()
                    )
                )
                .await()
            
            Log.d(TAG, "Successfully updated word type: $wordId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating word type: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun isWordSaved(userId: String, word: String): Result<Boolean> {
        return try {
            Log.d(TAG, "Checking if word is saved: $word for user: $userId")
            
            val snapshot = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("word", word)
                .get()
                .await()
            
            val isSaved = !snapshot.isEmpty
            Log.d(TAG, "Word $word is saved: $isSaved")
            Result.success(isSaved)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if word is saved: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun isWordSavedWithType(userId: String, word: String, wordType: WordType): Result<SavedWord?> {
        return try {
            Log.d(TAG, "Checking if word is saved with type: $word for user: $userId, type: ${wordType.name}")
            
            val snapshot = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("word", word)
                .whereEqualTo("wordType", wordType.value)
                .get()
                .await()
            
            val savedWord = snapshot.documents.firstOrNull()?.toObject(SavedWord::class.java)?.copy(
                id = snapshot.documents.firstOrNull()?.id ?: ""
            )
            
            Log.d(TAG, "Word $word with type ${wordType.name} found: ${savedWord != null}")
            Result.success(savedWord)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if word is saved with type: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun deleteWordByUserAndName(userId: String, word: String, wordType: WordType): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting word by user and name: $word for user: $userId, type: ${wordType.name}")
            
            val snapshot = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("word", word)
                .whereEqualTo("wordType", wordType.value)
                .get()
                .await()
            
            snapshot.documents.forEach { document ->
                document.reference.delete().await()
            }
            
            Log.d(TAG, "Successfully deleted word: $word for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting word by user and name: ${e.message}", e)
            Result.failure(e)
        }
    }
}