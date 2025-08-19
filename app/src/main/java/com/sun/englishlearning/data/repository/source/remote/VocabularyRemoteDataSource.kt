package com.sun.englishlearning.data.repository.source.remote

import android.content.Context
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.repository.source.VocabularyDataSource
import com.sun.englishlearning.data.repository.source.remote.fetchjson.GetJsonFromUrl

class VocabularyRemoteDataSource : VocabularyDataSource.Remote {
    override fun getWords(listener: OnResultListener<MutableList<Word>>) {
        // Not supported by Dictionary API, return error
        listener.onError("getWords is not supported by Dictionary API")
    }

    override fun getWordsByLesson(
        context: Context,
        lessonId: String,
        listener: OnResultListener<MutableList<Word>>
    ) {
        listener.onLoading()
        listener.onSuccess(mutableListOf()) // Return empty list instead of error
    }

    override fun getWord(wordId: String, listener: OnResultListener<Word?>) {
        GetJsonFromUrl(
            word = wordId,
            keyEntity = "word",
            listener = listener
        )
    }

    override fun createWord(word: Word, listener: OnResultListener<Unit>) {
        listener.onError("createWord is not supported by Dictionary API")
    }

    override fun updateWord(word: Word, listener: OnResultListener<Unit>) {
        listener.onError("updateWord is not supported by Dictionary API")
    }

    override fun deleteWord(wordId: String, listener: OnResultListener<Unit>) {
        listener.onError("deleteWord is not supported by Dictionary API")
    }

    companion object {
        private var instance: VocabularyRemoteDataSource? = null

        fun getInstance() = synchronized(this) {
            instance ?: VocabularyRemoteDataSource().also { instance = it }
        }
    }
}
