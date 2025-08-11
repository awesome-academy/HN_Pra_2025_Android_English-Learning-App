package com.sun.englishlearning.data.repository

import android.content.Context
import com.sun.englishlearning.data.api.DictionaryApiService
import com.sun.englishlearning.data.database.DictionaryDatabase
import com.sun.englishlearning.data.database.DictionaryWord
import com.sun.englishlearning.data.model.WordSearchResult
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class HybridDictionaryRepository(context: Context) {
    
    private val database = DictionaryDatabase.getDatabase(context)
    private val dao = database.dictionaryDao()
    
    private val apiService: DictionaryApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
        
        Retrofit.Builder()
            .baseUrl("https://api.dictionaryapi.dev/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DictionaryApiService::class.java)
    }
    
    suspend fun searchWord(word: String): Result<WordSearchResult> {
        val searchTerm = word.lowercase().trim()
        
        return try {
            // For now, always call API to ensure fresh data with audio
            // TODO: Re-enable caching after confirming audio works
            // First, try local cache
            // val cachedWord = dao.getWord(searchTerm)
            // if (cachedWord != null) {
            //     return Result.success(convertToSearchResult(cachedWord))
            // }
            
            // If not in cache, call API
            val response = apiService.searchWord(searchTerm)
            
            if (response.isSuccessful) {
                val dictionaryData = response.body()
                if (dictionaryData != null && dictionaryData.isNotEmpty()) {
                    val entry = dictionaryData[0]
                    val meaning = entry.meanings.firstOrNull()
                    val definition = meaning?.definitions?.firstOrNull()
                    val phonetic = entry.phonetics.firstOrNull { !it.text.isNullOrEmpty() }
                    val audioUrl = entry.phonetics.firstOrNull { !it.audio.isNullOrEmpty() }?.audio
                    
                    val result = WordSearchResult(
                        word = entry.word,
                        phonetic = phonetic?.text ?: "",
                        audioUrl = audioUrl,
                        partOfSpeech = meaning?.partOfSpeech ?: "",
                        definition = definition?.definition ?: "",
                        example = definition?.example,
                        synonyms = definition?.synonyms ?: emptyList(),
                        antonyms = definition?.antonyms ?: emptyList()
                    )
                    
                    // Cache the result for faster future lookups
                    cacheWord(entry.word, result)
                    
                    Result.success(result)
                } else {
                    Result.failure(Exception("No definition found for '$word'"))
                }
            } else {
                Result.failure(Exception("Word not found: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Search failed: ${e.message}"))
        }
    }
    
    private suspend fun cacheWord(word: String, result: WordSearchResult) {
        try {
            val dictionaryWord = DictionaryWord(
                word = word.lowercase(),
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
    
    suspend fun initializeDictionary() {
        try {
            val wordCount = dao.getWordCount()
            if (wordCount == 0) {
                // Insert a few common words for offline fallback
                val commonWords = getCommonWords()
                dao.insertWords(commonWords)
            }
        } catch (e: Exception) {
            // Handle initialization error
        }
    }
    
    private fun getCommonWords(): List<DictionaryWord> {
        return listOf(
            DictionaryWord("hello", "/həˈloʊ/", "exclamation", "Used as a greeting or to begin a phone conversation.", "Hello, how are you?", "", "https://api.dictionaryapi.dev/media/pronunciations/en/hello-au.mp3"),
            DictionaryWord("world", "/wɜːrld/", "noun", "The earth, together with all of its countries, peoples, and natural features.", "Welcome to the world."),
            DictionaryWord("love", "/lʌv/", "noun", "An intense feeling of deep affection.", "I love spending time with family.", "adore,cherish,care"),
            DictionaryWord("happy", "/ˈhæpi/", "adjective", "Feeling or showing pleasure or contentment.", "She looks very happy today.", "joyful,cheerful,glad"),
            DictionaryWord("beautiful", "/ˈbjuːtɪfəl/", "adjective", "Pleasing the senses or mind aesthetically.", "What a beautiful sunset!", "pretty,lovely,gorgeous"),
        )
    }
}
