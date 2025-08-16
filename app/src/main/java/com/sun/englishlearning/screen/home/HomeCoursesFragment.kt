package com.sun.englishlearning.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sun.englishlearning.R
import com.sun.englishlearning.data.model.Lesson
import com.sun.englishlearning.screen.home.adapter.CourseCategory
import com.sun.englishlearning.screen.home.adapter.CourseCategoryAdapter

class HomeCoursesFragment : Fragment(), HomeView {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var presenter: HomePresenter
    private lateinit var adapter: CourseCategoryAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_courses, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupRecyclerView()
        setupPresenter()
        
        // Load the course categories from Firebase
        presenter.loadCourseCategories()
    }
    
    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.rvCourseCategories)
        progressBar = view.findViewById(R.id.progressBar)
    }
    
    private fun setupRecyclerView() {
        adapter = CourseCategoryAdapter { category ->
            onCategoryClicked(category)
        }
        
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        recyclerView.adapter = adapter
    }
    
    private fun setupPresenter() {
        presenter = HomePresenter(this)
    }
    
    private fun onCategoryClicked(category: CourseCategory) {
        // Handle category click - navigate to lessons list or show lessons
        Toast.makeText(context, "Clicked: ${category.title} (${category.lessons.size} lessons)", Toast.LENGTH_SHORT).show()
        
        // You can navigate to a detailed view here
        // For example: navigate to lessons list for this category
    }
    
    // HomeView interface implementations
    override fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }
    
    override fun hideLoading() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
    
    override fun showCourseCategories(categories: List<CourseCategory>) {
        adapter.updateCategories(categories)
    }
    
    override fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    override fun navigateToLessonDetail(lesson: Lesson) {
        // Navigate to lesson detail
        Toast.makeText(context, "Navigate to lesson: ${lesson.title}", Toast.LENGTH_SHORT).show()
    }
}