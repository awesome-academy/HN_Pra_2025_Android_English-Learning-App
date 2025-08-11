package com.sun.englishlearning.screen.vocabulary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sun.englishlearning.data.model.VocabularyWord
import com.sun.englishlearning.databinding.ItemVocabularyWordBinding

class VocabularyWordsAdapter(
    private var words: List<VocabularyWord> = emptyList(),
    private val onAudioClick: (VocabularyWord) -> Unit = {},
    private val onBookmarkClick: (VocabularyWord) -> Unit = {},
    private val onMoreClick: (VocabularyWord) -> Unit = {}
) : RecyclerView.Adapter<VocabularyWordsAdapter.WordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val binding = ItemVocabularyWordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(words[position])
    }

    override fun getItemCount(): Int = words.size

    fun updateWords(newWords: List<VocabularyWord>) {
        words = newWords
        notifyDataSetChanged()
    }

    inner class WordViewHolder(private val binding: ItemVocabularyWordBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        fun bind(word: VocabularyWord) {
            binding.tvWord.text = word.word
            binding.tvMeaning.text = word.meaning

            // Set bookmark state
            binding.ivBookmark.setImageResource(
                if (word.isBookmarked) {
                    com.sun.englishlearning.R.drawable.ic_bookmark_red
                } else {
                    com.sun.englishlearning.R.drawable.ic_bookmark_outline
                }
            )

            // Click listeners
            binding.ivAudio.setOnClickListener { onAudioClick(word) }
            binding.ivBookmark.setOnClickListener { onBookmarkClick(word) }
            binding.ivMore.setOnClickListener { onMoreClick(word) }
        }
    }
}
