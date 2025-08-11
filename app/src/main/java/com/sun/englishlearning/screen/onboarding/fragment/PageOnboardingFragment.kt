package com.sun.englishlearning.screen.onboarding.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.sun.englishlearning.databinding.FragmentPageOnboardingBinding
import com.sun.englishlearning.screen.onboarding.adapter.OnboardingVPAdapter
import com.sun.englishlearning.utils.base.BaseFragment
import com.sun.englishlearning.utils.SharePreference

class PageOnboardingFragment : BaseFragment<FragmentPageOnboardingBinding>() {

    override val isInsets: Boolean
        get() = false

    private var onboardingVPAdapter: OnboardingVPAdapter? = null

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPageOnboardingBinding = FragmentPageOnboardingBinding.inflate(inflater, container, false)

    override fun initData() {}

    override fun initView() {
        onboardingVPAdapter = OnboardingVPAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        viewBinding.viewPager.adapter = onboardingVPAdapter

        viewBinding.btnLetGo.setOnClickListener {
            val viewPager = viewBinding.viewPager
            if (viewPager.currentItem < (onboardingVPAdapter?.itemCount ?: 0) - 1) {
                viewPager.currentItem = viewPager.currentItem + 1
            } else {
                SharePreference.setOnboardingSeen(requireContext(), true)
                findNavController().navigate(
                    com.sun.englishlearning.R.id.action_onboarding_to_home
                )
            }
        }

        viewBinding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

            }
        })
    }
}

