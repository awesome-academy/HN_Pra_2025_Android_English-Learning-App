package com.sun.englishlearning.data.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sun.englishlearning.data.model.Word
import kotlinx.coroutines.tasks.await

interface WordRepository {
suspend fun getAllWords(): Result<List<Word>>
    suspend fun getWordsByLesson(lessonId: String): Result<List<Word>>
    suspend fun getWordsByDifficulty(difficulty: String): Result<List<Word>>
    suspend fun getWord(wordId: String): Result<Word?>
    suspend fun createWord(word: Word): Result<Unit>
    suspend fun updateWord(word: Word): Result<Unit>
    suspend fun deleteWord(wordId: String): Result<Unit>
}

class WordRepositoryImpl : WordRepository {
    private val db = Firebase.firestore

    override suspend fun getAllWords(): Result<List<Word>> {
        return try {
            val snapshot = db.collection("words")
                .get()
                .await()

            val words = snapshot.documents.mapNotNull { document ->
                document.toObject(Word::class.java)?.copy(id = document.id)
            }
            Result.success(words)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWordsByLesson(lessonId: String): Result<List<Word>> {
        return try {
            val snapshot = db.collection("words")
                .whereEqualTo("lessonId", lessonId)
                .get()
                .await()

            val words = snapshot.documents.mapNotNull { document ->
                document.toObject(Word::class.java)?.copy(id = document.id)
            }
            Result.success(words)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWordsByDifficulty(difficulty: String): Result<List<Word>> {
        return try {
            val snapshot = db.collection("words")
                .whereEqualTo("difficulty", difficulty)
                .get()
                .await()

            val words = snapshot.documents.mapNotNull { document ->
                document.toObject(Word::class.java)?.copy(id = document.id)
            }
            Result.success(words)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWord(wordId: String): Result<Word?> {
        return try {
            val document = db.collection("words")
                .document(wordId)
                .get()
                .await()

            val word = document.toObject(Word::class.java)?.copy(id = document.id)
            Result.success(word)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createWord(word: Word): Result<Unit> {
        return try {
            db.collection("words")
                .document(word.id)
                .set(word)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateWord(word: Word): Result<Unit> {
        return try {
            val wordMap = mapOf(
                "word" to word.word,
                "definition" to word.definition,
                "pronunciation" to word.pronunciation,
                "phonetic" to word.phonetic,
                "partOfSpeech" to word.partOfSpeech,
                "example" to word.example,
                "soundUrl" to word.soundUrl,
                "imageUrl" to word.imageUrl,
                "lessonId" to word.lessonId,
                "difficulty" to word.difficulty
            )
            
            db.collection("words")
                .document(word.id)
                .update(wordMap)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteWord(wordId: String): Result<Unit> {
        return try {
            db.collection("words")
                .document(wordId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
