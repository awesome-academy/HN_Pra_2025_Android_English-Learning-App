package com.sun.englishlearning.screen.courses

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
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
    private var lessonRepository: LessonRepository? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun setContext(context: Context) {
        this.context = context
        lessonRepository = LessonRepositoryImpl(context, userProgressRepository)
    }

    override fun loadOngoingLessons() {
        view?.showLoading()
        coroutineScope.launch {
            // Use lessonRepository safely
            val repo = lessonRepository ?: return@launch
            try {
                // For now, get all lessons and filter for started ones
                val lessonsResult = repo.getAllLessons()
                if (lessonsResult.isSuccess) {
                    val lessons = lessonsResult.getOrNull() ?: emptyList()
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
    
    private fun getCurrentUserId(): String {
        // TODO: Implement proper user identification logic
        // This could come from SharedPreferences, Firebase Auth, or session management
        return "user_${System.currentTimeMillis() % 1000}" // Temporary placeholder
    }
}
