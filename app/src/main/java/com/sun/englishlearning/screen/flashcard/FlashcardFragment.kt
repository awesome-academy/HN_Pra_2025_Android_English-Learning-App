package com.sun.englishlearning.screen.flashcard

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.databinding.FragmentFlashcardBinding

class FlashcardFragment : Fragment() {

    companion object {
        private const val ARG_WORD = "arg_word"

        fun newInstance(word: Word): FlashcardFragment {
            return FlashcardFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_WORD, word)
                }
            }
        }
    }

    private var _binding: FragmentFlashcardBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var word: Word

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        word = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_WORD, Word::class.java) ?: Word()
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(ARG_WORD) ?: Word()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlashcardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWordData()
        setupClickListeners()
    }

    private fun setupWordData() {
        binding.apply {
            // Set word name
            textWordName.text = word.name
            textWordName.contentDescription = "English word: ${word.name}"

            // Set first letter as initial
            val initial = word.name.firstOrNull()?.toString()?.uppercase() ?: "?"
            textWordInitial.text = initial
            textWordInitial.contentDescription = "First letter: $initial"

            // Set definition
            val definition = if (word.definition.isNotEmpty()) {
                word.definition
            } else {
                "Definition not available"
            }
            textDefinition.text = definition
            textDefinition.contentDescription = "Definition: $definition"

            // Set example
            val example = if (word.example.isNotEmpty()) {
                word.example
            } else {
                "Example not available"
            }
            textExample.text = example
            textExample.contentDescription = "Example sentence: $example"

            // Set phonetic transcription
            if (word.phonetic.isNotEmpty()) {
                textPhonetic.text = word.phonetic
                textPhonetic.contentDescription = "Pronunciation: ${word.phonetic}"
                textPhonetic.visibility = View.VISIBLE
            } else {
                textPhonetic.visibility = View.GONE
            }

            // Set part of speech
            if (word.partOfSpeech.isNotEmpty()) {
                textPartOfSpeech.text = word.partOfSpeech
                textPartOfSpeech.contentDescription = "Part of speech: ${word.partOfSpeech}"
                textPartOfSpeech.visibility = View.VISIBLE
            } else {
                textPartOfSpeech.visibility = View.GONE
            }

            // Set audio button content description
            btnAudio.contentDescription = if (word.soundUrl.isNotEmpty()) {
                "Play pronunciation for ${word.name}"
            } else {
                "Audio not available for ${word.name}"
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnAudio.setOnClickListener {
            // Provide visual feedback
            it.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100)
                        .start()
                }
                .start()

            // Get parent activity and play audio
            (activity as? FlashcardActivity)?.playWordAudio(word)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
