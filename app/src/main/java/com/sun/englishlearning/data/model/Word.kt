package com.sun.englishlearning.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Word(
    val id: String = "",
    val word: String = "",
    val phonetic: String = "",
    val phonetics: List<Phonetic> = emptyList(),
    val meanings: List<Meaning> = emptyList(),
    val lessonId: String = ""
) : Parcelable

@Parcelize
data class Phonetic(
    val text: String = "",
    val audio: String = ""
) : Parcelable

@Parcelize
data class Meaning(
    val partOfSpeech: String = "",
    val definitions: List<Definition> = emptyList()
) : Parcelable

@Parcelize
data class Definition(
    val definition: String = "",
    val example: String = ""
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
