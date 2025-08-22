package com.sun.englishlearning.screen.courses

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.data.repository.LessonRepository
import com.sun.englishlearning.data.repository.LessonRepositoryImpl
import com.sun.englishlearning.data.repository.UserLessonProgressRepository
import com.sun.englishlearning.data.repository.UserLessonProgressRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CoursesPresenter internal constructor(private val lessonRepository: LessonRepository?) :
    CoursesContract.Presenter {

    private var mView: CoursesContract.View? = null
    private var context: Context? = null
    private var isOngoingTabSelected = true
    private val userProgressRepository: UserLessonProgressRepository = UserLessonProgressRepositoryImpl()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val auth = FirebaseAuth.getInstance()

    fun setContext(context: Context) {
        this.context = context
        LessonRepositoryImpl(context, userProgressRepository)
    }

    override fun loadOngoingLessons() {
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

                    // Pass both progress and words learned to view
                    (mView as? CoursesFragment)?.showOngoingLessons(lessons, progressMap, wordsLearnedMap) ?:
                        mView?.showOngoingLessons(lessons, progressMap, wordsLearnedMap)
                } else {
                    mView?.showError("Error loading lessons")
                }
            } catch (e: Exception) {
                mView?.showError(e.message ?: "Error loading ongoing lessons")
            }
        }
    }

    override fun loadCompletedLessons() {
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

                    // Pass both progress and words learned to view
                    (mView as? CoursesFragment)?.showCompletedLessons(lessons, progressMap, wordsLearnedMap) ?:
                        mView?.showCompletedLessons(lessons, progressMap, wordsLearnedMap)
                } else {
                    mView?.showError("Error loading completed lessons")
                }
            } catch (e: Exception) {
                mView?.showError(e.message ?: "Error loading completed lessons")
            }
        }
    }

    override fun onTabSelected(isOngoing: Boolean) {
        isOngoingTabSelected = isOngoing
        mView?.updateTabSelection(isOngoing)

        if (isOngoing) {
            loadOngoingLessons()
        } else {
            loadCompletedLessons()
        }
    }

    override fun onLessonClicked(lesson: Lesson) {
        try {
            // Validate lesson data before navigation
            if (lesson.id.isEmpty()) {
                Log.w("CoursesPresenter", "Lesson clicked with empty ID: ${lesson.title}")
                mView?.showError("Invalid lesson selected")
                return
            }

            if (lesson.title.isEmpty()) {
                Log.w("CoursesPresenter", "Lesson clicked with empty title: ${lesson.id}")
                mView?.showError("Invalid lesson data")
                return
            }

            // Validate vocabulary list
            if (lesson.vocabulary.isEmpty()) {
                Log.w("CoursesPresenter", "Lesson has no vocabulary: ${lesson.title}")
                mView?.showError("This lesson has no vocabulary words available")
                return
            }

            // Check if view is still attached
            if (mView == null) {
                Log.w("CoursesPresenter", "View is null when trying to navigate")
                return
            }

            Log.d("CoursesPresenter", "Navigating to lesson: ${lesson.title} with ${lesson.vocabulary.size} vocabulary words")
            mView?.navigateToLessonDetail(lesson)
        } catch (e: Exception) {
            Log.e("CoursesPresenter", "Error handling lesson click", e)
            mView?.showError("Failed to open lesson: ${e.message}")
        }
    }

    override fun refreshLessons() {
        if (isOngoingTabSelected) {
            loadOngoingLessons()
        } else {
            loadCompletedLessons()
        }
    }

    override fun onStart() {
        // Load initial data - start with ongoing tab
        onTabSelected(true)
    }

    override fun onStop() {

    }

    override fun attachView(view: CoursesContract.View?) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }
}
