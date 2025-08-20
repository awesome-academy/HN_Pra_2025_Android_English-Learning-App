package com.sun.englishlearning.data.repository.source

import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.repository.source.remote.OnResultListener

interface WordDataSource {
    /**
     * Local
     */
    interface Local {
        fun getWordsLocal(listener: OnResultListener<MutableList<Word>>)
        fun addWordLocal(word: Word, listener: OnResultListener<Boolean>)
    }

    /**
     * Remote
     */
    interface Remote {
        fun getWords(word: String, listener: OnResultListener<MutableList<Word>>)
    }
}
