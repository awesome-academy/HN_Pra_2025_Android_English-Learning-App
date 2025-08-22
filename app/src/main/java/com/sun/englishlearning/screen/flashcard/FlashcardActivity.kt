package com.sun.englishlearning.screen.flashcard

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.databinding.ActivityFlashcardBinding
import com.sun.englishlearning.screen.flashcard.adapter.FlashcardAdapter
import com.sun.englishlearning.utils.AudioManager
import com.sun.englishlearning.utils.DialogUtils
import com.sun.englishlearning.utils.base.BaseActivity

class FlashcardActivity : BaseActivity<ActivityFlashcardBinding>(), FlashcardFragment.OnProgressUpdateListener {

    companion object {
        private const val TAG = "FlashcardActivity"
        private const val EXTRA_WORDS = "extra_words"
        private const val EXTRA_CURRENT_INDEX = "extra_current_index"
        private const val EXTRA_LESSON_TITLE = "extra_lesson_title"

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
    private var lessonIdForUpdate: String? = null
    private var pageChangeCallback: ViewPager2.OnPageChangeCallback? = null

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
            animateInitialAppearance()

            Log.d(TAG, "FlashcardActivity initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing FlashcardActivity", e)
            showError("Failed to initialize flashcard: ${e.message}")
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
            // Only check for non-empty word, no need for null check
            words = words.filter { it.word.isNotEmpty() }
            currentIndex = intent.getIntExtra(EXTRA_CURRENT_INDEX, 0)
            lessonTitle = intent.getStringExtra(EXTRA_LESSON_TITLE) ?: ""
            if (currentIndex < 0 || currentIndex >= words.size) {
                Log.w(TAG, "Invalid current index: $currentIndex, resetting to 0")
                currentIndex = 0
            }
            Log.d(TAG, "Intent data: ${words.size} valid words, index: $currentIndex, title: $lessonTitle")
            if (words.isEmpty()) {
                Log.e(TAG, "No valid words received in intent")
                throw IllegalArgumentException("No valid vocabulary words provided")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting intent data", e)
            words = emptyList()
            currentIndex = 0
            lessonTitle = ""
        }
    }

    private fun setupToolbar() {
        binding.textTitle.text = if (lessonTitle.isNotEmpty()) {
            lessonTitle
        } else {
            "Lesson"
        }
        updatePositionIndicator(currentIndex)
    }

    private fun setupViewPager() {
        flashcardAdapter = FlashcardAdapter(this, words)
        binding.viewPagerFlashcards.apply {
            adapter = flashcardAdapter
            currentItem = currentIndex
            isUserInputEnabled = true
            setPageTransformer(PageTransformer())
            // Unregister previous callback if exists
            pageChangeCallback?.let { unregisterOnPageChangeCallback(it) }
            pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
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
            }
            registerOnPageChangeCallback(pageChangeCallback!!)
        }
    }

    private fun updatePositionIndicator(position: Int) {
        val displayPosition = position + 1
        val totalWords = words.size
        val positionText = "$displayPosition / $totalWords"

        // Animate position indicator update
        binding.textPosition.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(150)
            .withEndAction {
                binding.textPosition.text = positionText
                binding.textPosition.contentDescription = "Flashcard $displayPosition of $totalWords"
                binding.textPosition.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }

    private fun updateNavigationHints(position: Int) {
        // No-op for now
    }

    fun playWordAudio(word: Word) {
        val soundUrl = word.phonetics.firstOrNull()?.audio.orEmpty()
        if (soundUrl.isNotEmpty()) {
            audioManager.playAudio(
                context = this,
                audioUrl = soundUrl,
                listener = object : AudioManager.AudioPlaybackListener {
                    override fun onAudioStarted() {}

                    override fun onAudioCompleted() {}

                    override fun onAudioError(error: String) {}
                }
            )
        } else {
            Toast.makeText(
                this,
                "Audio pronunciation not available for '${word.word}'",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { view ->
            // Animate button press
            animateButtonPress(view) {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun setupAccessibility() {
        binding.viewPagerFlashcards.contentDescription = "Swipe left or right to navigate between flashcards"
        // Use sendAccessibilityEvent instead of deprecated announceForAccessibility
        binding.viewPagerFlashcards.sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun animateInitialAppearance() {
        // Animate top bar
        binding.textTitle.apply {
            alpha = 0f
            translationY = -50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setInterpolator(OvershootInterpolator())
                .start()
        }

        binding.textPosition.apply {
            alpha = 0f
            scaleX = 0.5f
            scaleY = 0.5f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setStartDelay(200)
                .setInterpolator(OvershootInterpolator())
                .start()
        }

        // Animate ViewPager
        binding.viewPagerFlashcards.apply {
            alpha = 0f
            scaleX = 0.9f
            scaleY = 0.9f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setStartDelay(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }

        // Find and animate bottom navigation hints
        binding.root.post {
            val bottomLayout = binding.root.getChildAt(binding.root.childCount - 1)
            bottomLayout.alpha = 0f
            bottomLayout.translationY = 100f
            bottomLayout.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(500)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
    }

    private fun showError(message: String) {
        Log.e(TAG, "Showing error: $message")
        DialogUtils.showErrorDialog(
            context = this,
            message = message
        ) {
            // Close activity after user acknowledges the error
            finish()
        }
    }

    private fun animateButtonPress(view: android.view.View, onAnimationEnd: () -> Unit) {
        val scaleDown = android.animation.AnimatorSet().apply {
            playTogether(
                android.animation.ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f),
                android.animation.ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f)
            )
            duration = 100
            interpolator = OvershootInterpolator()
        }

        val scaleUp = android.animation.AnimatorSet().apply {
            playTogether(
                android.animation.ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1.05f),
                android.animation.ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1.05f)
            )
            duration = 150
            interpolator = OvershootInterpolator()
        }

        val scaleNormal = android.animation.AnimatorSet().apply {
            playTogether(
                android.animation.ObjectAnimator.ofFloat(view, "scaleX", 1.05f, 1f),
                android.animation.ObjectAnimator.ofFloat(view, "scaleY", 1.05f, 1f)
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

    // Implementation of OnProgressUpdateListener
    override fun onProgressUpdated(lessonId: String) {
        Log.d(TAG, "Progress updated for lesson: $lessonId")
        // Store lessonId for later use when finishing the activity
        lessonIdForUpdate = lessonId
    }

    override fun onDestroy() {
        // Stop all audio playback
        try {
            audioManager.stopAudio()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping audio in onDestroy", e)
        }
        // Clean up ViewPager2 callback
        pageChangeCallback?.let { callback ->
            binding.viewPagerFlashcards.unregisterOnPageChangeCallback(callback)
            pageChangeCallback = null
        }
        // Clear ViewPager2 adapter to release fragments
        binding.viewPagerFlashcards.adapter = null
        words = emptyList()
        lessonIdForUpdate = null
        super.onDestroy()
    }

    override fun finish() {
        // Custom finish logic if needed
        super.finish()
    }
}
