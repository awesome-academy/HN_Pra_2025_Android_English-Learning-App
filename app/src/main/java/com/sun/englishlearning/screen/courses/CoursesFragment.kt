package com.sun.englishlearning.screen.courses

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sun.englishlearning.R
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.data.repository.LessonRepositoryImpl
import com.sun.englishlearning.data.repository.UserLessonProgressRepositoryImpl
import com.sun.englishlearning.databinding.FragmentLessonsBinding
import com.sun.englishlearning.screen.courses.adapter.CoursesAdapter
import com.sun.englishlearning.utils.base.BaseFragment

class CoursesFragment :
    BaseFragment<FragmentLessonsBinding>(),
    CoursesContract.View {

    private lateinit var coursesAdapter: CoursesAdapter
    private lateinit var presenter: CoursesPresenter
    private var isOngoingTabSelected = true

    override val isInsets = true

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentLessonsBinding {
        return FragmentLessonsBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        presenter = CoursesPresenter(
            LessonRepositoryImpl(
                requireContext(),
                UserLessonProgressRepositoryImpl()
            )
        )
        presenter.setContext(requireContext())
        setupRecyclerView()
        setupTabs()
    }

    override fun initData() {
        presenter.attachView(this)
        presenter.onTabSelected(isOngoingTabSelected)
        // Load initial lessons based on the selected tab
        if (isOngoingTabSelected) {
            presenter.loadOngoingLessons()
        } else {
            presenter.loadCompletedLessons()
        }
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
        super.onDestroyView()
        presenter.detachView()
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

    // Showing lessons with progress
    override fun showOngoingLessons(lessons: List<Lesson>, progressMap: Map<String, Int>, wordsLearnedMap: Map<String, Int>) {
        coursesAdapter.updateLessonsWithProgress(lessons, progressMap, wordsLearnedMap)

        // Show empty state message if no lessons
        if (lessons.isEmpty()) {
            Toast.makeText(requireContext(), "No ongoing lessons available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun showCompletedLessons(lessons: List<Lesson>, progressMap: Map<String, Int>, wordsLearnedMap: Map<String, Int>) {
        coursesAdapter.updateLessonsWithProgress(lessons, progressMap, wordsLearnedMap)

        // Show empty state message for completed lessons
        if (lessons.isEmpty()) {
            Toast.makeText(requireContext(), "No completed lessons yet. Start learning to see progress!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun navigateToLessonDetail(lesson: Lesson) {
        try {
            // Add validation for lesson data
            if (lesson.id.isEmpty() || lesson.title.isEmpty()) {
                Log.w("CoursesFragment", "Invalid lesson data: id=${lesson.id}, title=${lesson.title}")
                showError("Invalid lesson data")
                return
            }

            // Check if fragment is still valid before navigation
            if (!isAdded || isDetached || view == null) {
                Log.w("CoursesFragment", "Fragment not in valid state for navigation")
                return
            }

            // Validate navigation controller
            val navController = try {
                findNavController()
            } catch (e: Exception) {
                Log.e("CoursesFragment", "Error getting nav controller", e)
                showError("Navigation error")
                return
            }

            Log.d("CoursesFragment", "Navigating to lesson: ${lesson.title}")
            val action = CoursesFragmentDirections.actionCoursesToLessonDetail(lesson)
            navController.navigate(action)
        } catch (e: Exception) {
            Log.e("CoursesFragment", "Error navigating to lesson detail", e)
            showError("Failed to open lesson: ${e.message}")
        }
    }

    override fun updateTabSelection(isOngoing: Boolean) {
        try {
            if (isAdded && !isDetached) {
                selectTab(isOngoing)
            }
        } catch (e: Exception) {
            Log.e("CoursesFragment", "Error updating tab selection", e)
        }
    }
    
    private fun refreshData() {
        try {
            // Refresh data when fragment becomes visible
            if (::presenter.isInitialized && isAdded && !isDetached) {
                presenter.onTabSelected(isOngoingTabSelected)
            }
        } catch (e: Exception) {
            Log.e("CoursesFragment", "Error refreshing data", e)
        }
    }
}
