package com.example.data.model

/**
 * نموذج قاعدة البيانات الشاملة للمحتوى العلمي والدعوي
 */
data class ScholarDatabase(
    val version: String,
    val lastUpdated: String,
    val scholarProfile: ScholarProfile,
    val topics: List<SubjectTopic>,
    val bookExplanations: List<BookReference>,
    val seriesList: List<LessonSeries>,
    val quotes: List<ScholarQuoteDetail>
)

/**
 * بيانات السيرة التعريفية للشيخ
 */
data class ScholarProfile(
    val id: String,
    val name: String,
    val title: String,
    val kunya: String,
    val biography: String,
    val birthPlace: String,
    val primaryFields: List<String>,
    val totalRecordedLessons: Int,
    val totalSeries: Int,
    val officialLinks: Map<String, String>
)

/**
 * التصنيف الموضوعي (المحاور والعلوم)
 */
data class SubjectTopic(
    val id: String,
    val name: String,
    val nameEnglish: String,
    val description: String,
    val iconEmoji: String,
    val colorHex: String,
    val seriesCount: Int
)

/**
 * بيانات شروح الكتب والمتون التراثية
 */
data class BookReference(
    val id: String,
    val bookTitle: String,
    val originalAuthor: String,
    val field: String,
    val summary: String,
    val explanationSeriesId: String
)

/**
 * بيانات السلسلة العلمية / المجلد الصوتي
 */
data class LessonSeries(
    val id: String,
    val title: String,
    val topicId: String,
    val bookId: String? = null,
    val description: String,
    val totalEpisodes: Int,
    val isCompleted: Boolean,
    val coverEmoji: String,
    val recordingPeriod: String,
    val primaryLocation: String,
    val lessons: List<DetailedLesson>
)

/**
 * بيانات الدرس الصوتي التفصيلي
 */
data class DetailedLesson(
    val id: String,
    val episodeNumber: Int,
    val title: String,
    val seriesId: String,
    val seriesTitle: String,
    val topicId: String,
    val topicDisplayName: String,
    val durationSeconds: Long,
    val durationFormatted: String,
    val audioUrl: String,
    val recordingDate: String,
    val recordingLocation: String,
    val summary: String,
    val keyTakeaways: List<String>,
    val tags: List<String>,
    val chapters: List<LessonTimestampChapter> = emptyList(),
    val relatedVerses: List<String> = emptyList(),
    val relatedHadiths: List<String> = emptyList()
)

/**
 * فواصل زمنية (فصول الدرس وعناوينه الفرعية)
 */
data class LessonTimestampChapter(
    val startSeconds: Long,
    val title: String
)

/**
 * اقتباس وفائدة علمية للشيخ
 */
data class ScholarQuoteDetail(
    val id: String,
    val quote: String,
    val sourceSeries: String,
    val lessonId: String,
    val tags: List<String>
)

/**
 * دوال تحويل لتسهيل التكامل مع طبقة الواجهات (UI Mapping Helpers)
 */
fun DetailedLesson.toLesson(): Lesson {
    val category = when (this.topicId) {
        "seerah_maghazi" -> LessonCategory.SEERAH
        "tafseer_tadabbur" -> LessonCategory.TAFSEER
        "tazkiyah_khawatir" -> LessonCategory.KHAWATIR
        "fiqh_adab" -> LessonCategory.FIQH
        "aqeedah_tawheed" -> LessonCategory.FIQH
        "khutbah_waaz" -> LessonCategory.KHUTBAH
        else -> LessonCategory.ALL
    }

    return Lesson(
        id = this.id,
        title = this.title,
        series = this.seriesTitle,
        category = category,
        durationSeconds = this.durationSeconds,
        durationFormatted = this.durationFormatted,
        date = this.recordingDate,
        description = this.summary,
        isFeatured = this.episodeNumber == 1,
        keyTakeaways = this.keyTakeaways,
        iconEmoji = when (category) {
            LessonCategory.SEERAH -> "🕌"
            LessonCategory.TAFSEER -> "📖"
            LessonCategory.KHAWATIR -> "🌙"
            LessonCategory.FIQH -> "⚖️"
            LessonCategory.KHUTBAH -> "🎙️"
            LessonCategory.ALL -> "✨"
        },
        tags = this.tags,
        audioUrl = this.audioUrl
    )
}
