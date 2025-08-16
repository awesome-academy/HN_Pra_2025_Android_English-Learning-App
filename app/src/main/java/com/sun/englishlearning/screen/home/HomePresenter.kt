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
        // Create categories directly from lessons using their title and imageUrl
        return lessons.take(3).map { lesson ->
            CourseCategory(
                title = lesson.title,
                imageUrl = lesson.imageUrl.takeIf { it.isNotBlank() } 
                    ?: "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=400",
                lessons = listOf(lesson)
            )
        }
    }



}