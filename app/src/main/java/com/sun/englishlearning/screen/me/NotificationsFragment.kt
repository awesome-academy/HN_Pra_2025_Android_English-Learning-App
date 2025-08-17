package com.sun.englishlearning.screen.me

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.sun.englishlearning.MainActivity
import com.sun.englishlearning.R
import com.sun.englishlearning.databinding.FragmentNotificationsBinding
import com.sun.englishlearning.utils.base.BaseFragment
import java.util.Calendar

class NotificationsFragment : BaseFragment<FragmentNotificationsBinding>() {
    
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            createNotificationChannel()
            Toast.makeText(requireContext(), "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            viewBinding.switchNotifications.isChecked = false
            Toast.makeText(requireContext(), "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNotificationsBinding = FragmentNotificationsBinding.inflate(layoutInflater, container, false)

    override fun initData() {
        // Initialize default values
        viewBinding.radioAm.isChecked = true
        loadNotificationSettings()
    }

    override fun initView() {
        setupClickListeners()
        setupTimePicker()
    }

    private fun setupClickListeners() {
        // Back button
        viewBinding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Notifications switch
        viewBinding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkNotificationPermission()
                scheduleTodayNotification()
            } else {
                cancelTodayNotification()
            }
        }

        // Reminder switch
        viewBinding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (viewBinding.switchNotifications.isChecked) {
                    scheduleDailyReminder()
                } else {
                    viewBinding.switchReminder.isChecked = false
                    Toast.makeText(requireContext(), "Please enable notifications first", Toast.LENGTH_SHORT).show()
                }
            } else {
                cancelDailyReminder()
            }
        }

        // Save button
        viewBinding.btnSave.setOnClickListener {
            saveNotificationSettings()
        }

        // Test button (temporary)
        viewBinding.btnSave.setOnLongClickListener {
            testNotification()
            true
        }
    }

    private fun setupTimePicker() {
        // Set default time to 9:00 AM
        viewBinding.timePicker.hour = 9
        viewBinding.timePicker.minute = 0
    }

    private fun checkNotificationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                createNotificationChannel()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                showNotificationPermissionDialog()
            }
            else -> {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showNotificationPermissionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Notification Permission")
            .setMessage("This app needs notification permission to send you learning reminders. Please grant the permission.")
            .setPositiveButton("Grant") { _, _ ->
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton("Cancel") { _, _ ->
                viewBinding.switchNotifications.isChecked = false
            }
            .show()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Learning Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders to learn English"
            }
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleTodayNotification() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), ReminderReceiver::class.java).apply {
            putExtra("notification_type", "today")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            TODAY_NOTIFICATION_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, viewBinding.timePicker.hour)
        calendar.set(Calendar.MINUTE, viewBinding.timePicker.minute)
        calendar.set(Calendar.SECOND, 0)

        // If time has passed today, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Debug: Check if alarm is scheduled
        android.util.Log.d("NotificationsFragment", "Scheduling today notification for: ${calendar.time}")
        android.util.Log.d("NotificationsFragment", "Current time: ${Calendar.getInstance().time}")
        android.util.Log.d("NotificationsFragment", "Time difference: ${calendar.timeInMillis - System.currentTimeMillis()} ms")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        // Verify alarm is set
        val nextAlarm = alarmManager.nextAlarmClock
        android.util.Log.d("NotificationsFragment", "Next alarm: $nextAlarm")

        Toast.makeText(requireContext(), "Today notification scheduled for ${calendar.time}", Toast.LENGTH_LONG).show()
    }

    private fun cancelTodayNotification() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            TODAY_NOTIFICATION_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleDailyReminder() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), ReminderReceiver::class.java).apply {
            putExtra("notification_type", "daily")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            DAILY_REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, viewBinding.timePicker.hour)
        calendar.set(Calendar.MINUTE, viewBinding.timePicker.minute)
        calendar.set(Calendar.SECOND, 0)

        // If time has passed today, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        android.util.Log.d("NotificationsFragment", "Daily reminder scheduled for ${calendar.time}")
        Toast.makeText(requireContext(), "Daily reminder scheduled for ${calendar.time}", Toast.LENGTH_LONG).show()
    }

    private fun cancelDailyReminder() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            DAILY_REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    // Test function - call this to test notification immediately
    private fun testNotification() {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create intent to open app when notification is tapped
        val appIntent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            requireContext(),
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val notification = androidx.core.app.NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setContentTitle("Test Notification")
            .setContentText("This is a test notification from English Learning App")
            .setSmallIcon(R.drawable.ic_book)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(999, notification)
        Toast.makeText(requireContext(), "Test notification sent!", Toast.LENGTH_SHORT).show()
    }

    private fun cancelAllNotifications() {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
        cancelTodayNotification()
        cancelDailyReminder()
    }

    private fun saveNotificationSettings() {
        // Save settings to SharedPreferences or database
        val sharedPrefs = requireContext().getSharedPreferences("notification_settings", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putBoolean("notifications_enabled", viewBinding.switchNotifications.isChecked)
            putBoolean("reminder_enabled", viewBinding.switchReminder.isChecked)
            putInt("reminder_hour", viewBinding.timePicker.hour)
            putInt("reminder_minute", viewBinding.timePicker.minute)
            putString("reminder_type", when (viewBinding.radioGroupAmpm.checkedRadioButtonId) {
                R.id.radio_am -> "am"
                R.id.radio_pm -> "pm"
                R.id.radio_off -> "off"
                else -> "am"
            })
            apply()
        }

        Toast.makeText(requireContext(), "Notification settings saved successfully!", Toast.LENGTH_SHORT).show()
        
        // Navigate back to previous screen
        requireActivity().onBackPressed()
    }

    private fun loadNotificationSettings() {
        val sharedPrefs = requireContext().getSharedPreferences("notification_settings", Context.MODE_PRIVATE)
        
        // Load saved settings
        val notificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", false)
        val reminderEnabled = sharedPrefs.getBoolean("reminder_enabled", false)
        val reminderHour = sharedPrefs.getInt("reminder_hour", 9)
        val reminderMinute = sharedPrefs.getInt("reminder_minute", 0)
        val reminderType = sharedPrefs.getString("reminder_type", "am") ?: "am"
        
        // Apply settings to UI
        viewBinding.switchNotifications.isChecked = notificationsEnabled
        viewBinding.switchReminder.isChecked = reminderEnabled
        viewBinding.timePicker.hour = reminderHour
        viewBinding.timePicker.minute = reminderMinute
        
        when (reminderType) {
            "am" -> viewBinding.radioAm.isChecked = true
            "pm" -> viewBinding.radioPm.isChecked = true
            "off" -> viewBinding.radioOff.isChecked = true
        }
    }

    companion object {
        const val CHANNEL_ID = "learning_reminders"
        private const val TODAY_NOTIFICATION_REQUEST_CODE = 123
        private const val DAILY_REMINDER_REQUEST_CODE = 456
    }
}
