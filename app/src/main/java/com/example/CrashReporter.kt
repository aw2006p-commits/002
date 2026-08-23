package com.example

import android.content.Context
import android.util.Log

object CrashReporter {
    fun setup(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            val prefs = context.getSharedPreferences("crash_prefs", Context.MODE_PRIVATE)
            val stackTrace = Log.getStackTraceString(exception)
            prefs.edit().putString("last_crash", stackTrace).commit()
            defaultHandler?.uncaughtException(thread, exception)
        }
    }
    
    fun getLastCrash(context: Context): String? {
        val prefs = context.getSharedPreferences("crash_prefs", Context.MODE_PRIVATE)
        val crash = prefs.getString("last_crash", null)
        prefs.edit().remove("last_crash").apply()
        return crash
    }
}
