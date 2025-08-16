package com.sun.englishlearning.data.model

import java.io.Serializable

data class Lesson(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val image: String = "",
    val vocabulary: List<String> = emptyList()
) : Serializable
