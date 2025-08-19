package com.sun.englishlearning.screen.testflashcard

import android.content.Context
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.utils.AudioManager
import java.lang.Exception

class TestFlashcardPresenter internal constructor() : TestFlashcardContract.Presenter {

    private var mView: TestFlashcardContract.View? = null
    private val audioManager = AudioManager.getInstance()
    private var context: Context? = null

    fun setContext(context: Context) {
        this.context = context
    }

    override fun onStart() {

    }

    override fun onStop() {

    }

    override fun attachView(view: TestFlashcardContract.View?) {
        this.mView = view
    }

    override fun detachView() {
        this.mView = null
    }

    override fun loadTestWord(word: Word) {
        try {
            mView?.showLoading()
            // Simulate loading process
            mView?.hideLoading()
            mView?.onTestWordSuccess(word)
        } catch (exception: Exception) {
            mView?.hideLoading()
            mView?.onError(exception)
        }
    }

    override fun checkAnswer(userAnswer: String, correctAnswer: String) {
        try {
            val isCorrect = userAnswer.trim().equals(correctAnswer.trim(), ignoreCase = true)
            mView?.onWordTested(isCorrect)
        } catch (exception: Exception) {
            mView?.onError(exception)
        }
    }

    override fun nextWord() {
        try {
            mView?.onTestCompleted()
        } catch (exception: Exception) {
            mView?.onError(exception)
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
