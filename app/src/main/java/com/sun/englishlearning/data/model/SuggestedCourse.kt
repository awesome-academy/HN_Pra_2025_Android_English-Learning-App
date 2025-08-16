package com.sun.englishlearning.data.model

data class SuggestedCourse(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val imageResId: Int = 0,
    val imageUrl: String = "",
    val isCompleted: Boolean = false,
    val category: String = "",
    val difficulty: String = "",
    val estimatedTime: String = ""
)
