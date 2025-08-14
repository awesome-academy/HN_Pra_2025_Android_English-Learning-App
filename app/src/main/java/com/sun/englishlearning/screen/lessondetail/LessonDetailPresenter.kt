package com.sun.englishlearning.screen.lessondetail

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.repository.VocabularyRepository
import com.sun.englishlearning.data.repository.source.remote.OnResultListener

class LessonDetailPresenter : LessonDetailContract.Presenter {

    private var view: LessonDetailContract.View? = null
    private var currentLesson: Lesson? = null
    private var context: Context? = null

    fun setContext(context: Context) {
        this.context = context
    }

    override fun loadLessonDetail(lesson: Lesson) {
        currentLesson = lesson
        view?.displayLessonInfo(lesson)
        loadVocabulary(lesson.id)
    }

    override fun loadVocabulary(lessonId: String) {
        context?.let { ctx ->
            view?.showLoading()

            // Use API-based vocabulary loading
            VocabularyRepository.getVocabularyByLessonId(ctx, lessonId, object : OnResultListener<List<Word>> {
                override fun onSuccess(data: List<Word>) {
                    view?.hideLoading()
                    view?.showVocabulary(data)
                }

                override fun onError(error: String) {
                    view?.hideLoading()
                    // Fallback to synchronous method if API fails
                    val fallbackVocabulary = VocabularyRepository.getVocabularyByLessonId(lessonId)
                    if (fallbackVocabulary.isNotEmpty()) {
                        view?.showVocabulary(fallbackVocabulary)
                    } else {
                        view?.showError(error)
                    }
                }

                override fun onLoading() {
                    view?.showLoading()
                }
            })
        } ?: run {
            // Fallback if no context
            val fallbackVocabulary = VocabularyRepository.getVocabularyByLessonId(lessonId)
            view?.showVocabulary(fallbackVocabulary)
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

    }

    override fun onStop() {

    }
}
