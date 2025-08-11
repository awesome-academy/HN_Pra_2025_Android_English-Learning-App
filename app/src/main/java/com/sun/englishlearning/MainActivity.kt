package com.sun.englishlearning

import android.view.LayoutInflater
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.sun.englishlearning.databinding.ActivityMainBinding
import com.sun.englishlearning.utils.base.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun inflateBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun initView() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val hideBottomNav = when (destination.id) {
                R.id.welcomeFragment, R.id.onboardingFragment -> true
                else -> false
            }
            binding.bottomNavigationView.visibility = if (hideBottomNav) View.GONE else View.VISIBLE
        }
    }

    override fun initData() {
    }
}
