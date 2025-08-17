package com.sun.englishlearning.screen.login

import com.sun.englishlearning.utils.base.BasePresenter

interface LoginContract {
    interface View {
        fun onLoginSuccess()
        fun onLoginFailure(message: String)
        fun showLoading()
        fun hideLoading()
    }

    interface Presenter : BasePresenter<View> {
        fun handleGoogleSignIn()

        fun handleEmailSignIn(email: String, password: String)
    }
}
