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

/**
 * NotificationsFragment
 *
 * Cleaned and optimized implementation for scheduling/cancelling two types of alarms:
 * - "today" one-shot notification
 * - "daily" repeating reminder (scheduled as exact daily alarm)
 *
 * Behavior notes:
 * - Reads/writes notification settings from SharedPreferences (namespace: "notification_settings").
 * - Defaults: notifications disabled, reminder disabled, default time = 09:00 (9 AM), reminder_type = "am".
 * - Requests runtime POST_NOTIFICATIONS permission on Android 13+ when needed.
 * - Creates notification channel on Android O+.
 */
class NotificationsFragment : BaseFragment<FragmentNotificationsBinding>() {

    private val prefsName = "notification_settings"

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            createNotificationChannel()
            // Apply saved preferences after permission granted (schedule alarms if needed)
            applySavedSettingsToAlarms()
            Toast.makeText(requireContext(), "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            // If user denies permission, disable notifications in UI and cancel any scheduled alarms
            viewBinding.switchNotifications.isChecked = false
            cancelAllNotifications()
            Toast.makeText(requireContext(), "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentNotificationsBinding.inflate(inflater, container, false)

    override fun initData() {
        // Load stored settings (or defaults) into UI
        loadNotificationSettings()
    }

    override fun initView() {
        setupClickListeners()
        // Do not override timePicker value here — loadNotificationSettings sets default 09:00 if absent
    }

    override fun onStart() {
        super.onStart()
        // Ensure alarms match saved preferences whenever the fragment becomes visible
        applySavedSettingsToAlarms()
    }

    // ------------------------- UI and listeners -------------------------
    private fun setupClickListeners() {
        viewBinding.btnBack.setOnClickListener { requireActivity().onBackPressed() }

        viewBinding.switchNotifications.setOnCheckedChangeListener { _, enabled ->
            if (enabled) {
                // If notifications enabled by user, ensure runtime permission (Android 13+)
                if (isNotificationPermissionGranted()) {
                    createNotificationChannel()
                    scheduleTodayNotification()
                    if (viewBinding.switchReminder.isChecked) scheduleDailyReminder()
                } else {
                    requestNotificationPermission()
                }
            } else {
                cancelTodayNotification()
                cancelDailyReminder()
            }
        }

        viewBinding.switchReminder.setOnCheckedChangeListener { _, enabled ->
            if (enabled) {
                if (!viewBinding.switchNotifications.isChecked) {
                    // Prevent enabling reminder when global notifications are off
                    viewBinding.switchReminder.setOnCheckedChangeListener(null)
                    viewBinding.switchReminder.isChecked = false
                    setupClickListeners() // reattach listeners
                    Toast.makeText(requireContext(), "Please enable notifications first", Toast.LENGTH_SHORT).show()
                    return@setOnCheckedChangeListener
                }

                if (isNotificationPermissionGranted()) {
                    scheduleDailyReminder()
                } else {
                    requestNotificationPermission()
                }
            } else {
                cancelDailyReminder()
            }
        }

        viewBinding.radioGroupAmpm.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_off -> {
                    // Turn everything off immediately
                    viewBinding.switchNotifications.isChecked = false
                    viewBinding.switchReminder.isChecked = false
                    cancelTodayNotification()
                    cancelDailyReminder()
                    Toast.makeText(requireContext(), "All notifications turned off", Toast.LENGTH_SHORT).show()
                }
                // AM/PM do not change scheduling behavior in this basic implementation;
                // If you want to show different content/time windows for AM/PM, add logic here.
            }
        }

        viewBinding.btnSave.setOnClickListener {
            saveNotificationSettings()
        }

        viewBinding.btnSave.setOnLongClickListener {
            testNotification()
            true
        }
    }

    // ------------------------- Permission helpers -------------------------
    private fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // no runtime permission on older Android versions
            createNotificationChannel()
            applySavedSettingsToAlarms()
            return
        }

        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                createNotificationChannel()
                applySavedSettingsToAlarms()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Notification Permission")
                    .setMessage("This app needs notification permission to send you learning reminders. Please grant the permission.")
                    .setPositiveButton("Grant") { _, _ -> notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
                    .setNegativeButton("Cancel") { _, _ -> viewBinding.switchNotifications.isChecked = false }
                    .show()
            }
            else -> {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // ------------------------- Alarm scheduling -------------------------
    private fun prefs() = requireContext().getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Learning Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Daily reminders to learn English" }
            val nm = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleTodayNotification() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = buildPendingIntent(TODAY_NOTIFICATION_REQUEST_CODE, "today")

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, viewBinding.timePicker.hour)
            set(Calendar.MINUTE, viewBinding.timePicker.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }

        // Use exact alarm when available
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
        Toast.makeText(requireContext(), "Today notification scheduled for ${calendar.time}", Toast.LENGTH_LONG).show()
    }

    private fun cancelTodayNotification() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = buildPendingIntent(TODAY_NOTIFICATION_REQUEST_CODE, "today")
        alarmManager.cancel(pending)
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleDailyReminder() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = buildPendingIntent(DAILY_REMINDER_REQUEST_CODE, "daily")

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, viewBinding.timePicker.hour)
            set(Calendar.MINUTE, viewBinding.timePicker.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }

        // For daily repeating exact alarm, schedule next occurrence and rely on receiver to reschedule the next day
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
    }

    private fun cancelDailyReminder() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = buildPendingIntent(DAILY_REMINDER_REQUEST_CODE, "daily")
        alarmManager.cancel(pending)
    }

    // Build a PendingIntent for ReminderReceiver with requestCode and type extra.
    // Using FLAG_UPDATE_CURRENT makes it safe to schedule/reschedule the same alarm.
    private fun buildPendingIntent(requestCode: Int, type: String): PendingIntent {
        val intent = Intent(requireContext(), ReminderReceiver::class.java).apply {
            putExtra("notification_type", type)
        }
        return PendingIntent.getBroadcast(
            requireContext(),
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // Cancel all notifications and alarms
    private fun cancelAllNotifications() {
        val nm = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancelAll()
        cancelTodayNotification()
        cancelDailyReminder()
    }

    // ------------------------- Persistence -------------------------
    private fun saveNotificationSettings() {
        val editor = prefs().edit()
        editor.putBoolean("notifications_enabled", viewBinding.switchNotifications.isChecked)
        editor.putBoolean("reminder_enabled", viewBinding.switchReminder.isChecked)
        editor.putInt("reminder_hour", viewBinding.timePicker.hour)
        editor.putInt("reminder_minute", viewBinding.timePicker.minute)
        editor.putString(
            "reminder_type",
            when (viewBinding.radioGroupAmpm.checkedRadioButtonId) {
                R.id.radio_am -> "am"
                R.id.radio_pm -> "pm"
                R.id.radio_off -> "off"
                else -> "am"
            }
        )
        editor.apply()
        Toast.makeText(requireContext(), "Notification settings saved", Toast.LENGTH_SHORT).show()
        // Apply immediately after saving
        applySavedSettingsToAlarms()
        requireActivity().onBackPressed()
    }

    private fun loadNotificationSettings() {
        val p = prefs()
        val notificationsEnabled = p.getBoolean("notifications_enabled", false)
        val reminderEnabled = p.getBoolean("reminder_enabled", false)
        val reminderHour = p.getInt("reminder_hour", 9) // default 9 AM
        val reminderMinute = p.getInt("reminder_minute", 0)
        val reminderType = p.getString("reminder_type", "am") ?: "am"

        // Apply values to UI
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

    // Read prefs and schedule/cancel alarms accordingly. Called on start, after granting permission, and after saving.
    private fun applySavedSettingsToAlarms() {
        val p = prefs()
        val notificationsEnabled = p.getBoolean("notifications_enabled", false)
        val reminderEnabled = p.getBoolean("reminder_enabled", false)

        if (!notificationsEnabled) {
            cancelAllNotifications()
            return
        }

        if (!isNotificationPermissionGranted()) {
            requestNotificationPermission()
            return
        }

        // Ensure channel exists
        createNotificationChannel()

        // Ensure timePicker matches stored time before scheduling
        val hour = p.getInt("reminder_hour", viewBinding.timePicker.hour)
        val minute = p.getInt("reminder_minute", viewBinding.timePicker.minute)
        viewBinding.timePicker.hour = hour
        viewBinding.timePicker.minute = minute

        scheduleTodayNotification()
        if (reminderEnabled) scheduleDailyReminder() else cancelDailyReminder()
    }

    // ------------------------- Debug/Test -------------------------
    private fun testNotification() {
        val nm = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val appIntent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(requireContext(), 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setContentTitle("Test Notification")
            .setContentText("This is a test notification from English Learning App")
            .setSmallIcon(R.drawable.ic_book)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        nm.notify(999, notification)
        Toast.makeText(requireContext(), "Test notification sent!", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val CHANNEL_ID = "learning_reminders"
        private const val TODAY_NOTIFICATION_REQUEST_CODE = 123
        private const val DAILY_REMINDER_REQUEST_CODE = 456
    }
}
