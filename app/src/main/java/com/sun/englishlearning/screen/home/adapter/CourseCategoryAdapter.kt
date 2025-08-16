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
        categories.clear()
        categories.addAll(newCategories)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.tvCategoryTitle)
        private val iconImage: ImageView = itemView.findViewById(R.id.ivCategoryIcon)
        private val lessonCountText: TextView = itemView.findViewById(R.id.tvLessonCount)

        fun bind(category: CourseCategory) {
            titleText.text = category.title
            
            // Load image from URL using Glide
            Glide.with(itemView.context)
                .load(category.imageUrl)
                .placeholder(R.drawable.ic_book)
                .error(R.drawable.ic_book)
                .centerCrop()
                .into(iconImage)
            
            // Display number of words in the lesson instead of lesson count
            val wordCount = if (category.lessons.isNotEmpty()) {
                category.lessons.first().wordIds.size
            } else {
                0
            }
            lessonCountText.text = "$wordCount words"
            
            itemView.setOnClickListener {
                onCategoryClick(category)
            }
        }
    }
}