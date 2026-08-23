package com.example

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppThemeMode
import com.example.ui.screens.SheikhAppRoot
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SheikhViewModel

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle permission result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        CrashReporter.setup(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val lastCrash = CrashReporter.getLastCrash(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                try {
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        setContent {
            val sheikhViewModel: SheikhViewModel = viewModel()
            val uiState by sheikhViewModel.uiState.collectAsStateWithLifecycle()
            val systemInDark = isSystemInDarkTheme()
            
            val isDark = when (uiState.userPreferences.themeMode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.SYSTEM -> systemInDark
            }

            MyApplicationTheme(
                darkTheme = isDark,
                fontScale = uiState.userPreferences.fontScale
            ) {
                if (lastCrash != null) {
                    var showDialog by remember { mutableStateOf(true) }
                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            confirmButton = {
                                Button(onClick = { showDialog = false }) {
                                    Text("موافق")
                                }
                            },
                            title = { Text("App Crashed Last Time") },
                            text = { Text(lastCrash, modifier = Modifier.padding(16.dp)) }
                        )
                    }
                }
                SheikhAppRoot(viewModel = sheikhViewModel)
            }
        }
    }
}


