package com.sun.englishlearning.data.model

import com.google.firebase.firestore.PropertyName
import java.util.Date

enum class WordType(val value: Int) {
    SAVED(1),
    WEAK(2),
    MEDIUM(3),
    STRONG(4)
}

data class SavedWord(
    val id: String = "",
    val userId: String = "",
    val word: String = "",
    val ipa: String = "",
    val partOfSpeech: String = "",
    val definition: String = "",
    val example: String = "",
    val soundUrl: String = "",
    @get:PropertyName("wordType") val wordType: Int = WordType.SAVED.value,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val streak: Int = 0 // Number of consecutive correct answers
)
