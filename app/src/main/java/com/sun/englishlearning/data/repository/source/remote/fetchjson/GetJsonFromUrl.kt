package com.sun.englishlearning.data.repository.source.remote.fetchjson

import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GetJsonFromUrl {
    
    companion object {
        private const val TAG = "GetJsonFromUrl"
        private const val TIMEOUT_CONNECT = 10000 // 10 seconds
        private const val TIMEOUT_READ = 15000 // 15 seconds
    }
    
    /**
     * Fetch JSON data from URL using HttpURLConnection
     */
    fun fetchJsonFromUrl(urlString: String): String {
        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null
        
        return try {
            Log.d(TAG, "Fetching JSON from: $urlString")
            
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            
            // Set connection properties
            connection.apply {
                requestMethod = "GET"
                connectTimeout = TIMEOUT_CONNECT
                readTimeout = TIMEOUT_READ
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "English Learning App")
            }
            
            // Check response code
            val responseCode = connection.responseCode
            Log.d(TAG, "Response code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read response
                reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                
                val jsonResponse = response.toString()
                Log.d(TAG, "Successfully fetched JSON data (${jsonResponse.length} characters)")
                jsonResponse
                
            } else {
                Log.e(TAG, "HTTP error: $responseCode")
                ""
            }
            
        } catch (e: IOException) {
            Log.e(TAG, "Network error while fetching JSON", e)
            ""
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while fetching JSON", e)
            ""
        } finally {
            // Clean up resources
            try {
                reader?.close()
                connection?.disconnect()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing connection", e)
            }
        }
    }
    
    /**
     * Check if URL is reachable
     */
    fun isUrlReachable(urlString: String): Boolean {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "HEAD"
                connectTimeout = 5000
                readTimeout = 5000
            }
            
            val responseCode = connection.responseCode
            connection.disconnect()
            
            responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            Log.e(TAG, "Error checking URL reachability", e)
            false
        }
    }
}
