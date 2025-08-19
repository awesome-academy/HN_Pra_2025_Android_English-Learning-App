package com.sun.englishlearning.data.repository.source.local

import android.content.Context
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.repository.source.remote.OnResultListener
import com.sun.englishlearning.data.repository.source.VocabularyDataSource
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

class VocabularyLocalDataSource : VocabularyDataSource.Local {
    companion object {
        private const val LESSONS_FILE = "lessons.json"
        private var instance: VocabularyLocalDataSource? = null
        fun getInstance() = synchronized(this) {
            instance ?: VocabularyLocalDataSource().also { instance = it }
        }
    }

    /**
     * Load vocabulary words for a specific lesson from lessons.json
     */
    override fun getWordsLocal(context: Context, lessonId: String, listener: OnResultListener<MutableList<Word>>) {
        try {
            val jsonString = loadJsonFromAssets(context, LESSONS_FILE)
            if (jsonString.isEmpty()) {
                listener.onError("JSON string is empty")
                return
            }
            
            val words = parseWordsFromLessonsJson(jsonString, lessonId)
            listener.onSuccess(words)

        } catch (e: Exception) {
            listener.onError(e.message ?: "Unknown error")
        }
    }

    /**
     * Parse vocabulary words from lessons.json for specific lesson
     */
    private fun parseWordsFromLessonsJson(jsonString: String, lessonId: String): MutableList<Word> {
        val result = mutableListOf<Word>()
        val jsonObject = JSONObject(jsonString)
        val lessonsArray = jsonObject.getJSONArray("lessons")

        for (i in 0 until lessonsArray.length()) {
            val lessonObj = lessonsArray.getJSONObject(i)
            if (lessonObj.getString("id") == lessonId) {
                val vocabArray = lessonObj.getJSONArray("vocabulary")

                for (j in 0 until vocabArray.length()) {
                    val wordText = vocabArray.getString(j)
                    result.add(Word(word = wordText, id = wordText.hashCode().toString(), lessonId = lessonId))
                }
                break
            }
        }
        return result
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
            ""
        }
    }
}
