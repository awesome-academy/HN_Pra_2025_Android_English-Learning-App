package com.sun.englishlearning.screen.courses.adapter

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
        // Add bounds checking to prevent crashes
        if (position >= 0 && position < lessons.size) {
            holder.bind(lessons[position])
        } else {
            Log.e("CoursesAdapter", "Invalid position: $position, lessons size: ${lessons.size}")
        }
    }

    override fun getItemCount(): Int = lessons.size

    fun updateLessons(newLessons: List<Lesson>?, progressMap: Map<String, Int>? = emptyMap()) {
        try {
            // Defensive null checks
            val safeLessons = newLessons?.filter {
                it.id.isNotEmpty() && it.title.isNotEmpty()
            } ?: emptyList()
            val safeProgressMap = progressMap ?: emptyMap()

            val oldSize = lessons.size
            lessons = safeLessons
            userProgressMap = safeProgressMap
            wordsLearnedMap = emptyMap()

            // Use efficient notify methods
            if (oldSize == 0 && lessons.isNotEmpty()) {
                notifyItemRangeInserted(0, lessons.size)
            } else if (oldSize > 0 && lessons.isEmpty()) {
                notifyItemRangeRemoved(0, oldSize)
            } else {
                notifyDataSetChanged()
            }
        } catch (e: Exception) {
            Log.e("CoursesAdapter", "Error updating lessons", e)
            lessons = emptyList()
            userProgressMap = emptyMap()
            wordsLearnedMap = emptyMap()
            notifyDataSetChanged()
        }
    }

    // Update lessons with progress and words learned
    fun updateLessonsWithProgress(newLessons: List<Lesson>?, progressMap: Map<String, Int>?, wordsLearnedMap: Map<String, Int>?) {
        try {
            // Defensive null checks and validation
            val safeLessons = newLessons?.filter {
                it.id.isNotEmpty() && it.title.isNotEmpty()
            } ?: emptyList()
            val safeProgressMap = progressMap ?: emptyMap()
            val safeWordsLearnedMap = wordsLearnedMap ?: emptyMap()

            val oldSize = lessons.size
            lessons = safeLessons
            userProgressMap = safeProgressMap
            this.wordsLearnedMap = safeWordsLearnedMap

            // Use efficient notify methods
            if (oldSize == 0 && lessons.isNotEmpty()) {
                notifyItemRangeInserted(0, lessons.size)
            } else if (oldSize > 0 && lessons.isEmpty()) {
                notifyItemRangeRemoved(0, oldSize)
            } else {
                notifyDataSetChanged()
            }
        } catch (e: Exception) {
            Log.e("CoursesAdapter", "Error updating lessons with progress", e)
            lessons = emptyList()
            userProgressMap = emptyMap()
            this.wordsLearnedMap = emptyMap()
            notifyDataSetChanged()
        }
    }

    inner class LessonViewHolder(private val binding: ItemLessonCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(lesson: Lesson) {
            try {
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
                        try {
                            Glide.with(binding.root.context)
                                .load(lesson.imageUrl)
                                .apply(RequestOptions().transform(RoundedCorners(16)))
                                .placeholder(R.drawable.ic_launcher_background)
                                .error(R.drawable.ic_launcher_background)
                                .into(imageLesson)
                        } catch (e: Exception) {
                            Log.w("CoursesAdapter", "Error loading image for lesson: ${lesson.title}", e)
                            imageLesson.setImageResource(R.drawable.ic_launcher_background)
                        }
                    } else {
                        imageLesson.setImageResource(R.drawable.ic_launcher_background)
                    }

                    // Set click listener with safe handling
                    root.setOnClickListener {
                        try {
                            Log.d("CoursesAdapter", "Lesson clicked: ${lesson.title}, ID: ${lesson.id}")
                            onLessonClick(lesson)
                        } catch (e: Exception) {
                            Log.e("CoursesAdapter", "Error handling lesson click", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CoursesAdapter", "Error binding lesson data", e)
                // Set fallback values to prevent blank items using string resources
                binding.textLessonTitle.text = binding.root.context.getString(R.string.error_loading_lesson)
                binding.textLessonPoints.text = binding.root.context.getString(R.string.words_progress_error)
                binding.progressLesson.progress = 0
                binding.imageLesson.setImageResource(R.drawable.ic_launcher_background)
            }
        }
    }
}
