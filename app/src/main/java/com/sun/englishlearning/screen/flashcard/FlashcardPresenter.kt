package com.sun.englishlearning.screen.flashcard

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.firebase.auth.FirebaseAuth
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.model.SavedWord
import com.sun.englishlearning.data.model.WordType
import com.sun.englishlearning.data.repository.LessonRepositoryImpl
import com.sun.englishlearning.data.repository.UserLessonProgressRepositoryImpl
import com.sun.englishlearning.data.repository.SavedWordsRepositoryImpl
import com.sun.englishlearning.utils.AudioManager
import kotlinx.coroutines.launch
import java.lang.Exception

class FlashcardPresenter internal constructor() : FlashcardContract.Presenter {

    private var mView: FlashcardContract.View? = null
    private var context: Context? = null
    private var lifecycleScope: LifecycleCoroutineScope? = null
    private val audioManager = AudioManager.getInstance()
    private val savedWordsRepository = SavedWordsRepositoryImpl()
    private val userProgressRepository = UserLessonProgressRepositoryImpl()

    fun setContext(context: Context) {
        this.context = context
    }

    fun setLifecycleScope(scope: LifecycleCoroutineScope) {
        this.lifecycleScope = scope
    }

    override fun onStart() {

    }

    override fun onStop() {

    }

    override fun attachView(view: FlashcardContract.View?) {
        this.mView = view
    }

    override fun detachView() {
        this.mView = null
    }

    override fun loadWord(word: Word) {
        try {
            mView?.showLoading()
            mView?.onWordLoaded(word)
            checkWordSavedStatus(word)
            checkWordLearnedStatus(word)
            mView?.hideLoading()
        } catch (exception: Exception) {
            mView?.hideLoading()
            mView?.onError(exception)
        }
    }

    override fun saveWord(word: Word) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            mView?.onError(Exception("User not logged in"))
            return
        }

        lifecycleScope?.launch {
            try {
                val savedWord = SavedWord(
                    userId = userId,
                    word = word.word,
                    ipa = word.phonetic,
                    partOfSpeech = word.partOfSpeech,
                    definition = word.definition,
                    example = word.example,
                    soundUrl = "",
                    wordType = WordType.SAVED.value
                )

                val saveResult = savedWordsRepository.saveWord(savedWord)
                if (saveResult.isSuccess) {
                    mView?.onWordSaved(true)
                    mView?.updateSaveButtonUI(true)
                } else {
                    mView?.onWordSaved(false)
                }
            } catch (exception: Exception) {
                mView?.onError(exception)
            }
        }
    }

    override fun unsaveWord(word: Word) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            mView?.onError(Exception("User not logged in"))
            return
        }

        lifecycleScope?.launch {
            try {
                val deleteResult = savedWordsRepository.deleteWordByUserAndName(userId, word.word, WordType.SAVED)
                if (deleteResult.isSuccess) {
                    mView?.onWordUnsaved(true)
                    mView?.updateSaveButtonUI(false)
                } else {
                    mView?.onWordUnsaved(false)
                }
            } catch (exception: Exception) {
                mView?.onError(exception)
            }
        }
    }

    override fun markWordAsLearned(word: Word) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            mView?.onError(Exception("User not logged in"))
            return
        }

        // Fix: Ensure word has required data for marking as learned
        val wordWithValidData = if (word.lessonId.isEmpty() || word.id.isEmpty()) {
            // Generate missing data for words from Dictionary API
            val lessonId = word.lessonId.ifEmpty { "default_lesson" }
            val wordId = word.id.ifEmpty { word.word.hashCode().toString() }

            word.copy(
                id = wordId,
                lessonId = lessonId
            )
        } else {
            word
        }

        lifecycleScope?.launch {
            try {
                val lessonRepository = LessonRepositoryImpl(context!!, userProgressRepository)
                val result = lessonRepository.updateLessonProgressForFlashcard(userId, wordWithValidData.lessonId, wordWithValidData.id)
                if (result.isSuccess) {
                    mView?.onWordMarkedAsLearned(true)
                    mView?.updateLearnedButtonUI(true)
                    mView?.onProgressUpdated(wordWithValidData.lessonId)
                } else {
                    mView?.onWordMarkedAsLearned(false)
                }
            } catch (exception: Exception) {
                mView?.onError(exception)
            }
        }
    }

    override fun checkWordSavedStatus(word: Word) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) return

        lifecycleScope?.launch {
            try {
                val result = savedWordsRepository.isWordSavedWithType(userId, word.word, WordType.SAVED)
                if (result.isSuccess) {
                    val isSaved = result.getOrNull() != null
                    mView?.showWordSavedStatus(isSaved)
                    mView?.updateSaveButtonUI(isSaved)
                }
            } catch (exception: Exception) {
                mView?.onError(exception)
            }
        }
    }

    override fun checkWordLearnedStatus(word: Word) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null || word.lessonId.isEmpty()) return

        lifecycleScope?.launch {
            try {
                val result = userProgressRepository.getUserLessonProgress(userId, word.lessonId)
                if (result.isSuccess) {
                    val progress = result.getOrNull()
                    val isLearned = progress?.learnedWordIds?.contains(word.id) == true
                    mView?.showWordLearnedStatus(isLearned)
                    mView?.updateLearnedButtonUI(isLearned)
                }
            } catch (exception: Exception) {
                mView?.onError(exception)
            }
        }
    }

    override fun playAudio(soundUrl: String) {
        try {
            if (soundUrl.isNotEmpty() && context != null) {
                audioManager.playAudio(
                    context = context!!,
                    audioUrl = soundUrl,
                    listener = object : AudioManager.AudioPlaybackListener {
                        override fun onAudioStarted() {
                            // Audio started
                        }

                        override fun onAudioCompleted() {
                            // Audio completed
                        }

                        override fun onAudioError(error: String) {
                            mView?.onError(Exception("Audio playback error: $error"))
                        }
                    }
                )
            }
        } catch (exception: Exception) {
            mView?.onError(exception)
        }
    }
}
