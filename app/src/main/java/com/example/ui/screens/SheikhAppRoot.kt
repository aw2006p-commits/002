package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Lesson
import com.example.ui.components.DownloadManagerSheet
import com.example.ui.components.FullAudioPlayerSheet
import com.example.ui.components.MiniPlayerBar
import com.example.ui.theme.NaturalCardBg
import com.example.ui.theme.NaturalCharcoal
import com.example.ui.theme.NaturalMuted
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalPillBg
import com.example.ui.theme.NaturalSandBg
import com.example.ui.theme.NaturalSandBorder
import com.example.ui.viewmodel.SheikhViewModel

data class NavItem(
    val title: String,
    val emoji: String,
    val testTag: String
)

@Composable
fun SheikhAppRoot(
    viewModel: SheikhViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()

    var isPlayerSheetOpen by remember { mutableStateOf(false) }
    var isAchievementsOpen by remember { mutableStateOf(false) }

    val navItems = listOf(
        NavItem("الرئيسية", "🏠", "nav_home"),
        NavItem("الدروس والخطب", "🎙️", "nav_lessons"),
        NavItem("السلاسل", "📚", "nav_library"),
        NavItem("البحث والفوائد", "🔍", "nav_search"),
        NavItem("المكتبة", "🤍", "nav_favorites")
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = NaturalSandBg,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NaturalCardBg)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    // Border line above bottom bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(NaturalSandBorder)
                    )

                    // Navigation items row matching Natural Tones style
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        navItems.forEachIndexed { index, item ->
                            val isSelected = uiState.selectedTab == index
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { viewModel.selectTab(index) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag(item.testTag)
                            ) {
                                // Active Pill background
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) NaturalPillBg else Color.Transparent)
                                        .padding(horizontal = 14.dp, vertical = 3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.emoji,
                                        fontSize = 18.sp
                                    )
                                }

                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) NaturalOlive else NaturalMuted
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Screen Content based on Selected Tab
                when (uiState.selectedTab) {
                    0 -> HomeScreen(
                        uiState = uiState,
                        onNavigateToLessons = {
                            viewModel.selectTab(1)
                        },
                        onNavigateToLibrary = {
                            viewModel.selectTab(2)
                        },
                        onOpenAchievements = { isAchievementsOpen = true },
                        onOpenSettings = { viewModel.setSettingsOpen(true) },
                        onStartQuiz = { lesson -> viewModel.openQuiz(lesson) },
                        onStartCategoryQuiz = { category -> viewModel.openCategoryQuiz(category) },
                        onPlayLesson = { lesson ->
                            viewModel.playLesson(lesson)
                            isPlayerSheetOpen = true
                        },
                        onToggleFavorite = { lessonId ->
                            viewModel.toggleFavorite(lessonId)
                        },
                        onDownloadLesson = { lesson ->
                            viewModel.downloadLesson(lesson)
                        },
                        onDeleteDownload = { lessonId ->
                            viewModel.deleteDownload(lessonId)
                        },
                        onSelectSeries = { series ->
                            viewModel.selectSeries(series)
                            viewModel.selectTab(1)
                        },
                        onToggleThemeMode = {
                            val nextMode = if (uiState.userPreferences.themeMode == com.example.data.local.AppThemeMode.DARK) {
                                com.example.data.local.AppThemeMode.LIGHT
                            } else {
                                com.example.data.local.AppThemeMode.DARK
                            }
                            viewModel.setThemeMode(nextMode)
                        },
                        onPlayLessonAtTimestamp = { lessonId, timestamp ->
                            viewModel.playLessonAtTimestamp(lessonId, timestamp)
                            isPlayerSheetOpen = true
                        }
                    )
                    1 -> LessonsScreen(
                        uiState = uiState,
                        currentPlayingLessonId = playerState.currentLesson?.id,
                        onPlayLesson = { lesson ->
                            viewModel.playLesson(lesson)
                            isPlayerSheetOpen = true
                        },
                        onToggleFavorite = { lessonId ->
                            viewModel.toggleFavorite(lessonId)
                        },
                        onSelectCategory = { category ->
                            viewModel.selectCategory(category)
                        },
                        onNavigateToLibrary = {
                            viewModel.selectTab(2)
                        },
                        onDownloadLesson = { lesson ->
                            viewModel.downloadLesson(lesson)
                        },
                        onDeleteDownload = { lessonId ->
                            viewModel.deleteDownload(lessonId)
                        },
                        onOpenDownloadManager = {
                            viewModel.setDownloadManagerOpen(true)
                        },
                        onStartQuiz = { lesson -> viewModel.openQuiz(lesson) },
                        onStartCategoryQuiz = { category -> viewModel.openCategoryQuiz(category) }
                    )
                    2 -> LibraryScreen(
                        uiState = uiState,
                        currentPlayingLessonId = playerState.currentLesson?.id,
                        onPlayLesson = { lesson ->
                            viewModel.playLesson(lesson)
                            isPlayerSheetOpen = true
                        },
                        onToggleFavorite = { lessonId ->
                            viewModel.toggleFavorite(lessonId)
                        },
                        onDownloadLesson = { lesson ->
                            viewModel.downloadLesson(lesson)
                        },
                        onDeleteDownload = { lessonId ->
                            viewModel.deleteDownload(lessonId)
                        },
                        onStartQuiz = { lesson -> viewModel.openQuiz(lesson) }
                    )
                    3 -> {
                        val allBookmarks by viewModel.allBookmarks.collectAsStateWithLifecycle()
                        SearchQuotesScreen(
                            uiState = uiState,
                            currentPlayingLessonId = playerState.currentLesson?.id,
                            allBookmarks = allBookmarks,
                            onSearchQueryChanged = { query ->
                                viewModel.onSearchQueryChanged(query)
                            },
                            onPlayLesson = { lesson ->
                                viewModel.playLesson(lesson)
                                isPlayerSheetOpen = true
                            },
                            onPlayLessonAtTimestamp = { lessonId, timestamp ->
                                viewModel.playLessonAtTimestamp(lessonId, timestamp)
                                isPlayerSheetOpen = true
                            },
                            onToggleFavorite = { lessonId ->
                                viewModel.toggleFavorite(lessonId)
                            },
                            onDownloadLesson = { lesson ->
                                viewModel.downloadLesson(lesson)
                            },
                            onDeleteDownload = { lessonId ->
                                viewModel.deleteDownload(lessonId)
                            },
                            onStartQuiz = { lesson -> viewModel.openQuiz(lesson) }
                        )
                    }
                    4 -> FavoritesScreen(
                        uiState = uiState,
                        currentPlayingLessonId = playerState.currentLesson?.id,
                        onPlayLesson = { lesson ->
                            viewModel.playLesson(lesson)
                            isPlayerSheetOpen = true
                        },
                        onToggleFavorite = { lessonId ->
                            viewModel.toggleFavorite(lessonId)
                        },
                        onDownloadLesson = { lesson ->
                            viewModel.downloadLesson(lesson)
                        },
                        onDeleteDownload = { lessonId ->
                            viewModel.deleteDownload(lessonId)
                        },
                        onOpenDownloadManager = {
                            viewModel.setDownloadManagerOpen(true)
                        },
                        onStartQuiz = { lesson -> viewModel.openQuiz(lesson) }
                    )
                }

                // Mini Player Bar positioned directly above bottom bar
                if (playerState.currentLesson != null) {
                    MiniPlayerBar(
                        playerState = playerState,
                        onTogglePlayPause = { viewModel.playerManager.togglePlayPause() },
                        onOpenFullPlayer = { isPlayerSheetOpen = true },
                        onClosePlayer = { viewModel.playerManager.closePlayer() },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }

            // Full Audio Player Modal Sheet
            if (isPlayerSheetOpen && playerState.currentLesson != null) {
                val currentLesson = playerState.currentLesson!!
                val allBookmarks by viewModel.allBookmarks.collectAsStateWithLifecycle()
                val currentBookmarks = allBookmarks.filter { it.lessonId == currentLesson.id }.sortedBy { it.timestampSeconds }
                
                FullAudioPlayerSheet(
                    playerState = playerState,
                    isFavorite = uiState.favoriteIds.contains(currentLesson.id),
                    isDownloaded = uiState.downloadedIds.contains(currentLesson.id),
                    downloadProgress = uiState.activeDownloads[currentLesson.id],
                    onDismiss = { isPlayerSheetOpen = false },
                    onTogglePlayPause = { viewModel.playerManager.togglePlayPause() },
                    onSeekTo = { pos -> viewModel.playerManager.seekTo(pos) },
                    onSeekBy = { delta -> viewModel.playerManager.seekBy(delta) },
                    onSpeedChange = { speed -> viewModel.playerManager.setPlaybackSpeed(speed) },
                    onSleepTimerChange = { min -> viewModel.playerManager.setSleepTimer(min) },
                    onToggleFavorite = { id -> viewModel.toggleFavorite(id) },
                    onDownloadClick = { lesson -> viewModel.downloadLesson(lesson) },
                    onDeleteDownloadClick = { id -> viewModel.deleteDownload(id) },
                    bookmarks = currentBookmarks,
                    onAddBookmark = { pos, note -> viewModel.addBookmark(currentLesson.id, pos, note) },
                    onDeleteBookmark = { id -> viewModel.deleteBookmark(id) },
                    onStartQuiz = { lesson ->
                        isPlayerSheetOpen = false
                        viewModel.openQuiz(lesson)
                    },
                    quizResult = uiState.quizResults[currentLesson.id]
                )
            }

            // Download Manager Sheet
            if (uiState.isDownloadManagerOpen) {
                val context = androidx.compose.ui.platform.LocalContext.current
                DownloadManagerSheet(
                    uiState = uiState,
                    onDismiss = { viewModel.setDownloadManagerOpen(false) },
                    onPlayLesson = { lesson ->
                        viewModel.playLesson(lesson)
                        isPlayerSheetOpen = true
                    },
                    onDeleteDownload = { lessonId ->
                        viewModel.deleteDownload(lessonId)
                    },
                    onDeleteAllDownloads = {
                        viewModel.deleteAllDownloads()
                    },
                    onCancelDownload = { lessonId ->
                        viewModel.cancelDownload(lessonId)
                    },
                    onExportDownloads = { uri ->
                        viewModel.exportDownloads(uri) { success, msg ->
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    onImportDownloads = { uri ->
                        viewModel.importDownloads(uri) { success, msg ->
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            // Achievements Sheet
            if (isAchievementsOpen) {
                val stats by viewModel.last30DaysStats.collectAsStateWithLifecycle()
                val playlists by viewModel.allPlaylists.collectAsStateWithLifecycle()
                
                com.example.ui.components.AchievementsSheet(
                    stats = stats,
                    playlists = playlists,
                    dailyGoalMinutes = uiState.userPreferences.dailyGoalMinutes,
                    onCreatePlaylist = { name -> viewModel.createPlaylist(name) },
                    onDismiss = { isAchievementsOpen = false },
                    quizResults = uiState.quizResults
                )
            }

            // Settings & Customization Sheet
            if (uiState.isSettingsOpen) {
                com.example.ui.components.SettingsSheet(
                    preferences = uiState.userPreferences,
                    onSetThemeMode = { viewModel.setThemeMode(it) },
                    onSetFontScale = { viewModel.setFontScale(it) },
                    onSetDailyGoal = { viewModel.setDailyGoalMinutes(it) },
                    onSetSmartRewind = { viewModel.setSmartRewind(it) },
                    onDismiss = { viewModel.setSettingsOpen(false) }
                )
            }

            // End-of-Lesson / Category Quiz Modal Sheet
            if (uiState.activeUnifiedQuiz != null) {
                com.example.ui.components.LessonQuizSheet(
                    quiz = uiState.activeUnifiedQuiz!!,
                    onDismiss = { viewModel.closeQuiz() },
                    onPlayLessonAtTimestamp = { lessonId, timestampSeconds ->
                        viewModel.playLessonAtTimestamp(lessonId, timestampSeconds)
                        isPlayerSheetOpen = true
                    },
                    onSubmitResult = { lessonId, score, total ->
                        viewModel.submitQuizResult(lessonId, score, total)
                    }
                )
            } else if (uiState.activeQuizLesson != null) {
                com.example.ui.components.LessonQuizSheet(
                    lesson = uiState.activeQuizLesson!!,
                    onDismiss = { viewModel.closeQuiz() },
                    onPlayLessonAtTimestamp = { lessonId, timestampSeconds ->
                        viewModel.playLessonAtTimestamp(lessonId, timestampSeconds)
                        isPlayerSheetOpen = true
                    },
                    onSubmitResult = { lessonId, score, total ->
                        viewModel.submitQuizResult(lessonId, score, total)
                    }
                )
            }
        }
    }
}
