package com.sun.englishlearning.data.model

data class Category(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val order: Int = 0,
    val wordList: List<String> = emptyList()
)
