package com.sun.englishlearning.screen.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.sun.englishlearning.databinding.FragmentOnboardingPageBinding
import com.sun.englishlearning.utils.base.BaseFragment

class OnboardingPageFragment : BaseFragment<FragmentOnboardingPageBinding>() {

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentOnboardingPageBinding {
        return FragmentOnboardingPageBinding.inflate(inflater, container, false)
    }

    override fun initData() {
        arguments?.let {
            viewBinding.ivOnboardingImage.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), it.getInt(ARG_IMAGE_RES))
            )
            viewBinding.tvOnboardingTitle.text = it.getString(ARG_TITLE)
            viewBinding.tvOnboardingDescription.text = it.getString(ARG_DESC)
        }
    }

    override fun initView() {}

    companion object {
        private const val ARG_IMAGE_RES = "image_res"
        private const val ARG_TITLE = "title"
        private const val ARG_DESC = "desc"

        fun newInstance(imageRes: Int, title: String, description: String): OnboardingPageFragment {
            return OnboardingPageFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_IMAGE_RES, imageRes)
                    putString(ARG_TITLE, title)
                    putString(ARG_DESC, description)
                }
            }
        }
    }
}
