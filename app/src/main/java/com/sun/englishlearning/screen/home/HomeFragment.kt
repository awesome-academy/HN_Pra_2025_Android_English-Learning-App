package com.sun.englishlearning.screen.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.sun.englishlearning.R
import com.sun.englishlearning.databinding.FragmentHomeBinding
import com.sun.englishlearning.screen.search.WordSearchActivity
import com.sun.englishlearning.utils.base.BaseFragment

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        setupClickListeners()
        updateStudyTime()
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
                Toast.makeText(context, "Refreshing suggestions", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStudyTime() {
        viewBinding.tvStudyTime.text = getString(R.string.study_time_format, 2, 15)
    }
}
