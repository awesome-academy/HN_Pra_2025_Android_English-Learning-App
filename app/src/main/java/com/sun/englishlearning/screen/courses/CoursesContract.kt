package com.sun.englishlearning.screen.courses

import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.utils.base.BasePresenter

interface CoursesContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showOngoingLessons(lessons: List<Lesson>)
        fun showCompletedLessons(lessons: List<Lesson>)
        fun showError(message: String)
        fun navigateToLessonDetail(lesson: Lesson)
        fun updateTabSelection(isOngoing: Boolean)
        
        // Showing lessons with progress
        fun showOngoingLessonsWithProgress(lessons: List<Lesson>, progressMap: Map<String, Int>)
        fun showCompletedLessonsWithProgress(lessons: List<Lesson>, progressMap: Map<String, Int>)
    }

    interface Presenter : BasePresenter<View> {
        fun loadOngoingLessons()
        fun loadCompletedLessons()
        fun onTabSelected(isOngoing: Boolean)
        fun onLessonClicked(lesson: Lesson)
        fun refreshLessons()
    }
}
