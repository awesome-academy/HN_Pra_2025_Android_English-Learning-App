package com.sun.englishlearning.screen.flashcard

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import androidx.viewpager2.widget.ViewPager2
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.databinding.ActivityFlashcardBinding
import com.sun.englishlearning.screen.flashcard.adapter.FlashcardAdapter
import com.sun.englishlearning.utils.AudioManager
import com.sun.englishlearning.utils.base.BaseActivity

class FlashcardActivity : BaseActivity<ActivityFlashcardBinding>() {

    companion object {
        private const val TAG = "FlashcardActivity"
        private const val EXTRA_WORDS = "extra_words"
        private const val EXTRA_CURRENT_INDEX = "extra_current_index"
        private const val EXTRA_LESSON_TITLE = "extra_lesson_title"
        private const val MIN_SCALE = 0.85f

        fun newIntent(
            context: Context,
            words: ArrayList<Word>,
            currentIndex: Int = 0,
            lessonTitle: String = ""
        ): Intent {
            return Intent(context, FlashcardActivity::class.java).apply {
                putParcelableArrayListExtra(EXTRA_WORDS, words)
                putExtra(EXTRA_CURRENT_INDEX, currentIndex)
                putExtra(EXTRA_LESSON_TITLE, lessonTitle)
            }
        }
    }

    private lateinit var flashcardAdapter: FlashcardAdapter
    private var words: List<Word> = emptyList()
    private var currentIndex: Int = 0
    private var lessonTitle: String = ""
    private val audioManager = AudioManager.getInstance()

    override fun inflateBinding(inflater: LayoutInflater): ActivityFlashcardBinding {
        return ActivityFlashcardBinding.inflate(inflater)
    }

    override fun initView() {
        try {
            Log.d(TAG, "Initializing FlashcardActivity")
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

            Log.d(TAG, "FlashcardActivity initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing FlashcardActivity", e)
            showError("Failed to initialize flashcard: ${e.message}")
        }
    }

    override fun initData() {
        // Data is initialized in initView
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

            Log.d(TAG, "Intent data: ${words.size} words, index: $currentIndex, title: $lessonTitle")
        } catch (e: Exception) {
            Log.e(TAG, "Error getting intent data", e)
            words = emptyList()
            currentIndex = 0
            lessonTitle = ""
        }
    }

    private fun setupToolbar() {
        binding.textTitle.text = if (lessonTitle.isNotEmpty()) {
            "$lessonTitle - Flashcards"
        } else {
            "Flashcards"
        }
        updatePositionIndicator(currentIndex)
    }

    private fun setupViewPager() {
        flashcardAdapter = FlashcardAdapter(this, words)

        binding.viewPagerFlashcards.apply {
            adapter = flashcardAdapter
            currentItem = currentIndex

            // Enable smooth scrolling
            isUserInputEnabled = true

            // Set page transformer for smooth transitions
            setPageTransformer { page, position ->
                page.apply {
                    when {
                        position < -1 -> { // [-Infinity,-1)
                            // This page is way off-screen to the left
                            alpha = 0f
                        }
                        position <= 1 -> { // [-1,1]
                            // Fade the page relative to its position
                            alpha = 1 - kotlin.math.abs(position)

                            // Scale the page down (between MIN_SCALE and 1)
                            val scaleFactor = kotlin.math.max(MIN_SCALE, 1 - kotlin.math.abs(position))
                            scaleX = scaleFactor
                            scaleY = scaleFactor
                        }
                        else -> { // (1,+Infinity]
                            // This page is way off-screen to the right
                            alpha = 0f
                        }
                    }
                }
            }

            // Add page change callback
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    currentIndex = position
                    updatePositionIndicator(position)
                    updateNavigationHints(position)
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    // Stop any playing audio when scrolling
                    if (positionOffset != 0f) {
                        audioManager.stopAudio()
                    }
                }
            })
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun updatePositionIndicator(position: Int) {
        val displayPosition = position + 1
        val totalWords = words.size
        val positionText = "$displayPosition / $totalWords"
        binding.textPosition.text = positionText
        binding.textPosition.contentDescription = "Flashcard $displayPosition of $totalWords"
    }

    private fun updateNavigationHints(position: Int) {
        // This method can be used to update navigation hints based on current position
        // For example, disable swipe hints at first/last positions
        val isFirstCard = position == 0
        val isLastCard = position == words.size - 1

        // You could add visual feedback here for edge cases
        // For now, the hints remain static as they provide general guidance
    }

    fun getCurrentWord(): Word? {
        return if (currentIndex < words.size) words[currentIndex] else null
    }

    fun navigateToNext() {
        if (currentIndex < words.size - 1) {
            binding.viewPagerFlashcards.currentItem = currentIndex + 1
        }
    }

    fun navigateToPrevious() {
        if (currentIndex > 0) {
            binding.viewPagerFlashcards.currentItem = currentIndex - 1
        }
    }

    fun canNavigateNext(): Boolean = currentIndex < words.size - 1

    fun canNavigatePrevious(): Boolean = currentIndex > 0

    fun playWordAudio(word: Word) {
        if (word.soundUrl.isNotEmpty()) {
            audioManager.playAudio(
                context = this,
                audioUrl = word.soundUrl,
                listener = object : AudioManager.AudioPlaybackListener {
                    override fun onAudioStarted() {
                        // Could add visual feedback here
                    }

                    override fun onAudioCompleted() {
                        // Audio finished playing
                    }

                    override fun onAudioError(error: String) {
                        // Error handled by AudioManager
                    }
                }
            )
        } else {
            // Show message that audio is not available
            android.widget.Toast.makeText(
                this,
                "Audio pronunciation not available for '${word.name}'",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupAccessibility() {
        // Set up accessibility for the ViewPager
        binding.viewPagerFlashcards.contentDescription = "Swipe left or right to navigate between flashcards"

        // Announce current position for screen readers
        binding.viewPagerFlashcards.announceForAccessibility(
            "Showing flashcard ${currentIndex + 1} of ${words.size}"
        )
    }

    private fun showError(message: String) {
        Log.e(TAG, "Showing error: $message")
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()

        // Delay finish to allow user to see the toast
        binding.root.postDelayed({
            finish()
        }, 2000)
    }

    override fun onDestroy() {
        super.onDestroy()
        audioManager.release()
    }
}
