package com.sun.englishlearning.screen.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sun.englishlearning.R
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.data.model.UserLessonProgress
import com.sun.englishlearning.data.repository.LessonRepository
import com.sun.englishlearning.data.repository.LessonRepositoryImpl
import com.sun.englishlearning.data.repository.UserLessonProgressRepository
import com.sun.englishlearning.data.repository.UserLessonProgressRepositoryImpl
import com.sun.englishlearning.databinding.FragmentHomeBinding
import com.sun.englishlearning.databinding.ItemSuggestedCourseBinding
import com.sun.englishlearning.screen.home.adapter.CourseCategory
import com.sun.englishlearning.screen.home.adapter.CourseCategoryAdapter
import com.sun.englishlearning.screen.login.LoginActivity
import com.sun.englishlearning.screen.search.WordSearchActivity
import com.sun.englishlearning.utils.base.BaseFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val userProgressRepository: UserLessonProgressRepository = UserLessonProgressRepositoryImpl()
    private val lessonRepository: LessonRepository = LessonRepositoryImpl(userProgressRepository)
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var suggestedLessons: List<Lesson> = emptyList()
    private var currentSuggestedIndex = 0
    
    // Authentication
    private val auth = Firebase.auth
    
    // Courses section
    private lateinit var coursesAdapter: CourseCategoryAdapter

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        setupClickListeners()
        updateStudyTime()
        setupSuggestedCourse()
        setupCoursesSection()
        
        // Debug: Show current user info immediately
        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: "anonymous_user"
        Toast.makeText(context, "User ID: $userId", Toast.LENGTH_LONG).show()
    }

    override fun initData() {
        println("=== HOME FRAGMENT INIT DATA ===")
        checkAuthenticationAndLoadData()
    }

    private fun setupClickListeners() {
        with(viewBinding) {
            ivSearch.setOnClickListener {
                val intent = Intent(requireContext(), WordSearchActivity::class.java)
                startActivity(intent)
            }

            btnLetsStart.setOnClickListener {
                Toast.makeText(context, "Let's start learning!", Toast.LENGTH_SHORT).show()
            }

            tvSeeAllCourses.setOnClickListener {
                // Navigate to Courses tab
                findNavController().navigate(R.id.navigation_courses)
            }

            tvSeeAllRecent.setOnClickListener {
                // Temporary: Create test data for recent lessons
                createTestProgressData()
            }

            ivRefresh.setOnClickListener {
                refreshSuggestedCourse()
            }
        }
    }

    private fun updateStudyTime() {
        viewBinding.tvStudyTime.text = getString(R.string.study_time_format, 2, 15)
    }

    private fun setupSuggestedCourse() {
        loadSuggestedLessons()
    }

    private fun loadSuggestedLessons() {
        coroutineScope.launch {
            try {
                // Get lessons that user has not started (suggested lessons)
                val userId = getCurrentUserId()
                val lessonsResult = lessonRepository.getSuggestedLessons(userId)
                if (lessonsResult.isSuccess) {
                    suggestedLessons = lessonsResult.getOrNull() ?: emptyList()
                    if (suggestedLessons.isNotEmpty()) {
                        updateSuggestedCourseCard(suggestedLessons[0])
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading suggestions", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun refreshSuggestedCourse() {
        if (suggestedLessons.isNotEmpty()) {
            currentSuggestedIndex = (currentSuggestedIndex + 1) % suggestedLessons.size
            updateSuggestedCourseCard(suggestedLessons[currentSuggestedIndex])
        } else {
            loadSuggestedLessons()
        }
        Toast.makeText(context, "Refreshing suggestions", Toast.LENGTH_SHORT).show()
    }

    private fun updateSuggestedCourseCard(lesson: Lesson) {
        val suggestedCourseBinding = ItemSuggestedCourseBinding.bind(viewBinding.suggestedCourseCard.root)
        
        suggestedCourseBinding.apply {
            tvCourseTitle.text = lesson.title
            tvCourseSubtitle.text = lesson.description
            // Use a default image or the lesson's image resource
            if (lesson.imageUrl.isNotBlank()) {
                Glide.with(this@HomeFragment)
                    .load(lesson.imageUrl)
                    .placeholder(R.drawable.img_ob3)
                    .error(R.drawable.img_ob3)
                    .into(ivCourseImage)
            } else {
                ivCourseImage.setImageResource(R.drawable.img_ob3)
            }
            
            // Handle click on suggested course card
            root.setOnClickListener {
                Toast.makeText(context, "Opening lesson: ${lesson.title}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "anonymous_user"
    }
    
    private fun checkAuthenticationAndLoadData() {
        val currentUser = auth.currentUser
        println("=== AUTH CHECK ===")
        println("Current user: $currentUser")
        println("User ID: ${currentUser?.uid}")
        println("================")
        
        if (currentUser == null) {
            // User is not signed in, show message and redirect to login
            println("No user authenticated - redirecting to login")
            Toast.makeText(context, "Please sign in to access course content", Toast.LENGTH_LONG).show()
            redirectToLogin()
        } else {
            // User is signed in, load the data
            println("User authenticated - loading data")
            loadCourseCategories()
            loadRecentLessons()
        }
    }
    
    private fun redirectToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
    
    private fun setupCoursesSection() {
        coursesAdapter = CourseCategoryAdapter { category ->
            onCategoryClicked(category)
        }
        
        viewBinding.rvCourseCategories.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = coursesAdapter
        }
    }
    
    private fun loadCourseCategories() {
        showCoursesLoading()
        coroutineScope.launch {
            try {
                val lessonsResult = lessonRepository.getAllLessons()
                hideCoursesLoading()
                
                if (lessonsResult.isSuccess) {
                    val allLessons = lessonsResult.getOrNull() ?: emptyList()
                    
                    // Debug: Log lesson IDs to help create test data
                    println("=== LESSON DEBUG INFO ===")
                    allLessons.take(3).forEach { lesson ->
                        println("Lesson ID: ${lesson.id}, Title: ${lesson.title}")
                    }
                    println("========================")
                    
                    if (allLessons.isEmpty()) {
                        Toast.makeText(context, "No courses available at the moment", Toast.LENGTH_SHORT).show()
                        val testCategories = createTestCategories()
                        coursesAdapter.updateCategories(testCategories)
                    } else {
                        val categories = createCategoriesFromLessons(allLessons)
                        
                        if (categories.isEmpty()) {
                            Toast.makeText(context, "No courses available at the moment", Toast.LENGTH_SHORT).show()
                            val testCategories = createTestCategories()
                            coursesAdapter.updateCategories(testCategories)
                        } else {
                            coursesAdapter.updateCategories(categories)
                        }
                    }
                } else {
                    Toast.makeText(context, "Unable to load courses. Please try again later.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                hideCoursesLoading()
                Toast.makeText(context, "Something went wrong. Please check your connection and try again.", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun loadRecentLessons() {
        coroutineScope.launch {
            try {
                val userId = getCurrentUserId()
                
                // Debug: Log current user info to help create test data
                val currentUser = auth.currentUser
                println("=== USER DEBUG INFO ===")
                println("User ID: $userId")
                println("User Email: ${currentUser?.email}")
                println("User Display Name: ${currentUser?.displayName}")
                println("Is Anonymous: ${currentUser?.isAnonymous}")
                println("=====================")
                
                val recentLessonsResult = lessonRepository.getRecentlyLearnedLessons(userId, 2)
                
                if (recentLessonsResult.isSuccess) {
                    val recentLessonsWithProgress = recentLessonsResult.getOrNull() ?: emptyList()
                    
                    // Debug: Show what we got
                    Toast.makeText(context, "Found ${recentLessonsWithProgress.size} recent lessons", Toast.LENGTH_LONG).show()
                    println("=== RECENT LESSONS WITH PROGRESS ===")
                    println("Found ${recentLessonsWithProgress.size} recent lessons")
                    recentLessonsWithProgress.forEach { (lesson, progress) ->
                        println("Lesson: ${lesson.title} (${lesson.id})")
                        println("Progress: Score=${progress.bestScore}, Words=${progress.wordsLearned}/${progress.totalWords}")
                    }
                    println("====================================")
                    
                    if (recentLessonsWithProgress.isEmpty()) {
                        // Show a helpful message and hide the section
                        showEmptyRecentLessonsState()
                        Toast.makeText(context, "No recent lessons to display", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Displaying ${recentLessonsWithProgress.size} recent lessons", Toast.LENGTH_SHORT).show()
                        println("=== CALLING updateRecentLessonsUI ===")
                        updateRecentLessonsUI(recentLessonsWithProgress)
                    }
                } else {
                    val error = recentLessonsResult.exceptionOrNull()
                    Toast.makeText(context, "Error loading recent lessons: ${error?.message}", Toast.LENGTH_LONG).show()
                    showEmptyRecentLessonsState()
                }
            } catch (e: Exception) {
                showEmptyRecentLessonsState()
            }
        }
    }
    
    private fun showEmptyRecentLessonsState() {
        // Hide both recent lesson cards
        viewBinding.cvRecentLesson1.visibility = View.GONE
        viewBinding.cvRecentLesson2.visibility = View.GONE
        
        // You could also show a placeholder message here if needed
        // For now, we'll just hide the cards gracefully
    }
    
    private fun updateRecentLessonsUI(recentLessons: List<Pair<Lesson, UserLessonProgress>>) {
        println("=== updateRecentLessonsUI CALLED ===")
        println("Received ${recentLessons.size} lessons to display")
        
        if (recentLessons.isNotEmpty()) {
            // Update first lesson
            val (lesson1, progress1) = recentLessons[0]
            println("Updating first lesson card:")
            println("  - Lesson: ${lesson1.title}")
            println("  - Progress: ${progress1.wordsLearned}/${progress1.totalWords}")
            println("  - View binding available")
            
            updateLessonCard(
                lesson1, progress1,
                viewBinding.cvRecentLesson1,
                viewBinding.ivRecentLesson1,
                viewBinding.tvRecentLesson1Title,
                viewBinding.tvRecentLesson1Number,
                viewBinding.tvRecentLesson1Difficulty,
                viewBinding.tvRecentLesson1Progress
            )
            
            // Update second lesson if available
            if (recentLessons.size > 1) {
                val (lesson2, progress2) = recentLessons[1]
                println("Updating second lesson card:")
                println("  - Lesson: ${lesson2.title}")
                println("  - Progress: ${progress2.wordsLearned}/${progress2.totalWords}")
                
                updateLessonCard(
                    lesson2, progress2,
                    viewBinding.cvRecentLesson2,
                    viewBinding.ivRecentLesson2,
                    viewBinding.tvRecentLesson2Title,
                    viewBinding.tvRecentLesson2Number,
                    viewBinding.tvRecentLesson2Difficulty,
                    viewBinding.tvRecentLesson2Progress
                )
            } else {
                // Hide second card if no second lesson
                println("Hiding second lesson card (only 1 lesson)")
                viewBinding.cvRecentLesson2.visibility = View.GONE
            }
        } else {
            // Hide both cards if no recent lessons
            println("Hiding both lesson cards (no lessons)")
            viewBinding.cvRecentLesson1.visibility = View.GONE
            viewBinding.cvRecentLesson2.visibility = View.GONE
        }
        println("=== updateRecentLessonsUI COMPLETED ===")
    }
    
    private fun updateLessonCard(
        lesson: Lesson,
        progress: UserLessonProgress,
        cardView: androidx.cardview.widget.CardView,
        imageView: android.widget.ImageView,
        titleView: android.widget.TextView,
        numberView: android.widget.TextView,
        difficultyView: android.widget.TextView,
        progressView: android.widget.TextView
    ) {
        println("=== updateLessonCard CALLED ===")
        println("Lesson: ${lesson.title}")
        println("Setting card visibility to VISIBLE")
        
        // Show the card
        cardView.visibility = View.VISIBLE
        
        println("Loading image: ${lesson.imageUrl}")
        // Load lesson image
        Glide.with(this)
            .load(lesson.imageUrl)
            .placeholder(R.drawable.img_ob1)
            .error(R.drawable.img_ob1)
            .centerCrop()
            .into(imageView)
        
        println("Setting title: ${lesson.title}")
        // Set lesson title
        titleView.text = lesson.title
        
        println("Setting word count: ${lesson.wordIds.size}")
        // Set total word count in this lesson
        numberView.text = "${lesson.wordIds.size} words"
        
        println("Setting difficulty: ${lesson.difficulty.name}, score: ${progress.bestScore}")
        // Set difficulty level and score
        difficultyView.text = "${lesson.difficulty.name}: ${progress.bestScore}"
        
        println("Setting words learned: ${progress.wordsLearned}/${lesson.wordIds.size}")
        // Set words learned out of total words in lesson
        progressView.text = "${progress.wordsLearned}/${lesson.wordIds.size}"
        
        // Set click listener to navigate to lesson detail
        cardView.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeToLessonDetail(lesson)
            findNavController().navigate(action)
        }
        
        println("=== updateLessonCard COMPLETED ===")
    }
    
    private fun createCategoriesFromLessons(lessons: List<Lesson>): List<CourseCategory> {
        // Create categories directly from lessons using their title and imageUrl
        return lessons.take(3).map { lesson ->
            CourseCategory(
                title = lesson.title,
                imageUrl = lesson.imageUrl.takeIf { it.isNotBlank() } 
                    ?: "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=400",
                lessons = listOf(lesson)
            )
        }
    }
    
    private fun createTestCategories(): List<CourseCategory> {
        // Create test categories for debugging
        return listOf(
            CourseCategory(
                title = "Test Practice",
                imageUrl = "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=400",
                lessons = emptyList()
            ),
            CourseCategory(
                title = "Test Business",
                imageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400",
                lessons = emptyList()
            ),
            CourseCategory(
                title = "Test Academic",
                imageUrl = "https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=400",
                lessons = emptyList()
            )
        )
    }
    
    private fun onCategoryClicked(category: CourseCategory) {
        // Navigate to lesson detail with the lesson from the category
        if (category.lessons.isNotEmpty()) {
            val lesson = category.lessons.first()
            val action = HomeFragmentDirections.actionHomeToLessonDetail(lesson)
            findNavController().navigate(action)
        } else {
            Toast.makeText(context, "No lesson available for ${category.title}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showCoursesLoading() {
        viewBinding.pbCoursesLoading.visibility = View.VISIBLE
        viewBinding.rvCourseCategories.visibility = View.GONE
    }
    
    private fun hideCoursesLoading() {
        viewBinding.pbCoursesLoading.visibility = View.GONE
        viewBinding.rvCourseCategories.visibility = View.VISIBLE
    }
    
    // Temporary function to create test progress data
    private fun createTestProgressData() {
        coroutineScope.launch {
            try {
                val userId = getCurrentUserId()
                val lessonsResult = lessonRepository.getAllLessons()
                
                if (lessonsResult.isSuccess) {
                    val lessons = lessonsResult.getOrNull() ?: emptyList()
                    if (lessons.size >= 2) {
                        // Create test progress for first two lessons
                        val testProgress1 = UserLessonProgress(
                            id = "test_progress_1",
                            userId = userId,
                            lessonId = lessons[0].id,
                            isStarted = true,
                            isCompleted = false,
                            currentPoints = 75,
                            totalPoints = 100,
                            progressPercentage = 75,
                            timeSpentMinutes = 15,
                            attempts = 2,
                            bestScore = 75,
                            wordsLearned = 8,
                            totalWords = 12,
                            completedExercises = listOf("exercise1", "exercise2"),
                            learnedWordIds = listOf("word1", "word2", "word3"),
                            startedAt = java.util.Date(),
                            completedAt = null,
                            lastAccessedAt = java.util.Date()
                        )
                        
                        val testProgress2 = UserLessonProgress(
                            id = "test_progress_2",
                            userId = userId,
                            lessonId = lessons[1].id,
                            isStarted = true,
                            isCompleted = false, // Keep as in-progress
                            currentPoints = 60,
                            totalPoints = 100,
                            progressPercentage = 60,
                            timeSpentMinutes = 12,
                            attempts = 1,
                            bestScore = 60,
                            wordsLearned = 8,
                            totalWords = 15,
                            completedExercises = listOf("exercise1"),
                            learnedWordIds = listOf("word4", "word5"),
                            startedAt = java.util.Date(System.currentTimeMillis() - 86400000), // 1 day ago
                            completedAt = null, // No completion date for in-progress
                            lastAccessedAt = java.util.Date(System.currentTimeMillis() - 3600000) // 1 hour ago
                        )
                        
                        // Create the progress records
                        println("=== CREATING TEST DATA ===")
                        println("Creating progress 1 for lesson: ${lessons[0].id} (${lessons[0].title})")
                        val result1 = userProgressRepository.createProgress(testProgress1)
                        println("Result 1: ${if (result1.isSuccess) "SUCCESS" else "FAILED: ${result1.exceptionOrNull()?.message}"}")
                        
                        println("Creating progress 2 for lesson: ${lessons[1].id} (${lessons[1].title})")
                        val result2 = userProgressRepository.createProgress(testProgress2)
                        println("Result 2: ${if (result2.isSuccess) "SUCCESS" else "FAILED: ${result2.exceptionOrNull()?.message}"}")
                        
                        Toast.makeText(context, "Test data created! Refresh to see recent lessons.", Toast.LENGTH_LONG).show()
                        
                        // Refresh the recent lessons display
                        println("Refreshing recent lessons after creating test data...")
                        loadRecentLessons()
                    } else {
                        Toast.makeText(context, "Need at least 2 lessons to create test data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Could not load lessons for test data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error creating test data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
