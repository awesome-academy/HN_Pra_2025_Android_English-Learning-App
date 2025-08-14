package com.sun.englishlearning.data.model

data class WordSearchResult(
    val word: String = "",
    val ipa: String = "",
    val partOfSpeech: String = "",
    val definition: String = "",
    val example: String = "",
    val soundUrl: String = "",
    val isFavorite: Boolean = false
)

data class WordApiResponse(
    val word: String = "",
    val phonetics: List<Phonetic> = emptyList(),
    val meanings: List<Meaning> = emptyList()
)

data class Phonetic(
    val text: String = "",
    val audio: String = ""
)

data class Meaning(
    val partOfSpeech: String = "",
    val definitions: List<Definition> = emptyList()
)

data class Definition(
    val definition: String = "",
    val example: String? = null
)
