package com.sun.englishlearning

import android.view.LayoutInflater
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.sun.englishlearning.databinding.ActivityMainBinding
import com.sun.englishlearning.utils.base.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun inflateBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun initView() {
        // Tìm NavController từ NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Kết nối BottomNavigationView với NavController
        // Thao tác này sẽ tự động xử lý việc chuyển đổi Fragment khi bạn nhấn vào các mục
        binding.bottomNavigationView.setupWithNavController(navController)
    }

    override fun initData() {
        // Để trống
    }
}
