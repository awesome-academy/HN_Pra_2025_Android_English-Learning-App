package com.sun.englishlearning.data.model

data class Word(
    var id: String = "",
    var name: String = "",
    var definition: String = "",
    var soundUrl: String = "",
    var example: String = "",
    var phonetic: String = "",
    var partOfSpeech: String = ""
)
