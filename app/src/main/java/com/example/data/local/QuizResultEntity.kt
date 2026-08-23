package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey val lessonId: String,
    val score: Int,
    val totalQuestions: Int,
    val percentage: Int,
    val isPassed: Boolean,
    val completedAt: Long = System.currentTimeMillis()
)
