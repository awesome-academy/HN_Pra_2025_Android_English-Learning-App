package com.sun.englishlearning.screen.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.sun.englishlearning.MainActivity
import com.sun.englishlearning.databinding.ActivityLoginBinding
import com.sun.englishlearning.utils.DialogUtils
import com.sun.englishlearning.screen.register.RegisterActivity
import com.sun.englishlearning.utils.base.BaseActivity

class LoginActivity : BaseActivity<ActivityLoginBinding>(), LoginContract.View {

    private lateinit var presenter: LoginContract.Presenter

    override fun inflateBinding(inflater: LayoutInflater): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(inflater)
    }

    override fun initView() {
        // Assign click events for all buttons
        binding.btnGoogleSignIn.setOnClickListener {
            presenter.handleGoogleSignIn()
        }

        binding.btnEmailLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            presenter.handleEmailSignIn(email, password)
        }

        binding.tvRegister.setOnClickListener {
            // Navigate to registration screen
            val intent = Intent(this, RegisterActivity::class.java)
            // Clear any existing RegisterActivity from stack to avoid duplicates
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        binding.tvForgotPassword.setOnClickListener {
            // Temporarily show Toast, will implement forgot password feature later
            Toast.makeText(this, "Feature under development", Toast.LENGTH_SHORT).show()
        }
    }

    override fun initData() {
        presenter = LoginPresenter(this)
        presenter.attachView(this)
    }

    override fun onLoginSuccess() {
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        // Clear previous activities from stack so user can't go back to login screen
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onLoginFailure(message: String) {
        DialogUtils.showErrorDialog(
            context = this,
            message = "Login failed: $message"
        )
    }

    override fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        // Disable buttons to prevent multiple clicks
        binding.btnEmailLogin.isEnabled = false
        binding.btnGoogleSignIn.isEnabled = false
        binding.tvRegister.isEnabled = false
    }

    override fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        // Re-enable buttons
        binding.btnEmailLogin.isEnabled = true
        binding.btnGoogleSignIn.isEnabled = true
        binding.tvRegister.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }
}
