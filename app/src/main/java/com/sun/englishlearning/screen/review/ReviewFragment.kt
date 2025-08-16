package com.sun.englishlearning.screen.review

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.sun.englishlearning.data.model.WordType
import com.sun.englishlearning.data.repository.SavedWordsRepository
import com.sun.englishlearning.data.repository.SavedWordsRepositoryImpl
import com.sun.englishlearning.databinding.FragmentReviewBinding
import com.sun.englishlearning.screen.savedwords.SavedWordsActivity
import com.sun.englishlearning.screen.vocabulary.VocabularyByTypeActivity
import com.sun.englishlearning.utils.base.BaseFragment
import kotlinx.coroutines.launch

class ReviewFragment : BaseFragment<FragmentReviewBinding>() {

    private val savedWordsRepository: SavedWordsRepository = SavedWordsRepositoryImpl()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentReviewBinding {
        return FragmentReviewBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        setupClickListeners()
    }

    override fun initData() {
        loadWordCounts()
    }

    private fun loadWordCounts() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Please login to view your vocabulary", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // Load counts for each word type
                loadWordCountForType(WordType.WEAK, "Weak words")
                loadWordCountForType(WordType.MEDIUM, "Medium words") 
                loadWordCountForType(WordType.STRONG, "Strong words")
                
                // Load saved words count
                val savedWordsResult = savedWordsRepository.getWordCountByType(currentUser.uid, WordType.SAVED)
                if (savedWordsResult.isSuccess) {
                    val count = savedWordsResult.getOrNull() ?: 0
                    // The saved words section doesn't show count in the current layout
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading vocabulary data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun loadWordCountForType(wordType: WordType, typeName: String) {
        val currentUser = auth.currentUser ?: return
        
        try {
            val result = savedWordsRepository.getWordCountByType(currentUser.uid, wordType)
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                updateCardCount(wordType, count)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading $typeName", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCardCount(wordType: WordType, count: Int) {
        when (wordType) {
            WordType.WEAK -> {
                viewBinding.tvWeakWordsCount.text = count.toString()
            }
            WordType.MEDIUM -> {
                viewBinding.tvMediumWordsCount.text = count.toString()
            }
            WordType.STRONG -> {
                viewBinding.tvStrongWordsCount.text = count.toString()
            }
            else -> {}
        }
    }

    private fun setupClickListeners() {
        with(viewBinding) {
            cardWeekWords.setOnClickListener {
                val intent = VocabularyByTypeActivity.createIntent(
                    requireContext(),
                    WordType.WEAK,
                    "Weak words"
                )
                startActivity(intent)
            }

            cardMediumWords.setOnClickListener {
                val intent = VocabularyByTypeActivity.createIntent(
                    requireContext(),
                    WordType.MEDIUM,
                    "Medium words"
                )
                startActivity(intent)
            }

            cardStrongWords.setOnClickListener {
                val intent = VocabularyByTypeActivity.createIntent(
                    requireContext(),
                    WordType.STRONG,
                    "Strong words"
                )
                startActivity(intent)
            }

            layoutSaveWords.setOnClickListener {
                val intent = Intent(requireContext(), SavedWordsActivity::class.java)
                startActivity(intent)
            }
        }
    }
}




