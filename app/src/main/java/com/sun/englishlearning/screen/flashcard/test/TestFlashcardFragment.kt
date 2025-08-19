package com.sun.englishlearning.screen.flashcard.test

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.sun.englishlearning.R
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.databinding.FragmentTestFlashcardBinding

class TestFlashcardFragment : Fragment() {

    companion object {
        private const val ARG_WORD = "arg_word"

        fun newInstance(word: Word): TestFlashcardFragment {
            return TestFlashcardFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_WORD, word)
                }
            }
        }
    }

    interface OnTestProgressListener {
        fun onWordTested(word: Word, isCorrect: Boolean)
    }

    private var _binding: FragmentTestFlashcardBinding? = null
    private val binding get() = _binding!!

    private lateinit var word: Word
    private var testProgressListener: OnTestProgressListener? = null
    private var isAnswerChecked = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnTestProgressListener) {
            testProgressListener = context
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestFlashcardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTestData()
        setupClickListeners()
        animateCardAppearance()
    }

    private fun setupTestData() {
        binding.apply {
            // Set definition
            val definition = if (word.definition.isNotEmpty()) {
                word.definition
            } else {
                getString(R.string.definition_not_available)
            }
            textDefinition.text = definition
            textDefinition.contentDescription = getString(R.string.definition_with_content, definition)

            // Set audio button content description
            btnAudio.contentDescription = if (word.soundUrl.isNotEmpty()) {
                getString(R.string.play_pronunciation_for, word.word)
            } else {
                getString(R.string.audio_not_available, word.word)
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            // Audio button click
            btnAudio.setOnClickListener { view ->
                animateButtonPress(view) {
                    // Get parent activity and play audio
                    (activity as? TestFlashcardActivity)?.playWordAudio(word)
                }
            }

            // Check answer button click
            btnCheckAnswer.setOnClickListener {
                checkAnswer()
            }

            // Next button click
            btnNext.setOnClickListener {
                (activity as? TestFlashcardActivity)?.moveToNextCard()
            }

            // Handle done action on keyboard
            editAnswer.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    checkAnswer()
                    return@setOnEditorActionListener true
                }
                false
            }
        }
    }

    private fun checkAnswer() {
        if (isAnswerChecked) return

        val userAnswer = binding.editAnswer.text.toString().trim()
        val correctAnswer = word.word.trim()
        val isCorrect = userAnswer.equals(correctAnswer, ignoreCase = true)

        showResult(isCorrect)
        testProgressListener?.onWordTested(word, isCorrect)
    }

    private fun showResult(isCorrect: Boolean) {
        isAnswerChecked = true
        binding.apply {
            layoutResult.visibility = View.VISIBLE
            if (isCorrect) {
                imageResultIcon.setImageResource(R.drawable.ic_check_circle)
                imageResultIcon.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.flashcard_success),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                textResultMessage.text = getString(R.string.check_learned_result_correct)
                textResultMessage.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.flashcard_success)
                )
                textCorrectWord.visibility = View.GONE
            } else {
                imageResultIcon.setImageResource(android.R.drawable.ic_delete)
                imageResultIcon.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.flashcard_error),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                textResultMessage.text = getString(R.string.check_learned_result_incorrect)
                textResultMessage.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.flashcard_error)
                )
                textCorrectWord.text = getString(R.string.text_correct_word, word.word)
                textCorrectWord.visibility = View.VISIBLE
            }
            editAnswer.isEnabled = false
            btnCheckAnswer.isEnabled = false
            btnNext.visibility = View.VISIBLE
        }
    }

    private fun animateCardAppearance() {
        binding.root.apply {
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(OvershootInterpolator())
                .start()
        }
    }

    private fun animateButtonPress(view: View, onAnimationEnd: () -> Unit) {
        val scaleDown = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f).apply {
            duration = 100
        }

        val scaleUp = ObjectAnimator.ofFloat(view, "scaleX", 0.9f, 1f).apply {
            duration = 100
        }

        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f).apply {
            duration = 100
        }

        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.9f, 1f).apply {
            duration = 100
        }

        AnimatorSet().apply {
            play(scaleDown).with(scaleDownY)
            play(scaleUp).with(scaleUpY).after(scaleDown)
            start()
        }

        view.postDelayed({
            onAnimationEnd()
        }, 200)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
