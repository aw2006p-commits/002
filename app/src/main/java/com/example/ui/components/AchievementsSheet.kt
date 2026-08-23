package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.data.local.DailyStatsEntity
import com.example.data.local.PlaylistEntity
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.LocalFontScale
import com.example.ui.theme.NaturalAccentGold
import com.example.ui.theme.NaturalOliveDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsSheet(
    stats: List<DailyStatsEntity>,
    playlists: List<PlaylistEntity>,
    dailyGoalMinutes: Int = 30,
    onCreatePlaylist: (String) -> Unit,
    onDismiss: () -> Unit,
    quizResults: Map<String, com.example.data.local.QuizResultEntity> = emptyMap()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    val appColors = LocalAppColors.current
    val fontScale = LocalFontScale.current

    val totalSeconds = stats.sumOf { it.listeningSeconds }
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60

    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
    val todayStats = stats.find { it.dateString == todayStr }
    val todayMinutes = (todayStats?.listeningSeconds ?: 0L) / 60
    val dailyProgressFraction = if (dailyGoalMinutes > 0) {
        (todayMinutes.toFloat() / dailyGoalMinutes).coerceIn(0f, 1f)
    } else 0f

    val passedQuizzesCount = quizResults.values.count { it.isPassed }
    val totalQuizzesTaken = quizResults.size
    val averageScore = if (totalQuizzesTaken > 0) quizResults.values.map { it.percentage }.average().toInt() else 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = appColors.background,
        modifier = Modifier.testTag("achievements_bottom_sheet")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NaturalAccentGold.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = NaturalAccentGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "إنجازي العلمي والورد اليومي",
                            fontSize = (18 * fontScale).sp,
                            fontWeight = FontWeight.Bold,
                            color = appColors.textPrimary
                        )
                        Text(
                            text = "متابعة الساعات المسموعة وإتقان الاختبارات",
                            fontSize = (12 * fontScale).sp,
                            color = appColors.textMuted
                        )
                    }
                }
            }

            // 1. Daily Habit Tracker Card (الورد اليومي)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = appColors.surface),
                    border = BorderStroke(1.dp, appColors.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = appColors.primary, modifier = Modifier.size(20.dp))
                                Text(
                                    text = "الورد اليومي للاستماع",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textPrimary
                                )
                            }
                            Text(
                                text = "$todayMinutes / $dailyGoalMinutes دقيقة",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (todayMinutes >= dailyGoalMinutes) Color(0xFF10B981) else appColors.primary
                            )
                        }

                        LinearProgressIndicator(
                            progress = { dailyProgressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (todayMinutes >= dailyGoalMinutes) Color(0xFF10B981) else appColors.primary,
                            trackColor = appColors.border
                        )

                        Text(
                            text = if (todayMinutes >= dailyGoalMinutes)
                                "🎉 ما شاء الله! أتممت وردك اليومي بنجاح، ثبّت الله علمك."
                            else
                                "تبقى لك ${dailyGoalMinutes - todayMinutes} دقيقة لإكمال الورد اليومي المقرر.",
                            fontSize = 11.sp,
                            color = if (todayMinutes >= dailyGoalMinutes) Color(0xFF10B981) else appColors.textMuted,
                            fontWeight = if (todayMinutes >= dailyGoalMinutes) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // 2. Stats Grid (وقت الاستماع + إتقان الاختبارات)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Listening Time
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = appColors.primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("إجمالي الاستماع", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${hours}س و${minutes}د", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("الأيام النشطة: ${stats.size} يوم", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        }
                    }

                    // Quiz Mastery Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NaturalAccentGold),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("إتقان الاختبارات 🧠", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$passedQuizzesCount مجتاز", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("معدل الدرجات: $averageScore%", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                        }
                    }
                }
            }

            // 3. Section Badges (أوسمة إتقان الأقسام)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("أوسمة إتقان الأقسام العلمية 🏆", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = appColors.textPrimary)

                    val sections = listOf(
                        Pair("category_seerah", "السيرة النبوية 🕌"),
                        Pair("category_tafseer", "التفسير 📖"),
                        Pair("category_khawatir", "الرقائق 💧"),
                        Pair("category_fiqh", "الفقه ⚖️"),
                        Pair("category_khutbah", "الخطب 🎙️")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sections.forEach { (catId, title) ->
                            val res = quizResults[catId]
                            val passed = res?.isPassed == true
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (passed) appColors.primary.copy(alpha = 0.12f) else appColors.surface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (passed) appColors.primary else appColors.border
                                )
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = appColors.textPrimary)
                                    Text(
                                        if (passed) "مجتاز (${res?.percentage}%) 👑" else "لم يكتمل بعد",
                                        fontSize = 10.sp,
                                        color = if (passed) appColors.primary else appColors.textMuted,
                                        fontWeight = if (passed) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Playlists Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("قوائم التشغيل الخاصة بي", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = appColors.textPrimary)
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "إنشاء قائمة", tint = appColors.primary)
                    }
                }
            }

            if (playlists.isEmpty()) {
                item {
                    Text("لا توجد قوائم تشغيل مخصصة حتى الآن", color = appColors.textMuted, fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp))
                }
            } else {
                items(playlists) { playlist ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = appColors.surface),
                        border = BorderStroke(1.dp, appColors.border),
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(appColors.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlaylistPlay, contentDescription = null, tint = appColors.primary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(playlist.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = appColors.textPrimary)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("إنشاء قائمة جديدة", fontWeight = FontWeight.Bold, color = appColors.textPrimary) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("اسم القائمة") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onCreatePlaylist(newPlaylistName)
                            newPlaylistName = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
                ) {
                    Text("إنشاء", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("إلغاء", color = appColors.textMuted) }
            },
            containerColor = appColors.surface
        )
    }
}
