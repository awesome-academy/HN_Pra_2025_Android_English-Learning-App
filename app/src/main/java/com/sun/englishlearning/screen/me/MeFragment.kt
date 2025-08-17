package com.sun.englishlearning.screen.me

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sun.englishlearning.MainActivity
import com.sun.englishlearning.databinding.FragmentMeBinding
import com.sun.englishlearning.utils.base.BaseFragment
import kotlinx.coroutines.launch
import android.widget.Toast
import android.app.usage.UsageStats
import com.sun.englishlearning.R
import java.util.Calendar
import android.app.AlertDialog
import androidx.activity.result.contract.ActivityResultContracts

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
        // Right circle: number of days learned in the week (already implemented in getDaysAppUsedThisWeek)
        val daysUsed = getDaysAppUsedThisWeek()
        val daysLearned = daysUsed.size
        viewBinding.circleDaysWeek.setProgress(daysLearned / 7f)
        viewBinding.circleDaysWeek.setText("$daysLearned/7")

        // Left circle: today's learning time (sum of app foreground time today)
        val minutesToday = getMinutesUsedToday()
        val targetMinutes = 120 // You can allow user to change this target if needed
        viewBinding.circleTimeToday.setProgress(minutesToday / targetMinutes.toFloat())
        viewBinding.circleTimeToday.setText("${minutesToday}p")
    }

    private fun getMinutesUsedToday(): Int {
        val usageStatsManager = requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        // Set to the start of today
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        // Set to the end of today
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endTime = calendar.timeInMillis
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
        val packageName = requireContext().packageName
        var totalForeground = 0L
        for (stat in stats) {
            if (stat.packageName == packageName) {
                totalForeground += stat.totalTimeInForeground
            }
        }
        // Convert from milliseconds to minutes
        return (totalForeground / 1000 / 60).toInt()
    }

    private val usagePermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // Khi quay lại từ màn hình cấp quyền, kiểm tra lại quyền và cập nhật UI
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
            // Đã có quyền, lấy dữ liệu sử dụng app
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
        // Lấy ngày đầu tuần (Thứ 2)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        // Kết thúc là cuối tuần (Chủ nhật 23:59:59)
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endTime = calendar.timeInMillis
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
        val daysUsed = mutableSetOf<Int>()
        val packageName = requireContext().packageName
        for (stat in stats) {
            if (stat.packageName == packageName && stat.totalTimeInForeground > 0) {
                val cal = Calendar.getInstance()
                cal.timeInMillis = stat.firstTimeStamp
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                // Chuyển đổi về thứ 2 = 1, ... chủ nhật = 7
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
