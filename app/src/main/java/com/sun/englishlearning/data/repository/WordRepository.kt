package com.sun.englishlearning.data.repository

import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.repository.source.WordDataSource
import com.sun.englishlearning.data.repository.source.remote.OnResultListener

class WordRepository private constructor(
    private val remote: WordDataSource.Remote,
    private val local: WordDataSource.Local
) : WordDataSource.Local, WordDataSource.Remote {

    override fun getWordsLocal(listener: OnResultListener<MutableList<Word>>) {
        local.getWordsLocal(listener)
    }

    override fun getWords(word: String, listener: OnResultListener<MutableList<Word>>) {
        remote.getWords(word, listener)
    }

    override fun addWordLocal(word: Word, listener: OnResultListener<Boolean>) {
        local.addWordLocal(word, listener)
    }

    companion object {
        private var instance: WordRepository? = null

        fun getInstance(remote: WordDataSource.Remote, local: WordDataSource.Local) = synchronized(this) {
            instance ?: WordRepository(remote, local).also { instance = it }
        }
    }
}
