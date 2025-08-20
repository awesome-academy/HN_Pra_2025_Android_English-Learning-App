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
        // Vocabulary level cards have been removed from the UI
    }


    private fun setupClickListeners() {
        with(viewBinding) {
            layoutSaveWords.setOnClickListener {
                val intent = Intent(requireContext(), SavedWordsActivity::class.java)
                startActivity(intent)
            }
        }
    }
}




