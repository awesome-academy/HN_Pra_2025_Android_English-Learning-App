package com.sun.englishlearning.screen.me

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.sun.englishlearning.MainActivity
import com.sun.englishlearning.R
import com.google.firebase.auth.FirebaseAuth
import com.sun.englishlearning.data.repository.UserLessonProgressRepositoryImpl
import com.sun.englishlearning.data.repository.LessonRepositoryImpl
import kotlinx.coroutines.runBlocking

class ReminderReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationType = intent.getStringExtra("notification_type") ?: "daily"
        
        // Create intent to open app when notification is tapped
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            appIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification based on type
        val notification = when (notificationType) {
            "today" -> {
                NotificationCompat.Builder(context, NotificationsFragment.CHANNEL_ID)
                    .setContentTitle("Today's Learning Reminder")
                    .setContentText("Time to practice English today! Tap to start learning.")
                    .setSmallIcon(R.drawable.ic_book)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()
            }
            else -> {
                // Enrich daily reminder with recent course and words learned
                                 val enrichedContent: String = runBlocking {
                     val user = FirebaseAuth.getInstance().currentUser
                     if (user == null) {
                         context.getString(R.string.daily_learning_reminder_default)
                     } else {
                         try {
                             val repo = UserLessonProgressRepositoryImpl()
                             val progressListResult = repo.getUserProgressByUser(user.uid)
                             val progressList = progressListResult.getOrNull()
                             val latest = progressList?.firstOrNull()
                             if (latest != null) {
                                 // Get lesson name from repository
                                 val lessonRepo = LessonRepositoryImpl(context, repo)
                                 val lessonResult = lessonRepo.getLesson(latest.lessonId)
                                 val lessonName = if (lessonResult.isSuccess) {
                                     lessonResult.getOrNull()?.title ?: context.getString(R.string.recent_course_fallback)
                                 } else {
                                     context.getString(R.string.recent_course_fallback)
                                 }
                                 val learned = latest.wordsLearned
                                 val total = latest.totalWords
                                 context.getString(R.string.daily_learning_reminder_with_progress, lessonName, learned, total)
                             } else {
                                 context.getString(R.string.daily_learning_reminder_default)
                             }
                         } catch (e: Exception) {
                             context.getString(R.string.daily_learning_reminder_default)
                         }
                     }
                 }

                                 NotificationCompat.Builder(context, NotificationsFragment.CHANNEL_ID)
                     .setContentTitle(context.getString(R.string.daily_learning_reminder_title))
                     .setContentText(enrichedContent)
                    .setSmallIcon(R.drawable.ic_book)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()
            }
        }
        
        // Use different notification IDs for different types
        val notificationId = when (notificationType) {
            "today" -> TODAY_NOTIFICATION_ID
            else -> DAILY_NOTIFICATION_ID
        }
        
        notificationManager.notify(notificationId, notification)
        
        // If it's a today notification, automatically disable it after showing
        if (notificationType == "today") {
            // You can add logic here to automatically disable the today notification
            // For example, save to SharedPreferences that today's notification has been shown
        }
    }
    
    companion object {
        private const val TODAY_NOTIFICATION_ID = 1
        private const val DAILY_NOTIFICATION_ID = 2
    }
}
