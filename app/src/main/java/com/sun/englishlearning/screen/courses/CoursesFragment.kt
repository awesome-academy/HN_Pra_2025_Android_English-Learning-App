package com.sun.englishlearning.screen.courses

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sun.englishlearning.R
import com.sun.englishlearning.databinding.ActivityLessonsBinding
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.screen.courses.adapter.CoursesAdapter
import com.sun.englishlearning.utils.base.BaseFragment

class CoursesFragment : BaseFragment<ActivityLessonsBinding>(), CoursesContract.View {

    private lateinit var coursesAdapter: CoursesAdapter
    private lateinit var presenter: CoursesContract.Presenter
    private var isOngoingTabSelected = true

    override val isInsets = true

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): ActivityLessonsBinding {
        return ActivityLessonsBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        setupRecyclerView()
        setupTabs()
    }

    override fun initData() {
        presenter = CoursesPresenter()
        (presenter as CoursesPresenter).setContext(requireContext())
        presenter.attachView(this)
        presenter.onTabSelected(isOngoingTabSelected)
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when fragment becomes visible
        if (isResumed && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            refreshData()
        }
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
    }

    private fun setupRecyclerView() {
        coursesAdapter = CoursesAdapter { lesson ->
            presenter.onLessonClicked(lesson)
        }

        viewBinding.recyclerViewLessons.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = coursesAdapter
        }
    }

    private fun setupTabs() {
        // Set initial tab state
        selectTab(true)

        viewBinding.tabOngoing.setOnClickListener {
            presenter.onTabSelected(true)
        }

        viewBinding.tabCompleted.setOnClickListener {
            presenter.onTabSelected(false)
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

    override fun showLoading() {
        // Show loading indicator
    }

    override fun hideLoading() {
        // Hide loading indicator
    }

    override fun showOngoingLessons(lessons: List<Lesson>) {
        coursesAdapter.updateLessons(lessons)

        // Show empty state message if no lessons
        if (lessons.isEmpty()) {
            Toast.makeText(requireContext(), "No ongoing lessons available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun showCompletedLessons(lessons: List<Lesson>) {
        coursesAdapter.updateLessons(lessons)

        // Show empty state message for completed lessons
        if (lessons.isEmpty()) {
            Toast.makeText(requireContext(), "No completed lessons yet. Start learning to see progress!", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Showing lessons with progress
    fun showOngoingLessonsWithProgress(lessons: List<Lesson>, progressMap: Map<String, Int>, wordsLearnedMap: Map<String, Int>) {
        coursesAdapter.updateLessonsWithProgress(lessons, progressMap, wordsLearnedMap)

        // Show empty state message if no lessons
        if (lessons.isEmpty()) {
            Toast.makeText(requireContext(), "No ongoing lessons available", Toast.LENGTH_SHORT).show()
        }
    }

    fun showCompletedLessonsWithProgress(lessons: List<Lesson>, progressMap: Map<String, Int>, wordsLearnedMap: Map<String, Int>) {
        coursesAdapter.updateLessonsWithProgress(lessons, progressMap, wordsLearnedMap)

        // Show empty state message for completed lessons
        if (lessons.isEmpty()) {
            Toast.makeText(requireContext(), "No completed lessons yet. Start learning to see progress!", Toast.LENGTH_SHORT).show()
        }
    }

    // Override methods for backward compatibility
    override fun showOngoingLessonsWithProgress(lessons: List<Lesson>, progressMap: Map<String, Int>) {
        showOngoingLessonsWithProgress(lessons, progressMap, emptyMap())
    }

    override fun showCompletedLessonsWithProgress(lessons: List<Lesson>, progressMap: Map<String, Int>) {
        showCompletedLessonsWithProgress(lessons, progressMap, emptyMap())
    }

    override fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun navigateToLessonDetail(lesson: Lesson) {
        val action = CoursesFragmentDirections.actionCoursesToLessonDetail(lesson)
        findNavController().navigate(action)
    }

    override fun updateTabSelection(isOngoing: Boolean) {
        selectTab(isOngoing)
    }
    
    private fun refreshData() {
        // Refresh data when fragment becomes visible
        presenter.onTabSelected(isOngoingTabSelected)
    }
}
