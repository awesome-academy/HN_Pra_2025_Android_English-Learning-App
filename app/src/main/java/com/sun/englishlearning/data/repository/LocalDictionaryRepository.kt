package com.sun.englishlearning.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sun.englishlearning.data.model.WordSearchResult
import java.io.InputStreamReader

data class LocalDictionaryData(
    val words: List<LocalWord>
)

data class LocalWord(
    val word: String,
    val phonetic: String,
    val partOfSpeech: String,
    val definition: String,
    val example: String? = null,
    val synonyms: List<String>? = null,
    val audioUrl: String? = null
)

class LocalDictionaryRepository(private val context: Context) {
    
    private val gson = Gson()
    private var dictionaryData: LocalDictionaryData? = null
    
    init {
        loadDictionary()
    }
    
    private fun loadDictionary() {
        try {
            val inputStream = context.assets.open("dictionary.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<LocalDictionaryData>() {}.type
            dictionaryData = gson.fromJson(reader, type)
            reader.close()
        } catch (e: Exception) {
            // Handle error loading dictionary
            dictionaryData = LocalDictionaryData(emptyList())
        }
    }
    
    suspend fun searchWord(word: String): Result<WordSearchResult> {
        return try {
            val searchTerm = word.lowercase().trim()
            val foundWord = dictionaryData?.words?.find { 
                it.word.lowercase() == searchTerm 
            }
            
            if (foundWord != null) {
                val result = WordSearchResult(
                    word = foundWord.word,
                    phonetic = foundWord.phonetic,
                    audioUrl = foundWord.audioUrl,
                    partOfSpeech = foundWord.partOfSpeech,
                    definition = foundWord.definition,
                    example = foundWord.example,
                    synonyms = foundWord.synonyms ?: emptyList()
                )
                Result.success(result)
            } else {
                Result.failure(Exception("Word '$word' not found. Try: hello, love, happy, learn, food, travel, music, computer..."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Search error: ${e.message}"))
        }
    }
    
    fun getAvailableWords(): List<String> {
        return dictionaryData?.words?.map { it.word } ?: emptyList()
    }
    
    fun getSampleWords(): String {
        val words = dictionaryData?.words?.take(8)?.map { it.word } ?: listOf("hello", "love", "happy")
        return words.joinToString(", ")
    }
}
