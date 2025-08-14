package com.sun.englishlearning.screen.lessondetail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.databinding.ItemVocabularyCardBinding

class VocabularyAdapter(
    private var words: List<Word> = emptyList(),
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

    fun updateWords(newWords: List<Word>) {
        words = newWords
        notifyDataSetChanged()
    }

    inner class VocabularyViewHolder(
        private val binding: ItemVocabularyCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(word: Word) {
            binding.apply {
                // Set word name
                textWordName.text = word.name

                // Set first letter as initial
                textWordInitial.text = word.name.firstOrNull()?.toString()?.uppercase() ?: "?"

                // Set definition
                textDefinition.text = word.definition

                // Set example
                textExample.text = word.example

                // Sound button click listener
                btnSound.setOnClickListener {
                    onSoundClick(word)
                }

                // Card click listener
                root.setOnClickListener {
                    onWordClick(word)
                }
            }
        }
    }
}