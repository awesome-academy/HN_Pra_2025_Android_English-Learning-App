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

    override fun register(name: String, email: String, pass: String) {
        // --- Start input validation ---
        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            view?.onRegisterFailure("Vui lòng nhập đầy đủ thông tin.")
            return
        }
        if (pass.length < 6) {
            view?.onRegisterFailure("Mật khẩu phải có ít nhất 6 ký tự.")
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
                    view?.onRegisterFailure(exception.message ?: "Lỗi không xác định")
                }
            }
        }
    }

    override fun attachView(view: RegisterContract.View?) { this.view = view }
    override fun detachView() { this.view = null }
    override fun onStart() {}
    override fun onStop() {}
}
