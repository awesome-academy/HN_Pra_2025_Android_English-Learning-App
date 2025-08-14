package com.sun.englishlearning.data.repository.source.remote

import com.sun.englishlearning.data.model.Word

/**
 * Interface for handling async results from remote data source
 */
interface OnResultListener<T> {
    fun onSuccess(data: T)
    fun onError(error: String)
    fun onLoading()
}
