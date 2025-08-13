package com.sun.englishlearning.screen.recent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.sun.englishlearning.databinding.FragmentRecentCoursesBinding
import com.sun.englishlearning.utils.base.BaseFragment

class RecentCoursesFragment : BaseFragment<FragmentRecentCoursesBinding>() {

    private lateinit var recentCoursesAdapter: RecentCoursesAdapter

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRecentCoursesBinding {
        return FragmentRecentCoursesBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        setupRecyclerView()
        setupClickListeners()
    }

    override fun initData() {
        loadRecentCourses()
    }

    private fun setupRecyclerView() {
        recentCoursesAdapter = RecentCoursesAdapter { course ->
            // Handle course click
        }
        
        viewBinding.rvRecentCourses.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = recentCoursesAdapter
        }
    }

    private fun setupClickListeners() {
        viewBinding.tvSeeAll.setOnClickListener {
            // Handle see all click
        }
    }

    private fun loadRecentCourses() {
        // Load sample data
        val sampleCourses = listOf(
            RecentCourse(
                id = 1,
                title = "Tourist trip",
                imageResId = com.sun.englishlearning.R.drawable.img_ob1,
                lessonCount = 3,
                advancedLevel = 76,
                progress = 6,
                maxProgress = 10
            ),
            RecentCourse(
                id = 2,
                title = "Hang out with friends",
                imageResId = com.sun.englishlearning.R.drawable.img_ob2,
                lessonCount = 3,
                advancedLevel = 76,
                progress = 6,
                maxProgress = 10
            )
        )
        
        recentCoursesAdapter.submitList(sampleCourses)
    }
}
