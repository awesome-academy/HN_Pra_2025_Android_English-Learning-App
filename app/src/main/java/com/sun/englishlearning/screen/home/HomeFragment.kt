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
    private lateinit var lessonRepository: LessonRepository
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
        // Initialize lesson repository with context
        lessonRepository = LessonRepositoryImpl(requireContext(), userProgressRepository)

        setupClickListeners()
        updateGreeting()
        setupSuggestedCourse()
        setupCoursesSection()
    }

    override fun initData() {
        checkAuthenticationAndLoadData()
    }

    private fun setupClickListeners() {
        with(viewBinding) {
            ivSearch.setOnClickListener {
                val intent = Intent(requireContext(), WordSearchActivity::class.java)
                startActivity(intent)
            }


            tvSeeAllCourses.setOnClickListener {
                // Navigate to Courses tab
                findNavController().navigate(R.id.navigation_courses)
            }

            tvSeeAllRecent.setOnClickListener {
                // Navigate to Courses tab
                findNavController().navigate(R.id.navigation_courses)
            }

            ivRefresh.setOnClickListener {
                refreshSuggestedCourse()
            }
        }
    }

    private fun updateGreeting() {
        val currentUser = auth.currentUser
        val displayName = currentUser?.displayName

        if (displayName.isNullOrBlank()) {
            // Fallback to email if display name is not available
            val email = currentUser?.email
            val userName = if (!email.isNullOrBlank()) {
                // Extract name part from email (before @) with validation
                val namePart = email.substringBefore("@")
                if (namePart.isNotBlank()) {
                    namePart.replaceFirstChar { it.uppercase() }
                } else {
                    "User"
                }
            } else {
                "User" // Default fallback
            }
            viewBinding.tvGreeting.text = "Hi, $userName👋"
        } else {
            viewBinding.tvGreeting.text = "Hi, $displayName👋"
        }
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
                val action = HomeFragmentDirections.actionHomeToLessonDetail(lesson)
                findNavController().navigate(action)
            }
        }
    }
    
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "anonymous_user"
    }
    
    private fun checkAuthenticationAndLoadData() {
        val currentUser = auth.currentUser
        
        if (currentUser == null) {
            // User is not signed in, show message and redirect to login
            Toast.makeText(context, "Please sign in to access course content", Toast.LENGTH_LONG).show()
            redirectToLogin()
        } else {
            // User is signed in, load the data
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
                showRecentLessonsLoading()
                val userId = getCurrentUserId()
                val recentLessonsResult = lessonRepository.getRecentlyLearnedLessons(userId, 2)

                hideRecentLessonsLoading()

                if (recentLessonsResult.isSuccess) {
                    val recentLessonsWithProgress = recentLessonsResult.getOrNull() ?: emptyList()

                    if (recentLessonsWithProgress.isEmpty()) {
                        showEmptyRecentLessonsState()
                    } else {
                        showRecentLessonsData()
                        updateRecentLessonsUI(recentLessonsWithProgress)
                    }
                } else {
                    showEmptyRecentLessonsState()
                }
            } catch (e: Exception) {
                hideRecentLessonsLoading()
                showEmptyRecentLessonsState()
            }
        }
    }

    private fun showRecentLessonsLoading() {
        // Show loading indicator
        viewBinding.pbRecentLessonsLoading.visibility = View.VISIBLE
        // Hide lesson cards and empty state
        viewBinding.cvRecentLesson1.visibility = View.GONE
        viewBinding.cvRecentLesson2.visibility = View.GONE
        viewBinding.llRecentLessonsEmpty.visibility = View.GONE
    }

    private fun hideRecentLessonsLoading() {
        viewBinding.pbRecentLessonsLoading.visibility = View.GONE
    }

    private fun showEmptyRecentLessonsState() {
        // Hide loading and lesson cards
        viewBinding.pbRecentLessonsLoading.visibility = View.GONE
        viewBinding.cvRecentLesson1.visibility = View.GONE
        viewBinding.cvRecentLesson2.visibility = View.GONE
        // Show empty state
        viewBinding.llRecentLessonsEmpty.visibility = View.VISIBLE
    }

    private fun showRecentLessonsData() {
        // Hide loading and empty state
        viewBinding.pbRecentLessonsLoading.visibility = View.GONE
        viewBinding.llRecentLessonsEmpty.visibility = View.GONE
        // Lesson cards will be shown by updateRecentLessonsUI
    }

    private fun updateRecentLessonsUI(recentLessons: List<Pair<Lesson, UserLessonProgress>>) {
        if (recentLessons.isNotEmpty()) {
            // Update first lesson
            val (lesson1, progress1) = recentLessons[0]
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
                viewBinding.cvRecentLesson2.visibility = View.GONE
            }
        } else {
            // Hide both cards if no recent lessons
            viewBinding.cvRecentLesson1.visibility = View.GONE
            viewBinding.cvRecentLesson2.visibility = View.GONE
        }
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
        // Show the card
        cardView.visibility = View.VISIBLE

        // Load lesson image
        Glide.with(this)
            .load(lesson.imageUrl)
            .placeholder(R.drawable.img_ob1)
            .error(R.drawable.img_ob1)
            .centerCrop()
            .into(imageView)

        // Set lesson title
        titleView.text = lesson.title

        // Set total word count in this lesson
        numberView.text = "${lesson.vocabulary.size} words"

        // Set lesson info and score
        difficultyView.text = "Score: ${progress.bestScore}"

        // Set words learned out of total words in lesson
        progressView.text = "${progress.wordsLearned}/${lesson.vocabulary.size}"

        // Set click listener to navigate to lesson detail
        cardView.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeToLessonDetail(lesson)
            findNavController().navigate(action)
        }
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

}
