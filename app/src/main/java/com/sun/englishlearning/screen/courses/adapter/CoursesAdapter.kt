package com.sun.englishlearning.screen.courses.adapter

import android.annotation.SuppressLint
import android.util.Log
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
        if (position in lessons.indices) {
            holder.bind(lessons[position])
        } else {
            Log.e("CoursesAdapter", "Invalid position: $position, lessons size: ${lessons.size}")
        }
    }

    override fun getItemCount(): Int = lessons.size

    // Update lessons with progress and words learned
    @SuppressLint("NotifyDataSetChanged")
    fun updateLessonsWithProgress(newLessons: List<Lesson>?, progressMap: Map<String, Int>?, wordsLearnedMap: Map<String, Int>?) {
        val safeLessons = newLessons?.filter {
            it.id.isNotEmpty() && it.title.isNotEmpty()
        } ?: emptyList()
        val safeProgressMap = progressMap ?: emptyMap()
        val safeWordsLearnedMap = wordsLearnedMap ?: emptyMap()

        val oldSize = lessons.size
        lessons = safeLessons
        userProgressMap = safeProgressMap
        this.wordsLearnedMap = safeWordsLearnedMap

        if (oldSize == 0 && lessons.isNotEmpty()) {
            notifyItemRangeInserted(0, lessons.size)
        } else if (oldSize > 0 && lessons.isEmpty()) {
            notifyItemRangeRemoved(0, oldSize)
        } else {
            notifyDataSetChanged()
        }
    }

    inner class LessonViewHolder(private val binding: ItemLessonCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(lesson: Lesson) {
            binding.apply {
                // Set lesson title with null safety
                textLessonTitle.text = lesson.title.takeIf { it.isNotEmpty() }
                    ?: root.context.getString(R.string.unknown_lesson)

                // Get progress data from userProgressMap with bounds checking
                val progress = userProgressMap[lesson.id]?.coerceIn(0, 100) ?: 0
                progressLesson.progress = progress

                // Get actual words learned from wordsLearnedMap or calculate from progress
                val totalWords = lesson.vocabulary.size.coerceAtLeast(0)
                val wordsLearned = if (wordsLearnedMap.containsKey(lesson.id)) {
                    // Use actual words learned count if available
                    (wordsLearnedMap[lesson.id] ?: 0).coerceIn(0, totalWords)
                } else {
                    // Fallback: calculate from percentage
                    if (totalWords > 0) {
                        ((progress * totalWords) / 100).coerceIn(0, totalWords)
                    } else {
                        0
                    }
                }

                // Set points display with actual word count using string resource
                textLessonPoints.text = root.context.getString(
                    R.string.words_progress_format,
                    wordsLearned,
                    totalWords
                )

                // Load lesson image using Glide with error handling
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

                // Set click listener with safe handling
                root.setOnClickListener {
                    if (lesson.id.isNotEmpty() && lesson.title.isNotEmpty()) {
                        Log.d("CoursesAdapter", "Lesson clicked: ${lesson.title}, ID: ${lesson.id}")
                        onLessonClick(lesson)
                    } else {
                        Log.e("CoursesAdapter", "Invalid lesson clicked: $lesson")
                    }
                }
            }
        }
    }
}
