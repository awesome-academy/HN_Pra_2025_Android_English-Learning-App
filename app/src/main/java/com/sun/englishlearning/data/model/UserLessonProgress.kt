package com.sun.englishlearning.data.model

import java.util.Date

data class UserLessonProgress(
    val id: String = "",
    val userId: String = "",
    val lessonId: String = "",
    val isStarted: Boolean = false,
    val isCompleted: Boolean = false,
    val currentPoints: Int = 0,
    val totalPoints: Int = 100,
    val progressPercentage: Int = 0,
    val timeSpentMinutes: Int = 0,
    val attempts: Int = 0,
    val bestScore: Int = 0,
    val wordsLearned: Int = 0,
    val totalWords: Int = 0,
    val completedExercises: List<String> = emptyList(),
    val learnedWordIds: List<String> = emptyList(),
    val startedAt: Date = Date(),
    val completedAt: Date? = null,
    val lastAccessedAt: Date = Date()
)