package com.sun.englishlearning.data.model

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class UserLessonProgress(
    var id: String = "",
    var userId: String = "",
    var lessonId: String = "",
    @get:PropertyName("isStarted") @set:PropertyName("isStarted") var isStarted: Boolean = false,
    @get:PropertyName("isCompleted") @set:PropertyName("isCompleted") var isCompleted: Boolean = false,
    var currentPoints: Int = 0,
    var totalPoints: Int = 100,
    var progressPercentage: Int = 0,
    var timeSpentMinutes: Int = 0,
    var attempts: Int = 0,
    var bestScore: Int = 0,
    var wordsLearned: Int = 0,
    var totalWords: Int = 0,
    var completedExercises: List<String> = emptyList(),
    var learnedWordIds: List<String> = emptyList(),
    var startedAt: Date = Date(),
    var completedAt: Date? = null,
    var lastAccessedAt: Date = Date()
)
