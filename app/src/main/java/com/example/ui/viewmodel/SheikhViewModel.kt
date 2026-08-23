package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.local.AppThemeMode
import com.example.data.local.UserPreferences
import com.example.data.local.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SheikhViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        UiState(
            userPreferences = UserPreferences(
                themeMode = AppThemeMode.SYSTEM,
                fontScale = 1.0f
            )
        )
    )
    val uiState: StateFlow<UiState> = _uiState
}
