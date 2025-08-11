package com.sun.englishlearning.screen.me

import android.view.LayoutInflater
import android.view.ViewGroup
import com.sun.englishlearning.R
import com.sun.englishlearning.databinding.FragmentMeBinding
import com.sun.englishlearning.utils.base.BaseFragment

class MeFragment : BaseFragment<FragmentMeBinding>() {

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMeBinding {
        return FragmentMeBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        // UI setup for me fragment
    }

    override fun initData() {
    }
}
