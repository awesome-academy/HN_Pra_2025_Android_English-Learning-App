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
        holder.bind(words[position])
    }

    override fun getItemCount(): Int = words.size

    fun updateWords(newWords: List<Word>, learnedWordIds: Set<String> = emptySet()) {
        // Sort: unlearned first, then learned
        this.learnedWordIds = learnedWordIds
        words = newWords.sortedBy { learnedWordIds.contains(it.id) }
        notifyDataSetChanged()
    }

    inner class VocabularyViewHolder(
        private val binding: ItemVocabularyCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(word: Word) {
            binding.apply {
                textWordName.text = word.word
                // Show learned indicator
                if (learnedWordIds.contains(word.id)) {
                    // Example: show a checkmark icon and change background color
                    imgLearned.visibility = android.view.View.VISIBLE
                    root.setBackgroundResource(com.sun.englishlearning.R.drawable.bg_learned_word)
                } else {
                    imgLearned.visibility = android.view.View.GONE
                    root.setBackgroundResource(com.sun.englishlearning.R.drawable.bg_vocabulary_card)
                }
                btnSound.setOnClickListener {
                    onSoundClick(word)
                }
                root.setOnClickListener {
                    onWordClick(word)
                }
            }
        }
    }
}
