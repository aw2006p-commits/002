package com.example.data.local

enum class AppThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

data class UserPreferences(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val fontScale: Float = 1.0f
)

data class UiState(
    val userPreferences: UserPreferences = UserPreferences()
)
