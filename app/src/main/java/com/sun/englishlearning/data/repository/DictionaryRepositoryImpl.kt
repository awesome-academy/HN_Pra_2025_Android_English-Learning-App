package com.sun.englishlearning.data.repository

import com.sun.englishlearning.data.model.WordSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class DictionaryRepositoryImpl : DictionaryRepository {
    override suspend fun searchWord(query: String): Result<WordSearchResult> {
        return withContext(Dispatchers.IO) {
            try {
                val apiUrl = "https://api.dictionaryapi.dev/api/v2/entries/en/$query"
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext Result.failure(Exception("Word not found or network error"))
                }

                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                connection.disconnect()

                val jsonArray = JSONArray(response.toString())
                val jsonObject = jsonArray.getJSONObject(0)
                val word = jsonObject.optString("word", query)
                val phoneticsArray = jsonObject.optJSONArray("phonetics")
                var ipa = ""
                var soundUrl = ""
                if (phoneticsArray != null && phoneticsArray.length() > 0) {
                    val phoneticObj = phoneticsArray.getJSONObject(0)
                    ipa = phoneticObj.optString("text", "")
                    soundUrl = phoneticObj.optString("audio", "")
                }
                val meaningsArray = jsonObject.optJSONArray("meanings")
                var partOfSpeech = ""
                var definition = ""
                var example = ""
                if (meaningsArray != null && meaningsArray.length() > 0) {
                    val meaningObj = meaningsArray.getJSONObject(0)
                    partOfSpeech = meaningObj.optString("partOfSpeech", "")
                    val definitionsArray = meaningObj.optJSONArray("definitions")
                    if (definitionsArray != null && definitionsArray.length() > 0) {
                        val defObj = definitionsArray.getJSONObject(0)
                        definition = defObj.optString("definition", "")
                        example = defObj.optString("example", "")
                    }
                }
                val result = WordSearchResult(
                    word = word,
                    ipa = ipa,
                    partOfSpeech = partOfSpeech,
                    definition = definition,
                    example = example,
                    soundUrl = soundUrl
                )
                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
