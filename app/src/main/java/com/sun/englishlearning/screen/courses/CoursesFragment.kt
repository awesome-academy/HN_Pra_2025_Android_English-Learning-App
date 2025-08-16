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
import androidx.fragment.app.Fragment
import com.sun.englishlearning.screen.courses.adapter.CoursesAdapter

class CoursesFragment : Fragment(), CoursesContract.View {

    private var _viewBinding: ActivityLessonsBinding? = null
    private val viewBinding get() = _viewBinding!!

    private lateinit var coursesAdapter: CoursesAdapter
    private lateinit var presenter: CoursesContract.Presenter
    private var isOngoingTabSelected = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _viewBinding = ActivityLessonsBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPresenter()
        initView()
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
        _viewBinding = null
    }

    private fun initPresenter() {
        presenter = CoursesPresenter()
        (presenter as CoursesPresenter).setContext(requireContext())
        presenter.attachView(this)
    }

    private fun initView() {
        setupRecyclerView()
        setupTabs()
        setupBackButton()
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

    private fun setupBackButton() {
        viewBinding.btnBack.setOnClickListener {
            // Handle back button click
            findNavController().navigateUp()
        }
    }

    // MVP View implementations
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
}
