package com.example

import android.app.Application

class SheikhApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashReporter.setup(this)
    }
}
