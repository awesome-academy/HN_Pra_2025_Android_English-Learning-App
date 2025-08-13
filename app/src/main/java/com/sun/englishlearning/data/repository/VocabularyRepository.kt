package com.sun.englishlearning.data.repository

import com.sun.englishlearning.data.model.Word

object VocabularyRepository {
    
    fun getVocabularyByLessonId(lessonId: String): List<Word> {
        return when (lessonId) {
            "35" -> getSchoolsVocabulary()
            else -> emptyList()
        }
    }
    
    private fun getSchoolsVocabulary(): List<Word> {
        return listOf(
            Word(
                id = "1",
                name = "School",
                definition = "An institution for educating children",
                soundUrl = "",
                example = "I go to school every day."
            ),
            Word(
                id = "2",
                name = "Teacher",
                definition = "A person who teaches students",
                soundUrl = "",
                example = "My teacher is very kind."
            ),
            Word(
                id = "3",
                name = "Student",
                definition = "A person who is learning at a school",
                soundUrl = "",
                example = "She is a good student."
            ),
            Word(
                id = "4",
                name = "Principal",
                definition = "The head of a school",
                soundUrl = "",
                example = "The principal gave a speech."
            ),
            Word(
                id = "5",
                name = "Library",
                definition = "A place where books are kept for reading",
                soundUrl = "",
                example = "I borrowed a book from the library."
            )
        )
    }
}
