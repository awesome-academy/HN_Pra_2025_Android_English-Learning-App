package com.sun.englishlearning.screen.search

import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.sun.englishlearning.data.model.WordSearchResult
import com.sun.englishlearning.data.model.SavedWord
import com.sun.englishlearning.data.model.WordType
import com.sun.englishlearning.data.repository.DictionaryRepository
import com.sun.englishlearning.data.repository.DictionaryRepositoryImpl
import com.sun.englishlearning.data.repository.SavedWordsRepository
import com.sun.englishlearning.data.repository.SavedWordsRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.sun.englishlearning.databinding.ActivityWordSearchBinding
import com.sun.englishlearning.utils.base.BaseActivity
import kotlinx.coroutines.*

class WordSearchActivity : BaseActivity<ActivityWordSearchBinding>() {

    private var searchJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentWordResult: WordSearchResult? = null
    private var isWordSaved: Boolean = false
    private val dictionaryRepository: DictionaryRepository = DictionaryRepositoryImpl()
    private val savedWordsRepository: SavedWordsRepository = SavedWordsRepositoryImpl()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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


        binding.btnSaveWord.setOnClickListener {
            currentWordResult?.let { toggleSaveWord(it) }
        }

        binding.btnClearSearch.setOnClickListener {
            binding.etSearchWord.text.clear()
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

        binding.etSearchWord.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnClearSearch.visibility = if (s?.isNotEmpty() == true) View.VISIBLE else View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        })
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
                    result.getOrNull()?.let { showSearchResult(it) } ?: showError()
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

        }
        
        // Check if word is already saved
        checkIfWordIsSaved(result.word)
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


    private fun checkIfWordIsSaved(word: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            updateSaveButton(false)
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val savedWordResult = savedWordsRepository.isWordSavedWithType(
                    currentUser.uid, 
                    word, 
                    WordType.SAVED
                )
                
                if (savedWordResult.isSuccess) {
                    val savedWord = savedWordResult.getOrNull()
                    isWordSaved = savedWord != null
                    updateSaveButton(isWordSaved)
                }
            } catch (e: Exception) {
                updateSaveButton(false)
            }
        }
    }

    private fun toggleSaveWord(wordResult: WordSearchResult) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login to save words", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                if (isWordSaved) {
                    // Delete the word
                    val deleteResult = savedWordsRepository.deleteWordByUserAndName(
                        currentUser.uid,
                        wordResult.word,
                        WordType.SAVED
                    )
                    
                    if (deleteResult.isSuccess) {
                        isWordSaved = false
                        updateSaveButton(false)
                        Toast.makeText(this@WordSearchActivity, "Word removed from saved list", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@WordSearchActivity, "Failed to remove word", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Save the word
                    val savedWord = SavedWord(
                        userId = currentUser.uid,
                        word = wordResult.word,
                        ipa = wordResult.ipa,
                        partOfSpeech = wordResult.partOfSpeech,
                        definition = wordResult.definition,
                        example = wordResult.example,
                        soundUrl = wordResult.soundUrl,
                        wordType = WordType.SAVED.value
                    )

                    val saveResult = savedWordsRepository.saveWord(savedWord)
                    if (saveResult.isSuccess) {
                        isWordSaved = true
                        updateSaveButton(true)
                        Toast.makeText(this@WordSearchActivity, "Word saved successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@WordSearchActivity, "Failed to save word", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@WordSearchActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSaveButton(isSaved: Boolean) {
        binding.btnSaveWord.apply {
            text = if (isSaved) "Remove Word" else "Save Word"
            setBackgroundColor(
                if (isSaved) {
                    ContextCompat.getColor(this@WordSearchActivity, android.R.color.holo_red_light)
                } else {
                    ContextCompat.getColor(this@WordSearchActivity, android.R.color.holo_blue_light)
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

