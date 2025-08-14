package com.sun.englishlearning.screen.review

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.sun.englishlearning.databinding.FragmentReviewBinding
import com.sun.englishlearning.screen.savedwords.SavedWordsActivity
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
        with(viewBinding) {
            cardWeekWords.setOnClickListener {
                Toast.makeText(context, "Week words clicked", Toast.LENGTH_SHORT).show()
            }

            cardTodayWords.setOnClickListener {
                Toast.makeText(context, "Today words clicked", Toast.LENGTH_SHORT).show()
            }

            cardMediumWords.setOnClickListener {
                Toast.makeText(context, "Medium words clicked", Toast.LENGTH_SHORT).show()
            }

            cardStrongWords.setOnClickListener {
                Toast.makeText(context, "Strong words clicked", Toast.LENGTH_SHORT).show()
            }

            layoutSaveWords.setOnClickListener {
                val intent = Intent(requireContext(), SavedWordsActivity::class.java)
                startActivity(intent)
            }
        }
    }
}




