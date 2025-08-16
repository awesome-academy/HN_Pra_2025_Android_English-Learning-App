package com.sun.englishlearning.screen.lessondetail

import android.content.Context
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.repository.VocabularyRepository
import com.sun.englishlearning.data.repository.source.remote.OnResultListener

class LessonDetailPresenter : LessonDetailContract.Presenter {

    private var view: LessonDetailContract.View? = null
    private var currentLesson: Lesson? = null
    private var currentVocabulary: List<Word> = emptyList()
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
                    currentVocabulary = data
                    view?.hideLoading()
                    view?.showVocabulary(data)
                }

                override fun onError(error: String) {
                    view?.hideLoading()
                    // Fallback to synchronous method if API fails
                    val fallbackVocabulary = VocabularyRepository.getVocabularyByLessonId(lessonId)
                    if (fallbackVocabulary.isNotEmpty()) {
                        currentVocabulary = fallbackVocabulary
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
            currentVocabulary = fallbackVocabulary
            view?.showVocabulary(fallbackVocabulary)
        }
    }

    override fun onBackClicked() {
        view?.navigateBack()
    }

    override fun onWordClicked(word: Word) {
        try {
            android.util.Log.d("LessonDetailPresenter", "Word clicked: ${word.name}")
            android.util.Log.d("LessonDetailPresenter", "Current vocabulary size: ${currentVocabulary.size}")

            // Find the index of the clicked word in the current vocabulary list
            val wordIndex = currentVocabulary.indexOfFirst { it.name == word.name }
            android.util.Log.d("LessonDetailPresenter", "Word index found: $wordIndex")

            if (wordIndex != -1) {
                val lessonTitle = currentLesson?.title ?: ""
                android.util.Log.d("LessonDetailPresenter", "Navigating to flashcard with title: $lessonTitle")
                view?.navigateToFlashcard(currentVocabulary, wordIndex, lessonTitle)
            } else {
                android.util.Log.w("LessonDetailPresenter", "Word index not found, showing word detail")
                // Fallback to showing word detail if index not found
                view?.showWordDetail(word)
            }
        } catch (e: Exception) {
            android.util.Log.e("LessonDetailPresenter", "Error handling word click", e)
            view?.showError("Error opening flashcard: ${e.message}")
        }
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
