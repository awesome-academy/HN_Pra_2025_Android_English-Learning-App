package com.sun.englishlearning.data.repository

import com.sun.englishlearning.data.model.VocabularyWord
import com.sun.englishlearning.data.model.WordType

interface VocabularyRepository {
    suspend fun getWordsByType(type: WordType): Result<List<VocabularyWord>>
    suspend fun getSavedWords(): Result<List<VocabularyWord>>
    suspend fun toggleBookmark(wordId: String): Result<VocabularyWord>
    suspend fun searchWords(query: String, type: WordType? = null): Result<List<VocabularyWord>>
}

class VocabularyRepositoryImpl : VocabularyRepository {
    
    // For now, using seed data. Replace with actual API calls when available
    override suspend fun getWordsByType(type: WordType): Result<List<VocabularyWord>> {
        return Result.success(generateSeedData(type))
    }
    
    override suspend fun getSavedWords(): Result<List<VocabularyWord>> {
        // Return all bookmarked words from all types
        val allWords = WordType.values().flatMap { generateSeedData(it) }
        return Result.success(allWords.filter { it.isBookmarked })
    }
    
    override suspend fun toggleBookmark(wordId: String): Result<VocabularyWord> {
        // TODO: Implement actual bookmark toggle with API
        return Result.failure(Exception("Not implemented - replace with API call"))
    }
    
    override suspend fun searchWords(query: String, type: WordType?): Result<List<VocabularyWord>> {
        val wordsToSearch = if (type != null) {
            generateSeedData(type)
        } else {
            WordType.values().flatMap { generateSeedData(it) }
        }
        
        val filteredWords = wordsToSearch.filter { word ->
            word.word.contains(query, ignoreCase = true) ||
            word.meaning.contains(query, ignoreCase = true)
        }
        
        return Result.success(filteredWords)
    }
    
    private fun generateSeedData(type: WordType): List<VocabularyWord> {
        return when (type) {
            WordType.WEAK -> listOf(
                VocabularyWord("1", "City break", "City tourist", "/ˈsɪti breɪk/", WordType.WEAK, true),
                VocabularyWord("2", "Cosmopolitan", "Cosmopolitan", "/ˌkɒzməˈpɒlɪtən/", WordType.WEAK, true),
                VocabularyWord("3", "Crowded", "Crowded", "/ˈkraʊdɪd/", WordType.WEAK, true),
                VocabularyWord("4", "Embassy", "Embassy", "/ˈembəsi/", WordType.WEAK, false),
                VocabularyWord("5", "Gateway", "Gateway", "/ˈɡeɪtweɪ/", WordType.WEAK, false)
            )
            WordType.TODAY -> listOf(
                VocabularyWord("6", "Disappoint", "Disappointing", "/ˌdɪsəˈpɔɪnt/", WordType.TODAY, true),
                VocabularyWord("7", "Jaw-dropping", "Jaw-dropping", "/ˈdʒɔː drɒpɪŋ/", WordType.TODAY, false),
                VocabularyWord("8", "Lively", "Lively", "/ˈlaɪvli/", WordType.TODAY, true)
            )
            WordType.MEDIUM -> listOf(
                VocabularyWord("9", "Adventure", "Adventure", "/ədˈventʃər/", WordType.MEDIUM, false),
                VocabularyWord("10", "Experience", "Experience", "/ɪkˈspɪəriəns/", WordType.MEDIUM, true),
                VocabularyWord("11", "Journey", "Journey", "/ˈdʒɜːrni/", WordType.MEDIUM, false),
                VocabularyWord("12", "Explore", "Explore", "/ɪkˈsplɔːr/", WordType.MEDIUM, true)
            )
            WordType.STRONG -> listOf(
                VocabularyWord("13", "Excellent", "Excellent", "/ˈeksələnt/", WordType.STRONG, false),
                VocabularyWord("14", "Perfect", "Perfect", "/ˈpɜːrfɪkt/", WordType.STRONG, true),
                VocabularyWord("15", "Amazing", "Amazing", "/əˈmeɪzɪŋ/", WordType.STRONG, false),
                VocabularyWord("16", "Outstanding", "Outstanding", "/aʊtˈstændɪŋ/", WordType.STRONG, true),
                VocabularyWord("17", "Remarkable", "Remarkable", "/rɪˈmɑːrkəbl/", WordType.STRONG, false)
            )
        }
    }
}
