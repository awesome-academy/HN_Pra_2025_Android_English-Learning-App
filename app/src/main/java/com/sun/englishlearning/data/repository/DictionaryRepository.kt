package com.sun.englishlearning.data.repository

import android.util.Log
import com.sun.englishlearning.data.api.DictionaryApiService
import com.sun.englishlearning.data.model.WordApiResponse
import com.sun.englishlearning.data.model.WordSearchResult
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface DictionaryRepository {
    suspend fun searchWord(word: String): Result<WordSearchResult>
}

class DictionaryRepositoryImpl : DictionaryRepository {
    
    companion object {
        private const val TAG = "DictionaryRepository"
        private const val BASE_URL = "https://api.dictionaryapi.dev/api/v2/entries/"
    }
    
    private val apiService: DictionaryApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DictionaryApiService::class.java)
    }
    
    override suspend fun searchWord(word: String): Result<WordSearchResult> {
        return try {
            Log.d(TAG, "Searching for word: $word")
            
            val response = apiService.searchWord(word.lowercase().trim())
            
            if (response.isSuccessful) {
                val apiResponseList = response.body()
                if (!apiResponseList.isNullOrEmpty()) {
                    val wordData = apiResponseList.first()
                    val searchResult = mapToWordSearchResult(wordData)
                    Log.d(TAG, "Successfully found word: ${searchResult.word}")
                    Result.success(searchResult)
                } else {
                    Log.w(TAG, "No data found for word: $word")
                    Result.failure(Exception("Word not found"))
                }
            } else {
                Log.e(TAG, "API error: ${response.code()} - ${response.message()}")
                Result.failure(Exception("Failed to search word: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception searching word: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    private fun mapToWordSearchResult(apiResponse: WordApiResponse): WordSearchResult {
        val firstMeaning = apiResponse.meanings.firstOrNull()
        val firstDefinition = firstMeaning?.definitions?.firstOrNull()
        val firstPhonetic = apiResponse.phonetics.firstOrNull { it.text.isNotEmpty() }
        val audioPhonetic = apiResponse.phonetics.firstOrNull { it.audio.isNotEmpty() }
        
        return WordSearchResult(
            word = apiResponse.word,
            ipa = firstPhonetic?.text ?: "",
            partOfSpeech = firstMeaning?.partOfSpeech ?: "",
            definition = firstDefinition?.definition ?: "",
            example = firstDefinition?.example ?: "",
            soundUrl = audioPhonetic?.audio ?: "",
            isFavorite = false
        )
    }
}