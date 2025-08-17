package com.sun.englishlearning.screen.lessondetail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.app.Activity
import android.content.Intent

class LessonDetailFragment : Fragment(), LessonDetailContract.View {

    companion object {
        private const val TAG = "LessonDetailFragment"
        private const val REQUEST_CODE_FLASHCARD = 1001
    }

    private var _viewBinding: FragmentLessonDetailBinding? = null
    private val viewBinding get() = _viewBinding!!

    private lateinit var vocabularyAdapter: VocabularyAdapter
    private lateinit var presenter: LessonDetailContract.Presenter
    private val args: LessonDetailFragmentArgs by navArgs()
    private var currentLesson: Lesson? = null
    private val userProgressRepository = UserLessonProgressRepositoryImpl()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    // Authentication
    private val auth = Firebase.auth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _viewBinding = FragmentLessonDetailBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPresenter()
        initView()

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
        super.onDestroyView()
        presenter.detachView()
        _viewBinding = null
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        // Handle result from FlashcardActivity
        if (requestCode == REQUEST_CODE_FLASHCARD && resultCode == Activity.RESULT_OK) {
            val updatedLessonId = data?.getStringExtra("updated_lesson_id")
            if (updatedLessonId != null) {
                Log.d(TAG, "Received updated lesson ID: $updatedLessonId")
                // Refresh progress UI for this lesson
                loadUserProgress(updatedLessonId)
            }
        }
    }

    private fun initPresenter() {
        presenter = LessonDetailPresenter()
        (presenter as LessonDetailPresenter).setContext(requireContext())
        presenter.attachView(this)
    }

    private fun initView() {
        setupRecyclerView()
        setupBackButton()
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
            textLessonPoints.text = "Topic: ${lesson.title}"
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
        vocabularyAdapter.updateWords(words)
        viewBinding.textWordCount.text = "${words.size} words"
    }

    override fun showError(message: String) {
        DialogUtils.showErrorDialog(
            context = requireContext(),
            message = message
        )
    }

    override fun navigateBack() {
        findNavController().navigateUp()
    }

    override fun playWordSound(word: Word) {
        Toast.makeText(requireContext(), "Playing sound for: ${word.word}", Toast.LENGTH_SHORT).show()
        // TODO: Implement actual sound playing
    }

    override fun showWordDetail(word: Word) {
        Toast.makeText(requireContext(), "Word: ${word.word}", Toast.LENGTH_SHORT).show()
        // TODO: Implement word detail functionality
    }

    override fun navigateToFlashcard(words: List<Word>, currentIndex: Int, lessonTitle: String) {
        try {
            Log.d(TAG, "Navigating to flashcard: ${words.size} words, index: $currentIndex, title: $lessonTitle")

            if (words.isEmpty()) {
                DialogUtils.showErrorDialog(
                    context = requireContext(),
                    message = "No vocabulary words available"
                )
                return
            }

            if (currentIndex < 0 || currentIndex >= words.size) {
                Log.w(TAG, "Invalid currentIndex: $currentIndex, using 0 instead")
                val intent = FlashcardActivity.newIntent(
                    context = requireContext(),
                    words = ArrayList(words),
                    currentIndex = 0,
                    lessonTitle = lessonTitle
                )
                // Start activity for result to get progress updates
                startActivityForResult(intent, REQUEST_CODE_FLASHCARD)
            } else {
                val intent = FlashcardActivity.newIntent(
                    context = requireContext(),
                    words = ArrayList(words),
                    currentIndex = currentIndex,
                    lessonTitle = lessonTitle
                )
                // Start activity for result to get progress updates
                startActivityForResult(intent, REQUEST_CODE_FLASHCARD)
            }
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
                    updateProgressUI(progress, lessonId)
                } else {
                    // If no progress found, show default progress (0/totalWords)
                    val lesson = currentLesson ?: return@launch
                    updateProgressUI(null, lessonId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user progress", e)
                // Show default progress in case of error
                val lesson = currentLesson ?: return@launch
                updateProgressUI(null, lessonId)
            }
        }
    }
    
    private fun updateProgressUI(progress: UserLessonProgress?, lessonId: String) {
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
            textLessonPoints.text = "$wordsLearned / $totalWords words learned"
        }
    }
}
