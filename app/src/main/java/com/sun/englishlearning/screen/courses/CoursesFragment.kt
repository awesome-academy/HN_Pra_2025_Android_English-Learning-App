package com.sun.englishlearning.screen.courses

import android.view.LayoutInflater
import android.view.ViewGroup
import com.sun.englishlearning.R
import com.sun.englishlearning.databinding.FragmentCoursesBinding
import com.sun.englishlearning.utils.base.BaseFragment

class CoursesFragment : BaseFragment<FragmentCoursesBinding>() {

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCoursesBinding {
        return FragmentCoursesBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        // UI setup for courses fragment
    }

    override fun initData() {
    }
}
