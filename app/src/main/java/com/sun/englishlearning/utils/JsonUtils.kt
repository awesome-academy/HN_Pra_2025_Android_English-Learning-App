package com.sun.englishlearning.utils

import android.content.Context
import android.util.Log
import com.sun.englishlearning.data.model.Lesson
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

object JsonUtils {

    private const val TAG = "JsonUtils"

    /**
     * Read JSON file from assets and parse it to List<Lesson>
     */
    fun loadLessonsFromAssets(context: Context?, fileName: String = "lessons.json"): List<Lesson> {
        return try {
            Log.d(TAG, "Loading lessons from assets: $fileName")

            val jsonString = loadJsonFromAssets(context!!, fileName)
            if (jsonString.isEmpty()) {
                Log.e(TAG, "JSON string is empty")
                return emptyList()
            }

            val lessons = parseLessonsFromJson(jsonString)
            Log.d(TAG, "Successfully loaded ${lessons.size} lessons")
            lessons

        } catch (e: Exception) {
            Log.e(TAG, "Error loading lessons from JSON", e)
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

    /**
     * Parse JSON string to List<Lesson>
     */
    fun parseLessonsFromJson(jsonString: String): List<Lesson> {
        return try {
            val jsonObject = JSONObject(jsonString)
            val lessonsArray = jsonObject.getJSONArray("lessons")
            val lessons = mutableListOf<Lesson>()

            for (i in 0 until lessonsArray.length()) {
                val lessonJson = lessonsArray.getJSONObject(i)
                val lesson = Lesson(
                    id = lessonJson.getString("id"),
                    title = lessonJson.getString("title"),
                    description = lessonJson.getString("description"),
                    imageUrl = lessonJson.getString("imageUrl"),
                    vocabulary = lessonJson.getJSONArray("vocabulary").run {
                        (0 until length()).map { getString(it) }
                    }
                )
                lessons.add(lesson)
            }

            lessons
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing lessons from JSON string", e)
            emptyList()
        }
    }
}
