package com.sun.englishlearning.data.model

data class SavedWord(
    val id: Int,
    val word: String,
    val translation: String,
    val isBookmarked: Boolean = true
)
