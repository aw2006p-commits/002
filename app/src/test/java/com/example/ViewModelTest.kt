package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ui.viewmodel.SheikhViewModel
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViewModelTest {
    @Test
    fun testViewModelInit() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = SheikhViewModel(app)
        println("ViewModel initialized successfully")
    }
}
