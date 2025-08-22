package com.sun.englishlearning.screen.lessondetail

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.sun.englishlearning.utils.DialogUtils
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sun.englishlearning.R
import com.sun.englishlearning.databinding.FragmentLessonDetailBinding
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.data.model.UserLessonProgress
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.repository.UserLessonProgressRepositoryImpl
import com.sun.englishlearning.screen.lessondetail.adapter.VocabularyAdapter
import com.sun.englishlearning.screen.flashcard.FlashcardActivity
import com.sun.englishlearning.screen.testflashcard.TestFlashcardActivity
import com.sun.englishlearning.utils.base.BaseFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.activity.result.contract.ActivityResultContracts
import com.sun.englishlearning.utils.AudioManager

class LessonDetailFragment : BaseFragment<FragmentLessonDetailBinding>(), LessonDetailContract.View {

    companion object {
        private const val TAG = "LessonDetailFragment"
    }

    private lateinit var vocabularyAdapter: VocabularyAdapter
    private lateinit var presenter: LessonDetailContract.Presenter
    private val args: LessonDetailFragmentArgs by navArgs()
    private var currentLesson: Lesson? = null
    private val userProgressRepository = UserLessonProgressRepositoryImpl()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val auth = Firebase.auth
    private var currentVocabulary: List<Word> = emptyList()
    private var learnedWordIds: Set<String> = emptySet()
    private val audioManager = AudioManager.getInstance()

    private val flashcardLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val updatedLessonId = result.data?.getStringExtra("updated_lesson_id")
            if (updatedLessonId != null) {
                Log.d(TAG, "Received updated lesson ID: $updatedLessonId")
                // Refresh progress UI for this lesson
                loadUserProgress(updatedLessonId)
            }
        }
    }

    private val testFlashcardLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val testCompleted = result.data?.getBooleanExtra("test_completed", false) ?: false
            val lessonId = result.data?.getStringExtra("lesson_id") ?: ""
            val correctAnswers = result.data?.getIntExtra("correct_answers", 0) ?: 0
            val totalWords = result.data?.getIntExtra("total_words", 0) ?: 0

            if (testCompleted && lessonId.isNotEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Test completed! Score: $correctAnswers/$totalWords",
                    Toast.LENGTH_LONG
                ).show()

                // Refresh progress UI for this lesson
                loadUserProgress(lessonId)
            }
        }
    }

    override val isInsets = true

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentLessonDetailBinding {
        return FragmentLessonDetailBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        setupRecyclerView()
        setupBackButton()
        setupTestButton()
    }

    override fun initData() {
        presenter = LessonDetailPresenter()
        (presenter as LessonDetailPresenter).setContext(requireContext())
        presenter.attachView(this)

        // Get lesson from args and load
        currentLesson = args.lesson
        currentLesson?.let { lesson ->
            presenter.loadLessonDetail(lesson)
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
    }

    private fun setupRecyclerView() {
        vocabularyAdapter = VocabularyAdapter(
            onWordClick = { word -> presenter.onWordClicked(word) },
            onSoundClick = { word -> presenter.onSoundClicked(word) }
        )

        viewBinding.recyclerViewVocabulary.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = vocabularyAdapter
        }
    }

    private fun setupBackButton() {
        viewBinding.btnBack.setOnClickListener {
            presenter.onBackClicked()
        }
    }

    private fun setupTestButton() {
        viewBinding.btnTestVocabulary.setOnClickListener {
            currentLesson?.let { lesson ->
                if (currentVocabulary.isEmpty()) {
                    Toast.makeText(requireContext(), "No vocabulary words available for testing", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                navigateToTest(currentVocabulary, lesson.title, lesson.id)
            }
        }
    }

    private fun navigateToTest(words: List<Word>, lessonTitle: String, lessonId: String) {
        try {
            Log.d(TAG, "Navigating to test flashcard: ${words.size} words, title: $lessonTitle")
            val intent = TestFlashcardActivity.newIntent(
                context = requireContext(),
                words = ArrayList(words),
                currentIndex = 0,
                lessonTitle = lessonTitle,
                lessonId = lessonId
            )
            testFlashcardLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to test flashcard", e)
            DialogUtils.showErrorDialog(
                context = requireContext(),
                message = "Failed to open vocabulary test: ${e.message}"
            )
        }
    }

    // MVP View implementations
    override fun showLoading() {
        // Show loading indicator
    }

    override fun hideLoading() {
        // Hide loading indicator
    }

    override fun displayLessonInfo(lesson: Lesson) {
        viewBinding.apply {
            // Set lesson title
            textLessonTitle.text = lesson.title

            // Set lesson details
            textLessonPoints.text = getString(R.string.lesson_topic_format, lesson.title)
            textLessonDescription.text = lesson.description

            // Load lesson image
            if (lesson.imageUrl.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(lesson.imageUrl)
                    .apply(RequestOptions().transform(RoundedCorners(24)))
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(imageLesson)
            } else {
                imageLesson.setImageResource(R.drawable.ic_launcher_background)
            }
        }

        // Load user progress after lesson info is displayed
        loadUserProgress(lesson.id)
    }

    override fun showVocabulary(words: List<Word>) {
        currentVocabulary = words
        updateVocabularyAdapter()
        viewBinding.textWordCount.text = getString(R.string.word_count_format, words.size)
    }

    private fun updateVocabularyAdapter() {
        try {
            if (::vocabularyAdapter.isInitialized) {
                vocabularyAdapter.updateWords(currentVocabulary, learnedWordIds)
            } else {
                Log.w("LessonDetailFragment", "VocabularyAdapter not initialized")
            }
        } catch (e: Exception) {
            Log.e("LessonDetailFragment", "Error updating vocabulary adapter", e)
        }
    }

    override fun showError(message: String) {
        try {
            if (isAdded && !isDetached) {
                DialogUtils.showErrorDialog(
                    context = requireContext(),
                    message = message
                )
            }
        } catch (e: Exception) {
            Log.e("LessonDetailFragment", "Error showing error dialog", e)
        }
    }

    override fun navigateBack() {
        findNavController().navigateUp()
    }

    override fun playWordSound(word: Word) {
        val soundUrl = word.phonetics.firstOrNull()?.audio.orEmpty()
        if (soundUrl.isNotEmpty()) {
            audioManager.playAudio(
                context = requireContext(),
                audioUrl = soundUrl,
                listener = object : AudioManager.AudioPlaybackListener {
                    override fun onAudioStarted() {}

                    override fun onAudioCompleted() {}

                    override fun onAudioError(error: String) {
                        Toast.makeText(requireContext(), "Error playing audio: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        } else {
            Toast.makeText(requireContext(), "Audio pronunciation not available for '${word.word}'", Toast.LENGTH_SHORT).show()
        }
    }

    override fun showWordDetail(word: Word) {
        Toast.makeText(requireContext(), "Word: ${word.word}", Toast.LENGTH_SHORT).show()
    }

    override fun navigateToFlashcard(words: List<Word>, currentIndex: Int, lessonTitle: String) {
        try {
            Log.d(TAG, "Navigating to flashcard: ${words.size} words, index: $currentIndex, title: $lessonTitle")
            val sortedWords = currentVocabulary // Use sorted vocabulary for flashcard
            val index = if (currentIndex < 0 || currentIndex >= sortedWords.size) 0 else currentIndex
            val intent = FlashcardActivity.newIntent(
                context = requireContext(),
                words = ArrayList(sortedWords),
                currentIndex = index,
                lessonTitle = lessonTitle
            )
            flashcardLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to flashcard", e)
            DialogUtils.showErrorDialog(
                context = requireContext(),
                message = "Failed to open flashcard: ${e.message}"
            )
        }
    }

    private fun loadUserProgress(lessonId: String) {
        coroutineScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val progressResult = userProgressRepository.getUserLessonProgress(userId, lessonId)
                if (progressResult.isSuccess) {
                    val progress = progressResult.getOrNull()
                    learnedWordIds = progress?.learnedWordIds?.toSet() ?: emptySet()
                    updateProgressUI(progress)
                } else {
                    learnedWordIds = emptySet()
                    updateProgressUI(null)
                }
                updateVocabularyAdapter()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load user progress", e)
                learnedWordIds = emptySet()
                updateProgressUI(null)
                updateVocabularyAdapter()
            }
        }
    }

    private fun updateProgressUI(progress: UserLessonProgress?) {
        val lesson = currentLesson ?: return
        val totalWords = lesson.vocabulary.size

        viewBinding.apply {
            // Update progress bar
            val progressPercentage = if (progress != null && totalWords > 0) {
                (progress.wordsLearned * 100) / totalWords
            } else {
                0
            }
            progressLesson.progress = progressPercentage

            // Update points text
            val wordsLearned = if (progress != null) progress.wordsLearned else 0
            textLessonPoints.text = getString(R.string.words_learned_format, wordsLearned, totalWords)
        }
    }

    override fun onGetWordsSuccess(words: MutableList<Word>) {
        currentVocabulary = words
        vocabularyAdapter.updateWords(words, learnedWordIds)
    }

    override fun onError(exception: Exception?) {
        Toast.makeText(requireContext(), exception?.message ?: "Error loading words", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // Always reload progress and vocabulary from Firebase when returning to this screen
        currentLesson?.let { lesson ->
            loadUserProgress(lesson.id)
            presenter.getWords(lesson.vocabulary)
        }
    }
}
