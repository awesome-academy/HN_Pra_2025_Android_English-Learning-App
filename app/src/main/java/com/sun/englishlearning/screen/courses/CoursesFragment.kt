package com.sun.englishlearning.screen.courses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.sun.englishlearning.R
import com.sun.englishlearning.databinding.ActivityLessonsBinding
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.utils.base.BaseFragment

class CoursesFragment : BaseFragment<ActivityLessonsBinding>() {

    private lateinit var lessonAdapter: LessonAdapter

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): ActivityLessonsBinding {
        return ActivityLessonsBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        setupRecyclerView()
    }

    override fun initData() {
        loadSampleLessons()
    }

    private fun setupRecyclerView() {
        lessonAdapter = LessonAdapter { lesson ->
            onLessonClick(lesson)
        }

        viewBinding.recyclerViewLessons.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = lessonAdapter
        }
    }

    private fun loadSampleLessons() {
        val sampleLessons = listOf(
            Lesson(
                id = "35",
                title = "Schools",
                lessonNumber = 1,
                advancedLevel = 25,
                currentPoints = 85,
                totalPoints = 100,
                progressPercentage = 85,
                description = "Learn vocabulary about schools and educational institutions",
                imageUrl = "https://learn.mochidemy.com/_next/image?url=https%3A%2F%2Fmochien3.1-api.mochidemy.com%2Fpublic%2Fimages%2Flesson%2FTACB_lesson_35.png&w=3840&q=75"
            ),
            Lesson(
                id = "36",
                title = "Examination",
                lessonNumber = 2,
                advancedLevel = 40,
                currentPoints = 67,
                totalPoints = 100,
                progressPercentage = 67,
                description = "Vocabulary and phrases related to exams and testing",
                imageUrl = "https://learn.mochidemy.com/_next/image?url=https%3A%2F%2Fmochien3.1-api.mochidemy.com%2Fpublic%2Fimages%2Flesson%2FTACB_lesson_36.png&w=3840&q=75"
            ),
            Lesson(
                id = "2",
                title = "Extracurricular Activities",
                lessonNumber = 3,
                advancedLevel = 30,
                currentPoints = 42,
                totalPoints = 100,
                progressPercentage = 42,
                description = "Learn about activities outside of regular classes",
                imageUrl = "https://learn.mochidemy.com/_next/image?url=https%3A%2F%2Fmochien3.1-api.mochidemy.com%2Fpublic%2Fimages%2Flesson%2FTACB_lesson_2.webp&w=256&q=70"
            ),
            Lesson(
                id = "31",
                title = "School Stationery",
                lessonNumber = 4,
                advancedLevel = 20,
                currentPoints = 93,
                totalPoints = 100,
                progressPercentage = 93,
                description = "Essential school supplies and stationery items",
                imageUrl = "https://learn.mochidemy.com/_next/image?url=https%3A%2F%2Fmochien3.1-api.mochidemy.com%2Fpublic%2Fimages%2Flesson%2FTACB_lesson_31.webp&w=256&q=70"
            ),
            Lesson(
                id = "32",
                title = "School Subjects",
                lessonNumber = 5,
                advancedLevel = 35,
                currentPoints = 58,
                totalPoints = 100,
                progressPercentage = 58,
                description = "Different academic subjects taught in school",
                imageUrl = "https://learn.mochidemy.com/_next/image?url=https%3A%2F%2Fmochien3.1-api.mochidemy.com%2Fpublic%2Fimages%2Flesson%2FTACB_lesson_32.webp&w=256&q=70"
            ),
            Lesson(
                id = "33",
                title = "Classroom",
                lessonNumber = 6,
                advancedLevel = 25,
                currentPoints = 74,
                totalPoints = 100,
                progressPercentage = 74,
                description = "Classroom objects and learning environment",
                imageUrl = "https://learn.mochidemy.com/_next/image?url=https%3A%2F%2Fmochien3.1-api.mochidemy.com%2Fpublic%2Fimages%2Flesson%2FTACB_lesson_33.webp&w=256&q=70"
            ),
            Lesson(
                id = "34",
                title = "Universities",
                lessonNumber = 7,
                advancedLevel = 50,
                currentPoints = 29,
                totalPoints = 100,
                progressPercentage = 29,
                description = "Higher education institutions and university life",
                imageUrl = "https://learn.mochidemy.com/_next/image?url=https%3A%2F%2Fmochien3.1-api.mochidemy.com%2Fpublic%2Fimages%2Flesson%2FTACB_lesson_34.webp&w=256&q=70"
            ),
            Lesson(
                id = "8",
                title = "Body",
                lessonNumber = 8,
                advancedLevel = 15,
                currentPoints = 88,
                totalPoints = 100,
                progressPercentage = 88,
                description = "Parts of the human body and related vocabulary",
                imageUrl = "https://learn.mochidemy.com/_next/image?url=https%3A%2F%2Fmochien3.1-api.mochidemy.com%2Fpublic%2Fimages%2Flesson%2FTACB_lesson_8.webp&w=256&q=70"
            ),
            Lesson(
                id = "9",
                title = "Appearance",
                lessonNumber = 9,
                advancedLevel = 30,
                currentPoints = 51,
                totalPoints = 100,
                progressPercentage = 51,
                description = "Describing physical appearance and looks",
                imageUrl = "https://learn.mochidemy.com/_next/image?url=https%3A%2F%2Fmochien3.1-api.mochidemy.com%2Fpublic%2Fimages%2Flesson%2FTACB_lesson_9.webp&w=256&q=70"
            ),
            Lesson(
                id = "10",
                title = "Characteristics",
                lessonNumber = 10,
                advancedLevel = 45,
                currentPoints = 36,
                totalPoints = 100,
                progressPercentage = 36,
                description = "Personality traits and character descriptions",
                imageUrl = "https://learn.mochidemy.com/_next/image?url=https%3A%2F%2Fmochien3.1-api.mochidemy.com%2Fpublic%2Fimages%2Flesson%2FTACB_lesson_10.webp&w=256&q=70"
            )
        )

        lessonAdapter.updateLessons(sampleLessons)
    }

    private fun onLessonClick(lesson: Lesson) {
        Toast.makeText(requireContext(), "Clicked on: ${lesson.title}", Toast.LENGTH_SHORT).show()
        // TODO: Navigate to lesson detail or start lesson
    }
}
