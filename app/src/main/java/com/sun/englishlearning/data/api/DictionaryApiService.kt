package com.sun.englishlearning.data.api

import com.sun.englishlearning.data.model.DictionaryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DictionaryApiService {
    @GET("api/v2/entries/en/{word}")
    suspend fun searchWord(@Path("word") word: String): Response<List<DictionaryResponse>>
}
