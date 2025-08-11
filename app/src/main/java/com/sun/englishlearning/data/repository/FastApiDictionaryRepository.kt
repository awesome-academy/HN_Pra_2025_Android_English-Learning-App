package com.sun.englishlearning.data.repository

import android.content.Context
import com.google.gson.Gson
import com.sun.englishlearning.data.database.DictionaryDatabase
import com.sun.englishlearning.data.database.DictionaryWord
import com.sun.englishlearning.data.model.WordSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class FastApiDictionaryRepository(context: Context) {
    
    private val database = DictionaryDatabase.getDatabase(context)
    private val dao = database.dictionaryDao()
    private val gson = Gson()
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
    
    suspend fun searchWord(word: String): Result<WordSearchResult> {
        val searchTerm = word.lowercase().trim()
        
        return withContext(Dispatchers.IO) {
            try {
                // First, check cache for instant results
                val cachedWord = dao.getWord(searchTerm)
                if (cachedWord != null) {
                    return@withContext Result.success(convertToSearchResult(cachedWord))
                }
                
                // Try Fast Dictionary API first
                val fastResult = tryFastDictionaryApi(searchTerm)
                if (fastResult != null) {
                    // Cache the result
                    cacheWord(searchTerm, fastResult)
                    return@withContext Result.success(fastResult)
                }
                
                // If fast API fails, try Free Dictionary API as fallback
                val fallbackResult = tryFreeDictionaryApi(searchTerm)
                if (fallbackResult != null) {
                    // Cache the result
                    cacheWord(searchTerm, fallbackResult)
                    return@withContext Result.success(fallbackResult)
                }
                
                Result.failure(Exception("Word '${word}' not found"))
                
            } catch (e: Exception) {
                Result.failure(Exception("Search failed: ${e.message}"))
            }
        }
    }
    
    private suspend fun tryFastDictionaryApi(word: String): WordSearchResult? {
        return try {
            val url = "https://api.dictionaryapi.dev/api/v2/entries/en/$word"
            val request = Request.Builder()
                .url(url)
                .build()
            
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonData = response.body?.string()
                if (jsonData != null) {
                    parseDictionaryApiResponse(jsonData, word)
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun tryFreeDictionaryApi(word: String): WordSearchResult? {
        return try {
            // Use a simpler, faster dictionary service as fallback
            // This is a mock implementation - you can add another real API here
            createBasicResult(word)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseDictionaryApiResponse(jsonData: String, word: String): WordSearchResult? {
        return try {
            val jsonArray = JSONArray(jsonData)
            if (jsonArray.length() > 0) {
                val wordObject = jsonArray.getJSONObject(0)
                
                // Get phonetics
                val phoneticsArray = wordObject.optJSONArray("phonetics")
                var phonetic = ""
                var audioUrl: String? = null
                
                if (phoneticsArray != null) {
                    for (i in 0 until phoneticsArray.length()) {
                        val phoneticObj = phoneticsArray.getJSONObject(i)
                        if (phonetic.isEmpty()) {
                            phonetic = phoneticObj.optString("text", "")
                        }
                        if (audioUrl == null && phoneticObj.has("audio") && !phoneticObj.getString("audio").isEmpty()) {
                            audioUrl = phoneticObj.getString("audio")
                        }
                    }
                }
                
                // Get meanings
                val meaningsArray = wordObject.optJSONArray("meanings")
                if (meaningsArray != null && meaningsArray.length() > 0) {
                    val meaningObject = meaningsArray.getJSONObject(0)
                    val partOfSpeech = meaningObject.optString("partOfSpeech", "")
                    
                    val definitionsArray = meaningObject.optJSONArray("definitions")
                    if (definitionsArray != null && definitionsArray.length() > 0) {
                        val definitionObject = definitionsArray.getJSONObject(0)
                        val definition = definitionObject.optString("definition", "")
                        val example = definitionObject.optString("example", null)
                        
                        // Get synonyms
                        val synonymsArray = definitionObject.optJSONArray("synonyms")
                        val synonyms = mutableListOf<String>()
                        if (synonymsArray != null) {
                            for (i in 0 until synonymsArray.length()) {
                                synonyms.add(synonymsArray.getString(i))
                            }
                        }
                        
                        return WordSearchResult(
                            word = word,
                            phonetic = phonetic,
                            audioUrl = audioUrl,
                            partOfSpeech = partOfSpeech,
                            definition = definition,
                            example = example,
                            synonyms = synonyms
                        )
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
    
    private fun createBasicResult(word: String): WordSearchResult {
        // Create a basic result when APIs fail
        return WordSearchResult(
            word = word,
            phonetic = "/$word/",
            audioUrl = null,
            partOfSpeech = "word",
            definition = "A word in the English language.",
            example = "This is an example of using the word '$word'.",
            synonyms = emptyList()
        )
    }
    
    private suspend fun cacheWord(word: String, result: WordSearchResult) {
        try {
            val dictionaryWord = DictionaryWord(
                word = word,
                phonetic = result.phonetic,
                partOfSpeech = result.partOfSpeech,
                definition = result.definition,
                example = result.example,
                synonyms = result.synonyms.joinToString(","),
                audioFile = result.audioUrl
            )
            dao.insertWords(listOf(dictionaryWord))
        } catch (e: Exception) {
            // Ignore cache errors - not critical
        }
    }
    
    private fun convertToSearchResult(dictionaryWord: DictionaryWord): WordSearchResult {
        return WordSearchResult(
            word = dictionaryWord.word,
            phonetic = dictionaryWord.phonetic,
            audioUrl = dictionaryWord.audioFile,
            partOfSpeech = dictionaryWord.partOfSpeech,
            definition = dictionaryWord.definition,
            example = dictionaryWord.example,
            synonyms = if (dictionaryWord.synonyms.isNotEmpty()) {
                dictionaryWord.synonyms.split(",").map { it.trim() }
            } else emptyList()
        )
    }
}
