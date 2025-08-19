package com.sun.englishlearning.screen.testflashcard

import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.utils.base.BasePresenter
import java.lang.Exception

class TestFlashcardContract {
    /**
     * View
     */
    interface View {
        fun onTestWordSuccess(word: Word)
        fun onTestCompleted()
        fun onError(exception: Exception?)
        fun showLoading()
        fun hideLoading()
        fun onWordTested(isCorrect: Boolean)
    }

    /**
     * Presenter
     */
    interface Presenter : BasePresenter<View> {
        fun loadTestWord(word: Word)
        fun checkAnswer(userAnswer: String, correctAnswer: String)
        fun nextWord()
        fun playAudio(soundUrl: String)
        override fun attachView(view: View?)
        override fun detachView()
    }
}
