package com.sun.englishlearning.screen.login

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.sun.englishlearning.MainActivity
import com.sun.englishlearning.databinding.ActivityLoginBinding
import com.sun.englishlearning.utils.base.BaseActivity

class LoginActivity : BaseActivity<ActivityLoginBinding>(), LoginContract.View {

    private lateinit var presenter: LoginContract.Presenter

    override fun inflateBinding(inflater: LayoutInflater): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(inflater)
    }

    override fun initView() {
        binding.btnGoogleSignIn.setOnClickListener {
            presenter.handleGoogleSignIn()
        }
    }

    override fun initData() {
        presenter = LoginPresenter(this)
        presenter.attachView(this)
    }

    override fun onLoginSuccess() {
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onLoginFailure(message: String) {
        Toast.makeText(this, "Login failed: $message", Toast.LENGTH_LONG).show()
    }

    override fun showLoading() {
        binding.btnGoogleSignIn.isEnabled = false
    }

    override fun hideLoading() {
        binding.btnGoogleSignIn.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }
}
