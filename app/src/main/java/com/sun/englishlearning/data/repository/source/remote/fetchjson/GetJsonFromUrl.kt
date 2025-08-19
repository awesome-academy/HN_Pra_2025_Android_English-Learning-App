package com.sun.englishlearning.data.repository.source.remote.fetchjson

import android.os.Handler
import android.os.Looper
import com.sun.englishlearning.data.repository.source.remote.OnResultListener
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class GetJsonFromUrl<T> constructor(
    private val word: String,
    private val keyEntity: String,
    private val listener: OnResultListener<T>
) {

    companion object {
        private const val TIME_OUT = 15000
        private const val METHOD_GET = "GET"
        private const val BASE_API = "https://api.dictionaryapi.dev/api/v2/entries/en/"
    }

    private val mExecutor: Executor = Executors.newSingleThreadExecutor()
    private val mHandler = Handler(Looper.getMainLooper())

    init {
        callAPI()
    }

    private fun callAPI() {
        mExecutor.execute {
            try {
                listener.onLoading()
                val responseJson = getJsonStringFromUrl(BASE_API + word)

                if (responseJson.isEmpty()) {
                    mHandler.post {
                        listener.onError("No data received for word: $word")
                    }
                    return@execute
                }

                // Parse JSON response from Dictionary API
                val jsonArray = JSONArray(responseJson)
                if (jsonArray.length() > 0) {
                    val wordObject = jsonArray.getJSONObject(0)
                    val parsedData = ParseDataWithJson().parseWordDefinition(responseJson, word)

                    mHandler.post {
                        if (parsedData != null) {
                            listener.onSuccess(parsedData as T)
                        } else {
                            listener.onError("Failed to parse word definition for: $word")
                        }
                    }
                } else {
                    mHandler.post {
                        listener.onError("No definition found for word: $word")
                    }
                }

            } catch (e: Exception) {
                mHandler.post {
                    listener.onError("Network error: ${e.message}")
                }
            }
        }
    }

    private fun getJsonStringFromUrl(urlString: String): String {
        var httpURLConnection: HttpURLConnection? = null
        var bufferedReader: BufferedReader? = null

        try {
            val url = URL(urlString)
            httpURLConnection = url.openConnection() as HttpURLConnection
            httpURLConnection.apply {
                connectTimeout = TIME_OUT
                readTimeout = TIME_OUT
                requestMethod = METHOD_GET
                doOutput = false // Set to false for GET requests
                connect()
            }

            // Check response code
            val responseCode = httpURLConnection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("HTTP Error: $responseCode")
            }

            bufferedReader = BufferedReader(InputStreamReader(httpURLConnection.inputStream))
            val stringBuilder = StringBuilder()
            var line: String?

            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }

            return stringBuilder.toString()

        } catch (e: Exception) {
            throw Exception("Network request failed: ${e.message}")
        } finally {
            try {
                bufferedReader?.close()
                httpURLConnection?.disconnect()
            } catch (e: Exception) {
                // Log but don't throw
            }
        }
    }
}
