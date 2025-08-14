package com.sun.englishlearning.screen.search

import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.sun.englishlearning.data.model.WordSearchResult
import com.sun.englishlearning.databinding.ActivityWordSearchBinding
import com.sun.englishlearning.utils.base.BaseActivity
import kotlinx.coroutines.*
import kotlin.random.Random

class WordSearchActivity : BaseActivity<ActivityWordSearchBinding>() {

    private var searchJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentWordResult: WordSearchResult? = null

    override fun inflateBinding(inflater: LayoutInflater): ActivityWordSearchBinding {
        return ActivityWordSearchBinding.inflate(inflater)
    }

    override fun initView() {
        setupClickListeners()
        setupSearchInput()
    }

    override fun initData() {
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSearch.setOnClickListener {
            performSearch()
        }

        binding.btnPlaySound.setOnClickListener {
            currentWordResult?.let { playWordSound(it.soundUrl) }
        }

        binding.btnFavorite.setOnClickListener {
            currentWordResult?.let { toggleFavorite(it) }
        }
    }

    private fun setupSearchInput() {
        binding.etSearchWord.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun performSearch() {
        val query = binding.etSearchWord.text.toString().trim()
        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter a word to search", Toast.LENGTH_SHORT).show()
            return
        }

        searchJob?.cancel()
        searchJob = CoroutineScope(Dispatchers.Main).launch {
            showLoading()
            try {
                delay(1500) // Simulate network delay
                val result = searchWord(query)
                showSearchResult(result)
            } catch (e: Exception) {
                showError()
            }
        }
    }

    private fun showLoading() {
        binding.apply {
            progressLoading.visibility = View.VISIBLE
            cardSearchResult.visibility = View.GONE
            layoutEmptyState.visibility = View.GONE
            layoutErrorState.visibility = View.GONE
        }
    }

    private fun showSearchResult(result: WordSearchResult) {
        currentWordResult = result
        binding.apply {
            progressLoading.visibility = View.GONE
            cardSearchResult.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
            layoutErrorState.visibility = View.GONE

            tvWordTitle.text = result.word
            tvIpa.text = result.ipa
            tvPartOfSpeech.text = result.partOfSpeech
            tvDefinition.text = result.definition
            tvExample.text = "\"${result.example}\""

            updateFavoriteButton(result.isFavorite)
        }
    }

    private fun showError() {
        binding.apply {
            progressLoading.visibility = View.GONE
            cardSearchResult.visibility = View.GONE
            layoutEmptyState.visibility = View.GONE
            layoutErrorState.visibility = View.VISIBLE
        }
    }

    private suspend fun searchWord(word: String): WordSearchResult {
        // Simulate API call with mock data
        return withContext(Dispatchers.IO) {
            // Mock dictionary data - in real app, this would be an API call
            when (word.lowercase()) {
                "hello" -> WordSearchResult(
                    word = "hello",
                    ipa = "/həˈloʊ/",
                    partOfSpeech = "interjection",
                    definition = "used as a greeting or to begin a phone conversation",
                    example = "hello there, Katie!",
                    soundUrl = "https://example.com/hello.mp3",
                    isFavorite = false
                )
                "example" -> WordSearchResult(
                    word = "example",
                    ipa = "/ɪɡˈzæm.pəl/",
                    partOfSpeech = "noun",
                    definition = "a thing characteristic of its kind or illustrating a general rule",
                    example = "it is a good example of how European action can produce results",
                    soundUrl = "https://example.com/example.mp3",
                    isFavorite = false
                )
                "search" -> WordSearchResult(
                    word = "search",
                    ipa = "/sɜːrtʃ/",
                    partOfSpeech = "verb",
                    definition = "try to find something by looking or otherwise seeking carefully and thoroughly",
                    example = "I searched for the missing keys everywhere",
                    soundUrl = "https://example.com/search.mp3",
                    isFavorite = false
                )
                else -> {
                    // Generate a mock result for any other word
                    WordSearchResult(
                        word = word,
                        ipa = "/ˈwɜːrd/",
                        partOfSpeech = "noun",
                        definition = "A single distinct meaningful element of speech or writing",
                        example = "The word '$word' has a specific meaning",
                        soundUrl = "https://example.com/word.mp3",
                        isFavorite = false
                    )
                }
            }
        }
    }

    private fun playWordSound(soundUrl: String) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                // In a real app, you would load the actual audio URL
                // For now, we'll just show a toast
                Toast.makeText(this@WordSearchActivity, "Playing pronunciation...", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to play sound", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleFavorite(wordResult: WordSearchResult) {
        val newFavoriteState = !wordResult.isFavorite
        currentWordResult = wordResult.copy(isFavorite = newFavoriteState)
        updateFavoriteButton(newFavoriteState)

        if (newFavoriteState) {
            // Here you would save to favorites database
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        binding.btnFavorite.apply {
            setColorFilter(
                if (isFavorite) {
                    resources.getColor(android.R.color.holo_red_dark, null)
                } else {
                    resources.getColor(android.R.color.darker_gray, null)
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        searchJob?.cancel()
        mediaPlayer?.release()
    }

    companion object {
        fun createIntent(activity: android.app.Activity): Intent {
            return Intent(activity, WordSearchActivity::class.java)
        }
    }
}

