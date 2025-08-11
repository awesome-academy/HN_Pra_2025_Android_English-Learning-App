package com.sun.englishlearning.screen.courses

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sun.englishlearning.databinding.ItemLessonCardBinding
import com.sun.englishlearning.data.model.Lesson

class LessonAdapter(
    private var lessons: List<Lesson> = emptyList(),
    private val onLessonClick: (Lesson) -> Unit = {}
) : RecyclerView.Adapter<LessonAdapter.LessonViewHolder>() {

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

    fun updateLessons(newLessons: List<Lesson>) {
        lessons = newLessons
        notifyDataSetChanged()
    }

    inner class LessonViewHolder(
        private val binding: ItemLessonCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(lesson: Lesson) {
            binding.apply {
                textLessonTitle.text = lesson.title
                textLessonNumber.text = "Lesson: ${lesson.lessonNumber}"
                textAdvancedLevel.text = "Advanced: ${lesson.advancedLevel}"
                textLessonPoints.text = "points: ${lesson.currentPoints} / ${lesson.totalPoints}"
                progressLesson.progress = lesson.progressPercentage
                
                // Set lesson image (placeholder for now)
                // imageLesson.setImageResource(lesson.imageRes)
                
                root.setOnClickListener {
                    onLessonClick(lesson)
                }
            }
        }
    }
}
