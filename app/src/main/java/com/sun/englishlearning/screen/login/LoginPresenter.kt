package com.sun.englishlearning.screen.login

import android.content.Context
import com.sun.englishlearning.BuildConfig
import com.sun.englishlearning.R
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
            val result = authRepository.signInWithGoogle(
                context = context,
                serverClientId = BuildConfig.WEB_CLIENT_ID
            )
            withContext(Dispatchers.Main) {
                view?.hideLoading()
                result.onSuccess {
                    view?.onLoginSuccess()
                }.onFailure { exception ->
                    view?.onLoginFailure(exception.message?: "An error has occurred")
                }
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
