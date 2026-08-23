package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class UiStateData(
    val message: String = "Welcome"
)

class SheikhViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiStateData())
    val uiState: StateFlow<UiStateData> = _uiState
}
