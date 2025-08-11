package com.sun.englishlearning.screen.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.sun.englishlearning.data.model.WordSearchResult
import com.sun.englishlearning.data.repository.FastApiDictionaryRepository
import com.sun.englishlearning.databinding.ActivityWordSearchBinding
import com.sun.englishlearning.utils.AudioPlayer
import com.sun.englishlearning.utils.base.BaseActivity
import kotlinx.coroutines.launch

class WordSearchActivity : BaseActivity<ActivityWordSearchBinding>() {

    private lateinit var dictionaryRepository: FastApiDictionaryRepository
    private lateinit var audioPlayer: AudioPlayer
    private var currentSearchResult: WordSearchResult? = null

    override fun inflateBinding(inflater: LayoutInflater): ActivityWordSearchBinding {
        return ActivityWordSearchBinding.inflate(inflater)
    }

    override fun initView() {
        setupClickListeners()
        setupSearchInput()
    }

    override fun initData() {
        audioPlayer = AudioPlayer(this)
        dictionaryRepository = FastApiDictionaryRepository(this)
        showInitialState()
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }
        
        binding.btnSearch.setOnClickListener {
            val word = binding.etSearch.text.toString().trim()
            if (word.isNotEmpty()) {
                searchWord(word)
            }
        }
        
        binding.ivAudio.setOnClickListener {
            currentSearchResult?.audioUrl?.let { audioUrl ->
                audioPlayer.playAudio(audioUrl)
            } ?: run {
                Toast.makeText(this, "No audio available", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.btnAddWord.setOnClickListener {
            currentSearchResult?.let { result ->
                addWordToVocabulary(result)
            }
        }
    }

    private fun setupSearchInput() {
        binding.etSearch.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val word = binding.etSearch.text.toString().trim()
                if (word.isNotEmpty()) {
                    searchWord(word)
                }
                true
            } else {
                false
            }
        }
    }

    private fun searchWord(word: String) {
        showLoading(word)
        
        lifecycleScope.launch {
            val result = dictionaryRepository.searchWord(word)
            
            result.onSuccess { wordResult ->
                showSearchResult(wordResult)
            }.onFailure { exception ->
                showError(exception.message ?: "Word not found")
            }
        }
    }

    private fun showInitialState() {
        binding.apply {
            llInitial.visibility = View.VISIBLE
            llLoading.visibility = View.GONE
            svResult.visibility = View.GONE
            llError.visibility = View.GONE
        }
    }

    private fun showLoading(word: String = "") {
        binding.apply {
            llInitial.visibility = View.GONE
            llLoading.visibility = View.VISIBLE
            svResult.visibility = View.GONE
            llError.visibility = View.GONE
            
            tvLoadingMessage.text = if (word.isNotEmpty()) {
                "Searching for \"$word\"..."
            } else {
                "Searching dictionary..."
            }
        }
    }

    private fun showSearchResult(result: WordSearchResult) {
        currentSearchResult = result
        
        binding.apply {
            llInitial.visibility = View.GONE
            llLoading.visibility = View.GONE
            svResult.visibility = View.VISIBLE
            llError.visibility = View.GONE

            // Populate result data
            tvWord.text = result.word.replaceFirstChar { it.uppercase() }
            tvPhonetic.text = result.phonetic
            tvPartOfSpeech.text = result.partOfSpeech
            tvDefinition.text = result.definition

            // Show/hide example
            if (!result.example.isNullOrEmpty()) {
                llExample.visibility = View.VISIBLE
                tvExample.text = "\"${result.example}\""
            } else {
                llExample.visibility = View.GONE
            }

            // Show/hide audio button based on availability
            val hasAudio = !result.audioUrl.isNullOrEmpty()
            ivAudio.visibility = if (hasAudio) View.VISIBLE else View.GONE
        }
    }

    private fun showError(message: String) {
        binding.apply {
            llInitial.visibility = View.GONE
            llLoading.visibility = View.GONE
            svResult.visibility = View.GONE
            llError.visibility = View.VISIBLE
            
            tvError.text = message
        }
    }
    
    private fun addWordToVocabulary(result: WordSearchResult) {
        // TODO: When user vocabulary API is available, save to backend
        // For now, just show success message
        Toast.makeText(this, "\"${result.word}\" added to your words!", Toast.LENGTH_LONG).show()
        
        // Optionally, you could save to local storage/database here
        // and sync with API later
    }
    
    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.release()
    }
}
