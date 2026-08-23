package com.example.data.repository

import com.example.data.local.FavoriteEntity
import com.example.data.local.LessonDao
import com.example.data.local.PlayHistoryEntity
import com.example.data.model.Lesson
import com.example.data.model.LessonCategory
import com.example.data.model.SheikhData
import com.example.data.model.SheikhQuote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LessonRepository(private val lessonDao: LessonDao) {

    val allLessons: List<Lesson> = SheikhData.allLessons
    val quotes: List<SheikhQuote> = SheikhData.quotes
    val seriesList = SheikhData.seriesList
    val heroLesson: Lesson = SheikhData.featuredHeroLesson

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
                    lesson.category.displayName.lowercase().contains(cleanQuery)
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

    // Bookmarks
    fun getBookmarksForLesson(lessonId: String) = lessonDao.getBookmarksForLesson(lessonId)
    fun getAllBookmarks() = lessonDao.getAllBookmarks()
    suspend fun addBookmark(bookmark: com.example.data.local.BookmarkEntity) = lessonDao.addBookmark(bookmark)
    suspend fun deleteBookmark(bookmarkId: String) = lessonDao.deleteBookmark(bookmarkId)

    // Playlists
    fun getPlaylists() = lessonDao.getPlaylists()
    fun getLessonsInPlaylist(playlistId: String) = lessonDao.getLessonsInPlaylist(playlistId)
    suspend fun addPlaylist(playlist: com.example.data.local.PlaylistEntity) = lessonDao.addPlaylist(playlist)
    suspend fun deletePlaylist(playlistId: String) = lessonDao.deletePlaylist(playlistId)
    suspend fun addLessonToPlaylist(playlistId: String, lessonId: String) {
        lessonDao.addLessonToPlaylist(com.example.data.local.PlaylistLessonCrossRef(playlistId, lessonId))
    }
    suspend fun removeLessonFromPlaylist(playlistId: String, lessonId: String) = lessonDao.removeLessonFromPlaylist(playlistId, lessonId)

    // Daily Stats
    suspend fun recordListeningTick(seconds: Long = 1) {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val updatedRows = lessonDao.incrementListeningSeconds(today, seconds)
        if (updatedRows == 0) {
            val stats = com.example.data.local.DailyStatsEntity(dateString = today, listeningSeconds = seconds)
            lessonDao.saveDailyStats(stats)
        }
    }
    fun getLast30DaysStats() = lessonDao.getLast30DaysStats()

    // Quiz Results
    fun getAllQuizResults() = lessonDao.getAllQuizResults()
    fun getQuizResult(lessonId: String) = lessonDao.getQuizResult(lessonId)
    suspend fun saveQuizResult(result: com.example.data.local.QuizResultEntity) = lessonDao.saveQuizResult(result)
    suspend fun deleteQuizResult(lessonId: String) = lessonDao.deleteQuizResult(lessonId)
}
