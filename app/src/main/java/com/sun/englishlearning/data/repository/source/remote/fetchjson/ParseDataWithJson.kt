package com.sun.englishlearning.data.repository.source.remote.fetchjson

import android.util.Log
import com.sun.englishlearning.data.model.Word
import org.json.JSONArray
import org.json.JSONObject

class ParseDataWithJson {
    
    companion object {
        private const val TAG = "ParseDataWithJson"
    }
    
    /**
     * Parse word definition from Dictionary API response
     */
    fun parseWordDefinition(jsonResponse: String, word: String): Word? {
        return try {
            Log.d(TAG, "Parsing definition for word: $word")
            
            val jsonArray = JSONArray(jsonResponse)
            if (jsonArray.length() == 0) {
                Log.w(TAG, "No definition found for word: $word")
                return null
            }
            
            val wordObject = jsonArray.getJSONObject(0)
            val meanings = wordObject.getJSONArray("meanings")
            
            if (meanings.length() == 0) {
                Log.w(TAG, "No meanings found for word: $word")
                return null
            }
            
            // Get first meaning
            val firstMeaning = meanings.getJSONObject(0)
            val partOfSpeech = firstMeaning.optString("partOfSpeech", "")
            val definitions = firstMeaning.getJSONArray("definitions")
            
            if (definitions.length() == 0) {
                Log.w(TAG, "No definitions found for word: $word")
                return null
            }
            
            // Get first definition
            val firstDefinition = definitions.getJSONObject(0)
            val definition = firstDefinition.getString("definition")
            val example = firstDefinition.optString("example", "")
            
            // Get phonetic if available
            val phonetics = wordObject.optJSONArray("phonetics")
            var phoneticText = ""
            var audioUrl = ""
            
            if (phonetics != null && phonetics.length() > 0) {
                for (i in 0 until phonetics.length()) {
                    val phonetic = phonetics.getJSONObject(i)
                    if (phonetic.has("text") && phoneticText.isEmpty()) {
                        phoneticText = phonetic.getString("text")
                    }
                    if (phonetic.has("audio") && audioUrl.isEmpty()) {
                        audioUrl = phonetic.getString("audio")
                    }
                }
            }
            
            val wordResult = Word(
                id = word.hashCode().toString(),
                word = word,
                definition = definition,
                soundUrl = audioUrl,
                example = example,
                phonetic = phoneticText,
                partOfSpeech = partOfSpeech
            )
            
            Log.d(TAG, "Successfully parsed definition for word: $word")
            wordResult
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing word definition for: $word", e)
            null
        }
    }
    
    /**
     * Check if JSON response is valid
     */
    fun isValidJsonResponse(jsonResponse: String): Boolean {
        return try {
            if (jsonResponse.isEmpty()) return false
            
            val jsonArray = JSONArray(jsonResponse)
            jsonArray.length() > 0
        } catch (e: Exception) {
            Log.e(TAG, "Invalid JSON response", e)
            false
        }
    }
}
