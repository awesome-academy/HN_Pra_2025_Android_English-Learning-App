package com.sun.englishlearning.screen.onboarding.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sun.englishlearning.screen.onboarding.fragment.OnboardingItemFragment

class OnboardingVPAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun createFragment(position: Int): Fragment {
        return OnboardingItemFragment.newInstance(position)
    }

    override fun getItemCount(): Int {
        return 3
    }

}
