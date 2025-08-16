package com.sun.englishlearning.screen.savedwords

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.sun.englishlearning.data.model.SavedWord
import com.sun.englishlearning.databinding.ActivitySavedWordsBinding
import com.sun.englishlearning.utils.base.BaseActivity

class SavedWordsActivity : BaseActivity<ActivitySavedWordsBinding>() {

    private lateinit var savedWordsAdapter: SavedWordsAdapter
    private var allSavedWords = mutableListOf<SavedWord>()
    private var filteredSavedWords = mutableListOf<SavedWord>()

    override fun inflateBinding(inflater: LayoutInflater): ActivitySavedWordsBinding {
        return ActivitySavedWordsBinding.inflate(inflater)
    }

    override fun initView() {
        setupRecyclerView()
        setupClickListeners()
        setupSearchView()
        loadSampleData()
    }

    override fun initData() {
    }

    private fun setupRecyclerView() {
        savedWordsAdapter = SavedWordsAdapter(filteredSavedWords) { savedWord, action ->
            when (action) {
                SavedWordsAdapter.Action.PLAY_SOUND -> {
                    playWordSound(savedWord.soundUrl)
                }
                SavedWordsAdapter.Action.TOGGLE_FAVORITE -> {
                    toggleFavorite(savedWord)
                }
            }
        }

        binding.rvSavedWords.apply {
            layoutManager = LinearLayoutManager(this@SavedWordsActivity)
            adapter = savedWordsAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupSearchView() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterWords(s.toString())
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterWords(query: String) {
        filteredSavedWords.clear()
        if (query.isEmpty()) {
            filteredSavedWords.addAll(allSavedWords)
        } else {
            val filtered = allSavedWords.filter { savedWord ->
                savedWord.word.lowercase().contains(query.lowercase()) ||
                        savedWord.definition.lowercase().contains(query.lowercase())
            }
            filteredSavedWords.addAll(filtered)
        }
        savedWordsAdapter.notifyDataSetChanged()
    }

    private fun loadSampleData() {
        allSavedWords.addAll(
            listOf(
                SavedWord(
                    id = "1",
                    userId = "sample",
                    word = "as many as",
                    ipa = "",
                    partOfSpeech = "phrase",
                    definition = "as many as",
                    soundUrl = "",
                    example = "I have as many books as you do."
                ),
                SavedWord(
                    id = "2",
                    userId = "sample",
                    word = "City break",
                    ipa = "",
                    partOfSpeech = "noun",
                    definition = "City break",
                    soundUrl = "",
                    example = "We're planning a city break to Paris."
                ),
                SavedWord(
                    id = "3",
                    userId = "sample",
                    word = "Cosmopolitan",
                    ipa = "",
                    partOfSpeech = "adjective",
                    definition = "Cosmopolitan",
                    soundUrl = "",
                    example = "New York is a cosmopolitan city."
                ),
                SavedWord(
                    id = "4",
                    userId = "sample",
                    word = "Crowded",
                    ipa = "",
                    partOfSpeech = "adjective",
                    definition = "Crowded",
                    soundUrl = "",
                    example = "The street was very crowded."
                ),
                SavedWord(
                    id = "5",
                    userId = "sample",
                    word = "Embassy",
                    ipa = "",
                    partOfSpeech = "noun",
                    definition = "Embassy",
                    soundUrl = "",
                    example = "The embassy is located downtown."
                ),
                SavedWord(
                    id = "6",
                    userId = "sample",
                    word = "Getaway",
                    ipa = "",
                    partOfSpeech = "noun",
                    definition = "Getaway",
                    soundUrl = "",
                    example = "We need a weekend getaway."
                ),
                SavedWord(
                    id = "7",
                    userId = "sample",
                    word = "Disappointing",
                    ipa = "",
                    partOfSpeech = "adjective",
                    definition = "Disappointing",
                    soundUrl = "",
                    example = "The movie was disappointing."
                ),
                SavedWord(
                    id = "8",
                    userId = "sample",
                    word = "Jaw-dropping",
                    ipa = "",
                    partOfSpeech = "adjective",
                    definition = "Jaw-dropping",
                    soundUrl = "",
                    example = "The view was absolutely jaw-dropping."
                ),
                SavedWord(
                    id = "9",
                    userId = "sample",
                    word = "Lively",
                    ipa = "",
                    partOfSpeech = "adjective",
                    definition = "Lively",
                    soundUrl = "",
                    example = "The market was very lively."
                )
            )
        )
        filteredSavedWords.addAll(allSavedWords)
        savedWordsAdapter.notifyDataSetChanged()
    }

    private fun playWordSound(soundUrl: String) {
    }

    private fun toggleFavorite(savedWord: SavedWord) {
        // TODO: Implement favorite functionality when isFavorite property is added to SavedWord model
    }
}


