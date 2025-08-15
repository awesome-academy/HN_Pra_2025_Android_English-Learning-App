package com.sun.englishlearning.screen.flashcard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sun.englishlearning.data.model.Word

/**
 * Simple test activity to verify flashcard functionality
 */
class FlashcardTestActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "FlashcardTestActivity"
        
        fun newIntent(context: Context): Intent {
            return Intent(context, FlashcardTestActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "Testing flashcard functionality")
        
        // Create test data
        val testWords = arrayListOf(
            Word(
                id = "1",
                name = "school",
                definition = "An institution for educating children",
                soundUrl = "",
                example = "I go to school every day",
                phonetic = "/skuːl/",
                partOfSpeech = "noun"
            ),
            Word(
                id = "2", 
                name = "teacher",
                definition = "A person who teaches",
                soundUrl = "",
                example = "My teacher is very kind",
                phonetic = "/ˈtiːtʃər/",
                partOfSpeech = "noun"
            )
        )
        
        try {
            // Test FlashcardActivity creation
            val intent = FlashcardActivity.newIntent(
                context = this,
                words = testWords,
                currentIndex = 0,
                lessonTitle = "Test Lesson"
            )
            
            Log.d(TAG, "FlashcardActivity intent created successfully")
            startActivity(intent)
            finish()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error testing flashcard", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
