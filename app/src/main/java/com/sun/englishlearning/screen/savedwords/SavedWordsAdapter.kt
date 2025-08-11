package com.sun.englishlearning.screen.savedwords

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sun.englishlearning.databinding.ItemSavedWordBinding
import com.sun.englishlearning.data.model.SavedWord

class SavedWordsAdapter(private val onItemClick: (SavedWord) -> Unit) : 
    RecyclerView.Adapter<SavedWordsAdapter.SavedWordViewHolder>() {

    private var savedWords = mutableListOf<SavedWord>()

    fun updateWords(newWords: List<SavedWord>) {
        savedWords.clear()
        savedWords.addAll(newWords)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedWordViewHolder {
        val binding = ItemSavedWordBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return SavedWordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SavedWordViewHolder, position: Int) {
        holder.bind(savedWords[position])
    }

    override fun getItemCount(): Int = savedWords.size

    inner class SavedWordViewHolder(private val binding: ItemSavedWordBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        fun bind(savedWord: SavedWord) {
            binding.apply {
                tvWord.text = savedWord.word
                tvTranslation.text = savedWord.translation
                
                ivAudio.setOnClickListener {
                    // Handle audio play
                }
                
                ivBookmark.setOnClickListener {
                    // Handle bookmark toggle
                }
                
                ivMore.setOnClickListener {
                    // Handle more options
                }
                
                root.setOnClickListener {
                    onItemClick(savedWord)
                }
            }
        }
    }
}
