package com.sun.englishlearning.screen.vocabulary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.sun.englishlearning.data.model.VocabularyWord
import com.sun.englishlearning.data.model.WordType
import com.sun.englishlearning.databinding.ActivityVocabularyWordsBinding
import com.sun.englishlearning.utils.base.BaseActivity

class VocabularyWordsActivity : BaseActivity<ActivityVocabularyWordsBinding>() {

    private lateinit var adapter: VocabularyWordsAdapter
    private lateinit var wordType: WordType
    private var allWords = listOf<VocabularyWord>()
    private var filteredWords = listOf<VocabularyWord>()

    companion object {
        const val EXTRA_WORD_TYPE = "word_type"
        const val EXTRA_TITLE = "title"

        fun createIntent(context: Context, wordType: WordType, title: String): Intent {
            return Intent(context, VocabularyWordsActivity::class.java).apply {
                putExtra(EXTRA_WORD_TYPE, wordType.name)
                putExtra(EXTRA_TITLE, title)
            }
        }
    }

    override fun inflateBinding(inflater: LayoutInflater): ActivityVocabularyWordsBinding {
        return ActivityVocabularyWordsBinding.inflate(inflater)
    }

    override fun initView() {
        // Get data from intent
        val wordTypeString = intent.getStringExtra(EXTRA_WORD_TYPE) ?: WordType.WEAK.name
        wordType = WordType.valueOf(wordTypeString)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Words"
        
        binding.tvTitle.text = title

        // Setup RecyclerView
        adapter = VocabularyWordsAdapter(
            onAudioClick = { word -> playAudio(word) },
            onBookmarkClick = { word -> toggleBookmark(word) },
            onMoreClick = { word -> showMoreOptions(word) }
        )
        
        binding.rvWords.layoutManager = LinearLayoutManager(this)
        binding.rvWords.adapter = adapter

        // Setup search
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterWords(s.toString())
            }
        })

        // Back button
        binding.ivBack.setOnClickListener { finish() }
    }

    override fun initData() {
        loadWords()
    }

    private fun loadWords() {
        // Generate seed data based on word type
        allWords = generateSeedData(wordType)
        filteredWords = allWords
        
        updateUI()
    }

    private fun generateSeedData(type: WordType): List<VocabularyWord> {
        return when (type) {
            WordType.WEAK -> listOf(
                VocabularyWord("1", "City break", "City tourist", "/ˈsɪti breɪk/", WordType.WEAK, true),
                VocabularyWord("2", "Cosmopolitan", "Cosmopolitan", "/ˌkɒzməˈpɒlɪtən/", WordType.WEAK, true),
                VocabularyWord("3", "Crowded", "Crowded", "/ˈkraʊdɪd/", WordType.WEAK, true),
                VocabularyWord("4", "Embassy", "Embassy", "/ˈembəsi/", WordType.WEAK, false),
                VocabularyWord("5", "Gateway", "Gateway", "/ˈɡeɪtweɪ/", WordType.WEAK, false)
            )
            WordType.TODAY -> listOf(
                VocabularyWord("6", "Disappoint", "Disappointing", "/ˌdɪsəˈpɔɪnt/", WordType.TODAY, true),
                VocabularyWord("7", "Jaw-dropping", "Jaw-dropping", "/ˈdʒɔː drɒpɪŋ/", WordType.TODAY, false),
                VocabularyWord("8", "Lively", "Lively", "/ˈlaɪvli/", WordType.TODAY, true)
            )
            WordType.MEDIUM -> listOf(
                VocabularyWord("9", "Adventure", "Adventure", "/ədˈventʃər/", WordType.MEDIUM, false),
                VocabularyWord("10", "Experience", "Experience", "/ɪkˈspɪəriəns/", WordType.MEDIUM, true),
                VocabularyWord("11", "Journey", "Journey", "/ˈdʒɜːrni/", WordType.MEDIUM, false),
                VocabularyWord("12", "Explore", "Explore", "/ɪkˈsplɔːr/", WordType.MEDIUM, true)
            )
            WordType.STRONG -> listOf(
                VocabularyWord("13", "Excellent", "Excellent", "/ˈeksələnt/", WordType.STRONG, false),
                VocabularyWord("14", "Perfect", "Perfect", "/ˈpɜːrfɪkt/", WordType.STRONG, true),
                VocabularyWord("15", "Amazing", "Amazing", "/əˈmeɪzɪŋ/", WordType.STRONG, false),
                VocabularyWord("16", "Outstanding", "Outstanding", "/aʊtˈstændɪŋ/", WordType.STRONG, true),
                VocabularyWord("17", "Remarkable", "Remarkable", "/rɪˈmɑːrkəbl/", WordType.STRONG, false)
            )
        }
    }

    private fun filterWords(query: String) {
        filteredWords = if (query.isEmpty()) {
            allWords
        } else {
            allWords.filter { word ->
                word.word.contains(query, ignoreCase = true) ||
                word.meaning.contains(query, ignoreCase = true)
            }
        }
        updateUI()
    }

    private fun updateUI() {
        if (filteredWords.isEmpty()) {
            binding.rvWords.visibility = View.GONE
            binding.llNoData.visibility = View.VISIBLE
        } else {
            binding.rvWords.visibility = View.VISIBLE
            binding.llNoData.visibility = View.GONE
            adapter.updateWords(filteredWords)
        }
    }

    private fun playAudio(word: VocabularyWord) {
        Toast.makeText(this, "Playing audio for: ${word.word}", Toast.LENGTH_SHORT).show()
        // TODO: Implement audio playback
    }

    private fun toggleBookmark(word: VocabularyWord) {
        val updatedWord = word.copy(isBookmarked = !word.isBookmarked)
        val index = allWords.indexOfFirst { it.id == word.id }
        if (index != -1) {
            allWords = allWords.toMutableList().apply { set(index, updatedWord) }
            filterWords(binding.etSearch.text.toString())
        }
        Toast.makeText(
            this, 
            if (updatedWord.isBookmarked) "Added to bookmarks" else "Removed from bookmarks", 
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showMoreOptions(word: VocabularyWord) {
        Toast.makeText(this, "More options for: ${word.word}", Toast.LENGTH_SHORT).show()
        // TODO: Implement more options menu
    }
}
