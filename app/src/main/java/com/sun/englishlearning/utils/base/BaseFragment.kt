package com.sun.englishlearning.utils.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _viewBinding: VB? = null
    protected val viewBinding get() = _viewBinding!!

    protected open val isInsets = true

    abstract fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB 

    abstract fun initData()
    abstract fun initView()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _viewBinding = inflateViewBinding(inflater, container) 
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isInsets) {
            ViewCompat.setOnApplyWindowInsetsListener(viewBinding.root) { v, insets ->
                val originalPaddingLeft = v.paddingLeft
                val originalPaddingTop = v.paddingTop
                val originalPaddingRight = v.paddingRight
                val originalPaddingBottom = v.paddingBottom

                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
                v.setPadding(
                    systemBars.left + originalPaddingLeft,
                    systemBars.top,
                    systemBars.right + originalPaddingRight,
                    navBars.bottom
                )
                insets
            }
        }

        initView()
        initData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }
}
