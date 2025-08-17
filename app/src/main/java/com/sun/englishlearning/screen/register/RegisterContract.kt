package com.sun.englishlearning.screen.register

import com.sun.englishlearning.utils.base.BasePresenter

interface RegisterContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun onRegisterSuccess()
        fun onRegisterFailure(message: String)
        fun showEmailCollisionError()
        fun showPasswordMismatchError()
    }

    interface Presenter : BasePresenter<View> {
        fun register(name: String, email: String, pass: String)
        fun handleGoogleSignUp()
    }
}
