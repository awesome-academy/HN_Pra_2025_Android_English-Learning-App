package com.sun.englishlearning.screen.savedwords

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sun.englishlearning.data.model.SavedWord
import com.sun.englishlearning.databinding.ItemSavedWordBinding

class SavedWordsAdapter(
    private val savedWords: List<SavedWord>,
    private val onAction: (SavedWord, Action) -> Unit
) : RecyclerView.Adapter<SavedWordsAdapter.SavedWordViewHolder>() {

    enum class Action {
        PLAY_SOUND,
        TOGGLE_FAVORITE
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
                tvDefinition.text = savedWord.definition
                
                ivSound.setOnClickListener {
                    onAction(savedWord, Action.PLAY_SOUND)
                }
                
                ivFavorite.setOnClickListener {
                    onAction(savedWord, Action.TOGGLE_FAVORITE)
                }
                
                // TODO: Set favorite visual state when isFavorite property is added to SavedWord model
                ivFavorite.alpha = 0.5f
            }
        }
    }
}




