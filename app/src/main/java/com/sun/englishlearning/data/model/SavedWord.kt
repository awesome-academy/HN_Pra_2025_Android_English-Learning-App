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
    var id: String = "",
    var userId: String = "",
    var word: String = "",
    var ipa: String = "",
    var partOfSpeech: String = "",
    var definition: String = "",
    var example: String = "",
    var soundUrl: String = "",
    @get:PropertyName("wordType") @set:PropertyName("wordType") var wordType: Int = WordType.SAVED.value,

    var createdAt: Date = Date(),
    var updatedAt: Date = Date()
)


