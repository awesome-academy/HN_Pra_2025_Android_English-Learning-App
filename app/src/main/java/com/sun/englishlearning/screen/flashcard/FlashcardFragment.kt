package com.sun.englishlearning.screen.flashcard

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.repository.LessonRepositoryImpl
import com.sun.englishlearning.data.repository.UserLessonProgressRepositoryImpl
import com.sun.englishlearning.databinding.FragmentFlashcardBinding
import kotlinx.coroutines.launch
import android.content.Context

class FlashcardFragment : Fragment() {

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

    // Interface để thông báo khi tiến trình học được cập nhật
    interface OnProgressUpdateListener {
        fun onProgressUpdated(lessonId: String)
    }

    private var _binding: FragmentFlashcardBinding? = null
    private val binding get() = _binding!!

    private lateinit var word: Word
    private var progressUpdateListener: OnProgressUpdateListener? = null

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlashcardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWordData()
        setupClickListeners()
        animateCardAppearance()

        // Set up click listener for the Mark as Learned button from layout
        binding.btnMarkLearned.setOnClickListener {
            // Get userId and lessonId (replace with actual retrieval logic)
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val lessonId = word.lessonId // Assuming Word has lessonId
            if (lessonId.isEmpty()) {
                Toast.makeText(requireContext(), "Lesson ID not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val wordId = word.id
            if (wordId.isEmpty()) {
                Toast.makeText(requireContext(), "Word ID not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val lessonRepository = LessonRepositoryImpl(requireContext(), UserLessonProgressRepositoryImpl())
            lifecycleScope.launch {
                val result = lessonRepository.updateLessonProgressForFlashcard(userId, lessonId, wordId)
                if (result.isSuccess) {
                    Toast.makeText(requireContext(), "Word marked as learned!", Toast.LENGTH_SHORT).show()
                    // Notify parent activity/fragment that progress has been updated
                    progressUpdateListener?.onProgressUpdated(lessonId)
                } else {
                    Toast.makeText(requireContext(), "Failed to update progress: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupWordData() {
        binding.apply {
            // Set word name
            textWordName.text = word.word
            textWordName.contentDescription = "English word: ${word.word}"

            // Set definition
            val definition = if (word.definition.isNotEmpty()) {
                word.definition
            } else {
                "Definition not available"
            }
            textDefinition.text = definition
            textDefinition.contentDescription = "Definition: $definition"

            // Set example
            val example = if (word.example.isNotEmpty()) {
                word.example
            } else {
                "Example not available"
            }
            textExample.text = example
            textExample.contentDescription = "Example sentence: $example"

            // Set phonetic transcription
            if (word.phonetic.isNotEmpty()) {
                textPhonetic.text = word.phonetic
                textPhonetic.contentDescription = "Pronunciation: ${word.phonetic}"
                textPhonetic.visibility = View.VISIBLE
            } else {
                textPhonetic.visibility = View.GONE
            }

            // Set part of speech
            if (word.partOfSpeech.isNotEmpty()) {
                textPartOfSpeech.text = word.partOfSpeech
                textPartOfSpeech.contentDescription = "Part of speech: ${word.partOfSpeech}"
                textPartOfSpeech.visibility = View.VISIBLE
            } else {
                textPartOfSpeech.visibility = View.GONE
            }

            // Set audio button content description
            btnAudio.contentDescription = if (word.soundUrl.isNotEmpty()) {
                "Play pronunciation for ${word.word}"
            } else {
                "Audio not available for ${word.word}"
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnAudio.setOnClickListener { view ->
            animateButtonPress(view) {
                // Get parent activity and play audio
                (activity as? FlashcardActivity)?.playWordAudio(word)
            }
        }

        // Add touch feedback to the entire card
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            insets
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
        binding.textWordName.apply {
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
        binding.textPhonetic.apply {
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
        binding.btnAudio.apply {
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
        binding.textDefinition.apply {
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
        binding.textExample.apply {
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
        if (binding.textPartOfSpeech.visibility == View.VISIBLE) {
            binding.textPartOfSpeech.apply {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
