package com.sun.englishlearning.screen.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sun.englishlearning.R
import com.sun.englishlearning.data.model.Lesson

data class CourseCategory(
    val title: String,
    val imageUrl: String,
    val lessons: List<Lesson> = emptyList()
)

class CourseCategoryAdapter(
    private val onCategoryClick: (CourseCategory) -> Unit
) : RecyclerView.Adapter<CourseCategoryAdapter.CategoryViewHolder>() {

    private val categories = mutableListOf<CourseCategory>()

    fun updateCategories(newCategories: List<CourseCategory>) {
        println("CourseCategoryAdapter: updateCategories called with ${newCategories.size} categories")
        newCategories.forEachIndexed { index, category ->
            println("Category $index: ${category.title}")
        }
        categories.clear()
        categories.addAll(newCategories)
        println("CourseCategoryAdapter: categories list now has ${categories.size} items")
        notifyDataSetChanged()
        println("CourseCategoryAdapter: notifyDataSetChanged() called")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        println("CourseCategoryAdapter: onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        println("CourseCategoryAdapter: onBindViewHolder called for position $position")
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int {
        println("CourseCategoryAdapter: getItemCount called, returning ${categories.size}")
        return categories.size
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.tvCategoryTitle)
        private val iconImage: ImageView = itemView.findViewById(R.id.ivCategoryIcon)
        private val lessonCountText: TextView = itemView.findViewById(R.id.tvLessonCount)

        fun bind(category: CourseCategory) {
            println("CategoryViewHolder: bind called for ${category.title}")
            titleText.text = category.title
            
            // Load image from URL using Glide
            Glide.with(itemView.context)
                .load(category.imageUrl)
                .placeholder(R.drawable.ic_book)
                .error(R.drawable.ic_book)
                .centerCrop()
                .into(iconImage)
            
            lessonCountText.text = "${category.lessons.size} lessons"
            
            itemView.setOnClickListener {
                onCategoryClick(category)
            }
            println("CategoryViewHolder: bind completed for ${category.title}")
        }
    }
}