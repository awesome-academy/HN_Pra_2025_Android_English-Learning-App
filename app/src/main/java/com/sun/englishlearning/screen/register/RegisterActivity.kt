// screen/register/RegisterActivity.kt
package com.sun.englishlearning.screen.register

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sun.englishlearning.MainActivity
import com.sun.englishlearning.databinding.ActivityRegisterBinding
import com.sun.englishlearning.screen.login.LoginActivity
import com.sun.englishlearning.utils.base.BaseActivity

class RegisterActivity : BaseActivity<ActivityRegisterBinding>(), RegisterContract.View {

    private lateinit var presenter: RegisterContract.Presenter

    override fun inflateBinding(inflater: LayoutInflater): ActivityRegisterBinding {
        return ActivityRegisterBinding.inflate(inflater)
    }

    override fun initView() {
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            presenter.register(name, email, password)
        }

        binding.btnGoogleSignUp.setOnClickListener {
            presenter.handleGoogleSignUp()
        }

        binding.tvLoginLink.setOnClickListener {
            // Navigate to login screen
            val intent = Intent(this, LoginActivity::class.java)
            // Clear any existing LoginActivity from stack to avoid duplicates
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    override fun initData() {
        presenter = RegisterPresenter(this)
        presenter.attachView(this)
    }

    override fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false
        binding.btnGoogleSignUp.isEnabled = false
        binding.tvLoginLink.isEnabled = false
    }

    override fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnRegister.isEnabled = true
        binding.btnGoogleSignUp.isEnabled = true
        binding.tvLoginLink.isEnabled = true
    }

    override fun onRegisterSuccess() {
        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onRegisterFailure(message: String) {
        Toast.makeText(this, "Lỗi: $message", Toast.LENGTH_LONG).show()
    }

    override fun showEmailCollisionError() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Email đã tồn tại")
            .setMessage("Email này đã được sử dụng. Vui lòng sử dụng email khác hoặc quay lại đăng nhập.")
            .setPositiveButton("Đã hiểu", null)
            .show()
    }

    override fun showPasswordMismatchError() {
        // This method is no longer needed since we removed confirm password field
        // But we keep it for interface compatibility
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }
}
