package com.sun.englishlearning.screen.recent

data class RecentCourse(
    val id: Int,
    val title: String,
    val imageResId: Int,
    val lessonCount: Int,
    val advancedLevel: Int,
    val progress: Int,
    val maxProgress: Int
)
