package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT lessonId FROM favorites ORDER BY addedAt DESC")
    fun getFavoriteIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE lessonId = :lessonId")
    suspend fun removeFavorite(lessonId: String)

    @Query("SELECT * FROM play_history ORDER BY lastPlayedAt DESC")
    fun getPlayHistory(): Flow<List<PlayHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayHistory(history: PlayHistoryEntity)

    @Query("DELETE FROM play_history WHERE lessonId = :lessonId")
    suspend fun deletePlayHistory(lessonId: String)

    @Query("SELECT * FROM downloaded_lessons ORDER BY downloadedAt DESC")
    fun getDownloadedLessons(): Flow<List<DownloadedLessonEntity>>

    @Query("SELECT lessonId FROM downloaded_lessons")
    fun getDownloadedLessonIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDownload(download: DownloadedLessonEntity)

    @Query("DELETE FROM downloaded_lessons WHERE lessonId = :lessonId")
    suspend fun deleteDownload(lessonId: String)

    @Query("DELETE FROM downloaded_lessons")
    suspend fun deleteAllDownloads()

    // Bookmarks
    @Query("SELECT * FROM bookmarks WHERE lessonId = :lessonId ORDER BY timestampSeconds ASC")
    fun getBookmarksForLesson(lessonId: String): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :bookmarkId")
    suspend fun deleteBookmark(bookmarkId: String)

    // Playlists
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addLessonToPlaylist(crossRef: PlaylistLessonCrossRef)

    @Query("DELETE FROM playlist_lessons WHERE playlistId = :playlistId AND lessonId = :lessonId")
    suspend fun removeLessonFromPlaylist(playlistId: String, lessonId: String)

    @Query("SELECT lessonId FROM playlist_lessons WHERE playlistId = :playlistId ORDER BY addedAt ASC")
    fun getLessonsInPlaylist(playlistId: String): Flow<List<String>>

    // Daily Stats
    @Query("SELECT * FROM daily_stats WHERE dateString = :dateString")
    suspend fun getDailyStats(dateString: String): DailyStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDailyStats(stats: DailyStatsEntity)

    @Query("UPDATE daily_stats SET listeningSeconds = listeningSeconds + :seconds WHERE dateString = :dateString")
    suspend fun incrementListeningSeconds(dateString: String, seconds: Long): Int

    @Query("SELECT * FROM daily_stats ORDER BY dateString DESC LIMIT 30")
    fun getLast30DaysStats(): Flow<List<DailyStatsEntity>>

    // Quiz Results
    @Query("SELECT * FROM quiz_results")
    fun getAllQuizResults(): Flow<List<QuizResultEntity>>

    @Query("SELECT * FROM quiz_results WHERE lessonId = :lessonId")
    fun getQuizResult(lessonId: String): Flow<QuizResultEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQuizResult(result: QuizResultEntity)

    @Query("DELETE FROM quiz_results WHERE lessonId = :lessonId")
    suspend fun deleteQuizResult(lessonId: String)
}
