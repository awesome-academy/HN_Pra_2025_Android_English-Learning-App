package com.sun.englishlearning.screen.review

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sun.englishlearning.R
import com.sun.englishlearning.data.model.WordType
import com.sun.englishlearning.databinding.FragmentReviewBinding
import com.sun.englishlearning.screen.savedwords.SavedWordsActivity
import com.sun.englishlearning.screen.vocabulary.VocabularyWordsActivity
import com.sun.englishlearning.utils.base.BaseFragment

class ReviewFragment : BaseFragment<FragmentReviewBinding>() {

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentReviewBinding {
        return FragmentReviewBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        setupClickListeners()
    }

    override fun initData() {
        
    }

    private fun setupClickListeners() {
        viewBinding.apply {
            llWeakWords.setOnClickListener {
                val intent = VocabularyWordsActivity.createIntent(
                    requireContext(), 
                    WordType.WEAK, 
                    "Weak Words"
                )
                startActivity(intent)
            }
            
            llTodayWords.setOnClickListener {
                val intent = VocabularyWordsActivity.createIntent(
                    requireContext(), 
                    WordType.TODAY, 
                    "Today Words"
                )
                startActivity(intent)
            }
            
            llMediumWords.setOnClickListener {
                val intent = VocabularyWordsActivity.createIntent(
                    requireContext(), 
                    WordType.MEDIUM, 
                    "Medium Words"
                )
                startActivity(intent)
            }
            
            llStrongWords.setOnClickListener {
                val intent = VocabularyWordsActivity.createIntent(
                    requireContext(), 
                    WordType.STRONG, 
                    "Strong Words"
                )
                startActivity(intent)
            }
            
            llSavedWords.setOnClickListener {
                val intent = Intent(requireContext(), SavedWordsActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
