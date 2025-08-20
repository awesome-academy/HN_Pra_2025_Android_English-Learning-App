package com.sun.englishlearning.data.repository.source.remote.fetchjson

import android.util.Log
import com.sun.englishlearning.data.model.WordEntry
import com.sun.englishlearning.utils.ext.notNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ParseDataWithJson {

    fun parseJsonToData(jsonArray: JSONArray?, keyEntity: String): Any {
        val data = mutableListOf<Any>()
        try {
            for (i in 0 until (jsonArray?.length() ?: 0)) {
                val item = parseJsonToObject(jsonArray?.getJSONObject(i), keyEntity)
                item.notNull {
                    data.add(it)
                }
            }
        } catch (e: JSONException) {
            Log.e("ParseDataWithJson", "parseJsonToData: ", e)
        }
        return data
    }

    private fun parseJsonToObject(jsonObject: JSONObject?, keyEntity: String): Any? {
        try {
            jsonObject?.notNull {
                return when (keyEntity) {
                    WordEntry.WORD -> ParseJson().wordParseJson(it)
                    else -> null
                }
            }
        } catch (e: JSONException) {
            Log.e("ParseDataWithJson", "parseJsonToObject: ", e)
        }
        return null
    }
}
