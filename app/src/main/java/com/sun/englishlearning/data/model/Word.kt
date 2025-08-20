package com.sun.englishlearning.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Word(
    var id: String = "",
    var word: String = "",
    var phonetic: String = "",
    var phonetics: List<Phonetic> = emptyList(),
    var meanings: List<Meaning> = emptyList(),
     var lessonId: String = ""
) : Parcelable

@Parcelize
data class Phonetic(
    var text: String = "",
    var audio: String = ""
) : Parcelable

@Parcelize
data class Meaning(
    var partOfSpeech: String = "",
    var definitions: List<Definition> = emptyList()
) : Parcelable

@Parcelize
data class Definition(
    var definition: String = "",
    var example: String = ""
) : Parcelable

object WordEntry {
    const val WORD = "word"
    const val PHONETIC = "phonetic"
    const val PHONETICS = "phonetics"
    const val MEANINGS = "meanings"
    const val PART_OF_SPEECH = "partOfSpeech"
    const val DEFINITIONS = "definitions"
    const val DEFINITION = "definition"
    const val EXAMPLE = "example"
    const val TEXT = "text"
    const val AUDIO = "audio"
}
