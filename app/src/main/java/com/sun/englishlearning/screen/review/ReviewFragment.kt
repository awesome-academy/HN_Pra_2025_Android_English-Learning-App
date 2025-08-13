package com.sun.englishlearning.screen.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sun.englishlearning.R
import com.sun.englishlearning.databinding.FragmentReviewBinding
import com.sun.englishlearning.utils.base.BaseFragment

class ReviewFragment : BaseFragment<FragmentReviewBinding>() {

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentReviewBinding {
        return FragmentReviewBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        viewBinding.textViewTitle.text = getString(R.string.title_review)
    }

    override fun initData() {
    }
}
