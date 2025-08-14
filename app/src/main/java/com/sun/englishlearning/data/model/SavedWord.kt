package com.sun.englishlearning.data.model

data class SavedWord(
    val id: String = "",
    val word: String = "",
    val definition: String = "",
    val soundUrl: String = "",
    val example: String = "",
    val isFavorite: Boolean = true
)


