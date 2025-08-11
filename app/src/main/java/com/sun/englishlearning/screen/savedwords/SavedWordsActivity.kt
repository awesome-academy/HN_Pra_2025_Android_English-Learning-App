package com.sun.englishlearning.screen.savedwords

import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.sun.englishlearning.databinding.ActivitySavedWordsBinding
import com.sun.englishlearning.data.model.SavedWord
import com.sun.englishlearning.utils.base.BaseActivity

class SavedWordsActivity : BaseActivity<ActivitySavedWordsBinding>() {

    private lateinit var savedWordsAdapter: SavedWordsAdapter

    override fun inflateBinding(inflater: LayoutInflater): ActivitySavedWordsBinding {
        return ActivitySavedWordsBinding.inflate(inflater)
    }

    override fun initView() {
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
    }


    override fun initData() {
        loadSampleData()
    }

    private fun setupToolbar() {
        binding.apply {
            ivBack.setOnClickListener {
                finish()
            }
        }
    }

    private fun setupRecyclerView() {
        savedWordsAdapter = SavedWordsAdapter { savedWord ->
            // Handle word item click
        }
        
        binding.rvSavedWords.apply {
            layoutManager = LinearLayoutManager(this@SavedWordsActivity)
            adapter = savedWordsAdapter
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            // Search functionality
            etSearch.setOnClickListener {
                // Handle search click
            }
        }
    }

    private fun loadSampleData() {
        val sampleWords = listOf(
            SavedWord(1, "as many as", "as many as"),
            SavedWord(2, "City break", "City break"),
            SavedWord(3, "Cosmopolitan", "Cosmopolitan"),
            SavedWord(4, "Crowded", "Crowded"),
            SavedWord(5, "Embassy", "Embassy"),
            SavedWord(6, "Getaway", "Getaway"),
            SavedWord(7, "Disappointing", "Disappointing"),
            SavedWord(8, "Jaw-dropping", "Jaw-dropping"),
            SavedWord(9, "Lively", "Lively")
        )
        
        savedWordsAdapter.updateWords(sampleWords)
    }
}
