package com.sun.englishlearning.screen.savedwords

import android.app.AlertDialog
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.sun.englishlearning.R
import com.sun.englishlearning.data.model.SavedWord
import com.sun.englishlearning.data.model.WordType
import com.sun.englishlearning.data.repository.SavedWordsRepository
import com.sun.englishlearning.data.repository.SavedWordsRepositoryImpl
import com.sun.englishlearning.databinding.ActivitySavedWordsBinding
import com.sun.englishlearning.utils.base.BaseActivity
import kotlinx.coroutines.launch

class SavedWordsActivity : BaseActivity<ActivitySavedWordsBinding>() {

    private lateinit var savedWordsAdapter: SavedWordsAdapter
    private var allSavedWords = mutableListOf<SavedWord>()
    private var filteredSavedWords = mutableListOf<SavedWord>()
    
    private val savedWordsRepository: SavedWordsRepository = SavedWordsRepositoryImpl()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mediaPlayer: MediaPlayer? = null

    override fun inflateBinding(inflater: LayoutInflater): ActivitySavedWordsBinding {
        return ActivitySavedWordsBinding.inflate(inflater)
    }

    override fun initView() {
        setupRecyclerView()
        setupClickListeners()
        setupSearchView()
    }

    override fun initData() {
        loadSavedWords()
    }

    private fun setupRecyclerView() {
        savedWordsAdapter = SavedWordsAdapter(filteredSavedWords) { savedWord, action ->
            when (action) {
                SavedWordsAdapter.Action.PLAY_SOUND -> {
                    playWordSound(savedWord.soundUrl)
                }
                SavedWordsAdapter.Action.REMOVE_WORD -> {
                    removeWord(savedWord)
                }
                SavedWordsAdapter.Action.VIEW_DETAILS -> {
                    showWordDetailsDialog(savedWord)
                }
            }
        }

        binding.rvSavedWords.apply {
            layoutManager = LinearLayoutManager(this@SavedWordsActivity)
            adapter = savedWordsAdapter
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnBack.setOnClickListener {
                onBackPressed()
            }
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
            hideAllEmptyStates()
            if (allSavedWords.isEmpty()) {
                showEmptyState()
            } else {
                binding.rvSavedWords.visibility = View.VISIBLE
            }
        } else {
            val filtered = allSavedWords.filter { savedWord ->
                savedWord.word.lowercase().contains(query.lowercase()) ||
                        savedWord.definition.lowercase().contains(query.lowercase())
            }
            filteredSavedWords.addAll(filtered)
            
            if (filtered.isEmpty()) {
                showEmptySearchState(query)
            } else {
                hideAllEmptyStates()
                binding.rvSavedWords.visibility = View.VISIBLE
            }
        }
        savedWordsAdapter.notifyDataSetChanged()
    }

    private fun loadSavedWords() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login to view saved words", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        showLoading()

        lifecycleScope.launch {
            try {
                // Get words with type SAVED (type 1)
                val result = savedWordsRepository.getUserWordsByType(currentUser.uid, WordType.SAVED)
                
                if (result.isSuccess) {
                    val savedWords = result.getOrNull() ?: emptyList()
                    allSavedWords.clear()
                    allSavedWords.addAll(savedWords)
                    
                    filteredSavedWords.clear()
                    filteredSavedWords.addAll(allSavedWords)
                    
                    savedWordsAdapter.notifyDataSetChanged()
                    hideLoading()
                    
                    if (savedWords.isEmpty()) {
                        showEmptyState()
                    } else {
                        binding.rvSavedWords.visibility = View.VISIBLE
                        hideAllEmptyStates()
                    }
                } else {
                    hideLoading()
                    showEmptyState()
                    Toast.makeText(this@SavedWordsActivity, "Failed to load saved words", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                hideLoading()
                showEmptyState()
                Toast.makeText(this@SavedWordsActivity, "Error loading saved words: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading() {
        // You can add a loading indicator if the layout has one
    }

    private fun hideLoading() {
        // Hide loading indicator
    }

    private fun showEmptyState() {
        binding.rvSavedWords.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.layoutEmptySearch.visibility = View.GONE
    }

    private fun showEmptySearchState(query: String) {
        binding.rvSavedWords.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.GONE
        binding.layoutEmptySearch.visibility = View.VISIBLE
        binding.tvSearchMessage.text = "The word \"$query\" is not in your saved list"
    }

    private fun hideAllEmptyStates() {
        binding.layoutEmptyState.visibility = View.GONE
        binding.layoutEmptySearch.visibility = View.GONE
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
                    Toast.makeText(this@SavedWordsActivity, "Playing pronunciation...", Toast.LENGTH_SHORT).show()
                }
                setOnErrorListener { _, _, _ ->
                    Toast.makeText(this@SavedWordsActivity, "Unable to play audio", Toast.LENGTH_SHORT).show()
                    false
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to play sound: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeWord(savedWord: SavedWord) {
        lifecycleScope.launch {
            try {
                val result = savedWordsRepository.deleteWord(savedWord.id)
                if (result.isSuccess) {
                    // Remove from local lists
                    allSavedWords.removeAll { it.id == savedWord.id }
                    filteredSavedWords.removeAll { it.id == savedWord.id }
                    savedWordsAdapter.notifyDataSetChanged()
                    
                    Toast.makeText(this@SavedWordsActivity, "Word removed from saved list", Toast.LENGTH_SHORT).show()
                    
                    if (filteredSavedWords.isEmpty()) {
                        val currentQuery = binding.etSearch.text.toString()
                        if (currentQuery.isNotEmpty()) {
                            showEmptySearchState(currentQuery)
                        } else if (allSavedWords.isEmpty()) {
                            showEmptyState()
                        }
                    }
                } else {
                    Toast.makeText(this@SavedWordsActivity, "Failed to remove word", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SavedWordsActivity, "Error removing word: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showWordDetailsDialog(savedWord: SavedWord) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_word_details, null)
        
        dialogView.findViewById<android.widget.TextView>(R.id.tv_word_popup).text = savedWord.word
        dialogView.findViewById<android.widget.TextView>(R.id.tv_part_of_speech_popup).text = savedWord.partOfSpeech.ifEmpty { "N/A" }
        dialogView.findViewById<android.widget.TextView>(R.id.tv_definition_popup).text = savedWord.definition
        
        val tvExample = dialogView.findViewById<android.widget.TextView>(R.id.tv_example_popup)
        if (savedWord.example.isNotEmpty()) {
            tvExample.text = getString(R.string.example_format, savedWord.example)
            tvExample.visibility = View.VISIBLE
        } else {
            tvExample.visibility = View.GONE
        }
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        dialogView.findViewById<android.widget.ImageView>(R.id.iv_sound_popup).setOnClickListener {
            playWordSound(savedWord.soundUrl)
        }
        
        dialogView.findViewById<android.widget.Button>(R.id.btn_close_popup).setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}


