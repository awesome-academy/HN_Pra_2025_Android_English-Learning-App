package com.sun.englishlearning.screen.lessondetail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.sun.englishlearning.R
import com.sun.englishlearning.databinding.FragmentLessonDetailBinding
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.screen.lessondetail.adapter.VocabularyAdapter
import com.sun.englishlearning.screen.flashcard.FlashcardActivity
import androidx.fragment.app.Fragment

class LessonDetailFragment : Fragment(), LessonDetailContract.View {

    companion object {
        private const val TAG = "LessonDetailFragment"
    }

    private var _viewBinding: FragmentLessonDetailBinding? = null
    private val viewBinding get() = _viewBinding!!

    private lateinit var vocabularyAdapter: VocabularyAdapter
    private lateinit var presenter: LessonDetailContract.Presenter
    private val args: LessonDetailFragmentArgs by navArgs()
    private var currentLesson: Lesson? = null

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
            textLessonNumber.text = "Lesson: ${lesson.lessonNumber}"
            textAdvancedLevel.text = "Level: ${lesson.difficulty.name}"
            textLessonPoints.text = "Total Points: ${lesson.totalPoints}"
            textLessonDescription.text = lesson.description

            // Set progress using UserLessonProgress if available
            loadUserProgress(lesson.id)

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
    }

    override fun showVocabulary(words: List<Word>) {
        vocabularyAdapter.updateWords(words)
        viewBinding.textWordCount.text = "${words.size} words"
    }

    override fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), "No vocabulary words available", Toast.LENGTH_SHORT).show()
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
                startActivity(intent)
            } else {
                val intent = FlashcardActivity.newIntent(
                    context = requireContext(),
                    words = ArrayList(words),
                    currentIndex = currentIndex,
                    lessonTitle = lessonTitle
                )
                startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to flashcard", e)
            Toast.makeText(requireContext(), "Failed to open flashcard: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadUserProgress(lessonId: String) {
        // TODO: Implement actual user progress loading
        // For now, set a placeholder progress
        viewBinding.progressLesson.progress = 25 // 25% as example
    }
}
