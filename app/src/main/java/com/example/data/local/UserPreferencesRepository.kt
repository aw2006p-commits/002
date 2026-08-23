package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    LIGHT, DARK, SYSTEM
}

data class UserPreferences(
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val fontScale: Float = 1.0f,
    val dailyGoalMinutes: Int = 30,
    val smartRewindEnabled: Boolean = true,
    val autoPlayNext: Boolean = true,
    val audioQualityKbps: Int = 128
)

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_settings_prefs", Context.MODE_PRIVATE)

    private val _userPreferencesFlow = MutableStateFlow(loadPreferences())
    val userPreferencesFlow: StateFlow<UserPreferences> = _userPreferencesFlow.asStateFlow()

    private fun loadPreferences(): UserPreferences {
        val themeStr = prefs.getString("theme_mode", AppThemeMode.LIGHT.name) ?: AppThemeMode.LIGHT.name
        val theme = try {
            AppThemeMode.valueOf(themeStr)
        } catch (e: Exception) {
            AppThemeMode.LIGHT
        }
        val fontScale = prefs.getFloat("font_scale", 1.0f)
        val dailyGoal = prefs.getInt("daily_goal_minutes", 30)
        val smartRewind = prefs.getBoolean("smart_rewind", true)
        val autoPlay = prefs.getBoolean("auto_play_next", true)
        val quality = prefs.getInt("audio_quality", 128)

        return UserPreferences(
            themeMode = theme,
            fontScale = fontScale,
            dailyGoalMinutes = dailyGoal,
            smartRewindEnabled = smartRewind,
            autoPlayNext = autoPlay,
            audioQualityKbps = quality
        )
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _userPreferencesFlow.value = _userPreferencesFlow.value.copy(themeMode = mode)
    }

    suspend fun setFontScale(scale: Float) {
        prefs.edit().putFloat("font_scale", scale).apply()
        _userPreferencesFlow.value = _userPreferencesFlow.value.copy(fontScale = scale)
    }

    suspend fun setDailyGoalMinutes(minutes: Int) {
        prefs.edit().putInt("daily_goal_minutes", minutes).apply()
        _userPreferencesFlow.value = _userPreferencesFlow.value.copy(dailyGoalMinutes = minutes)
    }

    suspend fun setSmartRewind(enabled: Boolean) {
        prefs.edit().putBoolean("smart_rewind", enabled).apply()
        _userPreferencesFlow.value = _userPreferencesFlow.value.copy(smartRewindEnabled = enabled)
    }
}
