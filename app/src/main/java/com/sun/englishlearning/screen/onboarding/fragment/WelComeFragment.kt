package com.sun.englishlearning.screen.onboarding.fragment


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.sun.englishlearning.databinding.FragmentWelComeBinding
import com.sun.englishlearning.utils.base.BaseFragment
import android.os.Handler
import android.os.Looper
import com.sun.englishlearning.R
import com.sun.englishlearning.utils.SharePreference

class WelComeFragment : BaseFragment<FragmentWelComeBinding>() {

    override val isInsets: Boolean
        get() = false

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentWelComeBinding = FragmentWelComeBinding.inflate(inflater, container, false)

    override fun initData() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isAdded) return@postDelayed
            val seen = SharePreference.isOnboardingSeen(requireContext())
            val actionId = if (seen) R.id.action_welcome_to_home else R.id.action_welcome_to_onboarding
            findNavController().navigate(actionId)
        }, 2000)
    }

    override fun initView() {

    }

}

