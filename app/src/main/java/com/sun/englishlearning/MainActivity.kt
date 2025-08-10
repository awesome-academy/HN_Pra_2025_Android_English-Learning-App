package com.sun.englishlearning

import android.content.Intent
import android.view.LayoutInflater
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.sun.englishlearning.databinding.ActivityMainBinding
import com.sun.englishlearning.screen.login.LoginActivity
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
    }

    override fun initData() {
    }
    fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        // These flags will clear all old Activities (like MainActivity) from the back stack
        // and create LoginActivity as a new task.
        // This prevents the user from pressing the "Back" button to return to the main screen after logging out.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Close the current MainActivity
    }
}
