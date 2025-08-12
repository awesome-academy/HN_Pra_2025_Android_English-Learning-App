package com.sun.englishlearning.data.model

import java.io.Serializable

data class Lesson(
    val id: String = "",
    val title: String = "",
    val lessonNumber: Int = 0,
    val advancedLevel: Int = 0,
    val currentPoints: Int = 0,
    val totalPoints: Int = 100,
    val progressPercentage: Int = 0,
    val imageRes: Int = 0,
    val imageUrl: String = "",
    val description: String = "",
    val isCompleted: Boolean = false
) : Serializable
