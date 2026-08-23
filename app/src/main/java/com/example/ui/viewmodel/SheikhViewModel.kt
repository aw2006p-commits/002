package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AppThemeMode
import com.example.data.local.DownloadedLessonEntity
import com.example.data.local.PlayHistoryEntity
import com.example.data.local.UserPreferences
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.Lesson
import com.example.data.model.LessonCategory
import com.example.data.model.SeriesInfo
import com.example.data.model.SheikhQuote
import com.example.data.model.UnifiedQuiz
import com.example.data.repository.LessonRepository
import com.example.data.repository.QuizDataProvider
import com.example.download.DownloadProgress
import com.example.download.LessonDownloadManager
import com.example.player.AudioPlayerManager
import com.example.player.PlayerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SheikhUiState(
    val selectedTab: Int = 0,
    val selectedCategory: LessonCategory = LessonCategory.ALL,
    val selectedSeries: SeriesInfo? = null,
    val searchQuery: String = "",
    val quoteQuery: String = "",
    val allLessons: List<Lesson> = emptyList(),
    val seriesList: List<SeriesInfo> = emptyList(),
    val filteredLessons: List<Lesson> = emptyList(),
    val filteredQuotes: List<SheikhQuote> = emptyList(),
    val isContentReady: Boolean = false,
    val favoriteIds: Set<String> = emptySet(),
    val playHistory: List<PlayHistoryEntity> = emptyList(),
    val downloadedIds: Set<String> = emptySet(),
    val downloadedLessons: List<DownloadedLessonEntity> = emptyList(),
    val activeDownloads: Map<String, DownloadProgress> = emptyMap(),
    val filterDownloadedOnly: Boolean = false,
    val isDownloadManagerOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val selectedLessonDetail: Lesson? = null,
    val activeQuizLesson: Lesson? = null,
    val activeUnifiedQuiz: UnifiedQuiz? = null,
    val quizResults: Map<String, com.example.data.local.QuizResultEntity> = emptyMap(),
    val userPreferences: UserPreferences = UserPreferences(),
    val copiedQuoteMessage: String? = null
)

data class UserDataSnapshot(
    val favorites: Set<String> = emptySet(),
    val playHistory: List<PlayHistoryEntity> = emptyList(),
    val quizResults: Map<String, com.example.data.local.QuizResultEntity> = emptyMap(),
    val downloadedIds: Set<String> = emptySet(),
    val downloadedLessons: List<DownloadedLessonEntity> = emptyList(),
    val activeDownloads: Map<String, DownloadProgress> = emptyMap()
)

class SheikhViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = LessonRepository(database.lessonDao(), application)
    val downloadManager = LessonDownloadManager(application, database.lessonDao())
    val preferencesRepository = UserPreferencesRepository(application)

    val playerManager = AudioPlayerManager(
        context = application,
        onProgressUpdate = { lessonId, pos, duration, completed ->
            viewModelScope.launch {
                repository.recordPlaybackProgress(lessonId, pos, duration, completed)
            }
        },
        onListeningTick = { seconds ->
            viewModelScope.launch {
                repository.recordListeningTick(seconds)
            }
        }
    )

    val playerState: StateFlow<PlayerState> = playerManager.playerState

    // لا نحمل الدروس على الـ Main Thread عند الإنشاء — يمنع تجمّد الـ Splash
    private val _uiState = MutableStateFlow(SheikhUiState())

    init {
        viewModelScope.launch {
            val (lessons, quotes, series) = withContext(Dispatchers.Default) {
                Triple(repository.allLessons, repository.quotes, repository.seriesList)
            }
            _uiState.update {
                it.copy(
                    allLessons = lessons,
                    seriesList = series,
                    filteredLessons = lessons,
                    filteredQuotes = quotes,
                    isContentReady = true
                )
            }
        }
    }

    private val downloadsCombinedFlow = combine(
        downloadManager.downloadedLessonIds,
        downloadManager.downloadedLessons,
        downloadManager.activeDownloads
    ) { ids, lessons, active ->
        Triple(ids, lessons, active)
    }

    private val persistentDataFlow = combine(
        repository.favoriteIds,
        repository.playHistory,
        repository.getAllQuizResults(),
        downloadsCombinedFlow
    ) { favorites, history, quizList, downloadsTriple ->
        val (downIds, downLessons, activeDowns) = downloadsTriple
        val quizMap = quizList.associateBy { it.lessonId }
        UserDataSnapshot(
            favorites = favorites,
            playHistory = history,
            quizResults = quizMap,
            downloadedIds = downIds,
            downloadedLessons = downLessons,
            activeDownloads = activeDowns
        )
    }

    val uiState: StateFlow<SheikhUiState> = combine(
        _uiState,
        persistentDataFlow,
        preferencesRepository.userPreferencesFlow
    ) { currentUiState, userData, prefs ->
        currentUiState.copy(
            favoriteIds = userData.favorites,
            playHistory = userData.playHistory,
            quizResults = userData.quizResults,
            downloadedIds = userData.downloadedIds,
            downloadedLessons = userData.downloadedLessons,
            activeDownloads = userData.activeDownloads,
            userPreferences = prefs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SheikhUiState()
    )

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun selectCategory(category: LessonCategory) {
        _uiState.update { state ->
            val query = state.searchQuery
            val baseLessons = repository.getLessonsByCategory(category)
            val filtered = if (query.isBlank()) {
                baseLessons
            } else {
                baseLessons.filter {
                    it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
                }
            }
            state.copy(
                selectedCategory = category,
                filteredLessons = filtered
            )
        }
    }

    fun selectSeries(series: SeriesInfo?) {
        _uiState.update { state ->
            state.copy(selectedSeries = series)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            val category = state.selectedCategory
            val baseLessons = if (category == LessonCategory.ALL) {
                repository.allLessons
            } else {
                repository.getLessonsByCategory(category)
            }
            val filtered = if (query.isBlank()) {
                baseLessons
            } else {
                baseLessons.filter {
                    it.title.contains(query, ignoreCase = true) ||
                        it.series.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
                }
            }
            val quotesFiltered = repository.searchQuotes(query)
            state.copy(
                searchQuery = query,
                filteredLessons = filtered,
                filteredQuotes = quotesFiltered
            )
        }
    }

    fun toggleFilterDownloadedOnly() {
        _uiState.update { state ->
            val newFilter = !state.filterDownloadedOnly
            val baseLessons = if (state.selectedCategory == LessonCategory.ALL) {
                repository.allLessons
            } else {
                repository.getLessonsByCategory(state.selectedCategory)
            }
            val filtered = if (newFilter) {
                baseLessons.filter { state.downloadedIds.contains(it.id) }
            } else {
                baseLessons
            }
            state.copy(
                filterDownloadedOnly = newFilter,
                filteredLessons = filtered
            )
        }
    }

    fun playLesson(lesson: Lesson) {
        val historyItem = uiState.value.playHistory.find { it.lessonId == lesson.id }
        var startPos = if (historyItem != null && !historyItem.isCompleted) {
            historyItem.lastPositionSeconds
        } else {
            0L
        }
        if (startPos > 5L && uiState.value.userPreferences.smartRewindEnabled) {
            startPos = (startPos - 5L).coerceAtLeast(0L)
        }

        val isOffline = uiState.value.downloadedIds.contains(lesson.id)
        val localPath = if (isOffline) {
            uiState.value.downloadedLessons.find { it.lessonId == lesson.id }?.localFilePath
                ?: downloadManager.getLocalPath(lesson.id)
        } else {
            null
        }

        playerManager.playLesson(
            lesson = lesson,
            startPositionSeconds = startPos,
            isOffline = isOffline && !localPath.isNullOrBlank(),
            localFilePath = localPath
        )
    }

    fun playLessonAtTimestamp(lessonId: String, timestampSeconds: Long) {
        val lesson = repository.getLessonById(lessonId) ?: repository.allLessons.firstOrNull() ?: return
        val isOffline = uiState.value.downloadedIds.contains(lesson.id)
        val localPath = if (isOffline) {
            uiState.value.downloadedLessons.find { it.lessonId == lesson.id }?.localFilePath
                ?: downloadManager.getLocalPath(lesson.id)
        } else {
            null
        }
        playerManager.playLesson(
            lesson = lesson,
            startPositionSeconds = timestampSeconds,
            isOffline = isOffline && !localPath.isNullOrBlank(),
            localFilePath = localPath
        )
    }

    fun setSettingsOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = isOpen) }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch {
            preferencesRepository.setFontScale(scale)
        }
    }

    fun setDailyGoalMinutes(minutes: Int) {
        viewModelScope.launch {
            preferencesRepository.setDailyGoalMinutes(minutes)
        }
    }

    fun setSmartRewind(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSmartRewind(enabled)
        }
    }

    fun toggleFavorite(lessonId: String) {
        val isFav = uiState.value.favoriteIds.contains(lessonId)
        viewModelScope.launch {
            repository.toggleFavorite(lessonId, isFav)
        }
    }

    fun downloadLesson(lesson: Lesson) {
        downloadManager.startDownload(lesson)
    }

    fun cancelDownload(lessonId: String) {
        downloadManager.cancelDownload(lessonId)
    }

    fun exportDownloads(uri: android.net.Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = downloadManager.exportDownloads(uri)
            if (result.isSuccess) {
                onResult(true, "تم تصدير الدروس بنجاح")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "حدث خطأ أثناء التصدير")
            }
        }
    }

    fun importDownloads(uri: android.net.Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = downloadManager.importDownloads(uri)
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                onResult(true, "تم استيراد $count درس بنجاح")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "حدث خطأ أثناء الاستيراد")
            }
        }
    }

    fun deleteDownloadedLesson(lessonId: String) {
        downloadManager.deleteDownloadedLesson(lessonId)
    }

    fun deleteDownload(lessonId: String) {
        downloadManager.deleteDownloadedLesson(lessonId)
    }

    fun deleteAllDownloads() {
        downloadManager.deleteAllDownloads()
    }

    fun clearAllDownloads() {
        downloadManager.deleteAllDownloads()
    }

    fun setDownloadManagerOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isDownloadManagerOpen = isOpen) }
    }

    fun showLessonDetail(lesson: Lesson?) {
        _uiState.update { it.copy(selectedLessonDetail = lesson) }
    }

    fun showCopiedToast(message: String) {
        _uiState.update { it.copy(copiedQuoteMessage = message) }
    }

    fun clearCopiedToast() {
        _uiState.update { it.copy(copiedQuoteMessage = null) }
    }

    val allBookmarks = repository.getAllBookmarks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allPlaylists = repository.getPlaylists().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val last30DaysStats = repository.getLast30DaysStats().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addBookmark(lessonId: String, timestampSeconds: Long, note: String) {
        viewModelScope.launch {
            repository.addBookmark(
                com.example.data.local.BookmarkEntity(
                    lessonId = lessonId,
                    timestampSeconds = timestampSeconds,
                    note = note
                )
            )
        }
    }

    fun deleteBookmark(id: String) {
        viewModelScope.launch { repository.deleteBookmark(id) }
    }

    fun getBookmarksForLesson(lessonId: String) = repository.getBookmarksForLesson(lessonId)

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.addPlaylist(com.example.data.local.PlaylistEntity(name = name))
        }
    }

    fun deletePlaylist(id: String) {
        viewModelScope.launch { repository.deletePlaylist(id) }
    }

    fun addLessonToPlaylist(playlistId: String, lessonId: String) {
        viewModelScope.launch { repository.addLessonToPlaylist(playlistId, lessonId) }
    }

    fun removeLessonFromPlaylist(playlistId: String, lessonId: String) {
        viewModelScope.launch { repository.removeLessonFromPlaylist(playlistId, lessonId) }
    }

    fun getLessonsInPlaylist(playlistId: String) = repository.getLessonsInPlaylist(playlistId)

    fun openQuiz(lesson: Lesson) {
        val unified = QuizDataProvider.getUnifiedQuizForLesson(lesson)
        _uiState.update { it.copy(activeQuizLesson = lesson, activeUnifiedQuiz = unified) }
    }

    fun openCategoryQuiz(category: LessonCategory) {
        val unified = QuizDataProvider.getQuizForCategory(category)
        _uiState.update { it.copy(activeQuizLesson = null, activeUnifiedQuiz = unified) }
    }

    fun openUnifiedQuiz(quiz: UnifiedQuiz) {
        _uiState.update { it.copy(activeUnifiedQuiz = quiz) }
    }

    fun closeQuiz() {
        _uiState.update { it.copy(activeQuizLesson = null, activeUnifiedQuiz = null) }
    }

    fun submitQuizResult(quizId: String, score: Int, totalQuestions: Int) {
        viewModelScope.launch {
            val percentage = if (totalQuestions > 0) (score * 100) / totalQuestions else 0
            val isPassed = percentage >= 70
            val result = com.example.data.local.QuizResultEntity(
                lessonId = quizId,
                score = score,
                totalQuestions = totalQuestions,
                percentage = percentage,
                isPassed = isPassed,
                completedAt = System.currentTimeMillis()
            )
            repository.saveQuizResult(result)
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
