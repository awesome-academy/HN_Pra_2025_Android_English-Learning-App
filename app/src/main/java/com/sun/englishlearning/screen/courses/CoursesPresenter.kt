// screen/courses/CoursesPresenter.kt
package com.sun.englishlearning.screen.courses

import com.sun.englishlearning.data.repository.CategoryRepository
import com.sun.englishlearning.data.repository.CategoryRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CoursesPresenter(
    private val repository: CategoryRepository = CategoryRepositoryImpl()
) : CoursesContract.Presenter {

    private var view: CoursesContract.View? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main)

    override fun loadCategories() {
        view?.showLoading()
        presenterScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.getCategories()
            }
            result.onSuccess { categories ->
                view?.showCategories(categories)
            }.onFailure { error ->
                view?.showError(error.message ?: "Lỗi không xác định")
            }
            view?.hideLoading()
        }
    }

    override fun attachView(view: CoursesContract.View?) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }

    override fun onStart() {}
    override fun onStop() {}
}
