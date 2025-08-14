package com.sun.englishlearning.screen.lessondetail

import android.os.Handler
import android.os.Looper
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.repository.VocabularyRepository
import kotlin.concurrent.thread

class LessonDetailPresenter : LessonDetailContract.Presenter {

    private var view: LessonDetailContract.View? = null
    private var currentLesson: Lesson? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun loadLessonDetail(lesson: Lesson) {
        currentLesson = lesson
        view?.displayLessonInfo(lesson)
        loadVocabulary(lesson.id)
    }

    override fun loadVocabulary(lessonId: String) {
        view?.showLoading()
        thread {
            try {
                val vocabulary = VocabularyRepository.getVocabularyByLessonId(lessonId)
                mainHandler.post {
                    view?.hideLoading()
                    view?.showVocabulary(vocabulary)
                }
            } catch (e: Exception) {
                mainHandler.post {
                    view?.hideLoading()
                    view?.showError(e.message ?: "Error loading vocabulary")
                }
            }
        }
    }

    override fun onBackClicked() {
        view?.navigateBack()
    }

    override fun onWordClicked(word: Word) {
        view?.showWordDetail(word)
    }

    override fun onSoundClicked(word: Word) {
        view?.playWordSound(word)
    }

    override fun attachView(view: LessonDetailContract.View?) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }

    override fun onStart() {
        // Initialize if needed
    }

    override fun onStop() {
        // Clean up if needed
    }
}
