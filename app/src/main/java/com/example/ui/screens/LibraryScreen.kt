package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PlayHistoryEntity
import com.example.data.model.Lesson
import com.example.data.model.SeriesInfo
import com.example.ui.components.LessonItemCard
import com.example.ui.theme.NaturalAccentGold
import com.example.ui.theme.NaturalCardBg
import com.example.ui.theme.NaturalCharcoal
import com.example.ui.theme.NaturalItemBg
import com.example.ui.theme.NaturalMuted
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalOliveDark
import com.example.ui.theme.NaturalOliveLight
import com.example.ui.theme.NaturalPillBg
import com.example.ui.theme.NaturalSandBg
import com.example.ui.theme.NaturalSandBorder
import com.example.ui.viewmodel.SheikhUiState

data class SeriesProgress(
    val totalLessons: Int,
    val completedLessons: Int,
    val inProgressLessons: Int,
    val progressFraction: Float,
    val percentage: Int
)

fun calculateSeriesProgress(seriesTitle: String, playHistory: List<PlayHistoryEntity>): SeriesProgress {
    val seriesLessons = uiState.allLessons.filter { it.series == seriesTitle }
    val totalCount = if (seriesLessons.isNotEmpty()) seriesLessons.size else 1
    val historyMap = playHistory.associateBy { it.lessonId }
    
    var completed = 0
    var inProgress = 0
    
    for (lesson in seriesLessons) {
        val history = historyMap[lesson.id]
        if (history != null) {
            if (history.isCompleted || (history.totalDurationSeconds > 0 && history.lastPositionSeconds >= (history.totalDurationSeconds * 0.85f).toLong())) {
                completed++
            } else if (history.lastPositionSeconds > 20L) {
                inProgress++
            }
        }
    }
    
    val fraction = (completed.toFloat() / totalCount).coerceIn(0f, 1f)
    val percentage = (fraction * 100).toInt()
    
    return SeriesProgress(
        totalLessons = totalCount,
        completedLessons = completed,
        inProgressLessons = inProgress,
        progressFraction = fraction,
        percentage = percentage
    )
}

@Composable
fun LibraryScreen(
    uiState: SheikhUiState,
    currentPlayingLessonId: String?,
    onPlayLesson: (Lesson) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onDownloadLesson: (Lesson) -> Unit = {},
    onDeleteDownload: (String) -> Unit = {},
    onStartQuiz: ((Lesson) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedSeriesTitle by remember { mutableStateOf<String?>(null) }

    val seriesProgressMap = remember(uiState.playHistory) {
        uiState.seriesList.associate { series ->
            series.title to calculateSeriesProgress(series.title, uiState.playHistory)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NaturalSandBg)
            .padding(horizontal = 20.dp)
            .testTag("library_screen_lazy_column"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
                if (selectedSeriesTitle != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clickable { selectedSeriesTitle = null }
                            .padding(bottom = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = NaturalOlive,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "العودة لقائمة السلاسل",
                            color = NaturalOlive,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = selectedSeriesTitle ?: "",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalCharcoal
                    )
                } else {
                    Text(
                        text = "المكتبة والسلاسل العلمية",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalCharcoal
                    )
                    Text(
                        text = "سلاسل ودروس الشيخ سمير مصطفى مرتبة ومبوبة مع متابعة نسبة إنجازك",
                        fontSize = 13.sp,
                        color = NaturalMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        if (selectedSeriesTitle == null) {
            // Display all Series Cards with Progress Indicators
            items(uiState.seriesList) { series ->
                val progress = seriesProgressMap[series.title] ?: SeriesProgress(
                    totalLessons = series.lessonsCount,
                    completedLessons = 0,
                    inProgressLessons = 0,
                    progressFraction = 0f,
                    percentage = 0
                )
                SeriesCard(
                    series = series,
                    progress = progress,
                    onClick = { selectedSeriesTitle = series.title }
                )
            }
        } else {
            // Series Detail Header with Big Progress Bar
            val seriesInfo = uiState.seriesList.find { it.title == selectedSeriesTitle }
            val seriesLessons = uiState.allLessons.filter { it.series == selectedSeriesTitle }
            val progress = seriesProgressMap[selectedSeriesTitle] ?: calculateSeriesProgress(selectedSeriesTitle ?: "", uiState.playHistory)

            if (seriesInfo != null) {
                item {
                    SeriesDetailHeaderCard(
                        series = seriesInfo,
                        progress = progress
                    )
                }
            }

            if (seriesLessons.isEmpty()) {
                item {
                    Text(
                        text = "لا توجد دروس حاليا في هذه السلسلة",
                        color = NaturalMuted,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }
            } else {
                items(seriesLessons, key = { it.id }) { lesson ->
                    LessonItemCard(
                        lesson = lesson,
                        isCurrentlyPlaying = currentPlayingLessonId == lesson.id,
                        isFavorite = uiState.favoriteIds.contains(lesson.id),
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

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SeriesCard(
    series: SeriesInfo,
    progress: SeriesProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = progress.percentage == 100
    val hasStarted = progress.completedLessons > 0 || progress.inProgressLessons > 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("series_card_${series.title}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = NaturalCardBg),
        border = BorderStroke(
            1.dp,
            if (isCompleted) NaturalAccentGold.copy(alpha = 0.6f) else NaturalSandBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isCompleted) NaturalAccentGold.copy(alpha = 0.15f) else NaturalPillBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = series.iconEmoji, fontSize = 22.sp)
                    }

                    Column {
                        Text(
                            text = series.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalCharcoal
                        )
                        Text(
                            text = "${series.lessonsCount}درسا •${series.totalDuration}",
                            fontSize = 11.sp,
                            color = NaturalOlive,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (isCompleted) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NaturalAccentGold.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "مكتملة",
                                tint = NaturalAccentGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "مكتملة",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalAccentGold
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "عرض السلسلة",
                        tint = NaturalSandBorder,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = series.description,
                fontSize = 12.sp,
                color = NaturalMuted,
                lineHeight = 18.sp
            )

            // Progress Bar Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NaturalItemBg)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            isCompleted -> "✨ أتممت جميع الدروس بنجاح"
                            hasStarted -> "🎧 أنجزت${progress.completedLessons}من أصل${progress.totalLessons}درسا"
                            else -> "لم تبدأ بعد في هذه السلسلة"
                        },
                        fontSize = 11.sp,
                        fontWeight = if (hasStarted) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCompleted) NaturalAccentGold else if (hasStarted) NaturalOliveDark else NaturalMuted
                    )

                    Text(
                        text = "${progress.percentage}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) NaturalAccentGold else if (hasStarted) NaturalOlive else NaturalMuted
                    )
                }

                LinearProgressIndicator(
                    progress = { progress.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isCompleted) NaturalAccentGold else NaturalOlive,
                    trackColor = NaturalSandBorder.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun SeriesDetailHeaderCard(
    series: SeriesInfo,
    progress: SeriesProgress,
    modifier: Modifier = Modifier
) {
    val isCompleted = progress.percentage == 100
    val hasStarted = progress.completedLessons > 0 || progress.inProgressLessons > 0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = NaturalCardBg),
        border = BorderStroke(
            1.dp,
            if (isCompleted) NaturalAccentGold.copy(alpha = 0.6f) else NaturalSandBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isCompleted) NaturalAccentGold.copy(alpha = 0.15f) else NaturalPillBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = series.iconEmoji, fontSize = 28.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = series.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalCharcoal
                    )
                    Text(
                        text = "${series.lessonsCount}مجالس ودروس •${series.totalDuration}",
                        fontSize = 12.sp,
                        color = NaturalOlive,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                text = series.description,
                fontSize = 13.sp,
                color = NaturalCharcoal.copy(alpha = 0.85f),
                lineHeight = 20.sp
            )

            // Prominent Progress Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isCompleted) NaturalAccentGold.copy(alpha = 0.08f) else NaturalItemBg,
                border = BorderStroke(
                    1.dp,
                    if (isCompleted) NaturalAccentGold.copy(alpha = 0.3f) else NaturalSandBorder
                ),
                modifier = Modifier.fillMaxWidth()
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
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "مؤشر التقدم العلمي:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalCharcoal
                            )
                            Text(
                                text = "${progress.completedLessons}من${progress.totalLessons}دروس",
                                fontSize = 12.sp,
                                color = NaturalOlive,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCompleted) NaturalAccentGold else NaturalOlive
                        ) {
                            Text(
                                text = "${progress.percentage}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { progress.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (isCompleted) NaturalAccentGold else NaturalOlive,
                        trackColor = NaturalSandBorder.copy(alpha = 0.5f)
                    )

                    Text(
                        text = when {
                            isCompleted -> "🏆 هنيئا لك! أتممت الاستماع لهذه السلسلة المباركة بالكامل."
                            hasStarted -> "🌿 واصل الاستماع واحرص على قيد الفوائد والعمل بالعلم."
                            else -> "🚀 ابدأ الآن بالاستماع إلى الدرس الأول وتتبع إنجازك خطوة بخطوة."
                        },
                        fontSize = 11.sp,
                        color = if (isCompleted) NaturalAccentGold else NaturalMuted,
                        fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
