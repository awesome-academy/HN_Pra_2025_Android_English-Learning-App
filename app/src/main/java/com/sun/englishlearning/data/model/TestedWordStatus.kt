package com.sun.englishlearning.data.model

import java.util.Date

/**
 * Model to store the test status of a word for a user.
 * If the user answers incorrectly, classify as Weak; if correct, classify as Medium.
 */
data class TestedWordStatus(
    val userId: String,
    val wordId: String,
    val status: WordType, // Weak or Medium
    val testedAt: Date = Date(),
    val isCorrect: Boolean
)
