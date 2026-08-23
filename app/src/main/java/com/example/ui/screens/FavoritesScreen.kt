package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PlayHistoryEntity
import com.example.data.model.Lesson
import com.example.data.model.SheikhData
import com.example.download.LessonDownloadManager
import com.example.player.PlayerState
import com.example.ui.components.LessonItemCard
import com.example.ui.theme.NaturalAccentGold
import com.example.ui.theme.NaturalCardBg
import com.example.ui.theme.NaturalCharcoal
import com.example.ui.theme.NaturalItemBg
import com.example.ui.theme.NaturalMuted
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalOliveLight
import com.example.ui.theme.NaturalPillBg
import com.example.ui.theme.NaturalSandBg
import com.example.ui.theme.NaturalSandBorder
import com.example.ui.viewmodel.SheikhUiState

@Composable
fun FavoritesScreen(
    uiState: SheikhUiState,
    currentPlayingLessonId: String?,
    onPlayLesson: (Lesson) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onDownloadLesson: (Lesson) -> Unit,
    onDeleteDownload: (String) -> Unit,
    onOpenDownloadManager: () -> Unit,
    onStartQuiz: ((Lesson) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: المفضلة, 1: التحميلات (بدون نت), 2: سجل الاستماع

    val favoriteLessons = remember(uiState.favoriteIds) {
        SheikhData.allLessons.filter { uiState.favoriteIds.contains(it.id) }
    }

    val downloadedLessons = remember(uiState.downloadedIds) {
        SheikhData.allLessons.filter { uiState.downloadedIds.contains(it.id) }
    }

    val totalBytesUsed = remember(uiState.downloadedLessons) {
        uiState.downloadedLessons.sumOf { it.fileSizeBytes }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NaturalSandBg)
            .padding(horizontal = 20.dp)
            .testTag("favorites_screen_lazy_column"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
                Text(
                    text = "المكتبة والمحفوظات",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = NaturalCharcoal
                )
                Text(
                    text = "الدروس المفضلة، التحميلات بدون نت، وسجل الاستماع",
                    fontSize = 13.sp,
                    color = NaturalMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // 3 Tabs: Favorites, Downloads, History
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = NaturalCardBg,
                contentColor = NaturalOlive,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NaturalOlive
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, NaturalSandBorder, RoundedCornerShape(16.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = if (selectedTab == 0) NaturalAccentGold else NaturalMuted,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "المفضلة (${favoriteLessons.size})",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OfflinePin,
                                contentDescription = null,
                                tint = if (selectedTab == 1) NaturalOlive else NaturalMuted,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "بدون نت (${downloadedLessons.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = if (selectedTab == 2) NaturalOlive else NaturalMuted,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "السجل (${uiState.playHistory.size})",
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // Favorites List
                if (favoriteLessons.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🤍", fontSize = 36.sp)
                            Text(
                                text = "لم تقم بإضافة أي درس للمفضلة بعد",
                                fontSize = 14.sp,
                                color = NaturalMuted,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = "اضغط على رمز القلب بجوار أي درس لحفظه هنا",
                                fontSize = 12.sp,
                                color = NaturalMuted
                            )
                        }
                    }
                } else {
                    items(favoriteLessons, key = { it.id }) { lesson ->
                        LessonItemCard(
                            lesson = lesson,
                            isCurrentlyPlaying = currentPlayingLessonId == lesson.id,
                            isFavorite = true,
                            isDownloaded = uiState.downloadedIds.contains(lesson.id),
                            downloadProgress = uiState.activeDownloads[lesson.id],
                            onDownloadClick = onDownloadLesson,
                            onDeleteDownloadClick = onDeleteDownload,
                            onLessonClick = onPlayLesson,
                            onToggleFavorite = onToggleFavorite,
                            quizResult = uiState.quizResults[lesson.id],
                            onQuizClick = onStartQuiz
                        )
                    }
                }
            }
            1 -> {
                // Offline Downloads Tab
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = NaturalCardBg),
                        border = BorderStroke(1.dp, NaturalSandBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${downloadedLessons.size}دروس محملة",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NaturalCharcoal
                                )
                                Text(
                                    text = "الحجم الكلي:${LessonDownloadManager.formatFileSize(totalBytesUsed)}",
                                    fontSize = 11.sp,
                                    color = NaturalMuted
                                )
                            }

                            Button(
                                onClick = onOpenDownloadManager,
                                colors = ButtonDefaults.buttonColors(containerColor = NaturalOlive),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "مدير التحميلات",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (downloadedLessons.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "📥", fontSize = 36.sp)
                            Text(
                                text = "لا توجد دروس محملة للاستماع بدون نت",
                                fontSize = 14.sp,
                                color = NaturalMuted,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = "اضغط على زر التحميل في أي درس للاستماع إليه بدون اتصال",
                                fontSize = 12.sp,
                                color = NaturalMuted
                            )
                        }
                    }
                } else {
                    items(downloadedLessons, key = { it.id }) { lesson ->
                        LessonItemCard(
                            lesson = lesson,
                            isCurrentlyPlaying = currentPlayingLessonId == lesson.id,
                            isFavorite = uiState.favoriteIds.contains(lesson.id),
                            isDownloaded = true,
                            downloadProgress = uiState.activeDownloads[lesson.id],
                            onDownloadClick = onDownloadLesson,
                            onDeleteDownloadClick = onDeleteDownload,
                            onLessonClick = onPlayLesson,
                            onToggleFavorite = onToggleFavorite,
                            quizResult = uiState.quizResults[lesson.id],
                            onQuizClick = onStartQuiz
                        )
                    }
                }
            }
            2 -> {
                // Play History List
                if (uiState.playHistory.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🎧", fontSize = 36.sp)
                            Text(
                                text = "لا يوجد سجل استماع حتى الآن",
                                fontSize = 14.sp,
                                color = NaturalMuted,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = "الدروس التي تبدأ الاستماع إليها ستظهر هنا",
                                fontSize = 12.sp,
                                color = NaturalMuted
                            )
                        }
                    }
                } else {
                    items(uiState.playHistory, key = { it.lessonId }) { historyItem ->
                        val lesson = SheikhData.allLessons.find { it.id == historyItem.lessonId }
                        if (lesson != null) {
                            HistoryItemCard(
                                lesson = lesson,
                                history = historyItem,
                                isCurrentlyPlaying = currentPlayingLessonId == lesson.id,
                                onResumeLesson = { onPlayLesson(lesson) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun HistoryItemCard(
    lesson: Lesson,
    history: PlayHistoryEntity,
    isCurrentlyPlaying: Boolean,
    onResumeLesson: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (history.totalDurationSeconds > 0) {
        (history.lastPositionSeconds.toFloat() / history.totalDurationSeconds).coerceIn(0f, 1f)
    } else {
        0f
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onResumeLesson() }
            .testTag("history_card_${lesson.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NaturalCardBg),
        border = BorderStroke(1.dp, NaturalSandBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCurrentlyPlaying) NaturalOlive.copy(alpha = 0.15f) else NaturalItemBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = lesson.iconEmoji, fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lesson.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalCharcoal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (history.isCompleted) "مكتمل الاستماع" else "وصلت إلى:${PlayerState.formatSeconds(history.lastPositionSeconds)}من${lesson.durationFormatted}",
                            fontSize = 11.sp,
                            color = if (history.isCompleted) NaturalOlive else NaturalMuted
                        )
                    }
                }

                if (history.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "مكتمل",
                        tint = NaturalOlive,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    IconButton(
                        onClick = onResumeLesson,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NaturalItemBg)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "متابعة",
                            tint = NaturalOlive,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (!history.isCompleted) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = NaturalOlive,
                    trackColor = NaturalOliveLight.copy(alpha = 0.4f),
                )
            }
        }
    }
}
