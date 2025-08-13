package com.sun.englishlearning.screen.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.sun.englishlearning.R
import com.sun.englishlearning.databinding.FragmentOnboardingPageBinding
import com.sun.englishlearning.utils.base.BaseFragment

class OnboardingPageFragment : BaseFragment<FragmentOnboardingPageBinding>() {

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentOnboardingPageBinding {
        return FragmentOnboardingPageBinding.inflate(inflater, container, false)
    }

    override fun initData() {
        // We'll handle everything in initView()
    }

    override fun initView() {
        val index = requireArguments().getInt(ARG_INDEX)
        when (index) {
            0 -> {
                viewBinding.tvOnboardingTitle.text = getString(R.string.title_ob1)
                viewBinding.tvOnboardingDescription.text = getString(R.string.content_ob1)
                viewBinding.ivOnboardingImage.setImageResource(R.drawable.img_ob1)
            }
            1 -> {
                viewBinding.tvOnboardingTitle.text = getString(R.string.title_ob2)
                viewBinding.tvOnboardingDescription.text = getString(R.string.content_ob2)
                viewBinding.ivOnboardingImage.setImageResource(R.drawable.img_ob2)
            }
            else -> {
                viewBinding.tvOnboardingTitle.text = getString(R.string.title_ob3)
                viewBinding.tvOnboardingDescription.text = getString(R.string.content_ob3)
                viewBinding.ivOnboardingImage.setImageResource(R.drawable.img_ob3)
            }
        }
    }

    companion object {
        private const val ARG_IMAGE_RES = "image_res"
        private const val ARG_TITLE = "title"
        private const val ARG_DESC = "desc"
        private const val ARG_INDEX = "index"

        fun newInstance(index: Int): OnboardingPageFragment {
            return OnboardingPageFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_INDEX, index)
                }
            }
        }
    }
}
