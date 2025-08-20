package com.sun.englishlearning.data.repository.source.remote

import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.model.WordEntry
import com.sun.englishlearning.data.repository.source.WordDataSource
import com.sun.englishlearning.data.repository.source.remote.fetchjson.GetJsonFromUrl

class WordRemoteDataSource : WordDataSource.Remote {

    override fun getWords(word: String, listener: OnResultListener<MutableList<Word>>) {
        val url = BASE_URL + word
        GetJsonFromUrl(
            urlString = url,
            keyEntity = WordEntry.WORD,
            listener = listener
        )
    }

    companion object {
        private var instance: WordRemoteDataSource? = null
        private const val BASE_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/"

        fun getInstance() = synchronized(this) {
            instance ?: WordRemoteDataSource().also { instance = it }
        }
    }
}
