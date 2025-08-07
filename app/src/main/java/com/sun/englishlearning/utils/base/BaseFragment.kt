package com.sun.englishlearning.utils.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _viewBinding: VB? = null
    protected val viewBinding get() = _viewBinding!!

    abstract fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB // Thêm container

    abstract fun initData()
    abstract fun initView()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _viewBinding = inflateViewBinding(inflater, container) // Sửa ở đây
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }
}
