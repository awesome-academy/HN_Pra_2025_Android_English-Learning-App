package com.sun.englishlearning.screen.search

import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.sun.englishlearning.data.model.WordSearchResult
import com.sun.englishlearning.data.repository.DictionaryRepository
import com.sun.englishlearning.data.repository.DictionaryRepositoryImpl
import com.sun.englishlearning.databinding.ActivityWordSearchBinding
import com.sun.englishlearning.utils.base.BaseActivity
import kotlinx.coroutines.*

class WordSearchActivity : BaseActivity<ActivityWordSearchBinding>() {

    private var searchJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentWordResult: WordSearchResult? = null
    private val dictionaryRepository: DictionaryRepository = DictionaryRepositoryImpl()

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
                val result = dictionaryRepository.searchWord(query)
                if (result.isSuccess) {
                    showSearchResult(result.getOrNull()!!)
                } else {
                    showError()
                    Toast.makeText(this@WordSearchActivity, "Word not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                showError()
                Toast.makeText(this@WordSearchActivity, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
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


    private fun playWordSound(soundUrl: String) {
        if (soundUrl.isEmpty()) {
            Toast.makeText(this, "No audio available for this word", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setDataSource(soundUrl)
                setOnPreparedListener { 
                    start()
                    Toast.makeText(this@WordSearchActivity, "Playing pronunciation...", Toast.LENGTH_SHORT).show()
                }
                setOnErrorListener { _, _, _ ->
                    Toast.makeText(this@WordSearchActivity, "Unable to play audio", Toast.LENGTH_SHORT).show()
                    false
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to play sound: ${e.message}", Toast.LENGTH_SHORT).show()
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

