package com.sun.englishlearning.data.model

data class VocabularyWord(
    val id: String = "",
    val word: String = "",
    val meaning: String = "",
    val pronunciation: String = "",
    val type: WordType = WordType.WEAK,
    val isBookmarked: Boolean = false
)

enum class WordType {
    WEAK,
    TODAY,
    MEDIUM,
    STRONG
}
