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
    private val auth = FirebaseAuth.getInstance()

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
                val userId = auth.currentUser?.uid ?: return@launch
                
                // Get in-progress lessons from repository
                val inProgressResult = repo.getInProgressLessons(userId)
                if (inProgressResult.isSuccess) {
                    val lessonsWithProgress = inProgressResult.getOrNull() ?: emptyList()
                    
                    // Extract lessons and create progress map with actual words learned
                    val lessons = lessonsWithProgress.map { it.first }
                    val progressMap = lessonsWithProgress.associate { (lesson, progress) ->
                        lesson.id to progress.progressPercentage
                    }
                    
                    // Create words learned map for accurate display
                    val wordsLearnedMap = lessonsWithProgress.associate { (lesson, progress) ->
                        lesson.id to progress.wordsLearned
                    }

                    view?.hideLoading()
                    // Pass both progress and words learned to view
                    (view as? CoursesFragment)?.showOngoingLessonsWithProgress(lessons, progressMap, wordsLearnedMap) ?:
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
                val userId = auth.currentUser?.uid ?: return@launch
                
                // Get completed lessons from repository
                val completedResult = lessonRepository?.getCompletedLessons(userId) ?: return@launch
                if (completedResult.isSuccess) {
                    val lessonsWithProgress = completedResult.getOrNull() ?: emptyList()
                    
                    // Extract lessons and create progress map with actual words learned
                    val lessons = lessonsWithProgress.map { it.first }
                    val progressMap = lessonsWithProgress.associate { (lesson, progress) ->
                        lesson.id to progress.progressPercentage
                    }
                    
                    // Create words learned map for accurate display
                    val wordsLearnedMap = lessonsWithProgress.associate { (lesson, progress) ->
                        lesson.id to progress.wordsLearned
                    }

                    view?.hideLoading()
                    // Pass both progress and words learned to view
                    (view as? CoursesFragment)?.showCompletedLessonsWithProgress(lessons, progressMap, wordsLearnedMap) ?:
                        view?.showCompletedLessons(lessons)
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
