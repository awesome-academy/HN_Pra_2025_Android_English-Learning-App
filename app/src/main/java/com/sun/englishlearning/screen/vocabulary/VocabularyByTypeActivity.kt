package com.sun.englishlearning.screen.vocabulary

import android.content.Context
import android.content.Intent
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
import com.sun.englishlearning.databinding.ActivityVocabularyByTypeBinding
import com.sun.englishlearning.screen.savedwords.SavedWordsAdapter
import com.sun.englishlearning.utils.base.BaseActivity
import kotlinx.coroutines.launch

class VocabularyByTypeActivity : BaseActivity<ActivityVocabularyByTypeBinding>() {

    private lateinit var wordsAdapter: SavedWordsAdapter
    private var allWords = mutableListOf<SavedWord>()
    private var filteredWords = mutableListOf<SavedWord>()
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
        setupSearchView()
    }

    override fun initData() {
        loadWords()
    }

    private fun setupExtras() {
        val wordTypeValue = intent.getIntExtra(EXTRA_WORD_TYPE, WordType.SAVED.value)
        wordType = WordType.values().find { it.value == wordTypeValue } ?: WordType.SAVED
        typeTitle = intent.getStringExtra(EXTRA_TYPE_TITLE) ?: getString(R.string.default_words_title)
    }

    private fun setupToolbar() {
        binding.apply {
            tvTitle.text = typeTitle
            btnBack.setOnClickListener {
                onBackPressed()
            }
        }
    }

    private fun setupRecyclerView() {
        wordsAdapter = SavedWordsAdapter(filteredWords) { savedWord, action ->
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
        filteredWords.clear()
        if (query.isEmpty()) {
            filteredWords.addAll(allWords)
        } else {
            val filtered = allWords.filter { savedWord ->
                savedWord.word.lowercase().contains(query.lowercase()) ||
                        savedWord.definition.lowercase().contains(query.lowercase())
            }
            filteredWords.addAll(filtered)
        }
        wordsAdapter.notifyDataSetChanged()
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
                        allWords.clear()
                        allWords.addAll(words)
                        
                        filteredWords.clear()
                        filteredWords.addAll(allWords)
                        
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
            
            when (wordType) {
                WordType.WEAK -> {
                    ivEmptyIcon.setImageResource(R.drawable.ic_book)
                    tvEmptyMessage.text = getString(R.string.lets_start_learning)
                    tvEmptySubtitle.text = getString(R.string.practice_lessons_to_build_vocabulary)
                }
                WordType.MEDIUM -> {
                    ivEmptyIcon.setImageResource(R.drawable.ic_book)
                    tvEmptyMessage.text = getString(R.string.lets_start_learning)
                    tvEmptySubtitle.text = getString(R.string.keep_practicing_to_strengthen_words)
                }
                WordType.STRONG -> {
                    ivEmptyIcon.setImageResource(R.drawable.ic_book)
                    tvEmptyMessage.text = getString(R.string.lets_start_learning)
                    tvEmptySubtitle.text = getString(R.string.master_words_through_practice)
                }
                WordType.SAVED -> {
                    ivEmptyIcon.setImageResource(R.drawable.ic_bookmark)
                    tvEmptyMessage.text = getString(R.string.lets_start_learning)
                    tvEmptySubtitle.text = getString(R.string.save_words_while_learning)
                }
                else -> {
                    ivEmptyIcon.setImageResource(R.drawable.ic_book)
                    tvEmptyMessage.text = getString(R.string.lets_start_learning)
                    tvEmptySubtitle.text = getString(R.string.complete_lessons_to_see_words)
                }
            }
        }
    }

    private fun playWordSound(soundUrl: String) {
        if (soundUrl.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_audio_available_for_word), Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setDataSource(soundUrl)
                setOnPreparedListener { 
                    start()
                    Toast.makeText(this@VocabularyByTypeActivity, getString(R.string.playing_pronunciation_ellipsis), Toast.LENGTH_SHORT).show()
                }
                setOnErrorListener { _, _, _ ->
                    Toast.makeText(this@VocabularyByTypeActivity, getString(R.string.unable_to_play_audio), Toast.LENGTH_SHORT).show()
                    false
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.unable_to_play_sound_format, e.message), Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeWord(savedWord: SavedWord) {
        lifecycleScope.launch {
            try {
                val result = savedWordsRepository.deleteWord(savedWord.id)
                if (result.isSuccess) {
                    // Remove from both lists
                    allWords.removeAll { it.id == savedWord.id }
                    filteredWords.removeAll { it.id == savedWord.id }
                    wordsAdapter.notifyDataSetChanged()
                    
                    Toast.makeText(this@VocabularyByTypeActivity, getString(R.string.word_removed_successfully), Toast.LENGTH_SHORT).show()
                    
                    if (filteredWords.isEmpty()) {
                        showEmptyState()
                    }
                } else {
                    Toast.makeText(this@VocabularyByTypeActivity, getString(R.string.failed_to_remove_word_generic), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@VocabularyByTypeActivity, getString(R.string.error_removing_word_format, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}