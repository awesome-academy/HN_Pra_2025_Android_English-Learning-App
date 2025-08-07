package com.sun.englishlearning.utils.base

interface BasePresenter<T> {
    fun onStart()
    fun onStop()
    fun attachView(view: T?)
    fun detachView()
}
