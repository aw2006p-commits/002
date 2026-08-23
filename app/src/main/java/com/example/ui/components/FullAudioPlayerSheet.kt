package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Lesson
import com.example.download.DownloadProgress
import com.example.download.DownloadStatus
import com.example.download.LessonDownloadManager
import com.example.player.PlayerState
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
import com.example.util.ShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullAudioPlayerSheet(
    playerState: PlayerState,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekBy: (Long) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onSleepTimerChange: (Int?) -> Unit,
    onToggleFavorite: (String) -> Unit,
    isDownloaded: Boolean = false,
    downloadProgress: DownloadProgress? = null,
    onDownloadClick: (Lesson) -> Unit = {},
    onDeleteDownloadClick: (String) -> Unit = {},
    onAddBookmark: (Long, String) -> Unit = { _, _ -> },
    onDeleteBookmark: ((String) -> Unit)? = null,
    bookmarks: List<com.example.data.local.BookmarkEntity> = emptyList(),
    onShareTimestamp: (Long) -> Unit = {},
    onStartQuiz: (Lesson) -> Unit = {},
    quizResult: com.example.data.local.QuizResultEntity? = null
) {
    val lesson = playerState.currentLesson ?: return
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDownloading = downloadProgress?.status == DownloadStatus.DOWNLOADING

    var showSpeedDialog by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }
    var showBookmarkDialog by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableFloatStateOf(0f) }
    var isFocusMode by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (playerState.isPlaying) 1.05f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "disc_pulse"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isFocusMode) NaturalCharcoal else NaturalSandBg,
        dragHandle = null,
        modifier = Modifier.testTag("full_audio_player_sheet")
    ) {
        val textColor = if (isFocusMode) Color.White else NaturalCharcoal
        val mutedColor = if (isFocusMode) Color.White.copy(alpha = 0.6f) else NaturalMuted
        val oliveColor = if (isFocusMode) NaturalSandBg else NaturalOlive

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("dismiss_player_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "تصغير",
                        tint = textColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isFocusMode) "وضع الخلوة والتركيز" else "جاري الاستماع",
                        fontSize = 12.sp,
                        color = mutedColor,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "الشيخ سمير مصطفى",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = oliveColor
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Focus Mode Toggle
                    IconButton(
                        onClick = { isFocusMode = !isFocusMode },
                        modifier = Modifier.testTag("focus_mode_button")
                    ) {
                        Icon(
                            imageVector = if (isFocusMode) Icons.Default.Lightbulb else Icons.Outlined.Lightbulb,
                            contentDescription = "وضع الخلوة",
                            tint = if (isFocusMode) NaturalSandBg else NaturalMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    if (!isFocusMode) {
                        if (isDownloading) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { downloadProgress?.progress ?: 0f },
                                    modifier = Modifier.size(20.dp),
                                    color = NaturalOlive,
                                    strokeWidth = 2.5.dp,
                                    trackColor = NaturalSandBorder
                                )
                            }
                        } else if (isDownloaded) {
                            IconButton(
                                onClick = { onDeleteDownloadClick(lesson.id) },
                                modifier = Modifier.testTag("full_downloaded_badge")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OfflinePin,
                                    contentDescription = "محمل على الجهاز",
                                    tint = NaturalOlive,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { onDownloadClick(lesson) },
                                modifier = Modifier.testTag("full_download_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Download,
                                    contentDescription = "تحميل للاستماع بدون نت",
                                    tint = NaturalMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { showBookmarkDialog = true },
                            modifier = Modifier.testTag("full_bookmark_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "أضف فائدة",
                                tint = NaturalMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(
                            onClick = { ShareHelper.shareLesson(context, lesson) },
                            modifier = Modifier.testTag("full_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "مشاركة الدرس",
                                tint = NaturalMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = { /* TODO: Open playlist selector */ },
                            modifier = Modifier.testTag("full_playlist_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlaylistAdd,
                                contentDescription = "أضف للقائمة",
                                tint = NaturalMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(
                            onClick = { onToggleFavorite(lesson.id) },
                            modifier = Modifier.testTag("full_fav_button")
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "المفضلة",
                                tint = if (isFavorite) NaturalAccentGold else NaturalMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audio Art Disc / Illustration with Natural Tones
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(NaturalOliveDark),
                contentAlignment = Alignment.Center
            ) {
                // Inner circle
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(NaturalOlive),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lesson.iconEmoji,
                        fontSize = 64.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Badges Row (Series + Offline status)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NaturalPillBg
                ) {
                    Text(
                        text = lesson.series,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalOlive,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                if (isDownloaded || playerState.isOffline) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NaturalOlive.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OfflinePin,
                                contentDescription = "استماع بدون إنترنت",
                                tint = NaturalOlive,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "بدون إنترنت",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalOlive
                            )
                        }
                    }
                }
            }

            // Lesson Title
            Text(
                text = lesson.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NaturalCharcoal,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Slider & Interactive Timeline
            val maxDuration = playerState.durationSeconds.coerceAtLeast(1L).toFloat()
            val currentPos = if (isDragging) dragPosition.coerceIn(0f, maxDuration) else playerState.currentPositionSeconds.toFloat().coerceIn(0f, maxDuration)
            val displayCurrentTime = if (isDragging) PlayerState.formatSeconds(dragPosition.toLong().coerceIn(0L, maxDuration.toLong())) else playerState.formattedCurrentTime
            val percent = if (maxDuration > 0) ((currentPos / maxDuration) * 100).toInt() else 0

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Slider(
                    value = currentPos,
                    onValueChange = {
                        isDragging = true
                        dragPosition = it
                    },
                    onValueChangeFinished = {
                        onSeekTo(dragPosition.toLong().coerceIn(0L, maxDuration.toLong()))
                        isDragging = false
                    },
                    valueRange = 0f..maxDuration,
                    colors = SliderDefaults.colors(
                        thumbColor = NaturalOlive,
                        activeTrackColor = NaturalOlive,
                        inactiveTrackColor = NaturalOliveLight.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("audio_seek_slider")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = displayCurrentTime,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalOlive
                        )
                        if (isDragging) {
                            Text(
                                text = "(سحب للتخطي)",
                                fontSize = 11.sp,
                                color = NaturalMuted
                            )
                        }
                    }

                    Text(
                        text = "$percent%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = NaturalMuted
                    )

                    Text(
                        text = playerState.formattedDuration,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Rewind 10s
                IconButton(
                    onClick = { onSeekBy(-10) },
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("rewind_10_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "رجوع ١٠ ثوان",
                        tint = textColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Big Play/Pause Button
                Surface(
                    shape = CircleShape,
                    color = oliveColor,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(68.dp)
                        .clickable { onTogglePlayPause() }
                        .testTag("full_play_pause_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "إيقاف مؤقت" else "تشغيل",
                            tint = if (isFocusMode) NaturalCharcoal else Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Forward 10s
                IconButton(
                    onClick = { onSeekBy(10) },
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("forward_10_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "تقديم ١٠ ثوان",
                        tint = textColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            if (!isFocusMode) {
                Spacer(modifier = Modifier.height(18.dp))

                // Extra Settings Row: Playback Speed & Sleep Timer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Speed Chip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = NaturalItemBg,
                    border = BorderStroke(1.dp, NaturalSandBorder),
                    modifier = Modifier
                        .clickable { showSpeedDialog = !showSpeedDialog }
                        .testTag("speed_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "سرعة التشغيل",
                            tint = NaturalOlive,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${playerState.playbackSpeed}x",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalCharcoal
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Sleep Timer Chip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (playerState.sleepTimerMinutesLeft != null) NaturalOlive else NaturalItemBg,
                    border = BorderStroke(1.dp, NaturalSandBorder),
                    modifier = Modifier
                        .clickable { showTimerDialog = !showTimerDialog }
                        .testTag("sleep_timer_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "مؤقت النوم",
                            tint = if (playerState.sleepTimerMinutesLeft != null) Color.White else NaturalOlive,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (playerState.sleepTimerMinutesLeft != null)
                                "${playerState.sleepTimerMinutesLeft}د"
                            else "مؤقت النوم",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (playerState.sleepTimerMinutesLeft != null) Color.White else NaturalCharcoal
                        )
                    }
                }
            }

            // Speed Selector Drawer
            if (showSpeedDialog) {
                Row(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .background(NaturalItemBg, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                        val isSelected = playerState.playbackSpeed == speed
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) NaturalOlive else Color.Transparent,
                            modifier = Modifier.clickable {
                                onSpeedChange(speed)
                                showSpeedDialog = false
                            }
                        ) {
                            Text(
                                text = "${speed}x",
                                color = if (isSelected) Color.White else NaturalCharcoal,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Timer Selector Drawer
            if (showTimerDialog) {
                Row(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .background(NaturalItemBg, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(null to "إيقاف", 15 to "١٥ د", 30 to "٣٠ د", 45 to "٤٥ د", 60 to "٦٠ د").forEach { (min, label) ->
                        val isSelected = playerState.sleepTimerMinutesLeft == min
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) NaturalOlive else Color.Transparent,
                            modifier = Modifier.clickable {
                                onSleepTimerChange(min)
                                showTimerDialog = false
                            }
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else NaturalCharcoal,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Key Takeaways & Description Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NaturalCardBg),
                border = BorderStroke(1.dp, NaturalSandBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "📖 محاور وفوائد الدرس",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NaturalOlive
                    )

                    Text(
                        text = lesson.description,
                        fontSize = 13.sp,
                        color = NaturalCharcoal,
                        lineHeight = 20.sp
                    )

                    if (lesson.keyTakeaways.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            lesson.keyTakeaways.forEach { takeaway ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = "•", color = NaturalOlive, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = takeaway,
                                        fontSize = 12.sp,
                                        color = NaturalCharcoal.copy(alpha = 0.9f),
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // End-of-Lesson Quiz Card (اختبار ترسيخ الدرس)
                    val isCompletedOrNearEnd = playerState.progressFraction >= 0.80f
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (quizResult?.isPassed == true)
                            Color(0xFFE8F5E9)
                        else if (isCompletedOrNearEnd)
                            NaturalAccentGold.copy(alpha = 0.15f)
                        else
                            NaturalItemBg,
                        border = BorderStroke(
                            1.dp,
                            if (quizResult?.isPassed == true)
                                Color(0xFF81C784)
                            else if (isCompletedOrNearEnd)
                                NaturalAccentGold.copy(alpha = 0.6f)
                            else
                                NaturalSandBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStartQuiz(lesson) }
                            .testTag("full_player_quiz_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (quizResult?.isPassed == true) Color(0xFF2E7D32).copy(alpha = 0.15f)
                                            else NaturalOlive.copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (quizResult?.isPassed == true) "🏆" else "🧠",
                                        fontSize = 18.sp
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "اختبار ترسيخ الفوائد والتمكين",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NaturalCharcoal
                                        )
                                        if (isCompletedOrNearEnd && quizResult == null) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = NaturalAccentGold
                                            ) {
                                                Text(
                                                    text = "حان وقته!",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = if (quizResult != null)
                                            "تم اجتياز الاختبار بنتيجة${quizResult.percentage}% (${quizResult.score}/${quizResult.totalQuestions}) • اضغط لإعادة الاختبار"
                                        else if (isCompletedOrNearEnd)
                                            "أوشكت على إنهاء المجلس! اختبر استيعابك للمسائل الدقيقة الآن"
                                        else
                                            "أسئلة دقيقة لترسيخ مسائل الدرس وتثبيت العلم في الصدر",
                                        fontSize = 11.sp,
                                        color = if (quizResult?.isPassed == true) Color(0xFF2E7D32) else NaturalMuted,
                                        fontWeight = if (quizResult?.isPassed == true) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "بدء الاختبار",
                                tint = if (quizResult?.isPassed == true) Color(0xFF2E7D32) else NaturalOlive,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Offline Download Action Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDownloaded) NaturalOlive.copy(alpha = 0.12f) else NaturalItemBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isDownloaded) {
                                    onDeleteDownloadClick(lesson.id)
                                } else if (!isDownloading) {
                                    onDownloadClick(lesson)
                                }
                            }
                            .testTag("full_download_action_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 14.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isDownloading) {
                                CircularProgressIndicator(
                                    progress = { downloadProgress?.progress ?: 0f },
                                    modifier = Modifier.size(16.dp),
                                    color = NaturalOlive,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "جاري التحميل...${downloadProgress?.progressPercent}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NaturalOlive
                                )
                            } else if (isDownloaded) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "محمل على الجهاز",
                                    tint = NaturalOlive,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "الدرس محمل في الذاكرة (اضغط للحذف)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NaturalOlive
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Download,
                                    contentDescription = "تحميل الدرس للاستماع بدون نت",
                                    tint = NaturalOlive,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تحميل الدرس للاستماع بدون إنترنت (${LessonDownloadManager.formatFileSize(LessonDownloadManager.calculateFileSize(lesson.durationSeconds))})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NaturalOlive
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    BookmarksList(
                        bookmarks = bookmarks,
                        onPlayBookmark = { onSeekTo(it) },
                        onShareBookmark = { bookmark ->
                            val link = "https://example.com/share?lesson=${lesson.id}&t=${bookmark.timestampSeconds}"
                            val text = "فائدة من الشيخ سمير مصطفى:\n\"${bookmark.note}\"\n\nاستمع من الدقيقة${PlayerState.formatSeconds(bookmark.timestampSeconds)}:\n$link"
                            ShareHelper.shareText(context, text)
                        },
                        onDeleteBookmark = onDeleteBookmark
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NaturalItemBg,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { ShareHelper.shareLesson(context, lesson) }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = NaturalOlive, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("الدرس كاملا", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NaturalOlive)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NaturalOlive,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val link = "https://example.com/share?lesson=${lesson.id}&t=${playerState.currentPositionSeconds}"
                                    val text = "🎧 استمع إلى مقطع من درس '${lesson.title}' للشيخ سمير مصطفى ابتداء من الدقيقة${playerState.formattedCurrentTime}:\n$link"
                                    ShareHelper.shareText(context, text)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("من هذه الدقيقة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            } // End of Focus Mode block
        }
    }

    if (showBookmarkDialog) {
        AddBookmarkDialog(
            currentTime = playerState.currentPositionSeconds,
            onDismiss = { showBookmarkDialog = false },
            onSave = { note ->
                onAddBookmark(playerState.currentPositionSeconds, note)
                showBookmarkDialog = false
            }
        )
    }
}
}
