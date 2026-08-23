package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_lessons")
data class DownloadedLessonEntity(
    @PrimaryKey val lessonId: String,
    val localFilePath: String,
    val fileSizeBytes: Long,
    val downloadedAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = true
)
