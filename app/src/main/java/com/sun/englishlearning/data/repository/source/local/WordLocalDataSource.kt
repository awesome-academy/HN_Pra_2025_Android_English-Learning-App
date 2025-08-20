package com.sun.englishlearning.data.repository.source.local

import com.google.firebase.firestore.FirebaseFirestore
import com.sun.englishlearning.data.model.Word
import com.sun.englishlearning.data.repository.source.WordDataSource
import com.sun.englishlearning.data.repository.source.remote.OnResultListener

class WordLocalDataSource : WordDataSource.Local {

    override fun getWordsLocal(listener: OnResultListener<MutableList<Word>>) {
        val db = FirebaseFirestore.getInstance()
        db.collection("words")
            .get()
            .addOnSuccessListener { result ->
                val words = mutableListOf<Word>()
                for (document in result) {
                    try {
                        val word = document.toObject(Word::class.java)
                        words.add(word)
                    } catch (e: Exception) {
                        // Skip malformed document
                    }
                }
                listener.onSuccess(words)
            }
            .addOnFailureListener { exception ->
                listener.onError(exception)
            }
    }

    override fun addWordLocal(word: Word, listener: OnResultListener<Boolean>) {
        val db = FirebaseFirestore.getInstance()
        db.collection("words")
            .document(word.word)
            .set(word)
            .addOnSuccessListener {
                listener.onSuccess(true)
            }
            .addOnFailureListener { exception ->
                listener.onError(exception)
            }
    }

    companion object {
        private var instance: WordLocalDataSource? = null

        fun getInstance() = synchronized(this) {
            instance ?: WordLocalDataSource().also { instance = it }
        }
    }
}
