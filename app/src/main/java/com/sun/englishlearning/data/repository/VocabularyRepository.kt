package com.sun.englishlearning.data.repository

import android.content.Context
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.repository.source.VocabularyDataSource
import com.sun.englishlearning.data.repository.source.remote.OnResultListener

object VocabularyRepository {

    private val vocabularyDataSource = VocabularyDataSource()

    /**
     * Get vocabulary for lesson with API definitions (async)
     */
    fun getVocabularyByLessonId(
        context: Context,
        lessonId: String,
        listener: OnResultListener<List<Word>>
    ) {
        vocabularyDataSource.getVocabularyForLesson(context, lessonId, listener)
    }

    /**
     * Get single word definition (async)
     */
    fun getWordDefinition(word: String, listener: OnResultListener<Word>) {
        vocabularyDataSource.getWordDefinition(word, listener)
    }

    /**
     * Synchronous method for backward compatibility (returns fallback data)
     */
    fun getVocabularyByLessonId(lessonId: String): List<Word> {
        // Return simple fallback data for backward compatibility
        return createFallbackVocabulary(lessonId)
    }

    /**
     * Create simple fallback vocabulary
     */
    private fun createFallbackVocabulary(lessonId: String): List<Word> {
        return listOf(
            Word(
                id = "fallback_$lessonId",
                name = "Loading...",
                definition = "Please wait while we load vocabulary from the internet",
                soundUrl = "",
                example = "This is a fallback word while loading real data"
            )
        )
    }
}
