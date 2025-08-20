package com.sun.englishlearning.data.repository.source.remote

import java.lang.Exception

/**
 * Interface for handling async results from remote data source
 */
interface OnResultListener<T> {
    fun onSuccess(data: T)
    fun onError(exception: Exception?)
}
