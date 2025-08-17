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
    private var wordsLearnedMap: Map<String, Int> = emptyMap(), // Map of lessonId to actual words learned count
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
        wordsLearnedMap = emptyMap()
        notifyDataSetChanged()
    }

    // Update lessons with progress and words learned
    fun updateLessonsWithProgress(newLessons: List<Lesson>, progressMap: Map<String, Int>, wordsLearnedMap: Map<String, Int>) {
        lessons = newLessons
        userProgressMap = progressMap
        this.wordsLearnedMap = wordsLearnedMap
        notifyDataSetChanged()
    }

    inner class LessonViewHolder(private val binding: ItemLessonCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(lesson: Lesson) {
            binding.apply {
                // Set lesson title
                textLessonTitle.text = lesson.title

                // Get progress data from userProgressMap
                val progress = userProgressMap[lesson.id] ?: 0
                progressLesson.progress = progress

                // Get actual words learned from wordsLearnedMap or calculate from progress
                val totalWords = lesson.vocabulary.size
                val wordsLearned = if (wordsLearnedMap.containsKey(lesson.id)) {
                    // Use actual words learned count if available
                    wordsLearnedMap[lesson.id] ?: 0
                } else {
                    // Fallback: calculate from percentage
                    if (totalWords > 0) {
                        (progress * totalWords) / 100
                    } else {
                        0
                    }
                }

                // Set points display with actual word count
                textLessonPoints.text = "$wordsLearned / $totalWords words"

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
