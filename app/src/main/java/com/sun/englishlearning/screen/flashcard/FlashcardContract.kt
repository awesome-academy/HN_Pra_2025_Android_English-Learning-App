package com.sun.englishlearning.screen.flashcard

import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.utils.base.BasePresenter
import java.lang.Exception

class FlashcardContract {
    /**
     * View
     */
    interface View {
        fun onWordLoaded(word: Word)
        fun onWordSaved(success: Boolean)
        fun onWordUnsaved(success: Boolean)
        fun onWordMarkedAsLearned(success: Boolean)
        fun onProgressUpdated(lessonId: String)
        fun onError(exception: Exception?)
        fun showLoading()
        fun hideLoading()
        fun updateSaveButtonUI(isSaved: Boolean)
        fun updateLearnedButtonUI(isLearned: Boolean)
        fun showWordSavedStatus(isSaved: Boolean)
        fun showWordLearnedStatus(isLearned: Boolean)
    }

    /**
     * Presenter
     */
    interface Presenter : BasePresenter<View> {
        fun loadWord(word: Word)
        fun saveWord(word: Word)
        fun unsaveWord(word: Word)
        fun markWordAsLearned(word: Word)
        fun checkWordSavedStatus(word: Word)
        fun checkWordLearnedStatus(word: Word)
        fun playAudio(soundUrl: String)
        override fun attachView(view: View?)
        override fun detachView()
    }
}
