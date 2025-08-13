package com.sun.englishlearning.screen.courses

import com.sun.englishlearning.data.model.Category
import com.sun.englishlearning.utils.base.BasePresenter

interface CoursesContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showCategories(categories: List<Category>)
        fun showError(message: String)
    }

    interface Presenter : BasePresenter<View> {
        fun loadCategories()
    }
}
