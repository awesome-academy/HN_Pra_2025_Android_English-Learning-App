package com.sun.englishlearning.data.model

import java.util.Date

data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoURL: String = "",
    val provider: String = "", // "google", "email", "facebook"
    val isEmailVerified: Boolean = false,
    val currentLevel: String = "Beginner",
    val totalPoints: Int = 0,
    val streak: Int = 0,
    val lessonsCompleted: Int = 0,
    val wordsLearned: Int = 0,
    val joinedAt: Date = Date(),
    val lastActiveAt: Date = Date(),
    val preferences: UserPreferences = UserPreferences()
)

data class UserPreferences(
    val language: String = "en",
    val notifications: Boolean = true,
    val studyReminder: String = "18:00" // Time format HH:mm
)
