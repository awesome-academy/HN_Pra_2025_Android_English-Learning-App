package com.sun.englishlearning.screen.lessondetail

import android.os.Bundle
import android.view.LayoutInflater
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
import com.sun.englishlearning.data.repository.VocabularyRepository
import com.sun.englishlearning.utils.base.BaseFragment

class LessonDetailFragment : BaseFragment<FragmentLessonDetailBinding>() {

    private lateinit var vocabularyAdapter: VocabularyAdapter
    private val args: LessonDetailFragmentArgs by navArgs()
    private var currentLesson: Lesson? = null

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentLessonDetailBinding {
        return FragmentLessonDetailBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        setupRecyclerView()
        setupBackButton()

        // Get lesson
        currentLesson = args.lesson
        currentLesson?.let { lesson ->
            displayLessonInfo(lesson)
            loadVocabulary(lesson.id)
        }
    }

    override fun initData() {

    }

    private fun setupRecyclerView() {
        vocabularyAdapter = VocabularyAdapter { word ->
            onWordClick(word)
        }

        viewBinding.recyclerViewVocabulary.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = vocabularyAdapter
        }
    }

    private fun setupBackButton() {
        viewBinding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun displayLessonInfo(lesson: Lesson) {
        viewBinding.apply {
            // Set lesson title
            textLessonTitle.text = lesson.title
            
            // Set lesson details
            textLessonNumber.text = "Lesson: ${lesson.lessonNumber}"
            textAdvancedLevel.text = "Advanced: ${lesson.advancedLevel}"
            textLessonPoints.text = "points: ${lesson.currentPoints} / ${lesson.totalPoints}"
            textLessonDescription.text = lesson.description
            
            // Set progress
            progressLesson.progress = lesson.progressPercentage

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

    private fun loadVocabulary(lessonId: String) {
        val vocabulary = VocabularyRepository.getVocabularyByLessonId(lessonId)
        vocabularyAdapter.updateWords(vocabulary)
        
        // Update word count
        viewBinding.textWordCount.text = "${vocabulary.size} words"
    }

    private fun onWordClick(word: Word) {
        Toast.makeText(requireContext(), "Word: ${word.name}", Toast.LENGTH_SHORT).show()
    }
}
