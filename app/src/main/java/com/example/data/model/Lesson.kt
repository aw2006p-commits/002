package com.example.data.model

enum class LessonCategory(val displayName: String, val iconEmoji: String) {
    ALL("الكل", "✨"),
    SEERAH("السيرة النبوية", "🕌"),
    TAFSEER("التفسير والتدبر", "📖"),
    KHAWATIR("الرقائق والخواطر", "🌙"),
    FIQH("الفقه والآداب", "⚖️"),
    KHUTBAH("الخطب والمواعظ", "🎙️")
}

data class Lesson(
    val id: String,
    val title: String,
    val series: String,
    val category: LessonCategory,
    val durationSeconds: Long,
    val durationFormatted: String,
    val date: String,
    val description: String,
    val isFeatured: Boolean = false,
    val keyTakeaways: List<String> = emptyList(),
    val iconEmoji: String = "🎙️",
    val tags: List<String> = emptyList(),
    val audioUrl: String = ""
)

data class SheikhQuote(
    val id: String,
    val quote: String,
    val context: String,
    val tags: List<String> = emptyList(),
    val lessonId: String? = null,
    val lessonTitle: String? = null,
    val timestampSeconds: Long = 0L,
    val timestampFormatted: String = "00:00"
)
