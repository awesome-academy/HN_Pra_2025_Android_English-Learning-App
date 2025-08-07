package com.sun.englishlearning.screen.courses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sun.englishlearning.R
import com.sun.englishlearning.databinding.FragmentHomeBinding
import com.sun.englishlearning.utils.base.BaseFragment

class CoursesFragment : BaseFragment<FragmentHomeBinding>() {

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        viewBinding.textViewTitle.text = getString(R.string.title_courses)
    }

    override fun initData() {
    }
}
