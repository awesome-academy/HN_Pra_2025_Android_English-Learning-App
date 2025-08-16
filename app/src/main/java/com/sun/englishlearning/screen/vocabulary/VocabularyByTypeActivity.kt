package com.sun.englishlearning.screen.vocabulary

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.sun.englishlearning.data.model.SavedWord
import com.sun.englishlearning.data.model.WordType
import com.sun.englishlearning.data.repository.SavedWordsRepository
import com.sun.englishlearning.data.repository.SavedWordsRepositoryImpl
import com.sun.englishlearning.databinding.ActivityVocabularyByTypeBinding
import com.sun.englishlearning.screen.savedwords.SavedWordsAdapter
import com.sun.englishlearning.utils.base.BaseActivity
import kotlinx.coroutines.launch

class VocabularyByTypeActivity : BaseActivity<ActivityVocabularyByTypeBinding>() {

    private lateinit var wordsAdapter: SavedWordsAdapter
    private var wordsList = mutableListOf<SavedWord>()
    private val savedWordsRepository: SavedWordsRepository = SavedWordsRepositoryImpl()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mediaPlayer: MediaPlayer? = null

    private lateinit var wordType: WordType
    private lateinit var typeTitle: String

    companion object {
        private const val EXTRA_WORD_TYPE = "extra_word_type"
        private const val EXTRA_TYPE_TITLE = "extra_type_title"

        fun createIntent(context: Context, wordType: WordType, typeTitle: String): Intent {
            return Intent(context, VocabularyByTypeActivity::class.java).apply {
                putExtra(EXTRA_WORD_TYPE, wordType.value)
                putExtra(EXTRA_TYPE_TITLE, typeTitle)
            }
        }
    }

    override fun inflateBinding(inflater: LayoutInflater): ActivityVocabularyByTypeBinding {
        return ActivityVocabularyByTypeBinding.inflate(inflater)
    }

    override fun initView() {
        setupExtras()
        setupToolbar()
        setupRecyclerView()
    }

    override fun initData() {
        loadWords()
    }

    private fun setupExtras() {
        val wordTypeValue = intent.getIntExtra(EXTRA_WORD_TYPE, WordType.SAVED.value)
        wordType = WordType.values().find { it.value == wordTypeValue } ?: WordType.SAVED
        typeTitle = intent.getStringExtra(EXTRA_TYPE_TITLE) ?: "Words"
    }

    private fun setupToolbar() {
        binding.apply {
            tvTitle.text = typeTitle
            btnBack.setOnClickListener {
                finish()
            }
        }
    }

    private fun setupRecyclerView() {
        wordsAdapter = SavedWordsAdapter(wordsList) { savedWord, action ->
            when (action) {
                SavedWordsAdapter.Action.PLAY_SOUND -> {
                    playWordSound(savedWord.soundUrl)
                }
                SavedWordsAdapter.Action.REMOVE_WORD -> {
                    removeWord(savedWord)
                }
            }
        }

        binding.rvWords.apply {
            layoutManager = LinearLayoutManager(this@VocabularyByTypeActivity)
            adapter = wordsAdapter
        }
    }

    private fun loadWords() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            showEmptyState()
            return
        }

        binding.progressLoading.visibility = View.VISIBLE
        binding.rvWords.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val result = savedWordsRepository.getUserWordsByType(currentUser.uid, wordType)
                if (result.isSuccess) {
                    val words = result.getOrNull() ?: emptyList()
                    if (words.isNotEmpty()) {
                        wordsList.clear()
                        wordsList.addAll(words)
                        wordsAdapter.notifyDataSetChanged()
                        showWordsList()
                    } else {
                        showEmptyState()
                    }
                } else {
                    showEmptyState()
                }
            } catch (e: Exception) {
                showEmptyState()
            }
        }
    }

    private fun showWordsList() {
        binding.apply {
            progressLoading.visibility = View.GONE
            rvWords.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
        }
    }

    private fun showEmptyState() {
        binding.apply {
            progressLoading.visibility = View.GONE
            rvWords.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
            tvEmptyMessage.text = "No $typeTitle found"
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
                    Toast.makeText(this@VocabularyByTypeActivity, "Playing pronunciation...", Toast.LENGTH_SHORT).show()
                }
                setOnErrorListener { _, _, _ ->
                    Toast.makeText(this@VocabularyByTypeActivity, "Unable to play audio", Toast.LENGTH_SHORT).show()
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
                    // Remove from local list
                    wordsList.removeAll { it.id == savedWord.id }
                    wordsAdapter.notifyDataSetChanged()
                    
                    Toast.makeText(this@VocabularyByTypeActivity, "Word removed successfully", Toast.LENGTH_SHORT).show()
                    
                    if (wordsList.isEmpty()) {
                        showEmptyState()
                    }
                } else {
                    Toast.makeText(this@VocabularyByTypeActivity, "Failed to remove word", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@VocabularyByTypeActivity, "Error removing word: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}