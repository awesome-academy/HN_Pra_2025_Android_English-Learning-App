package com.sun.englishlearning.screen.lessondetail

import android.content.Context
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.repository.LessonRepository
import com.sun.englishlearning.data.repository.LessonRepositoryImpl
import com.sun.englishlearning.data.repository.UserLessonProgressRepository
import com.sun.englishlearning.data.repository.UserLessonProgressRepositoryImpl
import com.sun.englishlearning.data.repository.source.WordDataSource
import com.sun.englishlearning.data.repository.source.remote.WordRemoteDataSource
import java.util.concurrent.atomic.AtomicInteger

class LessonDetailPresenter : LessonDetailContract.Presenter {

    private var view: LessonDetailContract.View? = null
    private var currentLesson: Lesson? = null
    private var currentVocabulary: List<Word> = emptyList()
    private var context: Context? = null

    private val wordDataSource: WordDataSource.Remote = WordRemoteDataSource()
    private var lessonRepository: LessonRepository? = null
    private val userProgressRepository: UserLessonProgressRepository = UserLessonProgressRepositoryImpl()

    fun setContext(context: Context) {
        this.context = context
        lessonRepository = LessonRepositoryImpl(context, userProgressRepository)
    }

    override fun loadLessonDetail(lesson: Lesson) {
        try {
            currentLesson = lesson
            view?.displayLessonInfo(lesson)
            getWords(lesson.vocabulary)
        } catch (e: Exception) {
            android.util.Log.e("LessonDetailPresenter", "Error loading lesson detail", e)
            view?.showError("Failed to load lesson: ${e.message}")
        }
    }

    override fun loadVocabulary(lessonId: String) {
        TODO("Not yet implemented")
    }

    override fun getWords(vocabulary: List<String>) {
        try {
            view?.showLoading()

            if (vocabulary.isEmpty()) {
                view?.onGetWordsSuccess(mutableListOf())
                view?.hideLoading()
                return
            }

            // Use thread-safe collections and atomic counter
            val resultWords = arrayOfNulls<Word>(vocabulary.size)
            val completed = AtomicInteger(0)
            val totalWords = vocabulary.size

            for ((index, vocab) in vocabulary.withIndex()) {
                if (vocab.isEmpty()) {
                    // Handle empty vocabulary string
                    resultWords[index] = Word(
                        id = "empty_$index",
                        word = "Unknown",
                        lessonId = currentLesson?.id ?: "",
                        phonetics = emptyList()
                    )
                    if (completed.incrementAndGet() == totalWords) {
                        handleWordsLoadingComplete(resultWords.filterNotNull().toMutableList())
                    }
                    continue
                }

                wordDataSource.getWords(vocab, object : com.sun.englishlearning.data.repository.source.remote.OnResultListener<MutableList<Word>> {
                    override fun onSuccess(data: MutableList<Word>) {
                        try {
                            val word = if (data.isNotEmpty() && data[0].word.isNotEmpty()) {
                                val w = data[0]
                                w.copy(
                                    id = w.id.takeIf { it.isNotEmpty() } ?: vocab,
                                    phonetics = w.phonetics ?: emptyList(),
                                    lessonId = currentLesson?.id ?: ""
                                )
                            } else {
                                Word(
                                    id = vocab,
                                    word = vocab,
                                    lessonId = currentLesson?.id ?: "",
                                    phonetics = emptyList()
                                )
                            }
                            resultWords[index] = word

                            if (completed.incrementAndGet() == totalWords) {
                                handleWordsLoadingComplete(resultWords.filterNotNull().toMutableList())
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("LessonDetailPresenter", "Error processing word success", e)
                            handleWordError(vocab, index, resultWords, completed, totalWords)
                        }
                    }

                    override fun onError(exception: Exception?) {
                        android.util.Log.w("LessonDetailPresenter", "Error loading word: $vocab", exception)
                        handleWordError(vocab, index, resultWords, completed, totalWords)
                    }
                })
            }
        } catch (e: Exception) {
            android.util.Log.e("LessonDetailPresenter", "Error in getWords", e)
            view?.hideLoading()
            view?.showError("Failed to load vocabulary: ${e.message}")
        }
    }

    private fun handleWordError(vocab: String, index: Int, resultWords: Array<Word?>, completed: AtomicInteger, totalWords: Int) {
        try {
            resultWords[index] = Word(
                id = vocab,
                word = vocab,
                lessonId = currentLesson?.id ?: "",
                phonetics = emptyList()
            )
            if (completed.incrementAndGet() == totalWords) {
                handleWordsLoadingComplete(resultWords.filterNotNull().toMutableList())
            }
        } catch (e: Exception) {
            android.util.Log.e("LessonDetailPresenter", "Error handling word error", e)
        }
    }

    private fun handleWordsLoadingComplete(words: MutableList<Word>) {
        try {
            currentVocabulary = words
            view?.onGetWordsSuccess(words)
            view?.hideLoading()
        } catch (e: Exception) {
            android.util.Log.e("LessonDetailPresenter", "Error completing words loading", e)
            view?.hideLoading()
            view?.showError("Failed to process vocabulary words")
        }
    }

    override fun onBackClicked() {
        view?.navigateBack()
    }

    override fun onWordClicked(word: Word) {
        try {
            android.util.Log.d("LessonDetailPresenter", "Word clicked: ${word.word}")

            // Validate word and current vocabulary
            if (word.word.isEmpty()) {
                android.util.Log.w("LessonDetailPresenter", "Empty word clicked")
                view?.showError("Invalid word selected")
                return
            }

            if (currentVocabulary.isEmpty()) {
                android.util.Log.w("LessonDetailPresenter", "No vocabulary available")
                view?.showError("No vocabulary available")
                return
            }

            android.util.Log.d("LessonDetailPresenter", "Current vocabulary size: ${currentVocabulary.size}")

            // Find the index of the clicked word in the current vocabulary list
            val wordIndex = currentVocabulary.indexOfFirst {
                it.word.equals(word.word, ignoreCase = true) || it.id == word.id
            }
            android.util.Log.d("LessonDetailPresenter", "Word index found: $wordIndex")

            if (wordIndex != -1 && wordIndex < currentVocabulary.size) {
                val lessonTitle = currentLesson?.title ?: "Lesson"
                android.util.Log.d("LessonDetailPresenter", "Navigating to flashcard with title: $lessonTitle")

                // Create a safe copy of vocabulary to prevent concurrent modification
                val safeVocabulary = currentVocabulary.toList()
                view?.navigateToFlashcard(safeVocabulary, wordIndex, lessonTitle)
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
        try {
            if (word.word.isEmpty()) {
                android.util.Log.w("LessonDetailPresenter", "Empty word for sound")
                return
            }
            view?.playWordSound(word)
        } catch (e: Exception) {
            android.util.Log.e("LessonDetailPresenter", "Error playing sound", e)
            view?.showError("Error playing sound: ${e.message}")
        }
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
