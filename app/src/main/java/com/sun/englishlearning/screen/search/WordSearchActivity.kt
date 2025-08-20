package com.sun.englishlearning.screen.search

import android.media.AudioManager
import android.media.MediaPlayer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.sun.englishlearning.R
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.model.SavedWord
import com.sun.englishlearning.data.model.WordType
import com.sun.englishlearning.data.repository.SavedWordsRepository
import com.sun.englishlearning.data.repository.SavedWordsRepositoryImpl
import com.sun.englishlearning.data.repository.WordRepository
import com.sun.englishlearning.data.repository.source.local.WordLocalDataSource
import com.sun.englishlearning.data.repository.source.remote.WordRemoteDataSource
import com.sun.englishlearning.data.repository.source.remote.OnResultListener
import com.google.firebase.auth.FirebaseAuth
import com.sun.englishlearning.databinding.ActivityWordSearchBinding
import com.sun.englishlearning.utils.base.BaseActivity
import com.sun.englishlearning.utils.DialogUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WordSearchActivity : BaseActivity<ActivityWordSearchBinding>() {

    private var mediaPlayer: MediaPlayer? = null
    private var currentWord: Word? = null
    private var isWordSaved: Boolean = false
    private val savedWordsRepository: SavedWordsRepository = SavedWordsRepositoryImpl()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val wordRepository: WordRepository by lazy {
        WordRepository.getInstance(
            WordRemoteDataSource.getInstance(),
            WordLocalDataSource.getInstance()
        )
    }

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
            onBackPressed()
        }

        binding.btnSearch.setOnClickListener {
            performSearch()
        }

        binding.btnPlaySound.setOnClickListener {
            currentWord?.let { playWordSound(it) }
        }

        binding.btnSaveWord.setOnClickListener {
            currentWord?.let { toggleSaveWord(it) }
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
            DialogUtils.showErrorDialog(
                context = this,
                message = getString(R.string.please_enter_word_to_search)
            )
            return
        }

        showLoading()

        wordRepository.getWords(query, object : OnResultListener<MutableList<Word>> {
            override fun onSuccess(data: MutableList<Word>) {
                if (data.isNotEmpty()) {
                    showSearchResult(data[0])
                } else {
                    showError()
                    Toast.makeText(this@WordSearchActivity, getString(R.string.word_not_found), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(exception: Exception?) {
                showError()
                Toast.makeText(this@WordSearchActivity, getString(R.string.network_error_try_again), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showLoading() {
        binding.apply {
            progressLoading.visibility = View.VISIBLE
            cardSearchResult.visibility = View.GONE
            layoutEmptyState.visibility = View.GONE
            layoutErrorState.visibility = View.GONE
        }
    }

    private fun showSearchResult(word: Word) {
        currentWord = word
        binding.apply {
            progressLoading.visibility = View.GONE
            cardSearchResult.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
            layoutErrorState.visibility = View.GONE

            tvWordTitle.text = word.word
            tvIpa.text = word.phonetic

            // Get part of speech from meanings
            val partOfSpeech = word.meanings.firstOrNull()?.partOfSpeech ?: ""
            tvPartOfSpeech.text = partOfSpeech

            // Get definition from meanings
            val definition = word.meanings.firstOrNull()?.definitions?.firstOrNull()?.definition ?: ""
            tvDefinition.text = definition
        }
        
        // Check if word is already saved
        checkIfWordIsSaved(word.word)
    }

    private fun showError() {
        binding.apply {
            progressLoading.visibility = View.GONE
            cardSearchResult.visibility = View.GONE
            layoutEmptyState.visibility = View.GONE
            layoutErrorState.visibility = View.VISIBLE
        }
    }

    private fun playWordSound(word: Word) {
        val soundUrl = word.phonetics.firstOrNull()?.audio ?: ""
        if (soundUrl.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_audio_available), Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setDataSource(soundUrl)
                setOnPreparedListener { 
                    start()
                    Toast.makeText(this@WordSearchActivity, getString(R.string.playing_pronunciation), Toast.LENGTH_SHORT).show()
                }
                setOnErrorListener { _, _, _ ->
                    Toast.makeText(this@WordSearchActivity, getString(R.string.unable_to_play_audio), Toast.LENGTH_SHORT).show()
                    false
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            DialogUtils.showErrorDialog(
                context = this,
                message = getString(R.string.unable_to_play_sound)
            )
            
            Toast.makeText(this, getString(R.string.unable_to_play_sound), Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkIfWordIsSaved(word: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            updateSaveButton(false)
            return
        }

        // Using coroutines for async operation
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

    private fun toggleSaveWord(word: Word) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, getString(R.string.please_login_to_save_words), Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                if (isWordSaved) {
                    // Delete the word
                    val deleteResult = savedWordsRepository.deleteWordByUserAndName(
                        currentUser.uid,
                        word.word,
                        WordType.SAVED
                    )
                    
                    if (deleteResult.isSuccess) {
                        isWordSaved = false
                        updateSaveButton(false)
                        Toast.makeText(this@WordSearchActivity, getString(R.string.word_removed_from_saved_list), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@WordSearchActivity, getString(R.string.failed_to_remove_word), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Save the word
                    val partOfSpeech = word.meanings.firstOrNull()?.partOfSpeech ?: ""
                    val definition = word.meanings.firstOrNull()?.definitions?.firstOrNull()?.definition ?: ""
                    val example = word.meanings.firstOrNull()?.definitions?.firstOrNull()?.example ?: ""
                    val soundUrl = word.phonetics.firstOrNull()?.audio ?: ""

                    val savedWord = SavedWord(
                        userId = currentUser.uid,
                        word = word.word,
                        ipa = word.phonetic,
                        partOfSpeech = partOfSpeech,
                        definition = definition,
                        example = example,
                        soundUrl = soundUrl,
                        wordType = WordType.SAVED.value
                    )

                    val saveResult = savedWordsRepository.saveWord(savedWord)
                    if (saveResult.isSuccess) {
                        isWordSaved = true
                        updateSaveButton(true)
                        Toast.makeText(this@WordSearchActivity, getString(R.string.word_saved_successfully), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@WordSearchActivity, getString(R.string.failed_to_save_word), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@WordSearchActivity, getString(R.string.error_format, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSaveButton(isSaved: Boolean) {
        binding.btnSaveWord.apply {
            text = if (isSaved) getString(R.string.remove_word) else getString(R.string.save_word)
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
        mediaPlayer?.release()
    }
}
