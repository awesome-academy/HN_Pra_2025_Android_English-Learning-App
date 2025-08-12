package com.sun.englishlearning.screen.splash
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sun.englishlearning.MainActivity
import com.sun.englishlearning.screen.login.LoginActivity
import com.sun.englishlearning.screen.onboarding.OnboardingActivity
import com.sun.englishlearning.utils.AppPreferences

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Navigation logic
        AppPreferences.setFirstLaunch(this, true)
        if (AppPreferences.isFirstLaunch(this)) {
            startActivity(Intent(this, OnboardingActivity::class.java))
        } else {
            val currentUser = Firebase.auth.currentUser
            if (currentUser != null) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
        finish()
    }
}
