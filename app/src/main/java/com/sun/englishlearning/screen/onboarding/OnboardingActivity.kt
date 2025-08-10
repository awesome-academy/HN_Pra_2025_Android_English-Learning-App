package com.sun.englishlearning.screen.onboarding

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sun.englishlearning.R

import android.content.Intent
import android.view.LayoutInflater
import com.google.android.material.tabs.TabLayoutMediator
import com.sun.englishlearning.databinding.ActivityOnboardingBinding
import com.sun.englishlearning.screen.login.LoginActivity
import com.sun.englishlearning.utils.AppPreferences
import com.sun.englishlearning.utils.base.BaseActivity

class OnboardingActivity : BaseActivity<ActivityOnboardingBinding>() {

    override fun inflateBinding(inflater: LayoutInflater): ActivityOnboardingBinding {
        return ActivityOnboardingBinding.inflate(inflater)
    }

    override fun initView() {
        val adapter = OnboardingAdapter(this)
        binding.viewPager.adapter = adapter

        binding.btnGetStarted.setOnClickListener {
            // Mark as onboarding viewed
            AppPreferences.setFirstLaunch(this, false)

            // Navigate to the login screen
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun initData() {}
}
