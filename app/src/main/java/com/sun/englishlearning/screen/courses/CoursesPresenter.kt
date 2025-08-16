package com.sun.englishlearning.screen.courses

import android.content.Context
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.data.repository.LessonRepository
import com.sun.englishlearning.data.repository.LessonRepositoryImpl
import com.sun.englishlearning.data.repository.UserLessonProgressRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CoursesPresenter : CoursesContract.Presenter {

    private var view: CoursesContract.View? = null
    private var context: Context? = null
    private var isOngoingTabSelected = true
    private lateinit var lessonRepository: LessonRepository
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun setContext(context: Context) {
        this.context = context
        lessonRepository = LessonRepositoryImpl(context, UserLessonProgressRepositoryImpl())
    }

    override fun loadOngoingLessons() {
        view?.showLoading()
        coroutineScope.launch {
            try {
                val lessonsResult = lessonRepository.getAllLessons()
                if (lessonsResult.isSuccess) {
                    val lessons = lessonsResult.getOrNull() ?: emptyList()
                    // All lessons are "ongoing" since user hasn't started any yet
                    view?.hideLoading()
                    view?.showOngoingLessons(lessons)
                } else {
                    view?.hideLoading()
                    view?.showError("Error loading lessons")
                }
            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError(e.message ?: "Error loading ongoing lessons")
            }
        }
    }

    override fun loadCompletedLessons() {
        view?.showLoading()
        coroutineScope.launch {
            try {
                // For now, no lessons are completed since user hasn't started any
                val completedLessons = emptyList<Lesson>()
                view?.hideLoading()
                view?.showCompletedLessons(completedLessons)
            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError(e.message ?: "Error loading completed lessons")
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
        // Simply reload the current tab since we don't have a cache to refresh
        if (isOngoingTabSelected) {
            loadOngoingLessons()
        } else {
            loadCompletedLessons()
        }
    }

    override fun attachView(view: CoursesContract.View?) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }

    override fun onStart() {
        // Load initial data - start with ongoing tab
        onTabSelected(true)
    }

    override fun onStop() {

    }
}
