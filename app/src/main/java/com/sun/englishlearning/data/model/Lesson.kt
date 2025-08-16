package com.sun.englishlearning.data.model

import java.io.Serializable

data class Lesson(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val vocabulary: List<String> = emptyList()
) : Serializable
