package com.sun.englishlearning.data.repository.source.remote

import android.util.Log
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.repository.source.remote.fetchjson.GetJsonFromUrl
import com.sun.englishlearning.data.repository.source.remote.fetchjson.ParseDataWithJson

class VocabularyRemoteDataSource {
    
    companion object {
        private const val TAG = "VocabularyRemoteDataSource"
        private const val DICTIONARY_API_BASE_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/"
    }
    
    private val jsonFetcher = GetJsonFromUrl()
    private val jsonParser = ParseDataWithJson()
    
    /**
     * Fetch word definition from Dictionary API
     */
    fun fetchWordDefinition(word: String): Word? {
        return try {
            Log.d(TAG, "Fetching definition for word: $word")
            
            val url = "$DICTIONARY_API_BASE_URL$word"
            val jsonResponse = jsonFetcher.fetchJsonFromUrl(url)
            
            if (jsonResponse.isEmpty()) {
                Log.w(TAG, "Empty response for word: $word")
                return createFallbackWord(word)
            }
            
            if (!jsonParser.isValidJsonResponse(jsonResponse)) {
                Log.w(TAG, "Invalid JSON response for word: $word")
                return createFallbackWord(word)
            }
            
            val wordDefinition = jsonParser.parseWordDefinition(jsonResponse, word)
            wordDefinition ?: createFallbackWord(word)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching definition for word: $word", e)
            createFallbackWord(word)
        }
    }
    
    /**
     * Create fallback word when API fails
     */
    private fun createFallbackWord(word: String): Word {
        return Word(
            id = word.hashCode().toString(),
            word = word,
            definition = "Definition not available",
            soundUrl = "",
            example = "Example not available",
            phonetic = "",
            partOfSpeech = ""
        )
    }
    
    /**
     * Check if Dictionary API is available
     */
    fun isDictionaryApiAvailable(): Boolean {
        return jsonFetcher.isUrlReachable("${DICTIONARY_API_BASE_URL}hello")
    }
}
