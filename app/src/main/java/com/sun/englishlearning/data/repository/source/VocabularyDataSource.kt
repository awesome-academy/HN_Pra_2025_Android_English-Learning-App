package com.sun.englishlearning.data.repository.source

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.repository.source.local.VocabularyLocalDataSource
import com.sun.englishlearning.data.repository.source.remote.OnResultListener
import com.sun.englishlearning.data.repository.source.remote.VocabularyRemoteDataSource
import kotlin.concurrent.thread

class VocabularyDataSource {
    
    companion object {
        private const val TAG = "VocabularyDataSource"
    }
    
    private val localDataSource = VocabularyLocalDataSource()
    private val remoteDataSource = VocabularyRemoteDataSource()
    private val mainHandler = Handler(Looper.getMainLooper())
    
    // Cache for word definitions
    private val wordCache = mutableMapOf<String, Word>()
    
    /**
     * Get vocabulary for a lesson (words + definitions from API)
     */
    fun getVocabularyForLesson(
        context: Context, 
        lessonId: String, 
        listener: OnResultListener<List<Word>>
    ) {
        listener.onLoading()
        
        thread {
            try {
                // 1. Get word list from local JSON
                val words = localDataSource.getVocabularyWords(context, lessonId)
                
                if (words.isEmpty()) {
                    mainHandler.post {
                        listener.onError("No vocabulary found for lesson $lessonId")
                    }
                    return@thread
                }
                
                Log.d(TAG, "Found ${words.size} words for lesson $lessonId")
                
                // 2. Fetch definitions from API
                val wordDefinitions = mutableListOf<Word>()
                
                words.forEach { word ->
                    // Check cache first
                    val cachedWord = wordCache[word.lowercase()]
                    if (cachedWord != null) {
                        wordDefinitions.add(cachedWord)
                        Log.d(TAG, "Using cached definition for: $word")
                    } else {
                        // Fetch from API
                        val wordDefinition = remoteDataSource.fetchWordDefinition(word)
                        if (wordDefinition != null) {
                            wordDefinitions.add(wordDefinition)
                            // Cache the result
                            wordCache[word.lowercase()] = wordDefinition
                            Log.d(TAG, "Fetched and cached definition for: $word")
                        }
                    }
                    
                    // Small delay to avoid overwhelming the API
                    Thread.sleep(100)
                }
                
                mainHandler.post {
                    listener.onSuccess(wordDefinitions)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error getting vocabulary for lesson $lessonId", e)
                mainHandler.post {
                    listener.onError(e.message ?: "Unknown error")
                }
            }
        }
    }
    
    /**
     * Get single word definition
     */
    fun getWordDefinition(word: String, listener: OnResultListener<Word>) {
        listener.onLoading()
        
        thread {
            try {
                // Check cache first
                val cachedWord = wordCache[word.lowercase()]
                if (cachedWord != null) {
                    mainHandler.post {
                        listener.onSuccess(cachedWord)
                    }
                    return@thread
                }
                
                // Fetch from API
                val wordDefinition = remoteDataSource.fetchWordDefinition(word)
                if (wordDefinition != null) {
                    // Cache the result
                    wordCache[word.lowercase()] = wordDefinition
                    
                    mainHandler.post {
                        listener.onSuccess(wordDefinition)
                    }
                } else {
                    mainHandler.post {
                        listener.onError("Failed to fetch definition for: $word")
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error getting word definition for: $word", e)
                mainHandler.post {
                    listener.onError(e.message ?: "Unknown error")
                }
            }
        }
    }
    
    /**
     * Clear cache
     */
    fun clearCache() {
        wordCache.clear()
        Log.d(TAG, "Cache cleared")
    }
    
    /**
     * Check if API is available
     */
    fun checkApiAvailability(listener: OnResultListener<Boolean>) {
        thread {
            val isAvailable = remoteDataSource.isDictionaryApiAvailable()
            mainHandler.post {
                listener.onSuccess(isAvailable)
            }
        }
    }
}
