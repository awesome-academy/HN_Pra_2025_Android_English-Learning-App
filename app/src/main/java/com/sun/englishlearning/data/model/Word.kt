package com.sun.englishlearning.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Word(
    val id: String = "",
    val word: String = "",
    val definition: String = "",
    val pronunciation: String = "",
    val phonetic: String = "",
    val partOfSpeech: String = "",
    val example: String = "",
    val soundUrl: String = "",
    val imageUrl: String = "",
    val lessonId: String = "",
    val difficulty: String = "easy"
) : Parcelable
