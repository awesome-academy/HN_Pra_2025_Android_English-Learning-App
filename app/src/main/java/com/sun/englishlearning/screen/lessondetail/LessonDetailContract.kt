package com.sun.englishlearning.screen.lessondetail

import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.utils.base.BasePresenter

interface LessonDetailContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun displayLessonInfo(lesson: Lesson)
        fun showVocabulary(words: List<Word>)
        fun showError(message: String)
        fun navigateBack()
        fun playWordSound(word: Word)
        fun showWordDetail(word: Word)
        fun navigateToFlashcard(words: List<Word>, currentIndex: Int, lessonTitle: String)
    }

    interface Presenter : BasePresenter<View> {
        fun loadLessonDetail(lesson: Lesson)
        fun loadVocabulary(lessonId: String)
        fun onBackClicked()
        fun onWordClicked(word: Word)
        fun onSoundClicked(word: Word)
    }
}
