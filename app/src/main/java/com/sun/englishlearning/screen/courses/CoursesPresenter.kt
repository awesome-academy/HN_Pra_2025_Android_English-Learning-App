package com.sun.englishlearning.screen.courses

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.data.repository.LessonRepository
import kotlin.concurrent.thread

class CoursesPresenter : CoursesContract.Presenter {

    private var view: CoursesContract.View? = null
    private var context: Context? = null
    private var isOngoingTabSelected = true
    private val mainHandler = Handler(Looper.getMainLooper())

    fun setContext(context: Context) {
        this.context = context
    }

    override fun loadOngoingLessons() {
        context?.let { ctx ->
            view?.showLoading()
            thread {
                try {
                    val lessons = LessonRepository.getOngoingLessons(ctx)
                    mainHandler.post {
                        view?.hideLoading()
                        view?.showOngoingLessons(lessons)
                    }
                } catch (e: Exception) {
                    mainHandler.post {
                        view?.hideLoading()
                        view?.showError(e.message ?: "Error loading ongoing lessons")
                    }
                }
            }
        }
    }

    override fun loadCompletedLessons() {
        context?.let { ctx ->
            view?.showLoading()
            thread {
                try {
                    val lessons = LessonRepository.getCompletedLessons(ctx)
                    mainHandler.post {
                        view?.hideLoading()
                        view?.showCompletedLessons(lessons)
                    }
                } catch (e: Exception) {
                    mainHandler.post {
                        view?.hideLoading()
                        view?.showError(e.message ?: "Error loading completed lessons")
                    }
                }
            }
        }
    }

    override fun onTabSelected(isOngoing: Boolean) {
        isOngoingTabSelected = isOngoing
        view?.updateTabSelection(isOngoing)

        if (isOngoing) {
            loadOngoingLessons()
        } else {
            loadCompletedLessons()
        }
    }

    override fun onLessonClicked(lesson: Lesson) {
        view?.navigateToLessonDetail(lesson)
    }

    override fun refreshLessons() {
        context?.let { ctx ->
            thread {
                LessonRepository.refreshCache(ctx)
                mainHandler.post {
                    if (isOngoingTabSelected) {
                        loadOngoingLessons()
                    } else {
                        loadCompletedLessons()
                    }
                }
            }
        }
    }

    override fun attachView(view: CoursesContract.View?) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }

    override fun onStart() {
        // Load initial data
        onTabSelected(true) // Load ongoing lessons by default
    }

    override fun onStop() {
        // Clean up if needed
    }
}
