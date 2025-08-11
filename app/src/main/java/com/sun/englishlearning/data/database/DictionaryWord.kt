package com.sun.englishlearning.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dictionary_words")
data class DictionaryWord(
    @PrimaryKey
    val word: String,
    val phonetic: String,
    val partOfSpeech: String,
    val definition: String,
    val example: String? = null,
    val synonyms: String = "", // JSON string of synonyms list
    val audioFile: String? = null // Local audio file name if available
)
