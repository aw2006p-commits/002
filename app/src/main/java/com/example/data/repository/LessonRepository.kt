package com.example.data.repository

import android.content.Context
import com.example.data.local.FavoriteEntity
import com.example.data.local.LessonDao
import com.example.data.local.PlayHistoryEntity
import com.example.data.model.Lesson
import com.example.data.model.LessonCategory
import com.example.data.model.SeriesInfo
import com.example.data.model.SheikhData
import com.example.data.model.SheikhQuote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * مستودع المحتوى والبيانات الدائمة.
 * المصدر الأساسي: sheikh_samir_database.json
 * الاحتياطي الآمن: SheikhData (MockData) إن فشل التحميل أو كان فارغاً.
 */
class LessonRepository(
    private val lessonDao: LessonDao,
    context: Context
) {
    private val appContext = context.applicationContext

    private val jsonLessons: List<Lesson> by lazy {
        runCatching {
            ScholarJsonDataLoader.getAllLessonsAsUiModel(appContext)
        }.getOrDefault(emptyList())
    }

    private val jsonQuotes: List<SheikhQuote> by lazy {
        runCatching {
            val db = ScholarJsonDataLoader.loadDatabase(appContext)
            db.quotes.map { q ->
                SheikhQuote(
                    id = q.id,
                    quote = q.quote,
                    context = q.sourceSeries,
                    tags = q.tags,
                    lessonId = q.lessonId.takeIf { it.isNotBlank() }
                )
            }
        }.getOrDefault(emptyList())
    }

    private val jsonSeries: List<SeriesInfo> by lazy {
        runCatching {
            val db = ScholarJsonDataLoader.loadDatabase(appContext)
            db.seriesList.map { s ->
                val totalSec = s.lessons.sumOf { it.durationSeconds }
                val hours = totalSec / 3600
                val mins = (totalSec % 3600) / 60
                val durationLabel = when {
                    hours > 0 && mins > 0 -> "$hours ساعة و $mins دقيقة"
                    hours > 0 -> "$hours ساعة"
                    else -> "$mins دقيقة"
                }
                SeriesInfo(
                    title = s.title,
                    lessonsCount = s.totalEpisodes.coerceAtLeast(s.lessons.size),
                    totalDuration = durationLabel,
                    description = s.description,
                    iconEmoji = s.coverEmoji.ifBlank { "🎙️" }
                )
            }
        }.getOrDefault(emptyList())
    }

    val allLessons: List<Lesson> by lazy {
        if (jsonLessons.isNotEmpty()) jsonLessons else SheikhData.allLessons
    }

    val quotes: List<SheikhQuote> by lazy {
        if (jsonQuotes.isNotEmpty()) jsonQuotes else SheikhData.quotes
    }

    val seriesList: List<SeriesInfo> by lazy {
        if (jsonSeries.isNotEmpty()) jsonSeries else SheikhData.seriesList
    }

    val heroLesson: Lesson by lazy {
        allLessons.firstOrNull { it.isFeatured } ?: allLessons.firstOrNull() ?: SheikhData.featuredHeroLesson
    }

    val favoriteIds: Flow<Set<String>> = lessonDao.getFavoriteIds().map { it.toSet() }

    val playHistory: Flow<List<PlayHistoryEntity>> = lessonDao.getPlayHistory()

    val downloadedLessons = lessonDao.getDownloadedLessons()

    val downloadedLessonIds: Flow<Set<String>> = lessonDao.getDownloadedLessonIds().map { it.toSet() }

    fun getLessonById(id: String): Lesson? {
        return allLessons.find { it.id == id }
    }

    fun getLessonsByCategory(category: LessonCategory): List<Lesson> {
        return if (category == LessonCategory.ALL) {
            allLessons
        } else {
            allLessons.filter { it.category == category }
        }
    }

    fun getLessonsBySeries(seriesTitle: String): List<Lesson> {
        return allLessons.filter { it.series == seriesTitle }
    }

    fun searchLessons(query: String): List<Lesson> {
        if (query.isBlank()) return allLessons
        val cleanQuery = query.trim().lowercase()
        return allLessons.filter { lesson ->
            lesson.title.lowercase().contains(cleanQuery) ||
                lesson.series.lowercase().contains(cleanQuery) ||
                lesson.description.lowercase().contains(cleanQuery) ||
                lesson.category.displayName.lowercase().contains(cleanQuery) ||
                lesson.tags.any { it.lowercase().contains(cleanQuery) }
        }
    }

    fun searchQuotes(query: String): List<SheikhQuote> {
        if (query.isBlank()) return quotes
        val cleanQuery = query.trim().lowercase()
        return quotes.filter { quote ->
            quote.quote.lowercase().contains(cleanQuery) ||
                quote.context.lowercase().contains(cleanQuery) ||
                quote.tags.any { it.lowercase().contains(cleanQuery) }
        }
    }

    suspend fun toggleFavorite(lessonId: String, isCurrentlyFavorite: Boolean) {
        if (isCurrentlyFavorite) {
            lessonDao.removeFavorite(lessonId)
        } else {
            lessonDao.addFavorite(FavoriteEntity(lessonId = lessonId))
        }
    }

    suspend fun recordPlaybackProgress(
        lessonId: String,
        positionSeconds: Long,
        totalDurationSeconds: Long,
        isCompleted: Boolean
    ) {
        lessonDao.savePlayHistory(
            PlayHistoryEntity(
                lessonId = lessonId,
                lastPositionSeconds = positionSeconds,
                totalDurationSeconds = totalDurationSeconds,
                isCompleted = isCompleted,
                lastPlayedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeHistory(lessonId: String) {
        lessonDao.deletePlayHistory(lessonId)
    }

    fun getBookmarksForLesson(lessonId: String) = lessonDao.getBookmarksForLesson(lessonId)
    fun getAllBookmarks() = lessonDao.getAllBookmarks()
    suspend fun addBookmark(bookmark: com.example.data.local.BookmarkEntity) = lessonDao.addBookmark(bookmark)
    suspend fun deleteBookmark(bookmarkId: String) = lessonDao.deleteBookmark(bookmarkId)

    fun getPlaylists() = lessonDao.getPlaylists()
    fun getLessonsInPlaylist(playlistId: String) = lessonDao.getLessonsInPlaylist(playlistId)
    suspend fun addPlaylist(playlist: com.example.data.local.PlaylistEntity) = lessonDao.addPlaylist(playlist)
    suspend fun deletePlaylist(playlistId: String) = lessonDao.deletePlaylist(playlistId)
    suspend fun addLessonToPlaylist(playlistId: String, lessonId: String) {
        lessonDao.addLessonToPlaylist(com.example.data.local.PlaylistLessonCrossRef(playlistId, lessonId))
    }
    suspend fun removeLessonFromPlaylist(playlistId: String, lessonId: String) =
        lessonDao.removeLessonFromPlaylist(playlistId, lessonId)

    suspend fun recordListeningTick(seconds: Long = 1) {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val updatedRows = lessonDao.incrementListeningSeconds(today, seconds)
        if (updatedRows == 0) {
            val stats = com.example.data.local.DailyStatsEntity(dateString = today, listeningSeconds = seconds)
            lessonDao.saveDailyStats(stats)
        }
    }
    fun getLast30DaysStats() = lessonDao.getLast30DaysStats()

    fun getAllQuizResults() = lessonDao.getAllQuizResults()
    fun getQuizResult(lessonId: String) = lessonDao.getQuizResult(lessonId)
    suspend fun saveQuizResult(result: com.example.data.local.QuizResultEntity) = lessonDao.saveQuizResult(result)
    suspend fun deleteQuizResult(lessonId: String) = lessonDao.deleteQuizResult(lessonId)
}
