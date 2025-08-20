package com.sun.englishlearning.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class WordSearchResult(
    val word: String = "",
    val ipa: String = "",
    val partOfSpeech: String = "",
    val definition: String = "",
    val example: String = "",
    val soundUrl: String = "",
    val isFavorite: Boolean = false
)
