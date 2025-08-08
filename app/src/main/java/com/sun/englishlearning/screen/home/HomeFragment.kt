package com.sun.englishlearning.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sun.englishlearning.R
import com.sun.englishlearning.databinding.FragmentHomeBinding
import com.sun.englishlearning.utils.base.BaseFragment

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        viewBinding.textViewTitle.text = getString(R.string.title_home)
    }

    override fun initData() {
    }
}
