package com.sun.englishlearning.screen.me

import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.credentials.CredentialManager
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sun.englishlearning.R
import com.sun.englishlearning.databinding.FragmentMeBinding
import com.sun.englishlearning.utils.base.BaseFragment
import java.util.Calendar

class MeFragment : BaseFragment<FragmentMeBinding>() {

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMeBinding {
        return FragmentMeBinding.inflate(inflater, container, false)
    }

    override fun initData() {
        auth = Firebase.auth
        credentialManager = CredentialManager.create(requireContext())
    }

    override fun initView() {
        // Settings -> navigate to profile screen
        viewBinding.icSettings.setOnClickListener {
            findNavController().navigate(
                com.sun.englishlearning.R.id.action_navigation_me_to_navigation_profile
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Pass real data to the two circle progress views
        updateCircleProgressViews()
    }

    private fun updateCircleProgressViews() {
        // Right circle: number of days learned in the week
        val daysUsed = getDaysAppUsedThisWeek()
        val daysLearned = daysUsed.size
        viewBinding.circleDaysWeek.setProgress(daysLearned / 7f)
        viewBinding.circleDaysWeek.setText("$daysLearned/7")

        // Left circle: today's learning time -> show % of 3 hours (180 minutes)
        val minutesToday = getMinutesUsedToday()
        val targetMinutes = 3 * 60 // 3 hours = 180 minutes

        // progress must be 0..1
        val progress = (minutesToday.toFloat() / targetMinutes).coerceAtMost(1f).coerceAtLeast(0f)
        viewBinding.circleTimeToday.setProgress(progress)

        // percent 0..100 (int)
        val percent = ((minutesToday * 100f) / targetMinutes).toInt().coerceAtMost(100).coerceAtLeast(0)

        // text: e.g. "1h 20m (44%)" — bạn có thể đổi chỉ hiện % hoặc chỉ thời gian
        viewBinding.circleTimeToday.setText("$percent%")
    }

    private fun getMinutesUsedToday(): Int {
        val usageStatsManager = requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val packageName = requireContext().packageName
        val pm = requireContext().packageManager

        // start of today
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        val now = System.currentTimeMillis()

        // get install time (safety)
        val installTime = try {
            pm.getPackageInfo(packageName, 0).firstInstallTime
        } catch (e: PackageManager.NameNotFoundException) {
            // shouldn't happen for self package, nhưng an toàn
            0L
        }

        // query events
        val events: UsageEvents = try {
            usageStatsManager.queryEvents(startOfDay, now)
        } catch (e: Exception) {
            // no permission or other error — trả về 0
            return 0
        }
        val event = UsageEvents.Event()

        var lastForegroundStart: Long = -1L
        var totalForeground: Long = 0L

        while (events.hasNextEvent()) {
            if (!events.getNextEvent(event)) continue
            val pkg = event.packageName ?: continue

            if (pkg != packageName) continue

            val ts = event.timeStamp
            if (ts < installTime) continue

            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND,
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    if (lastForegroundStart == -1L) {
                        lastForegroundStart = maxOf(ts, startOfDay)
                    }
                }

                UsageEvents.Event.MOVE_TO_BACKGROUND,
                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    if (lastForegroundStart != -1L) {
                        val clippedStart = maxOf(lastForegroundStart, startOfDay)
                        val clippedEnd = minOf(ts, now)
                        if (clippedEnd > clippedStart) {
                            totalForeground += (clippedEnd - clippedStart)
                        }
                        lastForegroundStart = -1L
                    }
                }
            }
        }

        if (lastForegroundStart != -1L) {
            totalForeground += (now - lastForegroundStart)
        }

        return (totalForeground / 1000 / 60).toInt()
    }


    private val usagePermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        checkUsageStatsPermission(showDialog = false)
    }

    override fun onStart() {
        super.onStart()
        // Check user state and update UI when the fragment is displayed
        updateUI(auth.currentUser)
        checkUsageStatsPermission(showDialog = true)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user!= null) {
            // User is logged in: display personal information
            viewBinding.layoutUserProfile.visibility = View.VISIBLE
            viewBinding.tvUser.text = user.displayName
        } else {
            // User is not logged in (rare case on this screen): hide information
            viewBinding.layoutUserProfile.visibility = View.GONE
        }
    }

    private fun checkUsageStatsPermission(showDialog: Boolean) {
        val appOps = requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                requireContext().packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                requireContext().packageName
            )
        }
        if (mode != AppOpsManager.MODE_ALLOWED) {
            if (showDialog) showUsagePermissionDialog()
        } else {
            updateDaysLearnedUI(getDaysAppUsedThisWeek())
        }
    }

    private fun showUsagePermissionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.usage_permission_title)
            .setMessage(R.string.usage_permission_message)
            .setPositiveButton(R.string.usage_permission_grant) { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                usagePermissionLauncher.launch(intent)
            }
            .setNegativeButton(R.string.usage_permission_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun getDaysAppUsedThisWeek(): Set<Int> {
        val usageStatsManager = requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()

        // start of week (monday)
        val todayDow = calendar.get(Calendar.DAY_OF_WEEK)
        val diffToMonday = if (todayDow == Calendar.SUNDAY) -6 else Calendar.MONDAY - todayDow
        calendar.add(Calendar.DATE, diffToMonday)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        // end = start + 6 day 23:59:59.999
        val endCal = Calendar.getInstance()
        endCal.timeInMillis = startTime
        endCal.add(Calendar.DATE, 6)
        endCal.set(Calendar.HOUR_OF_DAY, 23)
        endCal.set(Calendar.MINUTE, 59)
        endCal.set(Calendar.SECOND, 59)
        endCal.set(Calendar.MILLISECOND, 999)
        val endTime = endCal.timeInMillis

        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        val daysUsed = mutableSetOf<Int>()
        val packageName = requireContext().packageName

        for (stat in stats) {
            if (stat.packageName == packageName && stat.totalTimeInForeground > 0) {
                val cal = Calendar.getInstance()
                cal.timeInMillis = stat.lastTimeStamp
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                val mappedDay = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1
                daysUsed.add(mappedDay)
            }
        }
        return daysUsed
    }


    private fun updateDaysLearnedUI(daysUsed: Set<Int>) {
        val dayViews = listOf(
            viewBinding.progDay1,
            viewBinding.progDay2,
            viewBinding.progDay3,
            viewBinding.progDay4,
            viewBinding.progDay5,
            viewBinding.progDay6,
            viewBinding.progDay7
        )
        for (i in 1..7) {
            val bg = if (daysUsed.contains(i)) R.drawable.bg_circle_blue else R.drawable.bg_circle_gray
            dayViews[i - 1].setBackgroundResource(bg)
        }
    }
}
