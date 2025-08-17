package com.sun.englishlearning.screen.register

import com.sun.englishlearning.data.repository.AuthRepository
import com.sun.englishlearning.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterPresenter(
    private val authRepository: AuthRepository = AuthRepositoryImpl()
) : RegisterContract.Presenter {

    private var view: RegisterContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main)

    override fun register(name: String, email: String, pass: String, confirmPass: String) {
        // --- Start input validation ---
        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            view?.onRegisterFailure("Please enter all required information.")
            return
        }
        if (pass != confirmPass) {
            view?.showPasswordMismatchError()
            return
        }
        if (pass.length < 6) {
            view?.onRegisterFailure("Password must be at least 6 characters.")
            return
        }
        // --- End validation ---

        view?.showLoading()
        presenterScope.launch {
            val result = withContext(Dispatchers.IO) {
                authRepository.registerWithEmailPassword(email, pass, name)
            }
            view?.hideLoading()
            result.onSuccess {
                // TODO: Update displayName for user
                view?.onRegisterSuccess()
            }.onFailure { exception ->
                if (exception.message == "EMAIL_COLLISION") {
                    view?.showEmailCollisionError()
                } else {
                    view?.onRegisterFailure(exception.message ?: "Unknown error")
                }
            }
        }
    }

    override fun attachView(view: RegisterContract.View?) { this.view = view }
    override fun detachView() { this.view = null }
    override fun onStart() {}
    override fun onStop() {}
}
