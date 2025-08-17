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
import com.sun.englishlearning.utils.base.BaseActivity

class RegisterActivity : BaseActivity<ActivityRegisterBinding>(), RegisterContract.View {

    private val presenter: RegisterContract.Presenter = RegisterPresenter()

    override fun inflateBinding(inflater: LayoutInflater): ActivityRegisterBinding {
        return ActivityRegisterBinding.inflate(inflater)
    }

    override fun initView() {
        presenter.attachView(this)

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            presenter.register(name, email, password, confirmPassword)
        }

        binding.tvLoginLink.setOnClickListener {
            // Return to login screen
            finish()
        }

        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    override fun initData() {}

    override fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false
    }

    override fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnRegister.isEnabled = true
    }

    override fun onRegisterSuccess() {
        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onRegisterFailure(message: String) {
        Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
    }

    override fun showEmailCollisionError() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Email already exists")
            .setMessage("This email is already in use. Please use a different email or go back to login.")
            .setPositiveButton("Understood", null)
            .show()
    }

    override fun showPasswordMismatchError() {
        // We can set error directly on TextInputLayout
        binding.tilConfirmPassword.error = "Passwords do not match"
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }
}
