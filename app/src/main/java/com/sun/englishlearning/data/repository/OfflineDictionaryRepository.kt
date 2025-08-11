package com.sun.englishlearning.data.repository

import android.content.Context
import com.sun.englishlearning.data.database.DictionaryDatabase
import com.sun.englishlearning.data.database.DictionaryWord
import com.sun.englishlearning.data.model.WordSearchResult

class OfflineDictionaryRepository(context: Context) {
    
    private val database = DictionaryDatabase.getDatabase(context)
    private val dao = database.dictionaryDao()
    
    suspend fun searchWord(word: String): Result<WordSearchResult> {
        return try {
            // First try exact match
            val dictionaryWord = dao.getWord(word.trim())
            
            if (dictionaryWord != null) {
                val result = WordSearchResult(
                    word = dictionaryWord.word,
                    phonetic = dictionaryWord.phonetic,
                    audioUrl = null, // No audio for offline dictionary
                    partOfSpeech = dictionaryWord.partOfSpeech,
                    definition = dictionaryWord.definition,
                    example = dictionaryWord.example,
                    synonyms = parseSynonyms(dictionaryWord.synonyms)
                )
                Result.success(result)
            } else {
                Result.failure(Exception("Word '${word}' not found in offline dictionary"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Database error: ${e.message}"))
        }
    }
    
    suspend fun initializeDictionary() {
        try {
            val wordCount = dao.getWordCount()
            if (wordCount == 0) {
                // Insert common English words
                val commonWords = getCommonWords()
                dao.insertWords(commonWords)
            }
        } catch (e: Exception) {
            // Handle initialization error
        }
    }
    
    private fun parseSynonyms(synonymsJson: String): List<String> {
        return if (synonymsJson.isNotEmpty()) {
            synonymsJson.split(",").map { it.trim() }
        } else {
            emptyList()
        }
    }
    
    private fun getCommonWords(): List<DictionaryWord> {
        return listOf(
            DictionaryWord("hello", "/həˈloʊ/", "exclamation", "Used as a greeting or to begin a phone conversation.", "Hello, how are you?"),
            DictionaryWord("world", "/wɜːrld/", "noun", "The earth, together with all of its countries, peoples, and natural features.", "Welcome to the world."),
            DictionaryWord("love", "/lʌv/", "noun", "An intense feeling of deep affection.", "I love spending time with family.", "adore,cherish,care"),
            DictionaryWord("happy", "/ˈhæpi/", "adjective", "Feeling or showing pleasure or contentment.", "She looks very happy today.", "joyful,cheerful,glad"),
            DictionaryWord("beautiful", "/ˈbjuːtɪfəl/", "adjective", "Pleasing the senses or mind aesthetically.", "What a beautiful sunset!", "pretty,lovely,gorgeous"),
            DictionaryWord("learn", "/lɜːrn/", "verb", "Acquire knowledge of or skill in something by study, experience, or being taught.", "I want to learn English.", "study,master,acquire"),
            DictionaryWord("friend", "/frend/", "noun", "A person whom one knows and with whom one has a bond of mutual affection.", "She is my best friend.", "companion,buddy,pal"),
            DictionaryWord("house", "/haʊs/", "noun", "A building for human habitation, especially one that is lived in by a family.", "They bought a new house.", "home,residence,dwelling"),
            DictionaryWord("good", "/ɡʊd/", "adjective", "To be desired or approved of.", "That's a good idea!", "excellent,great,fine"),
            DictionaryWord("time", "/taɪm/", "noun", "The indefinite continued progress of existence and events.", "What time is it?", "moment,period,duration"),
            DictionaryWord("water", "/ˈwɔːtər/", "noun", "A colorless, transparent, odorless liquid that forms the seas, lakes, rivers, and rain.", "Please drink more water.", "H2O,liquid"),
            DictionaryWord("food", "/fuːd/", "noun", "Any nutritious substance that people or animals eat or drink.", "The food was delicious.", "nourishment,meal,cuisine"),
            DictionaryWord("book", "/bʊk/", "noun", "A written or printed work consisting of pages glued or sewn together along one side.", "I'm reading a good book.", "novel,publication,volume"),
            DictionaryWord("school", "/skuːl/", "noun", "An institution for educating children.", "The children go to school every day.", "academy,institution,college"),
            DictionaryWord("work", "/wɜːrk/", "verb", "Activity involving mental or physical effort done in order to achieve a purpose or result.", "I work at a technology company.", "employment,job,labor"),
            DictionaryWord("family", "/ˈfæməli/", "noun", "A group consisting of parents and children living together in a household.", "I love spending time with my family.", "relatives,household,clan"),
            DictionaryWord("money", "/ˈmʌni/", "noun", "A current medium of exchange in the form of coins and banknotes.", "I need to save more money.", "currency,cash,funds"),
            DictionaryWord("car", "/kɑːr/", "noun", "A road vehicle, typically with four wheels, powered by an internal combustion engine.", "She drives a red car.", "automobile,vehicle"),
            DictionaryWord("city", "/ˈsɪti/", "noun", "A large town.", "New York is a big city.", "metropolis,urban area,town"),
            DictionaryWord("country", "/ˈkʌntri/", "noun", "A nation with its own government, occupying a particular territory.", "Which country are you from?", "nation,state,homeland"),
            DictionaryWord("language", "/ˈlæŋɡwɪdʒ/", "noun", "The method of human communication, either spoken or written.", "English is a global language.", "tongue,speech,communication"),
            DictionaryWord("travel", "/ˈtrævəl/", "verb", "Go from one place to another, typically over a distance of some length.", "I love to travel around the world.", "journey,trip,voyage"),
            DictionaryWord("music", "/ˈmjuːzɪk/", "noun", "Vocal or instrumental sounds combined in such a way as to produce beauty of form.", "I enjoy listening to music.", "melody,harmony,song"),
            DictionaryWord("computer", "/kəmˈpjuːtər/", "noun", "An electronic device for storing and processing data.", "I use my computer for work.", "PC,laptop,machine"),
            DictionaryWord("phone", "/foʊn/", "noun", "A telephone.", "Can you call me on my phone?", "telephone,mobile,cell"),
            DictionaryWord("internet", "/ˈɪntərnet/", "noun", "A global computer network providing information and communication facilities.", "I found the information on the internet.", "web,online,net"),
            DictionaryWord("business", "/ˈbɪznəs/", "noun", "The activity of making, buying, or selling goods or providing services.", "She runs her own business.", "company,enterprise,trade"),
            DictionaryWord("health", "/helθ/", "noun", "The state of being free from illness or injury.", "Good health is important.", "wellness,fitness,wellbeing"),
            DictionaryWord("exercise", "/ˈeksərsaɪz/", "noun", "Activity requiring physical effort, carried out to sustain or improve health and fitness.", "I do exercise every morning.", "workout,fitness,training"),
            DictionaryWord("restaurant", "/ˈrestərɑːnt/", "noun", "A place where people pay to sit and eat meals that are cooked and served on the premises.", "Let's go to that new restaurant.", "eatery,cafe,diner")
        )
    }
}
