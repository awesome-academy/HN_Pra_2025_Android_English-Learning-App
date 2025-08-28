package com.sun.englishlearning.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sun.englishlearning.data.model.SavedWord
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.model.WordType
import kotlinx.coroutines.tasks.await
import java.util.Date

interface SavedWordsRepository {
    suspend fun saveWord(savedWord: SavedWord): Result<String>
    suspend fun getUserSavedWords(userId: String): Result<List<SavedWord>>
    suspend fun getUserWordsByType(userId: String, wordType: WordType): Result<List<SavedWord>>
    suspend fun getWordCountByType(userId: String, wordType: WordType): Result<Int>
    suspend fun deleteWord(wordId: String): Result<Unit>
    suspend fun updateWordType(wordId: String, wordType: WordType): Result<Unit>
    suspend fun isWordSaved(userId: String, word: String): Result<Boolean>
    suspend fun isWordSavedWithType(userId: String, word: String, wordType: WordType): Result<SavedWord?>
    suspend fun deleteWordByUserAndName(userId: String, word: String, wordType: WordType): Result<Unit>
    suspend fun saveOrUpdateTestedWord(userId: String, word: Word, isCorrect: Boolean): Result<Unit>
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
    
    override suspend fun getUserWordsByType(userId: String, wordType: WordType): Result<List<SavedWord>> {
        return try {
            Log.d(TAG, "Getting words by type: ${wordType.name} for user: $userId")
            
            val snapshot = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("wordType", wordType.value)
                .get()
                .await()
            
            val savedWords = snapshot.documents.mapNotNull { document ->
                document.toObject(SavedWord::class.java)?.copy(id = document.id)
            }.sortedByDescending { it.createdAt }
            
            Log.d(TAG, "Found ${savedWords.size} words of type ${wordType.name} for user: $userId")
            Result.success(savedWords)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting words by type: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getWordCountByType(userId: String, wordType: WordType): Result<Int> {
        return try {
            Log.d(TAG, "Getting word count by type: ${wordType.name} for user: $userId")
            
            val snapshot = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("wordType", wordType.value)
                .get()
                .await()
            
            val count = snapshot.size()
            Log.d(TAG, "Found $count words of type ${wordType.name} for user: $userId")
            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting word count by type: ${e.message}", e)
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

    override suspend fun saveOrUpdateTestedWord(userId: String, word: Word, isCorrect: Boolean): Result<Unit> {
        return try {
            Log.d(TAG, "Saving/updating tested word: ${word.word} for user: $userId, isCorrect: $isCorrect")
            val snapshot = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("word", word.word)
                .get()
                .await()
            var streak = 0
            var wordType = WordType.WEAK
            var savedWordId = ""
            if (snapshot.isEmpty) {
                // New word
                if (isCorrect) {
                    streak = 1
                    wordType = WordType.MEDIUM
                }
            } else {
                val doc = snapshot.documents.first()
                val savedWord = doc.toObject(SavedWord::class.java)
                savedWordId = doc.id
                if (isCorrect) {
                    streak = (savedWord?.streak ?: 0) + 1
                    if (streak >= 3) {
                        wordType = WordType.STRONG
                        streak = 0
                    } else {
                        wordType = WordType.MEDIUM
                    }
                } else {
                    streak = 0
                    wordType = WordType.WEAK
                }
            }
            // Save/update word
            if (snapshot.isEmpty) {
                val document = db.collection(COLLECTION_NAME).document()
                val wordToSave = SavedWord(
                    id = document.id,
                    userId = userId,
                    word = word.word,
                    ipa = word.phonetic,
                    partOfSpeech = word.meanings.firstOrNull()?.partOfSpeech ?: "",
                    definition = word.meanings.firstOrNull()?.definitions?.firstOrNull()?.definition ?: "",
                    example = word.meanings.firstOrNull()?.definitions?.firstOrNull()?.example ?: "",
                    soundUrl = word.phonetics.firstOrNull()?.audio ?: "",
                    wordType = wordType.value,
                    createdAt = Date(),
                    updatedAt = Date(),
                    streak = streak
                )
                document.set(wordToSave).await()
            } else {
                db.collection(COLLECTION_NAME)
                    .document(savedWordId)
                    .update(
                        mapOf(
                            "wordType" to wordType.value,
                            "updatedAt" to Date(),
                            "streak" to streak
                        )
                    )
                    .await()
            }
            // Update user points if correct
            if (isCorrect) {
                updateUserPoints(userId, 1)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving/updating tested word: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun updateUserPoints(userId: String, points: Int) {
        val userDoc = db.collection("users").document(userId)
        val snapshot = userDoc.get().await()
        val currentPoints = snapshot.getLong("totalPoints") ?: 0L
        userDoc.update("totalPoints", currentPoints + points).await()
    }
}
