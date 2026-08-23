package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.PlayerState
import com.example.ui.theme.NaturalCardBg
import com.example.ui.theme.NaturalCharcoal
import com.example.ui.theme.NaturalItemBg
import com.example.ui.theme.NaturalMuted
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalOliveLight
import com.example.ui.theme.NaturalSandBorder

@Composable
fun MiniPlayerBar(
    playerState: PlayerState,
    onTogglePlayPause: () -> Unit,
    onOpenFullPlayer: () -> Unit,
    onClosePlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lesson = playerState.currentLesson ?: return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clickable { onOpenFullPlayer() }
            .testTag("mini_player_bar"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NaturalCardBg),
        border = BorderStroke(1.dp, NaturalSandBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Linear playback progress on top edge
            LinearProgressIndicator(
                progress = { playerState.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = NaturalOlive,
                trackColor = NaturalOliveLight.copy(alpha = 0.4f),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(NaturalOlive),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = lesson.iconEmoji,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lesson.title,
                            fontSize = 13.sp,
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
                                text = if (playerState.isPlaying) "جاري الاستماع الآن" else "متوقف مؤقتا",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (playerState.isPlaying) NaturalOlive else NaturalMuted
                            )
                            Text(
                                text = "•",
                                fontSize = 11.sp,
                                color = NaturalMuted
                            )
                            Text(
                                text = "${playerState.formattedCurrentTime} / ${playerState.formattedDuration}",
                                fontSize = 11.sp,
                                color = NaturalMuted
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Playback speed button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NaturalItemBg,
                        modifier = Modifier
                            .clickable { onOpenFullPlayer() }
                            .padding(end = 4.dp)
                    ) {
                        Text(
                            text = "${playerState.playbackSpeed}x",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalOlive,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }

                    // Play/Pause button
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NaturalOlive)
                            .testTag("mini_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "إيقاف مؤقت" else "تشغيل",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Close button
                    IconButton(
                        onClick = onClosePlayer,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("mini_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق المشغل",
                            tint = NaturalMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
