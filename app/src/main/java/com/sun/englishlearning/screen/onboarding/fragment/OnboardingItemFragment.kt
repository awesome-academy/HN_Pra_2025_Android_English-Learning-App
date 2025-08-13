package com.sun.englishlearning.screen.onboarding.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.sun.englishlearning.databinding.FragmentOnboardingItemBinding
import com.sun.englishlearning.utils.base.BaseFragment
import com.sun.englishlearning.R
class OnboardingItemFragment : BaseFragment<FragmentOnboardingItemBinding>() {

    override val isInsets: Boolean
        get() = false

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOnboardingItemBinding = FragmentOnboardingItemBinding.inflate(inflater, container, false)

    override fun initData() {}

    override fun initView() {
        val index = requireArguments().getInt(ARG_INDEX)
        when (index) {
            0 -> {
                viewBinding.title.text = getString(R.string.title_ob1)
                viewBinding.subtitle.text = getString(R.string.content_ob1)
                viewBinding.image.setImageResource(R.drawable.img_ob1)
            }
            1 -> {
                viewBinding.title.text = getString(R.string.title_ob2)
                viewBinding.subtitle.text = getString(R.string.content_ob2)
                viewBinding.image.setImageResource(R.drawable.img_ob2)
            }
            else -> {
                viewBinding.title.text = getString(R.string.title_ob3)
                viewBinding.subtitle.text = getString(R.string.content_ob3)
                viewBinding.image.setImageResource(R.drawable.img_ob3)
            }
        }
    }

    companion object {
        private const val ARG_INDEX = "arg_index"
        fun newInstance(index: Int): OnboardingItemFragment {
            val fragment = OnboardingItemFragment()
            fragment.arguments = Bundle().apply { putInt(ARG_INDEX, index) }
            return fragment
        }
    }
}


