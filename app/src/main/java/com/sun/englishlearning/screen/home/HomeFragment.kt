package com.sun.englishlearning.screen.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sun.englishlearning.R
import com.sun.englishlearning.data.model.Lesson
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

            btnLetsStart.setOnClickListener {
                Toast.makeText(context, "Let's start learning!", Toast.LENGTH_SHORT).show()
            }

            tvSeeAllCourses.setOnClickListener {
                // Navigate to Courses tab
                findNavController().navigate(R.id.navigation_courses)
            }

            tvSeeAllRecent.setOnClickListener {
                Toast.makeText(context, "See all recent", Toast.LENGTH_SHORT).show()
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
            ivCourseImage.setImageResource(if (lesson.imageRes != 0) lesson.imageRes else R.drawable.img_ob3)
            
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
        
        if (currentUser == null) {
            // User is not signed in, show message and redirect to login
            Toast.makeText(context, "Please sign in to access course content", Toast.LENGTH_LONG).show()
            redirectToLogin()
        } else {
            // User is signed in, load the data
            loadCourseCategories()
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
