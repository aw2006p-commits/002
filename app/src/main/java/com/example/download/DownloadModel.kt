package com.example.download

enum class DownloadStatus {
    IDLE,
    DOWNLOADING,
    COMPLETED,
    FAILED
}

data class DownloadProgress(
    val lessonId: String,
    val progress: Float = 0f,
    val status: DownloadStatus = DownloadStatus.IDLE,
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L
) {
    val progressPercent: Int
        get() = (progress * 100).toInt().coerceIn(0, 100)
}
