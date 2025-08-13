package com.sun.englishlearning.screen.recent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sun.englishlearning.R
import com.sun.englishlearning.databinding.ItemRecentCourseBinding

class RecentCoursesAdapter(
    private val onCourseClick: (RecentCourse) -> Unit
) : ListAdapter<RecentCourse, RecentCoursesAdapter.RecentCourseViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentCourseViewHolder {
        val binding = ItemRecentCourseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecentCourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentCourseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecentCourseViewHolder(
        private val binding: ItemRecentCourseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(course: RecentCourse) {
            binding.apply {
                tvCourseTitle.text = course.title
                tvLessonCount.text = root.context.getString(R.string.lesson_count, course.lessonCount)
                tvAdvancedLevel.text = root.context.getString(R.string.advanced_level, course.advancedLevel)
                tvProgress.text = root.context.getString(R.string.progress_format, course.progress, course.maxProgress)
                
                ivCourseImage.setImageResource(course.imageResId)
                
                progressCourse.max = course.maxProgress
                progressCourse.progress = course.progress
                
                root.setOnClickListener {
                    onCourseClick(course)
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<RecentCourse>() {
        override fun areItemsTheSame(oldItem: RecentCourse, newItem: RecentCourse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RecentCourse, newItem: RecentCourse): Boolean {
            return oldItem == newItem
        }
    }
}
