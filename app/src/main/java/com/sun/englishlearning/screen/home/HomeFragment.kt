package com.sun.englishlearning.screen.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.sun.englishlearning.R
import com.sun.englishlearning.data.model.SuggestedCourse
import com.sun.englishlearning.databinding.FragmentHomeBinding
import com.sun.englishlearning.databinding.ItemSuggestedCourseBinding
import com.sun.englishlearning.screen.search.WordSearchActivity
import com.sun.englishlearning.utils.base.BaseFragment

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        setupClickListeners()
        updateStudyTime()
        setupSuggestedCourse()
    }

    override fun initData() {
    }

    private fun setupClickListeners() {
        with(viewBinding) {
            ivSearch.setOnClickListener {
                val intent = Intent(requireContext(), WordSearchActivity::class.java)
                startActivity(intent)
            }

            btnLetsStart.setOnClickListener {
                Toast.makeText(context, "Let's start learning!", Toast.LENGTH_SHORT).show()
            }

            tvSeeAllCourses.setOnClickListener {
                Toast.makeText(context, "See all courses", Toast.LENGTH_SHORT).show()
            }

            tvSeeAllRecent.setOnClickListener {
                Toast.makeText(context, "See all recent", Toast.LENGTH_SHORT).show()
            }

            ivRefresh.setOnClickListener {
                refreshSuggestedCourse()
            }
        }
    }

    private fun updateStudyTime() {
        viewBinding.tvStudyTime.text = getString(R.string.study_time_format, 2, 15)
    }

    private fun setupSuggestedCourse() {
        val suggestedCourse = getSampleSuggestedCourse()
        updateSuggestedCourseCard(suggestedCourse)
    }

    private fun refreshSuggestedCourse() {
        val newSuggestedCourse = getRandomSuggestedCourse()
        updateSuggestedCourseCard(newSuggestedCourse)
        Toast.makeText(context, "Refreshing suggestions", Toast.LENGTH_SHORT).show()
    }

    private fun updateSuggestedCourseCard(course: SuggestedCourse) {
        val suggestedCourseBinding = ItemSuggestedCourseBinding.bind(viewBinding.suggestedCourseCard.root)
        
        suggestedCourseBinding.apply {
            tvCourseTitle.text = course.title
            tvCourseSubtitle.text = course.subtitle
            ivCourseImage.setImageResource(course.imageResId)
            
            // Handle click on suggested course card
            root.setOnClickListener {
                Toast.makeText(context, "Opening course: ${course.title}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getSampleSuggestedCourse(): SuggestedCourse {
        return SuggestedCourse(
            id = "1",
            title = "How to Conversation in the office with your colleagues",
            subtitle = "At Work vocabulary",
            imageResId = R.drawable.img_ob3,
            isCompleted = true,
            category = "Business",
            difficulty = "Intermediate",
            estimatedTime = "30 min"
        )
    }

    private fun getRandomSuggestedCourse(): SuggestedCourse {
        val courses = listOf(
            SuggestedCourse(
                id = "1",
                title = "How to Conversation in the office with your colleagues",
                subtitle = "At Work vocabulary",
                imageResId = R.drawable.img_ob3,
                isCompleted = true
            ),
            SuggestedCourse(
                id = "2",
                title = "Essential Travel English for Your Next Trip",
                subtitle = "Travel vocabulary",
                imageResId = R.drawable.img_ob1,
                isCompleted = false
            ),
            SuggestedCourse(
                id = "3",
                title = "Daily English Conversation Practice",
                subtitle = "Speaking practice",
                imageResId = R.drawable.img_ob2,
                isCompleted = false
            )
        )
        return courses.random()
    }
}
