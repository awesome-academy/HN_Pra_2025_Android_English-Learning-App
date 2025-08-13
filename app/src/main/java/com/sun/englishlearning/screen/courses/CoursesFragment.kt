package com.sun.englishlearning.screen.courses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sun.englishlearning.R
import com.sun.englishlearning.databinding.ActivityLessonsBinding
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.data.repository.LessonRepository
import com.sun.englishlearning.utils.base.BaseFragment

class CoursesFragment : BaseFragment<ActivityLessonsBinding>() {

    private lateinit var lessonAdapter: LessonAdapter
    private var isOngoingTabSelected = true

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): ActivityLessonsBinding {
        return ActivityLessonsBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        setupRecyclerView()
        setupTabs()
        setupBackButton()
    }

    override fun initData() {
        loadLessons()
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

    private fun setupTabs() {
        // Set initial tab state
        selectTab(true)

        viewBinding.tabOngoing.setOnClickListener {
            selectTab(true)
            loadLessons()
        }

        viewBinding.tabCompleted.setOnClickListener {
            selectTab(false)
            loadLessons()
        }
    }

    private fun selectTab(isOngoing: Boolean) {
        isOngoingTabSelected = isOngoing

        if (isOngoing) {
            // Select Ongoing tab
            viewBinding.tabOngoing.isSelected = true
            viewBinding.tabCompleted.isSelected = false
            viewBinding.tabOngoing.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_text_color))
            viewBinding.tabCompleted.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_text_inactive))
        } else {
            // Select Completed tab
            viewBinding.tabOngoing.isSelected = false
            viewBinding.tabCompleted.isSelected = true
            viewBinding.tabOngoing.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_text_inactive))
            viewBinding.tabCompleted.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_text_color))
        }
    }

    private fun setupBackButton() {
        viewBinding.btnBack.setOnClickListener {
            // Handle back button click
            requireActivity().onBackPressed()
        }
    }

    private fun loadLessons() {
        val lessons = if (isOngoingTabSelected) {
            LessonRepository.getOngoingLessons()
        } else {
            LessonRepository.getCompletedLessons()
        }

        lessonAdapter.updateLessons(lessons)
    }

    private fun onLessonClick(lesson: Lesson) {
        // Navigate to lesson detail
        val action = CoursesFragmentDirections.actionCoursesToLessonDetail(lesson)
        findNavController().navigate(action)
    }
}
