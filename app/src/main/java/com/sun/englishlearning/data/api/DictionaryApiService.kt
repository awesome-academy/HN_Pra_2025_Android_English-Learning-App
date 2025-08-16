package com.sun.englishlearning.data.api

import com.sun.englishlearning.data.model.WordApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DictionaryApiService {
    @GET("en/{word}")
    suspend fun searchWord(@Path("word") word: String): Response<List<WordApiResponse>>
}