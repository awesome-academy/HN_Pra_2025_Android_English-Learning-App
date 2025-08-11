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
                id = "1",
                title = "Friends Series",
                lessonNumber = 3,
                advancedLevel = 45,
                currentPoints = 43,
                totalPoints = 100,
                progressPercentage = 43
            ),
            Lesson(
                id = "2",
                title = "Business English",
                lessonNumber = 5,
                advancedLevel = 60,
                currentPoints = 78,
                totalPoints = 100,
                progressPercentage = 78
            ),
            Lesson(
                id = "3",
                title = "Daily Conversation",
                lessonNumber = 2,
                advancedLevel = 30,
                currentPoints = 25,
                totalPoints = 100,
                progressPercentage = 25
            ),
            Lesson(
                id = "4",
                title = "Grammar Basics",
                lessonNumber = 1,
                advancedLevel = 15,
                currentPoints = 90,
                totalPoints = 100,
                progressPercentage = 90
            ),
            Lesson(
                id = "5",
                title = "Travel English",
                lessonNumber = 4,
                advancedLevel = 35,
                currentPoints = 12,
                totalPoints = 100,
                progressPercentage = 12
            )
        )

        lessonAdapter.updateLessons(sampleLessons)
    }

    private fun onLessonClick(lesson: Lesson) {
        Toast.makeText(requireContext(), "Clicked on: ${lesson.title}", Toast.LENGTH_SHORT).show()
        // TODO: Navigate to lesson detail or start lesson
    }
}
