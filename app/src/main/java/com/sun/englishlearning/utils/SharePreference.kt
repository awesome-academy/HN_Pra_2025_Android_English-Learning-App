package com.sun.englishlearning.utils

import android.content.Context

object SharePreference {

    private const val PREFS_NAME = "app_prefs"
    private const val KEY_ONBOARDING_SEEN = "key_onboarding_seen"

    fun setOnboardingSeen(context: Context, seen: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ONBOARDING_SEEN, seen).apply()
    }

    fun isOnboardingSeen(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ONBOARDING_SEEN, false)
    }
}
