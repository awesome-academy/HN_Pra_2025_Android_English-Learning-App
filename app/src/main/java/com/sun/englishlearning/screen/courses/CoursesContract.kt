package com.sun.englishlearning.screen.courses

import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.utils.base.BasePresenter

interface CoursesContract {
    interface View {
        fun showOngoingLessons(
            lessons: List<Lesson>,
            progressMap: Map<String, Int>,
            wordsLearnedMap: Map<String, Int>
        )
        fun showCompletedLessons(
            lessons: List<Lesson>,
            progressMap: Map<String, Int>,
            wordsLearnedMap: Map<String, Int>
        )

        fun showError(message: String)
        fun navigateToLessonDetail(lesson: Lesson)
        fun updateTabSelection(isOngoing: Boolean)
    }

    interface Presenter : BasePresenter<View> {
        fun loadOngoingLessons()
        fun loadCompletedLessons()
        fun onTabSelected(isOngoing: Boolean)
        fun onLessonClicked(lesson: Lesson)
        fun refreshLessons()
    }
}
