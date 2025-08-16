package com.sun.englishlearning.screen.lessondetail

import android.content.Context
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.data.model.Word

class LessonDetailPresenter : LessonDetailContract.Presenter {

    private var view: LessonDetailContract.View? = null
    private var currentLesson: Lesson? = null
    private var currentVocabulary: List<Word> = emptyList()
    private var context: Context? = null

    fun setContext(context: Context) {
        this.context = context
    }

    override fun loadLessonDetail(lesson: Lesson) {
        currentLesson = lesson
        view?.displayLessonInfo(lesson)
        loadVocabulary(lesson.id)
    }

    override fun loadVocabulary(lessonId: String) {
        view?.showLoading()

        try {
            // Load vocabulary from the current lesson's embedded vocabulary list
            val vocabularyWords = currentLesson?.vocabulary ?: emptyList()
            val wordObjects = createWordObjectsFromVocabulary(vocabularyWords, lessonId)
            currentVocabulary = wordObjects
            view?.hideLoading()
            view?.showVocabulary(wordObjects)
        } catch (e: Exception) {
            view?.hideLoading()
            view?.showError("Error loading vocabulary: ${e.message}")
        }
    }
    
    private fun createWordObjectsFromVocabulary(vocabularyWords: List<String>, lessonId: String): List<Word> {
        return vocabularyWords.mapIndexed { index, wordText ->
            Word(
                id = "${lessonId}_$index",
                word = wordText,
                definition = "Definition for $wordText", // Placeholder - could be fetched from API
                pronunciation = "",
                phonetic = "",
                partOfSpeech = "",
                example = "Example sentence with $wordText", // Placeholder
                lessonId = lessonId
            )
        }
    }

    override fun onBackClicked() {
        view?.navigateBack()
    }

    override fun onWordClicked(word: Word) {
        try {
            android.util.Log.d("LessonDetailPresenter", "Word clicked: ${word.word}")
            android.util.Log.d("LessonDetailPresenter", "Current vocabulary size: ${currentVocabulary.size}")

            // Find the index of the clicked word in the current vocabulary list
            val wordIndex = currentVocabulary.indexOfFirst { it.word == word.word }
            android.util.Log.d("LessonDetailPresenter", "Word index found: $wordIndex")

            if (wordIndex != -1) {
                val lessonTitle = currentLesson?.name ?: ""
                android.util.Log.d("LessonDetailPresenter", "Navigating to flashcard with title: $lessonTitle")
                view?.navigateToFlashcard(currentVocabulary, wordIndex, lessonTitle)
            } else {
                android.util.Log.w("LessonDetailPresenter", "Word index not found, showing word detail")
                // Fallback to showing word detail if index not found
                view?.showWordDetail(word)
            }
        } catch (e: Exception) {
            android.util.Log.e("LessonDetailPresenter", "Error handling word click", e)
            view?.showError("Error opening flashcard: ${e.message}")
        }
    }

    override fun onSoundClicked(word: Word) {
        view?.playWordSound(word)
    }

    override fun attachView(view: LessonDetailContract.View?) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }

    override fun onStart() {

    }

    override fun onStop() {

    }
}
