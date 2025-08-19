package com.sun.englishlearning.data.repository

import com.sun.englishlearning.data.model.WordSearchResult

interface DictionaryRepository {
    suspend fun searchWord(query: String): Result<WordSearchResult>
}

