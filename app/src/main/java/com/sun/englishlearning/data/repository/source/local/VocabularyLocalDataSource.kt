package com.sun.englishlearning.data.repository.source.local

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

class VocabularyLocalDataSource {
    
    companion object {
        private const val TAG = "VocabularyLocalDataSource"
        private const val VOCABULARY_FILE = "vocabulary.json"
    }
    
    /**
     * Load vocabulary words for a specific lesson from assets
     */
    fun getVocabularyWords(context: Context, lessonId: String): List<String> {
        return try {
            Log.d(TAG, "Loading vocabulary for lesson: $lessonId")
            
            val jsonString = loadJsonFromAssets(context, VOCABULARY_FILE)
            if (jsonString.isEmpty()) {
                Log.e(TAG, "JSON string is empty")
                return emptyList()
            }
            
            parseVocabularyWords(jsonString, lessonId)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading vocabulary from assets", e)
            emptyList()
        }
    }
    
    /**
     * Parse vocabulary words from JSON for specific lesson
     */
    private fun parseVocabularyWords(jsonString: String, lessonId: String): List<String> {
        return try {
            val jsonObject = JSONObject(jsonString)
            val vocabularyObject = jsonObject.getJSONObject("vocabulary")
            
            if (vocabularyObject.has(lessonId)) {
                val lessonObject = vocabularyObject.getJSONObject(lessonId)
                val wordsArray = lessonObject.getJSONArray("words")
                
                val words = mutableListOf<String>()
                for (i in 0 until wordsArray.length()) {
                    words.add(wordsArray.getString(i))
                }
                
                Log.d(TAG, "Loaded ${words.size} words for lesson $lessonId")
                words
            } else {
                Log.w(TAG, "No vocabulary found for lesson: $lessonId")
                emptyList()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing vocabulary JSON", e)
            emptyList()
        }
    }

    /**
     * Read JSON file content from assets folder
     */
    private fun loadJsonFromAssets(context: Context, fileName: String): String {
        return try {
            val inputStream: InputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (e: IOException) {
            Log.e(TAG, "Error reading JSON file from assets", e)
            ""
        }
    }
}
