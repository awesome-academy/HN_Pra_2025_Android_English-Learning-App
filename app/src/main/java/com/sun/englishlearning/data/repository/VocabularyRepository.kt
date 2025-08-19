package com.sun.englishlearning.data.repository

import android.content.Context
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.repository.source.VocabularyDataSource
import com.sun.englishlearning.data.repository.source.remote.OnResultListener

class VocabularyRepository private constructor(
    private val remote: VocabularyDataSource.Remote,
    private val local: VocabularyDataSource.Local
) : VocabularyDataSource.Local, VocabularyDataSource.Remote {
    // Always use local for lesson vocabulary
    override fun getWordsLocal(context: Context, lessonId: String, listener: OnResultListener<MutableList<Word>>) {
        local.getWordsLocal(context, lessonId, listener)
    }

    // Always use remote for word data
    override fun getWords(listener: OnResultListener<MutableList<Word>>) {
        remote.getWords(listener)
    }

    override fun getWordsByLesson(context: Context, lessonId: String, listener: OnResultListener<MutableList<Word>>) {
        listener.onLoading()
        local.getWordsLocal(context, lessonId, object : OnResultListener<MutableList<Word>> {
            override fun onSuccess(data: MutableList<Word>) {
                if (data.isEmpty()) {
                    listener.onError("No words found for lesson $lessonId")
                    return
                }
                val resultList = mutableListOf<Word>()
                var completed = 0
                for (word in data) {
                    remote.getWord(word.word, object : OnResultListener<Word?> {
                        override fun onSuccess(wordDetail: Word?) {
                            wordDetail?.let { resultList.add(it) }
                            completed++
                            if (completed == data.size) {
                                listener.onSuccess(resultList)
                            }
                        }
                        override fun onError(error: String) {
                            completed++
                            if (completed == data.size) {
                                listener.onSuccess(resultList)
                            }
                        }
                        override fun onLoading() {}
                    })
                }
            }
            override fun onError(error: String) {
                listener.onError(error)
            }
            override fun onLoading() {}
        })
    }

    override fun getWord(wordId: String, listener: OnResultListener<Word?>) {
        remote.getWord(wordId, listener)
    }

    override fun createWord(word: Word, listener: OnResultListener<Unit>) {
        remote.createWord(word, listener)
    }

    override fun updateWord(word: Word, listener: OnResultListener<Unit>) {
        remote.updateWord(word, listener)
    }

    override fun deleteWord(wordId: String, listener: OnResultListener<Unit>) {
        remote.deleteWord(wordId, listener)
    }

    companion object {
        @Volatile
        private var instance: VocabularyRepository? = null
        fun getInstance(remote: VocabularyDataSource.Remote, local: VocabularyDataSource.Local): VocabularyRepository =
            instance ?: synchronized(this) {
                instance ?: VocabularyRepository(remote, local).also { instance = it }
            }
    }
}
