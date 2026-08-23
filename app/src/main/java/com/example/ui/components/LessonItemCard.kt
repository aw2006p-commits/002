package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Lesson
import com.example.download.DownloadProgress
import com.example.download.DownloadStatus
import com.example.download.LessonDownloadManager
import com.example.ui.theme.NaturalAccentGold
import com.example.ui.theme.NaturalCardBg
import com.example.ui.theme.NaturalCharcoal
import com.example.ui.theme.NaturalItemBg
import com.example.ui.theme.NaturalMuted
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalSandBorder
import com.example.util.ShareHelper

@Composable
fun LessonItemCard(
    lesson: Lesson,
    isCurrentlyPlaying: Boolean,
    isFavorite: Boolean,
    onLessonClick: (Lesson) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
    isDownloaded: Boolean = false,
    downloadProgress: DownloadProgress? = null,
    onDownloadClick: ((Lesson) -> Unit)? = null,
    onDeleteDownloadClick: ((String) -> Unit)? = null,
    onTagClick: ((String) -> Unit)? = null,
    quizResult: com.example.data.local.QuizResultEntity? = null,
    onQuizClick: ((Lesson) -> Unit)? = null
) {
    val context = LocalContext.current
    val isDownloading = downloadProgress?.status == DownloadStatus.DOWNLOADING

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onLessonClick(lesson) }
            .testTag("lesson_card_${lesson.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentlyPlaying) NaturalItemBg else NaturalCardBg
        ),
        border = BorderStroke(
            width = if (isCurrentlyPlaying) 1.5.dp else 1.dp,
            color = if (isCurrentlyPlaying) NaturalOlive else NaturalSandBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left content: Icon + Title & Duration & Tags
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon Box with Offline badge if downloaded
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isCurrentlyPlaying) NaturalOlive.copy(alpha = 0.15f) else NaturalItemBg),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCurrentlyPlaying) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "جاري التشغيل",
                            tint = NaturalOlive,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = lesson.iconEmoji,
                            fontSize = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = lesson.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalCharcoal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "${lesson.date} • ${lesson.durationFormatted}",
                            fontSize = 11.sp,
                            color = NaturalMuted
                        )

                        if (isDownloaded) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NaturalOlive.copy(alpha = 0.15f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "متاح بدون إنترنت",
                                        tint = NaturalOlive,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = "بدون نت",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NaturalOlive
                                    )
                                }
                            }
                        }

                        if (quizResult != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (quizResult.isPassed) NaturalAccentGold.copy(alpha = 0.15f)
                                        else NaturalItemBg
                                    )
                                    .then(
                                        if (onQuizClick != null) Modifier.clickable { onQuizClick(lesson) }
                                        else Modifier
                                    )
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = if (quizResult.isPassed) "🏆 اختبار${quizResult.percentage}%" else "🧠 اختبار${quizResult.percentage}%",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (quizResult.isPassed) NaturalAccentGold else NaturalMuted
                                )
                            }
                        } else if (onQuizClick != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NaturalOlive.copy(alpha = 0.10f))
                                    .clickable { onQuizClick(lesson) }
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "🧠 اختبار",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NaturalOlive
                                )
                            }
                        }
                    }

                    if (lesson.tags.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            lesson.tags.take(3).forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NaturalOlive.copy(alpha = 0.10f))
                                        .then(
                                            if (onTagClick != null) {
                                                Modifier.clickable { onTagClick(tag) }
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                    Text(
                                        text = "#$tag",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NaturalOlive
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Right side: Download action + Share button + Favorite button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                // Download button / progress indicator
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
                        onClick = { onDeleteDownloadClick?.invoke(lesson.id) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("downloaded_badge_${lesson.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OfflinePin,
                            contentDescription = "تم التحميل - اضغط للحذف من الذاكرة",
                            tint = NaturalOlive,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                } else if (onDownloadClick != null) {
                    IconButton(
                        onClick = { onDownloadClick(lesson) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("download_btn_${lesson.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = "تحميل الدرس للاستماع بدون نت",
                            tint = NaturalMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { ShareHelper.shareLesson(context, lesson) },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("share_btn_${lesson.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "مشاركة الدرس",
                        tint = NaturalMuted,
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = { onToggleFavorite(lesson.id) },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("fav_btn_${lesson.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "إضافة للمفضلة",
                        tint = if (isFavorite) NaturalAccentGold else NaturalMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "عرض التفاصيل",
                    tint = NaturalSandBorder,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
