package com.sun.englishlearning.data.repository.source.remote.fetchjson

import com.sun.englishlearning.data.model.Definition
import com.sun.englishlearning.data.model.Meaning
import com.sun.englishlearning.data.model.Phonetic
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.model.WordEntry
import org.json.JSONObject

class ParseJson {

    fun wordParseJson(jsonObject: JSONObject) = Word().apply {
        // Word
        word = jsonObject.optString(WordEntry.WORD)
        phonetic = jsonObject.optString(WordEntry.PHONETIC)

        // Phonetics
        val phoneticsArray = jsonObject.optJSONArray(WordEntry.PHONETICS)
        val phoneticsList = mutableListOf<Phonetic>()
        for (i in 0 until (phoneticsArray?.length() ?: 0)) {
            phoneticsArray?.optJSONObject(i)?.let { p ->
                phoneticsList.add(
                    Phonetic(
                        text = p.optString(WordEntry.TEXT),
                        audio = p.optString(WordEntry.AUDIO)
                    )
                )
            }
        }
        phonetics = phoneticsList

        // Meanings
        val meaningsArray = jsonObject.optJSONArray(WordEntry.MEANINGS)
        val meaningsList = mutableListOf<Meaning>()
        for (i in 0 until (meaningsArray?.length() ?: 0)) {
            meaningsArray?.optJSONObject(i)?.let { m ->
                // Definitions
                val defArray = m.optJSONArray(WordEntry.DEFINITIONS)
                val defList = mutableListOf<Definition>()
                for (j in 0 until (defArray?.length() ?: 0)) {
                    defArray?.optJSONObject(j)?.let { d ->
                        defList.add(
                            Definition(
                                definition = d.optString(WordEntry.DEFINITION),
                                example = d.optString(WordEntry.EXAMPLE)
                            )
                        )
                    }
                }

                meaningsList.add(
                    Meaning(
                        partOfSpeech = m.optString(WordEntry.PART_OF_SPEECH),
                        definitions = defList
                    )
                )
            }
        }
        meanings = meaningsList
    }
}
