package com.sun.englishlearning.screen.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sun.englishlearning.databinding.ActivityOnboardingBinding
import com.sun.englishlearning.screen.login.LoginActivity
import com.sun.englishlearning.screen.register.RegisterActivity
import com.sun.englishlearning.utils.AppPreferences
import com.sun.englishlearning.utils.base.BaseActivity

class OnboardingActivity : BaseActivity<ActivityOnboardingBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun inflateBinding(inflater: LayoutInflater): ActivityOnboardingBinding {
        return ActivityOnboardingBinding.inflate(inflater)
    }

    override fun initView() {
        val adapter = OnboardingAdapter(this)
        binding.viewPager.adapter = adapter

        binding.btnGetStarted.setOnClickListener {
            // Mark as onboarding viewed
            AppPreferences.setFirstLaunch(this, false)

            // Navigate to the register screen
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
        
        binding.tvLogin.setOnClickListener {
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
