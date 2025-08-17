package com.sun.englishlearning.screen.courses.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.sun.englishlearning.R
import com.sun.englishlearning.databinding.ItemLessonCardBinding
import com.sun.englishlearning.data.model.Lesson

class CoursesAdapter(
    private var lessons: List<Lesson> = emptyList(),
    private var userProgressMap: Map<String, Int> = emptyMap(), // Map of lessonId to progress percentage
    private val onLessonClick: (Lesson) -> Unit = {}
) : RecyclerView.Adapter<CoursesAdapter.LessonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val binding = ItemLessonCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LessonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        holder.bind(lessons[position])
    }

    override fun getItemCount(): Int = lessons.size

    fun updateLessons(newLessons: List<Lesson>, progressMap: Map<String, Int> = emptyMap()) {
        lessons = newLessons
        userProgressMap = progressMap
        notifyDataSetChanged()
    }
    
    // New method for updating lessons with progress
    fun updateLessonsWithProgress(newLessons: List<Lesson>, progressMap: Map<String, Int>) {
        lessons = newLessons
        userProgressMap = progressMap
        notifyDataSetChanged()
    }

    inner class LessonViewHolder(private val binding: ItemLessonCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(lesson: Lesson) {
            binding.apply {
                // Set lesson title
                textLessonTitle.text = lesson.title

                // Set progress from userProgressMap
                val progress = userProgressMap[lesson.id] ?: 0
                progressLesson.progress = progress

                // Set points display
                textLessonPoints.text = "$progress / 10 words"

                // Load lesson image using Glide
                if (lesson.imageUrl.isNotEmpty()) {
                    Glide.with(binding.root.context)
                        .load(lesson.imageUrl)
                        .apply(RequestOptions().transform(RoundedCorners(16)))
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(imageLesson)
                } else {
                    imageLesson.setImageResource(R.drawable.ic_launcher_background)
                }

                // Set click listener
                root.setOnClickListener {
                    onLessonClick(lesson)
                }
            }
        }
    }
}
