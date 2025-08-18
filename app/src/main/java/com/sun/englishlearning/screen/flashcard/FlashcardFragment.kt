package com.sun.englishlearning.screen.flashcard

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.sun.englishlearning.R
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.model.SavedWord
import com.sun.englishlearning.data.model.WordType
import com.sun.englishlearning.data.repository.LessonRepositoryImpl
import com.sun.englishlearning.data.repository.UserLessonProgressRepositoryImpl
import com.sun.englishlearning.data.repository.SavedWordsRepositoryImpl
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

    // Interface to notify when the learning progress is updated
    interface OnProgressUpdateListener {
        fun onProgressUpdated(lessonId: String)
    }

    private var _binding: FragmentFlashcardBinding? = null
    private val binding get() = _binding!!

    private lateinit var word: Word
    private var progressUpdateListener: OnProgressUpdateListener? = null
    private val savedWordsRepository = SavedWordsRepositoryImpl()
    private var isWordSaved = false
    private var isWordLearned = false

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
        checkIfWordIsSaved()
        checkIfWordIsLearned()

        // Set up click listener for the Save Word button
        binding.btnSaveWordContainer.setOnClickListener {
            toggleSaveWord()
        }

        // Set up click listener for the Mark as Learned button
        binding.btnMarkLearned.setOnClickListener {
            toggleLearnedStatus()
        }
    }

    private fun setupWordData() {
        binding.apply {
            // Set word name
            textWordName.text = word.word
            textWordName.contentDescription = getString(R.string.english_word_with_name, word.word)

            // Set definition
            val definition = if (word.definition.isNotEmpty()) {
                word.definition
            } else {
                getString(R.string.definition_not_available)
            }
            textDefinition.text = definition
            textDefinition.contentDescription = getString(R.string.definition_with_content, definition)

            // Set example
            val example = if (word.example.isNotEmpty()) {
                word.example
            } else {
                getString(R.string.example_not_available)
            }
            textExample.text = example
            textExample.contentDescription = getString(R.string.example_sentence_with_content, example)

            // Set phonetic transcription
            if (word.phonetic.isNotEmpty()) {
                textPhonetic.text = word.phonetic
                textPhonetic.contentDescription = getString(R.string.pronunciation_with_content, word.phonetic)
                textPhonetic.visibility = View.VISIBLE
            } else {
                textPhonetic.visibility = View.GONE
            }

            // Set part of speech
            if (word.partOfSpeech.isNotEmpty()) {
                textPartOfSpeech.text = word.partOfSpeech
                textPartOfSpeech.contentDescription = getString(R.string.part_of_speech_with_content, word.partOfSpeech)
                textPartOfSpeech.visibility = View.VISIBLE
            } else {
                textPartOfSpeech.visibility = View.GONE
            }

            // Set audio button content description
            btnAudio.contentDescription = if (word.soundUrl.isNotEmpty()) {
                getString(R.string.play_pronunciation_for, word.word)
            } else {
                getString(R.string.audio_not_available, word.word)
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

    private fun checkIfWordIsSaved() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            return
        }

        lifecycleScope.launch {
            try {
                val result = savedWordsRepository.isWordSavedWithType(userId, word.word, WordType.SAVED)
                if (result.isSuccess) {
                    isWordSaved = result.getOrNull() != null
                    updateSaveButtonUI()
                }
            } catch (e: Exception) {

            }
        }
    }

    private fun checkIfWordIsLearned() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null || word.lessonId.isEmpty()) {
            return
        }

        lifecycleScope.launch {
            try {
                val userLessonProgressRepository = UserLessonProgressRepositoryImpl()
                val result = userLessonProgressRepository.getUserLessonProgress(userId, word.lessonId)
                if (result.isSuccess) {
                    val progress = result.getOrNull()
                    isWordLearned = progress?.learnedWordIds?.contains(word.id) == true
                    updateLearnedButtonUI()
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    private fun toggleSaveWord() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "Please log in to save words", Toast.LENGTH_SHORT).show()
            return
        }

        // Add button animation
        animateButtonPress(binding.btnSaveWordContainer) {
            lifecycleScope.launch {
                try {
                    if (isWordSaved) {
                        // Remove word from saved
                        val deleteResult = savedWordsRepository.deleteWordByUserAndName(userId, word.word, WordType.SAVED)
                        if (deleteResult.isSuccess) {
                            isWordSaved = false
                            updateSaveButtonUI()
                            Toast.makeText(requireContext(), "Word removed from saved list", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed to remove word", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Save word
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
                            isWordSaved = true
                            updateSaveButtonUI()
                            Toast.makeText(requireContext(), "Word saved successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed to save word", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun toggleLearnedStatus() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val lessonId = word.lessonId
        if (lessonId.isEmpty()) {
            Toast.makeText(requireContext(), "Lesson ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        val wordId = word.id
        if (wordId.isEmpty()) {
            Toast.makeText(requireContext(), "Word ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Add button animation
        animateButtonPress(binding.btnMarkLearned) {
            val lessonRepository = LessonRepositoryImpl(requireContext(), UserLessonProgressRepositoryImpl())
            lifecycleScope.launch {
                val result = lessonRepository.updateLessonProgressForFlashcard(userId, lessonId, wordId)
                if (result.isSuccess) {
                    isWordLearned = true
                    updateLearnedButtonUI()
                    Toast.makeText(requireContext(), "Word marked as learned!", Toast.LENGTH_SHORT).show()
                    // Notify parent activity/fragment that progress has been updated
                    progressUpdateListener?.onProgressUpdated(lessonId)
                } else {
                    Toast.makeText(requireContext(), "Failed to update progress: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateSaveButtonUI() {
        val container = binding.btnSaveWordContainer
        val imageView = binding.btnSaveWord

        if (isWordSaved) {
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

    private fun updateLearnedButtonUI() {
        if (isWordLearned) {
            // Learned state - change to success color and text
            binding.btnMarkLearned.apply {
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
            binding.btnMarkLearned.apply {
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
