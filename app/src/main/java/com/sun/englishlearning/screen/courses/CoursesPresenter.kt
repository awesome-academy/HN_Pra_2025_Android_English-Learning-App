package com.sun.englishlearning.screen.courses

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.api.LessonApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CoursesPresenter : CoursesContract.Presenter {

    private var view: CoursesContract.View? = null
    private var context: Context? = null
    private var isOngoingTabSelected = true
    private val lessonApiService = LessonApiService()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun setContext(context: Context) {
        this.context = context
    }

    override fun loadOngoingLessons() {
        view?.showLoading()
        coroutineScope.launch {
            try {
                val userId = getCurrentUserId()
                val lessonsResult = lessonApiService.getLessonsForUser(userId)
                if (lessonsResult.isSuccess) {
                    val lessons = lessonsResult.getOrNull() ?: emptyList()
                    val ongoingLessons = lessons.filter { it.isStarted && !isLessonCompleted(it.id) }
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
                val userId = getCurrentUserId()
                val lessonsResult = lessonApiService.getLessonsForUser(userId)
                if (lessonsResult.isSuccess) {
                    val lessons = lessonsResult.getOrNull() ?: emptyList()
                    val completedLessons = lessons.filter { isLessonCompleted(it.id) }
                    view?.hideLoading()
                    view?.showCompletedLessons(completedLessons)
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
        // Get the current user ID from Firebase Authentication
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
    
    private fun isLessonCompleted(lessonId: String): Boolean {
        // This would typically check UserLessonProgress, but for now return false
        // You can implement this logic based on your UserLessonProgress data
        return false
    }
    
    /**
     * Simple method to load all lessons from Firebase
     * This is what you requested - a simple API to get all lessons
     */
    fun loadAllLessons() {
        view?.showLoading()
        coroutineScope.launch {
            try {
                val lessonsResult = lessonApiService.getAllLessons()
                if (lessonsResult.isSuccess) {
                    val lessons = lessonsResult.getOrNull() ?: emptyList()
                    view?.hideLoading()
                    view?.showOngoingLessons(lessons) // Display all lessons
                } else {
                    view?.hideLoading()
                    view?.showError("Error loading lessons")
                }
            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError(e.message ?: "Error loading lessons")
            }
        }
    }
}
