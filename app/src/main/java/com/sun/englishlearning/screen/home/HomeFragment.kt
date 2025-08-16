package com.sun.englishlearning.screen.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.sun.englishlearning.R
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.data.repository.LessonRepository
import com.sun.englishlearning.data.repository.LessonRepositoryImpl
import com.sun.englishlearning.data.repository.UserLessonProgressRepository
import com.sun.englishlearning.data.repository.UserLessonProgressRepositoryImpl
import com.sun.englishlearning.databinding.FragmentHomeBinding
import com.sun.englishlearning.databinding.ItemSuggestedCourseBinding
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

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        setupClickListeners()
        updateStudyTime()
        setupSuggestedCourse()
    }

    override fun initData() {
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
                Toast.makeText(context, "See all courses", Toast.LENGTH_SHORT).show()
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
        // TODO: Implement proper user identification logic
        // This could come from SharedPreferences, Firebase Auth, or session management
        return "user_${System.currentTimeMillis() % 1000}" // Temporary placeholder
    }

}
