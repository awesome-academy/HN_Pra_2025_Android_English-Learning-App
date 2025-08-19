package com.sun.englishlearning.screen.testflashcard.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.screen.testflashcard.TestFlashcardFragment

class TestFlashcardAdapter(
    fragmentActivity: FragmentActivity,
    private val words: List<Word>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = words.size

    override fun createFragment(position: Int): Fragment {
        return TestFlashcardFragment.newInstance(words[position])
    }
}
