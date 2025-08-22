package com.sun.englishlearning.screen.flashcard

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import com.sun.englishlearning.R
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.databinding.FragmentFlashcardBinding
import com.sun.englishlearning.utils.base.BaseFragment
import java.lang.Exception
import androidx.core.view.isVisible

class FlashcardFragment :
    BaseFragment<FragmentFlashcardBinding>(),
    FlashcardContract.View {

    companion object {
        private const val ARG_WORD = "arg_word"

        fun newInstance(word: Word): FlashcardFragment {
            return FlashcardFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_WORD, word)
                }
            }
        }
    }

    // Interface to notify when the learning progress is updated
    interface OnProgressUpdateListener {
        fun onProgressUpdated(lessonId: String)
    }

    private lateinit var mFlashcardPresenter: FlashcardPresenter
    private lateinit var word: Word
    private var progressUpdateListener: OnProgressUpdateListener? = null
    private var isWordSaved = false
    private var isWordLearned = false

    override val isInsets = false

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFlashcardBinding {
        return FragmentFlashcardBinding.inflate(inflater, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnProgressUpdateListener) {
            progressUpdateListener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        word = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_WORD, Word::class.java) ?: Word()
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(ARG_WORD) ?: Word()
        }
    }

    override fun initView() {
        setupClickListeners()
        animateCardAppearance()
    }

    override fun initData() {
        mFlashcardPresenter = FlashcardPresenter()
        mFlashcardPresenter.setContext(requireContext())
        mFlashcardPresenter.setLifecycleScope(lifecycleScope)
        mFlashcardPresenter.attachView(this)
        mFlashcardPresenter.loadWord(word)
    }

    private fun setupClickListeners() {
        viewBinding.apply {
            // Audio button click
            btnAudio.setOnClickListener { view ->
                animateButtonPress(view) {
                    // Get parent activity and play audio
                    (activity as? FlashcardActivity)?.playWordAudio(word)
                    mFlashcardPresenter.playAudio(word.phonetics.firstOrNull()?.audio.orEmpty())
                }
            }

            // Save word button click
            btnSaveWordContainer.setOnClickListener {
                toggleSaveWord()
            }

            // Mark as learned button click
            btnMarkLearned.setOnClickListener {
                toggleLearnedStatus()
            }

            // Add touch feedback to the entire card
            ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
                insets
            }
        }
    }

    private fun setupWordData() {
        viewBinding.apply {
            // Set word name
            textWordName.text = word.word
            textWordName.contentDescription = getString(R.string.english_word_with_name, word.word)

            // Set definition
            val definition =
                word.meanings.firstOrNull()?.definitions?.firstOrNull()?.definition.orEmpty()
            textDefinition.text = definition
            textDefinition.contentDescription =
                getString(R.string.definition_with_content, definition)

            // Set example
            val example = word.meanings.firstOrNull()?.definitions?.firstOrNull()?.example.orEmpty()
            textExample.text = example
            textExample.contentDescription =
                getString(R.string.example_sentence_with_content, example)

            // Set phonetic transcription
            val phonetic = word.phonetic.ifEmpty { word.phonetics.firstOrNull()?.text.orEmpty() }
            if (phonetic.isNotEmpty()) {
                textPhonetic.text = phonetic
                textPhonetic.contentDescription =
                    getString(R.string.pronunciation_with_content, phonetic)
                textPhonetic.visibility = View.VISIBLE
            } else {
                textPhonetic.visibility = View.GONE
            }

            // Set part of speech
            val partOfSpeech = word.meanings.firstOrNull()?.partOfSpeech.orEmpty()
            if (partOfSpeech.isNotEmpty()) {
                textPartOfSpeech.text = partOfSpeech
                textPartOfSpeech.contentDescription =
                    getString(R.string.part_of_speech_with_content, partOfSpeech)
                textPartOfSpeech.visibility = View.VISIBLE
            } else {
                textPartOfSpeech.visibility = View.GONE
            }

            // Set audio button content description
            val soundUrl = word.phonetics.firstOrNull()?.audio.orEmpty()
            btnAudio.contentDescription = if (soundUrl.isNotEmpty()) {
                getString(R.string.play_pronunciation_for, word.word)
            } else {
                getString(R.string.audio_not_available, word.word)
            }
        }
    }

    private fun toggleSaveWord() {
        animateButtonPress(viewBinding.btnSaveWordContainer) {
            if (isWordSaved) {
                mFlashcardPresenter.unsaveWord(word)
            } else {
                mFlashcardPresenter.saveWord(word)
            }
        }
    }

    private fun toggleLearnedStatus() {
        animateButtonPress(viewBinding.btnMarkLearned) {
            mFlashcardPresenter.markWordAsLearned(word)
        }
    }

    private fun animateButtonPress(view: View, onAnimationEnd: () -> Unit) {
        val scaleDown = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f),
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f)
            )
            duration = 100
            interpolator = OvershootInterpolator()
        }

        val scaleUp = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1.05f),
                ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1.05f)
            )
            duration = 150
            interpolator = OvershootInterpolator()
        }

        val scaleNormal = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1.05f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 1.05f, 1f)
            )
            duration = 100
            interpolator = OvershootInterpolator()
        }

        scaleDown.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                scaleUp.start()
                onAnimationEnd()
            }
        })

        scaleUp.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                scaleNormal.start()
            }
        })

        scaleDown.start()
    }

    private fun animateCardAppearance() {
        // Animate word name with scale up
        viewBinding.textWordName.apply {
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setStartDelay(200)
                .setInterpolator(OvershootInterpolator())
                .start()
        }

        // Animate phonetic section
        viewBinding.textPhonetic.apply {
            alpha = 0f
            translationY = 50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay(400)
                .start()
        }

        // Animate audio button
        viewBinding.btnAudio.apply {
            alpha = 0f
            scaleX = 0.5f
            scaleY = 0.5f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setStartDelay(500)
                .setInterpolator(OvershootInterpolator())
                .start()
        }

        // Animate definition section
        viewBinding.textDefinition.apply {
            alpha = 0f
            translationY = 30f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(600)
                .start()
        }

        // Animate example section
        viewBinding.textExample.apply {
            alpha = 0f
            translationY = 30f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(700)
                .start()
        }

        // Animate part of speech if visible
        if (viewBinding.textPartOfSpeech.isVisible) {
            viewBinding.textPartOfSpeech.apply {
                alpha = 0f
                scaleX = 0.8f
                scaleY = 0.8f
                animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .setStartDelay(800)
                    .setInterpolator(OvershootInterpolator())
                    .start()
            }
        }
    }

    // MVP Contract.View implementation
    override fun onWordLoaded(word: Word) {
        setupWordData()
    }

    override fun onWordSaved(success: Boolean) {
        if (success) {
            Toast.makeText(requireContext(), "Word saved successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Failed to save word", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onWordUnsaved(success: Boolean) {
        if (success) {
            Toast.makeText(requireContext(), "Word removed from saved list", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(requireContext(), "Failed to remove word", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onWordMarkedAsLearned(success: Boolean) {
        if (success) {
            Toast.makeText(requireContext(), "Word marked as learned!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onProgressUpdated(lessonId: String) {
        progressUpdateListener?.onProgressUpdated(lessonId)
    }

    override fun onError(exception: Exception?) {
        Toast.makeText(
            requireContext(),
            exception?.message ?: getString(R.string.error_general),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun showLoading() {
        // Implement loading indicator if needed
    }

    override fun hideLoading() {
        // Hide loading indicator if needed
    }

    override fun updateSaveButtonUI(isSaved: Boolean) {
        isWordSaved = isSaved
        val container = viewBinding.btnSaveWordContainer
        val imageView = viewBinding.btnSaveWord

        if (isSaved) {
            // Saved state - highlight with accent color
            container.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.flashcard_primary)
            )
            imageView.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
            container.alpha = 1.0f
        } else {
            // Not saved state - default appearance
            container.backgroundTintList = null
            imageView.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
            container.alpha = 0.8f
        }
    }

    override fun updateLearnedButtonUI(isLearned: Boolean) {
        isWordLearned = isLearned
        if (isLearned) {
            // Learned state - change to success color and text
            viewBinding.btnMarkLearned.apply {
                text = getString(R.string.learned_checkmark)
                backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.state_success)
                )
                iconTint = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.white)
                )
                isEnabled = false // Disable further clicks
                alpha = 0.9f
            }
        } else {
            // Not learned state - default appearance
            viewBinding.btnMarkLearned.apply {
                text = getString(R.string.mark_as_learned)
                backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.flashcard_primary)
                )
                iconTint = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.white)
                )
                isEnabled = true
                alpha = 1.0f
            }
        }
    }

    override fun showWordSavedStatus(isSaved: Boolean) {
        isWordSaved = isSaved
        updateSaveButtonUI(isSaved)
    }

    override fun showWordLearnedStatus(isLearned: Boolean) {
        isWordLearned = isLearned
        updateLearnedButtonUI(isLearned)
    }

    override fun onDestroyView() {
        mFlashcardPresenter.detachView()
        super.onDestroyView()
    }
}
