package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val lessonId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "play_history")
data class PlayHistoryEntity(
    @PrimaryKey val lessonId: String,
    val lastPositionSeconds: Long,
    val totalDurationSeconds: Long,
    val isCompleted: Boolean = false,
    val lastPlayedAt: Long = System.currentTimeMillis()
)
