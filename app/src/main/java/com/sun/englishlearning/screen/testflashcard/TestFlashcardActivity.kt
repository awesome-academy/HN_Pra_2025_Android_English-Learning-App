package com.sun.englishlearning.screen.testflashcard

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.sun.englishlearning.R
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.repository.SavedWordsRepository
import com.sun.englishlearning.data.repository.SavedWordsRepositoryImpl
import com.sun.englishlearning.databinding.ActivityFlashcardBinding
import com.sun.englishlearning.screen.testflashcard.adapter.TestFlashcardAdapter
import com.sun.englishlearning.utils.AudioManager
import com.sun.englishlearning.utils.DialogUtils
import com.sun.englishlearning.utils.base.BaseActivity
import kotlinx.coroutines.launch

class TestFlashcardActivity : BaseActivity<ActivityFlashcardBinding>(),
    TestFlashcardFragment.OnTestProgressListener {

    companion object {
        private const val TAG = "TestVocabularyActivity"
        private const val EXTRA_WORDS = "extra_words"
        private const val EXTRA_CURRENT_INDEX = "extra_current_index"
        private const val EXTRA_LESSON_TITLE = "extra_lesson_title"
        private const val EXTRA_LESSON_ID = "extra_lesson_id"

        fun newIntent(
            context: Context,
            words: ArrayList<Word>,
            currentIndex: Int = 0,
            lessonTitle: String = "",
            lessonId: String = ""
        ): Intent {
            return Intent(context, TestFlashcardActivity::class.java).apply {
                putParcelableArrayListExtra(EXTRA_WORDS, words)
                putExtra(EXTRA_CURRENT_INDEX, currentIndex)
                putExtra(EXTRA_LESSON_TITLE, lessonTitle)
                putExtra(EXTRA_LESSON_ID, lessonId)
            }
        }
    }

    private lateinit var testVocabularyAdapter: TestFlashcardAdapter
    private var words: List<Word> = emptyList()
    private var currentIndex: Int = 0
    private var lessonTitle: String = ""
    private var lessonId: String = ""
    private val audioManager = AudioManager.getInstance()
    private val savedWordsRepository: SavedWordsRepository = SavedWordsRepositoryImpl()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Track test results
    private val correctAnswers = mutableListOf<String>()
    private val wrongAnswers = mutableListOf<String>()

    override fun inflateBinding(inflater: LayoutInflater): ActivityFlashcardBinding {
        return ActivityFlashcardBinding.inflate(inflater)
    }

    override fun initView() {
        try {
            Log.d(TAG, "Initializing TestVocabularyActivity")
            getIntentData()

            // Validate data before proceeding
            if (words.isEmpty()) {
                Log.e(TAG, "No vocabulary words available")
                showError("No vocabulary words available")
                return
            }

            Log.d(TAG, "Loaded ${words.size} words, starting at index $currentIndex")

            setupToolbar()
            setupViewPager()
            setupClickListeners()
            setupAccessibility()
            animateInitialAppearance()

            Log.d(TAG, "TestVocabularyActivity initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TestVocabularyActivity", e)
            showError("Failed to initialize vocabulary test: ${e.message}")
        }
    }

    override fun initData() {

    }

    private fun getIntentData() {
        try {
            words = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra(EXTRA_WORDS, Word::class.java) ?: emptyList()
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra<Word>(EXTRA_WORDS) ?: emptyList()
            }
            currentIndex = intent.getIntExtra(EXTRA_CURRENT_INDEX, 0)
            lessonTitle = intent.getStringExtra(EXTRA_LESSON_TITLE) ?: ""
            lessonId = intent.getStringExtra(EXTRA_LESSON_ID) ?: ""

            Log.d(TAG, "Intent data: ${words.size} words, index: $currentIndex, title: $lessonTitle, lessonId: $lessonId")
        } catch (e: Exception) {
            Log.e(TAG, "Error getting intent data", e)
            words = emptyList()
            currentIndex = 0
            lessonTitle = ""
            lessonId = ""
        }
    }

    private fun setupToolbar() {
        binding.apply {
            // Set up toolbar title
            val displayTitle = if (lessonTitle.isNotEmpty()) {
                "Test: $lessonTitle"
            } else {
                getString(R.string.test_vocabulary)
            }
            textTitle.text = displayTitle

            // Set up back button
            btnBack.setOnClickListener {
                onBackPressed()
            }

            // Update progress text
            updateProgressIndicator(currentIndex)
        }
    }

    private fun setupViewPager() {
        testVocabularyAdapter = TestFlashcardAdapter(this, words)

        binding.viewPagerFlashcards.apply {
            adapter = testVocabularyAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL

            // Disable swiping between cards (user must answer each question)
            isUserInputEnabled = false

            // Set the current item
            if (currentIndex >= 0 && currentIndex < words.size) {
                setCurrentItem(currentIndex, false)
            }
        }
    }

    private fun setupClickListeners() {

    }

    private fun setupAccessibility() {

    }

    private fun animateInitialAppearance() {
        binding.root.apply {
            alpha = 0f
            animate()
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
    }

    private fun updateProgressIndicator(position: Int) {
        binding.textProgress.text = "${position + 1}/${words.size}"
    }

    private fun showTestResults() {
        val totalWords = words.size
        val correctCount = correctAnswers.size
        val scorePercent = if (totalWords > 0) (correctCount * 100 / totalWords) else 0
        val resultMessage = getString(R.string.test_completed) + "\n" +
                getString(R.string.test_score_format, correctCount, totalWords, scorePercent)

        DialogUtils.showInfoDialog(
            context = this,
            title = getString(R.string.test_results),
            message = resultMessage,
            onPositiveClick = {
                setResult(RESULT_OK, Intent().apply {
                    putExtra("test_completed", true)
                    putExtra("lesson_id", lessonId)
                    putExtra("correct_answers", correctCount)
                    putExtra("total_words", totalWords)
                })
                finish()
            }
        )
    }

    private fun showError(message: String) {
        DialogUtils.showErrorDialog(
            context = this,
            message = message
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this) {
            DialogUtils.showConfirmationDialog(
                context = this@TestFlashcardActivity,
                title = getString(R.string.exit_test_title),
                message = getString(R.string.exit_test_message),
                positiveButtonText = getString(R.string.exit_test_positive),
                negativeButtonText = getString(R.string.exit_test_negative),
                onPositiveClick = {
                    setResult(RESULT_CANCELED)
                    finish()
                },
                onNegativeClick = null
            )
        }
    }

    // TestVocabularyFragment.OnTestProgressListener implementation
    override fun onWordTested(word: Word, isCorrect: Boolean) {
        if (isCorrect) {
            correctAnswers.add(word.id)
        } else {
            wrongAnswers.add(word.id)
        }
        // Save classification, streak, and points to Firebase
        val userId = auth.currentUser?.uid ?: return
        lifecycleScope.launch {
            savedWordsRepository.saveOrUpdateTestedWord(userId, word, isCorrect)
        }
    }

    override fun onMoveToNextCard() {
        if (currentIndex < words.size - 1) {
            currentIndex++
            binding.viewPagerFlashcards.setCurrentItem(currentIndex, true)
            updateProgressIndicator(currentIndex)
        } else {
            // Show test completion
            showTestResults()
        }
    }

    override fun onPlayWordAudio(word: Word) {
        val soundUrl = word.phonetics.firstOrNull()?.audio.orEmpty()
        if (soundUrl.isNotEmpty()) {
            audioManager.playAudio(
                context = this,
                audioUrl = soundUrl,
                listener = object : AudioManager.AudioPlaybackListener {
                    override fun onAudioStarted() {
                        // Audio playback started
                    }
                    override fun onAudioCompleted() {
                        // Audio playback completed
                    }
                    override fun onAudioError(error: String) {
                        Toast.makeText(this@TestFlashcardActivity,
                            "Could not play pronunciation: $error",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            )
        } else {
            Toast.makeText(this, "Audio pronunciation not available for '${word.word}'",
                Toast.LENGTH_SHORT).show()
        }
    }
}
