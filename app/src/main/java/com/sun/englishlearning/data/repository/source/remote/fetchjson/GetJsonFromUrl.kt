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
    private val urlString: String,
    private val keyEntity: String,
    private val listener: OnResultListener<T>
) {

    private val mExecutor: Executor = Executors.newSingleThreadExecutor()
    private val mHandler = Handler(Looper.getMainLooper())
    private var data: T? = null

    init {
        callAPI()
    }

    private fun callAPI() {
        mExecutor.execute {
            try {
                android.util.Log.d("GetJsonFromUrl", "Fetching data from: $urlString")
                val responseJson = getJsonStringFromUrl(urlString)
                android.util.Log.d("GetJsonFromUrl", "Response received, length: ${responseJson.length}")

                data = ParseDataWithJson().parseJsonToData(JSONArray(responseJson), keyEntity) as? T
                android.util.Log.d("GetJsonFromUrl", "Data parsed successfully: ${data != null}")

                mHandler.post {
                    if (data != null) {
                        listener.onSuccess(data!!)
                    } else {
                        android.util.Log.w("GetJsonFromUrl", "Parsed data is null")
                        listener.onError(Exception("Failed to parse data from JSON"))
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("GetJsonFromUrl", "Error in callAPI", e)
                mHandler.post {
                    listener.onError(e)
                }
            }
        }
    }

    private fun getJsonStringFromUrl(urlString: String): String {
        var httpURLConnection: HttpURLConnection? = null
        var bufferedReader: BufferedReader? = null

        try {
            val url = URL(urlString)
            httpURLConnection = url.openConnection() as? HttpURLConnection
            httpURLConnection?.run {
                connectTimeout = TIME_OUT
                readTimeout = TIME_OUT
                requestMethod = METHOD_GET
                doOutput = false  // Changed from true to false for GET requests
                connect()

                // More lenient response code check - allow 200-299 range
                if (responseCode < 200 || responseCode >= 300) {
                    throw Exception("HTTP Error: $responseCode - $responseMessage")
                }
            }

            // Use httpURLConnection.inputStream instead of url.openStream() for better error handling
            val inputStream = httpURLConnection?.inputStream ?: url.openStream()
            bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            return stringBuilder.toString()

        } catch (e: Exception) {
            // Log the actual error for debugging
            android.util.Log.e("GetJsonFromUrl", "Error fetching from URL: $urlString", e)
            throw Exception("Failed to fetch data from URL: ${e.message}", e)
        } finally {
            try {
                bufferedReader?.close()
                httpURLConnection?.disconnect()
            } catch (e: Exception) {
                // Ignore cleanup errors
                android.util.Log.w("GetJsonFromUrl", "Error during cleanup", e)
            }
        }
    }

    companion object {
        private const val TIME_OUT = 15000
        private const val METHOD_GET = "GET"
    }
}
