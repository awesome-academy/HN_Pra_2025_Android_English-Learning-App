package com.sun.englishlearning.screen.courses

import android.content.Context
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.data.repository.LessonRepository
import com.sun.englishlearning.data.repository.LessonRepositoryImpl
import com.sun.englishlearning.data.repository.UserLessonProgressRepository
import com.sun.englishlearning.data.repository.UserLessonProgressRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CoursesPresenter : CoursesContract.Presenter {

    private var view: CoursesContract.View? = null
    private var context: Context? = null
    private var isOngoingTabSelected = true
    private val userProgressRepository: UserLessonProgressRepository = UserLessonProgressRepositoryImpl()
    private val lessonRepository: LessonRepository = LessonRepositoryImpl(userProgressRepository)
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun setContext(context: Context) {
        this.context = context
    }

    override fun loadOngoingLessons() {
        view?.showLoading()
        coroutineScope.launch {
            try {
                // For now, get all lessons and filter for started ones
                val lessonsResult = lessonRepository.getAllLessons()
                if (lessonsResult.isSuccess) {
                    val lessons = lessonsResult.getOrNull() ?: emptyList()
                    val ongoingLessons = lessons.filter { it.isStarted }
                    view?.hideLoading()
                    view?.showOngoingLessons(ongoingLessons)
                } else {
                    view?.hideLoading()
                    view?.showError("Error loading ongoing lessons")
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
                // Get all lessons and filter for completed ones using UserLessonProgress
                val lessonsResult = lessonRepository.getAllLessons()
                if (lessonsResult.isSuccess) {
                    val lessons = lessonsResult.getOrNull() ?: emptyList()
                    val userId = getCurrentUserId()
                    val completedProgressResult = userProgressRepository.getCompletedLessons(userId)
                    
                    if (completedProgressResult.isSuccess) {
                        val completedProgress = completedProgressResult.getOrNull() ?: emptyList()
                        val completedLessonIds = completedProgress.map { it.lessonId }.toSet()
                        val completedLessons = lessons.filter { it.id in completedLessonIds }
                        view?.hideLoading()
                        view?.showCompletedLessons(completedLessons)
                    } else {
                        view?.hideLoading()
                        view?.showCompletedLessons(emptyList())
                    }
                } else {
                    view?.hideLoading()
                    view?.showError("Error loading completed lessons")
                }
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
        // Load initial data
        onTabSelected(true) // Load ongoing lessons by default
    }

    override fun onStop() {

    }
    
    private fun getCurrentUserId(): String {
        // TODO: Implement proper user identification logic
        // This could come from SharedPreferences, Firebase Auth, or session management
        return "user_${System.currentTimeMillis() % 1000}" // Temporary placeholder
    }
}
