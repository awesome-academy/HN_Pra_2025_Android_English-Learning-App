package com.sun.englishlearning.data.repository.source

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.repository.source.local.VocabularyLocalDataSource
import com.sun.englishlearning.data.repository.source.remote.OnResultListener
import com.sun.englishlearning.data.repository.source.remote.VocabularyRemoteDataSource
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class VocabularyDataSource {
    
    companion object {
        private const val TAG = "VocabularyDataSource"
        private const val CONCURRENT_REQUESTS = 5 // Number of concurrent API requests
    }
    
    private val localDataSource = VocabularyLocalDataSource()
    private val remoteDataSource = VocabularyRemoteDataSource()
    private val mainHandler = Handler(Looper.getMainLooper())
    
    // Thread pool for concurrent API requests
    private val apiExecutor: ExecutorService = Executors.newFixedThreadPool(CONCURRENT_REQUESTS)
    
    // Cache for word definitions with LRU eviction policy
    private val wordCache = mutableMapOf<String, Word>()
    private val cacheMaxSize = 500 // Maximum number of cached words
    
    /**
     * Get vocabulary for a lesson (words + definitions from API)
     * Optimized for speed with concurrent API requests and better caching
     */
    fun getVocabularyForLesson(
        context: Context,
        lessonId: String,
        listener: OnResultListener<List<Word>>
    ) {
        listener.onLoading()
        
        // Use a background thread for the overall operation
        apiExecutor.execute {
            try {
                // 1. Get word list from local JSON
                val words = localDataSource.getVocabularyWords(context, lessonId)
                
                if (words.isEmpty()) {
                    mainHandler.post {
                        listener.onError("No vocabulary found for lesson $lessonId")
                    }
                    return@execute
                }
                
                Log.d(TAG, "Found ${words.size} words for lesson $lessonId")
                
                // 2. Fetch definitions from API with concurrent requests
                val wordDefinitions = fetchWordDefinitionsConcurrently(words, lessonId)
                
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
     * Fetch word definitions concurrently for better performance
     */
    private fun fetchWordDefinitionsConcurrently(words: List<String>, lessonId: String): List<Word> {
        val wordDefinitions = mutableListOf<Word>()
        val fetchTasks = mutableListOf<() -> Unit>()
        
        // Create fetch tasks for each word
        words.forEach { word ->
            fetchTasks.add {
                val definition = fetchWordDefinitionWithCache(word, lessonId)
                synchronized(wordDefinitions) {
                    definition?.let { wordDefinitions.add(it) }
                }
            }
        }
        
        // Execute fetch tasks concurrently
        val futures = fetchTasks.map { task ->
            apiExecutor.submit(task)
        }
        
        // Wait for all tasks to complete
        futures.forEach { future ->
            try {
                future.get(10, TimeUnit.SECONDS) // 10 second timeout per task
            } catch (e: Exception) {
                Log.e(TAG, "Error waiting for fetch task", e)
            }
        }
        
        // Sort results to match original word order
        val wordOrderMap = words.withIndex().associate { it.value to it.index }
        return wordDefinitions.sortedBy { wordOrderMap[it.word] }
    }
    
    /**
     * Fetch word definition with caching
     */
    private fun fetchWordDefinitionWithCache(word: String, lessonId: String): Word? {
        // Check cache first
        val cachedWord = wordCache[word.lowercase()]
        if (cachedWord != null) {
            Log.d(TAG, "Using cached definition for: $word")
            // Update lessonId if it's missing
            val updatedWord = if (cachedWord.lessonId.isEmpty()) {
                cachedWord.copy(lessonId = lessonId)
            } else {
                cachedWord
            }
            return updatedWord
        }
        
        // Fetch from API
        val wordDefinition = remoteDataSource.fetchWordDefinition(word)
        if (wordDefinition != null) {
            // Add lessonId to the word definition
            val wordWithLessonId = wordDefinition.copy(lessonId = lessonId)
            
            // Cache the result with size limit
            synchronized(wordCache) {
                // Evict oldest entries if cache is full
                if (wordCache.size >= cacheMaxSize) {
                    val oldestKey = wordCache.keys.firstOrNull()
                    if (oldestKey != null) {
                        wordCache.remove(oldestKey)
                    }
                }
                
                wordCache[word.lowercase()] = wordWithLessonId
            }
            Log.d(TAG, "Fetched and cached definition for: $word")
            return wordWithLessonId
        }
        
        return wordDefinition
    }
    
    /**
     * Get single word definition
     */
    fun getWordDefinition(word: String, lessonId: String, listener: OnResultListener<Word>) {
        listener.onLoading()
        
        apiExecutor.execute {
            try {
                val wordDefinition = fetchWordDefinitionWithCache(word, lessonId)

                mainHandler.post {
                    if (wordDefinition != null) {
                        listener.onSuccess(wordDefinition)
                    } else {
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
        apiExecutor.execute {
            val isAvailable = remoteDataSource.isDictionaryApiAvailable()
            mainHandler.post {
                listener.onSuccess(isAvailable)
            }
        }
    }
    
    /**
     * Shutdown executor service
     */
    fun shutdown() {
        apiExecutor.shutdown()
        try {
            if (!apiExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                apiExecutor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            apiExecutor.shutdownNow()
        }
    }
}
