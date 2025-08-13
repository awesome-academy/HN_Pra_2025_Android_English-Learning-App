package com.sun.englishlearning.utils

import android.content.Context

object AppPreferences {
    private const val PREFS_NAME = "EnglishAppPrefs"
    private const val KEY_FIRST_LAUNCH = "isFirstLaunch"

    fun isFirstLaunch(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true) 
    }

    fun setFirstLaunch(context: Context, isFirst: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, isFirst).apply()
    }
}
