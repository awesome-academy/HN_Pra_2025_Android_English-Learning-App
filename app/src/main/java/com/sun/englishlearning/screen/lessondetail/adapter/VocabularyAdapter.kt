package com.sun.englishlearning.screen.lessondetail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.databinding.ItemVocabularyCardBinding

class VocabularyAdapter(
    private var words: List<Word> = emptyList(),
    private var learnedWordIds: Set<String> = emptySet(),
    private val onWordClick: (Word) -> Unit = {},
    private val onSoundClick: (Word) -> Unit = {}
) : RecyclerView.Adapter<VocabularyAdapter.VocabularyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocabularyViewHolder {
        val binding = ItemVocabularyCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VocabularyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VocabularyViewHolder, position: Int) {
        // Add defensive null checks and bounds validation
        if (position >= 0 && position < words.size) {
            holder.bind(words[position])
        }
    }

    override fun getItemCount(): Int = words.size

    fun updateWords(newWords: List<Word>?, learnedWordIds: Set<String>? = emptySet()) {
        try {
            // Defensive null checks
            val safeWords = newWords ?: emptyList()
            val safeLearnedIds = learnedWordIds ?: emptySet()

            // Sort: unlearned first, then learned
            this.learnedWordIds = safeLearnedIds
            val oldSize = words.size
            words = safeWords.sortedBy { safeLearnedIds.contains(it.id) }

            // Use more efficient notify methods when possible
            if (oldSize == 0 && words.isNotEmpty()) {
                notifyItemRangeInserted(0, words.size)
            } else if (oldSize > 0 && words.isEmpty()) {
                notifyItemRangeRemoved(0, oldSize)
            } else {
                notifyDataSetChanged() // Fallback for complex changes
            }
        } catch (e: Exception) {
            // Log error and fallback to empty list
            android.util.Log.e("VocabularyAdapter", "Error updating words", e)
            val oldSize = words.size
            words = emptyList()
            this.learnedWordIds = emptySet()
            if (oldSize > 0) {
                notifyItemRangeRemoved(0, oldSize)
            }
        }
    }

    inner class VocabularyViewHolder(
        private val binding: ItemVocabularyCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(word: Word) {
            try {
                binding.apply {
                    // Safe text assignment with null checks
                    textWordName.text = word.word.takeIf { it.isNotEmpty() } ?: "Unknown Word"

                    // Show learned indicator with null safety
                    val wordId = word.id.takeIf { it.isNotEmpty() } ?: word.word
                    if (learnedWordIds.contains(wordId)) {
                        imgLearned.visibility = android.view.View.VISIBLE
                        root.setBackgroundResource(com.sun.englishlearning.R.drawable.bg_learned_word)
                    } else {
                        imgLearned.visibility = android.view.View.GONE
                        root.setBackgroundResource(com.sun.englishlearning.R.drawable.bg_vocabulary_card)
                    }

                    // Sound button with safe click handling
                    btnSound.setOnClickListener {
                        try {
                            onSoundClick(word)
                        } catch (e: Exception) {
                            android.util.Log.e("VocabularyAdapter", "Error playing sound", e)
                        }
                    }

                    // Root click with safe handling
                    root.setOnClickListener {
                        try {
                            onWordClick(word)
                        } catch (e: Exception) {
                            android.util.Log.e("VocabularyAdapter", "Error handling word click", e)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("VocabularyAdapter", "Error binding word data", e)
            }
        }
    }
}
