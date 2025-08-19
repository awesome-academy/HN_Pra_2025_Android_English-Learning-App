package com.sun.englishlearning.data.repository.source

import android.content.Context
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.repository.source.remote.OnResultListener

interface VocabularyDataSource {
    interface Local {
        fun getWordsLocal(context: Context, lessonId: String, listener: OnResultListener<MutableList<Word>>)
    }
    interface Remote {
        fun getWords(listener: OnResultListener<MutableList<Word>>)
        fun getWordsByLesson(context: Context, lessonId: String, listener: OnResultListener<MutableList<Word>>)
        fun getWord(wordId: String, listener: OnResultListener<Word?>)
        fun createWord(word: Word, listener: OnResultListener<Unit>)
        fun updateWord(word: Word, listener: OnResultListener<Unit>)
        fun deleteWord(wordId: String, listener: OnResultListener<Unit>)
    }
}
