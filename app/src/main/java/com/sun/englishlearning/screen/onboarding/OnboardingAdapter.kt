package com.sun.englishlearning.screen.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sun.englishlearning.R

class OnboardingAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3 // Number of onboarding pages

    override fun createFragment(position: Int): Fragment {
        // TODO: replace with new fragment when implementing this screen
        return when (position) {
            0 -> OnboardingPageFragment.newInstance(
                R.drawable.ic_home,
                "Learn vocabulary by topic",
                "Easily access and memorize important vocabulary by field."
            )
            1 -> OnboardingPageFragment.newInstance(
                R.drawable.ic_review,
                "Track your progress",
                "Know how many words you've learned and what needs to be reviewed."
            )
            else -> OnboardingPageFragment.newInstance(
                R.drawable.ic_me,
                "Intelligent review",
                "The reminder and classification system helps you consolidate your knowledge effectively."
            )
        }
    }
}
