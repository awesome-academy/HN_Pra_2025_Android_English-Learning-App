package com.sun.englishlearning.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DictionaryDao {
    
    @Query("SELECT * FROM dictionary_words WHERE word = :word COLLATE NOCASE")
    suspend fun getWord(word: String): DictionaryWord?
    
    @Query("SELECT * FROM dictionary_words WHERE word LIKE '%' || :query || '%' COLLATE NOCASE LIMIT 10")
    suspend fun searchWords(query: String): List<DictionaryWord>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<DictionaryWord>)
    
    @Query("SELECT COUNT(*) FROM dictionary_words")
    suspend fun getWordCount(): Int
}
