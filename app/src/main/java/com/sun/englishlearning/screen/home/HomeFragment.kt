package com.sun.englishlearning.screen.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    }

    override fun initData() {
        
    }

    private fun setupClickListeners() {
        viewBinding.apply {
            ivSearch.setOnClickListener {
                val intent = Intent(requireContext(), WordSearchActivity::class.java)
                startActivity(intent)
            }
            
            btnLetsStart.setOnClickListener {
                // Handle let's start button click
            }
            
            llViewAllCourses.setOnClickListener {
                // Handle view all courses click
            }
            
            llViewAllRecent.setOnClickListener {
                // Handle view all recent click
            }
            
            // Category clicks
            llTravelCategory.setOnClickListener {
                // Handle travel category click
            }
            
            llPracticeCategory.setOnClickListener {
                // Handle practice category click
            }
            
            llBusinessCategory.setOnClickListener {
                // Handle business category click
            }
            
            llAcademicCategory.setOnClickListener {
                // Handle academic category click
            }
            
            // Recent learning cards
            llTouristTrip.setOnClickListener {
                // Handle tourist trip card click
            }
            
            llHangOutFriends.setOnClickListener {
                // Handle hang out friends card click
            }
            
            // Suggested courses
            ivRefreshSuggestions.setOnClickListener {
                // Handle refresh suggestions click
            }
            
            llSuggestedCourse.setOnClickListener {
                // Handle suggested course click
            }
        }
    }
}
