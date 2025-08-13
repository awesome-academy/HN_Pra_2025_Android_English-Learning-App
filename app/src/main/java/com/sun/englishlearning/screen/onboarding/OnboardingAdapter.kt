package com.sun.englishlearning.screen.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sun.englishlearning.R

class OnboardingAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3 // Number of onboarding pages

    override fun createFragment(position: Int): Fragment {
        return OnboardingPageFragment.newInstance(position)
    }
}
