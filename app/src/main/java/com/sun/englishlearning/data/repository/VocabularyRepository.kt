package com.sun.englishlearning.data.repository

import com.sun.englishlearning.data.model.Word

object VocabularyRepository {
    
    fun getVocabularyByLessonId(lessonId: String): List<Word> {
        return when (lessonId) {
            "35" -> getSchoolsVocabulary()
            "36" -> getExaminationVocabulary()
            "2" -> getExtracurricularVocabulary()
            "31" -> getSchoolStationeryVocabulary()
            "32" -> getSchoolSubjectsVocabulary()
            "33" -> getClassroomVocabulary()
            "34" -> getUniversitiesVocabulary()
            "8" -> getBodyVocabulary()
            "9" -> getAppearanceVocabulary()
            "10" -> getCharacteristicsVocabulary()
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
    
    private fun getExaminationVocabulary(): List<Word> {
        return listOf(
            Word(
                id = "6",
                name = "Exam",
                definition = "A formal test of knowledge or ability",
                soundUrl = "",
                example = "I have an exam tomorrow."
            ),
            Word(
                id = "7",
                name = "Test",
                definition = "A procedure to assess knowledge",
                soundUrl = "",
                example = "The test was difficult."
            ),
            Word(
                id = "8",
                name = "Grade",
                definition = "A mark indicating quality of work",
                soundUrl = "",
                example = "I got a good grade on my test."
            ),
            Word(
                id = "9",
                name = "Quiz",
                definition = "A short test",
                soundUrl = "",
                example = "We had a pop quiz today."
            ),
            Word(
                id = "10",
                name = "Study",
                definition = "To learn about something",
                soundUrl = "",
                example = "I need to study for the exam."
            )
        )
    }
    
    private fun getExtracurricularVocabulary(): List<Word> {
        return listOf(
            Word(
                id = "11",
                name = "Club",
                definition = "A group of people with shared interests",
                soundUrl = "",
                example = "I joined the drama club."
            ),
            Word(
                id = "12",
                name = "Sports",
                definition = "Physical activities and games",
                soundUrl = "",
                example = "I love playing sports."
            ),
            Word(
                id = "13",
                name = "Music",
                definition = "Sounds arranged in a pleasing way",
                soundUrl = "",
                example = "She plays music beautifully."
            ),
            Word(
                id = "14",
                name = "Drama",
                definition = "Acting and theater",
                soundUrl = "",
                example = "The drama performance was amazing."
            ),
            Word(
                id = "15",
                name = "Competition",
                definition = "A contest between people or groups",
                soundUrl = "",
                example = "We won the competition."
            )
        )
    }
    
    private fun getSchoolStationeryVocabulary(): List<Word> {
        return listOf(
            Word(
                id = "16",
                name = "Pen",
                definition = "A writing instrument with ink",
                soundUrl = "",
                example = "I write with a blue pen."
            ),
            Word(
                id = "17",
                name = "Pencil",
                definition = "A writing tool with graphite",
                soundUrl = "",
                example = "Use a pencil for the drawing."
            ),
            Word(
                id = "18",
                name = "Notebook",
                definition = "A book of blank pages for writing",
                soundUrl = "",
                example = "I take notes in my notebook."
            ),
            Word(
                id = "19",
                name = "Eraser",
                definition = "A tool for removing pencil marks",
                soundUrl = "",
                example = "I need an eraser to fix this mistake."
            ),
            Word(
                id = "20",
                name = "Ruler",
                definition = "A tool for measuring and drawing straight lines",
                soundUrl = "",
                example = "Use a ruler to draw a straight line."
            )
        )
    }
    
    private fun getSchoolSubjectsVocabulary(): List<Word> {
        return listOf(
            Word(
                id = "21",
                name = "Mathematics",
                definition = "The study of numbers and calculations",
                soundUrl = "",
                example = "Mathematics is my favorite subject."
            ),
            Word(
                id = "22",
                name = "Science",
                definition = "The study of the natural world",
                soundUrl = "",
                example = "We learned about plants in science class."
            ),
            Word(
                id = "23",
                name = "History",
                definition = "The study of past events",
                soundUrl = "",
                example = "History helps us understand the past."
            ),
            Word(
                id = "24",
                name = "Geography",
                definition = "The study of places and locations",
                soundUrl = "",
                example = "Geography teaches us about different countries."
            ),
            Word(
                id = "25",
                name = "Literature",
                definition = "The study of written works",
                soundUrl = "",
                example = "We read poetry in literature class."
            )
        )
    }
    
    private fun getClassroomVocabulary(): List<Word> {
        return listOf(
            Word(
                id = "26",
                name = "Desk",
                definition = "A table for writing or working",
                soundUrl = "",
                example = "I sit at my desk to study."
            ),
            Word(
                id = "27",
                name = "Chair",
                definition = "A seat with a back",
                soundUrl = "",
                example = "Please sit on your chair."
            ),
            Word(
                id = "28",
                name = "Blackboard",
                definition = "A dark board for writing with chalk",
                soundUrl = "",
                example = "The teacher writes on the blackboard."
            ),
            Word(
                id = "29",
                name = "Window",
                definition = "An opening in a wall with glass",
                soundUrl = "",
                example = "Open the window for fresh air."
            ),
            Word(
                id = "30",
                name = "Door",
                definition = "An entrance to a room",
                soundUrl = "",
                example = "Please close the door."
            )
        )
    }
    
    private fun getUniversitiesVocabulary(): List<Word> {
        return listOf(
            Word(
                id = "31",
                name = "University",
                definition = "A higher education institution",
                soundUrl = "",
                example = "I want to go to university."
            ),
            Word(
                id = "32",
                name = "Professor",
                definition = "A university teacher",
                soundUrl = "",
                example = "The professor is very knowledgeable."
            ),
            Word(
                id = "33",
                name = "Degree",
                definition = "An academic qualification",
                soundUrl = "",
                example = "She has a degree in engineering."
            ),
            Word(
                id = "34",
                name = "Campus",
                definition = "The grounds of a university",
                soundUrl = "",
                example = "The campus is very beautiful."
            ),
            Word(
                id = "35",
                name = "Lecture",
                definition = "A formal talk on a subject",
                soundUrl = "",
                example = "I attended a lecture on history."
            )
        )
    }
    
    private fun getBodyVocabulary(): List<Word> {
        return listOf(
            Word(
                id = "36",
                name = "Head",
                definition = "The upper part of the body",
                soundUrl = "",
                example = "I have a headache."
            ),
            Word(
                id = "37",
                name = "Hand",
                definition = "The part at the end of the arm",
                soundUrl = "",
                example = "Wash your hands before eating."
            ),
            Word(
                id = "38",
                name = "Foot",
                definition = "The part at the end of the leg",
                soundUrl = "",
                example = "My foot hurts."
            ),
            Word(
                id = "39",
                name = "Eye",
                definition = "The organ of sight",
                soundUrl = "",
                example = "She has beautiful eyes."
            ),
            Word(
                id = "40",
                name = "Ear",
                definition = "The organ of hearing",
                soundUrl = "",
                example = "I can hear with my ears."
            )
        )
    }
    
    private fun getAppearanceVocabulary(): List<Word> {
        return listOf(
            Word(
                id = "41",
                name = "Tall",
                definition = "Having great height",
                soundUrl = "",
                example = "He is very tall."
            ),
            Word(
                id = "42",
                name = "Short",
                definition = "Having little height",
                soundUrl = "",
                example = "She is short but strong."
            ),
            Word(
                id = "43",
                name = "Beautiful",
                definition = "Pleasing to look at",
                soundUrl = "",
                example = "The sunset is beautiful."
            ),
            Word(
                id = "44",
                name = "Handsome",
                definition = "Good-looking (usually for men)",
                soundUrl = "",
                example = "He is a handsome young man."
            ),
            Word(
                id = "45",
                name = "Pretty",
                definition = "Attractive in appearance",
                soundUrl = "",
                example = "She looks pretty in that dress."
            )
        )
    }
    
    private fun getCharacteristicsVocabulary(): List<Word> {
        return listOf(
            Word(
                id = "46",
                name = "Kind",
                definition = "Caring and helpful",
                soundUrl = "",
                example = "She is very kind to everyone."
            ),
            Word(
                id = "47",
                name = "Smart",
                definition = "Intelligent and clever",
                soundUrl = "",
                example = "He is a smart student."
            ),
            Word(
                id = "48",
                name = "Funny",
                definition = "Causing laughter",
                soundUrl = "",
                example = "The comedian is very funny."
            ),
            Word(
                id = "49",
                name = "Brave",
                definition = "Showing courage",
                soundUrl = "",
                example = "The firefighter is brave."
            ),
            Word(
                id = "50",
                name = "Honest",
                definition = "Truthful and sincere",
                soundUrl = "",
                example = "An honest person always tells the truth."
            )
        )
    }
}
