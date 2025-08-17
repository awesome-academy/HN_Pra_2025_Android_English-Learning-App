package com.sun.englishlearning.screen.login

import android.content.Context
import com.sun.englishlearning.BuildConfig
import com.sun.englishlearning.data.repository.AuthRepository
import com.sun.englishlearning.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginPresenter(
    private val context: Context,
    private val authRepository: AuthRepository = AuthRepositoryImpl()
) : LoginContract.Presenter {

    private var view: LoginContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main)

    override fun handleGoogleSignIn() {
        view?.showLoading()
        presenterScope.launch {
            val result = withContext(Dispatchers.IO) {
                authRepository.signInWithGoogle(
                    context = context,
                    serverClientId = BuildConfig.WEB_CLIENT_ID
                )
            }
            view?.hideLoading()
            result.onSuccess {
                view?.onLoginSuccess()
            }.onFailure { exception ->
                view?.onLoginFailure(exception.message ?: "An error occurred during Google login")
            }
        }
    }

    override fun handleEmailSignIn(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            view?.onLoginFailure("Please enter both email and password.")
            return
        }

        view?.showLoading()
        presenterScope.launch {
            val result = withContext(Dispatchers.IO) {
                authRepository.signInWithEmailPassword(email, password)
            }
            view?.hideLoading()
            result.onSuccess {
                view?.onLoginSuccess()
            }.onFailure { exception ->
                view?.onLoginFailure(exception.message ?: "Email or password is incorrect")
            }
        }
    }

    override fun onStart() {}
    override fun onStop() {}

    override fun attachView(view: LoginContract.View?) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }
}
