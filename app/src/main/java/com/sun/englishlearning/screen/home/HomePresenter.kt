package com.sun.englishlearning.screen.home

import com.sun.englishlearning.api.LessonApiService
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.screen.home.adapter.CourseCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface HomeView {
    fun showLoading()
    fun hideLoading()
    fun showCourseCategories(categories: List<CourseCategory>)
    fun showError(message: String)
    fun navigateToLessonDetail(lesson: Lesson)
}

class HomePresenter(private val view: HomeView) {
    
    private val lessonApiService = LessonApiService()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    fun loadCourseCategories() {
        view.showLoading()
        coroutineScope.launch {
            try {
                val lessonsResult = lessonApiService.getAllLessons()
                
                if (lessonsResult.isSuccess) {
                    val allLessons = lessonsResult.getOrNull() ?: emptyList()
                    val categories = createCategoriesFromLessons(allLessons)
                    view.hideLoading()
                    view.showCourseCategories(categories)
                } else {
                    view.hideLoading()
                    view.showError("Failed to load lessons")
                }
            } catch (e: Exception) {
                view.hideLoading()
                view.showError(e.message ?: "An error occurred")
            }
        }
    }
    
    private fun createCategoriesFromLessons(lessons: List<Lesson>): List<CourseCategory> {
        // Group lessons by course type and create categories
        val travelLessons = lessons.filter { it.courseId.contains("travel", ignoreCase = true) }
        val practiceEasyLessons = lessons.filter { it.difficulty.name == "EASY" }
        val businessLessons = lessons.filter { 
            it.courseId.contains("business", ignoreCase = true) || 
            it.courseId.contains("intermediate", ignoreCase = true) 
        }
        val academicLessons = lessons.filter { 
            it.courseId.contains("academic", ignoreCase = true) || 
            it.courseId.contains("advanced", ignoreCase = true) 
        }
        
        return listOf(
            CourseCategory(
                title = "Travel",
                iconRes = android.R.drawable.ic_menu_compass, // You can replace with custom icons
                lessons = travelLessons
            ),
            CourseCategory(
                title = "Practice",
                iconRes = android.R.drawable.ic_menu_edit,
                lessons = practiceEasyLessons
            ),
            CourseCategory(
                title = "Business",
                iconRes = android.R.drawable.ic_menu_agenda,
                lessons = businessLessons
            ),
            CourseCategory(
                title = "Academic",
                iconRes = android.R.drawable.ic_menu_info_details,
                lessons = academicLessons
            )
        ).filter { it.lessons.isNotEmpty() } // Only show categories that have lessons
    }
}