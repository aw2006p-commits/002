package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val lessonId: String,
    val timestampSeconds: Long,
    val note: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_lessons", primaryKeys = ["playlistId", "lessonId"])
data class PlaylistLessonCrossRef(
    val playlistId: String,
    val lessonId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey val dateString: String, // format: "YYYY-MM-DD"
    val listeningSeconds: Long = 0
)
