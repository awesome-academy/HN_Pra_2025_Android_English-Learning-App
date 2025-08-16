package com.sun.englishlearning.data.model

import java.io.Serializable
import java.util.Date

enum class LessonDifficulty {
    EASY, MEDIUM, ADVANCED
}

data class Lesson(
    val id: String = "",
    val courseId: String = "",
    val title: String = "",
    val lessonNumber: Int = 0,
    val description: String = "",
    val duration: String = "",
    val difficulty: LessonDifficulty = LessonDifficulty.EASY,
    val totalPoints: Int = 100,
    val wordIds: List<String> = emptyList(),
    val exercises: List<String> = emptyList(),
    val videoUrl: String = "",
    val audioUrl: String = "",
    val imageRes: Int = 0,
    val imageUrl: String = "",
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    // Field to determine if user has started this lesson - computed at runtime, not stored in DB
    val isStarted: Boolean = false
) : Serializable
