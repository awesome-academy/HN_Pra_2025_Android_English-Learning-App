package com.sun.englishlearning.data.repository

import com.sun.englishlearning.data.api.DictionaryApiService
import com.sun.englishlearning.data.model.WordSearchResult
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface DictionaryRepository {
    suspend fun searchWord(word: String): Result<WordSearchResult>
}

class DictionaryRepositoryImpl : DictionaryRepository {
    
    private val apiService: DictionaryApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        
        Retrofit.Builder()
            .baseUrl("https://api.dictionaryapi.dev/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DictionaryApiService::class.java)
    }
    
    override suspend fun searchWord(word: String): Result<WordSearchResult> {
        return try {
            val response = apiService.searchWord(word.lowercase().trim())
            
            if (response.isSuccessful) {
                val dictionaryData = response.body()
                if (dictionaryData != null && dictionaryData.isNotEmpty()) {
                    val entry = dictionaryData[0]
                    val meaning = entry.meanings.firstOrNull()
                    val definition = meaning?.definitions?.firstOrNull()
                    val phonetic = entry.phonetics.firstOrNull { !it.text.isNullOrEmpty() }
                    
                    val result = WordSearchResult(
                        word = entry.word,
                        phonetic = phonetic?.text ?: "",
                        audioUrl = phonetic?.audio?.takeIf { it.isNotEmpty() },
                        partOfSpeech = meaning?.partOfSpeech ?: "",
                        definition = definition?.definition ?: "",
                        example = definition?.example,
                        synonyms = definition?.synonyms ?: emptyList(),
                        antonyms = definition?.antonyms ?: emptyList()
                    )
                    
                    Result.success(result)
                } else {
                    Result.failure(Exception("No definition found for '$word'"))
                }
            } else {
                Result.failure(Exception("Word not found: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
}
