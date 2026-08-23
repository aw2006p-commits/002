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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DownloadedLessonEntity
import com.example.data.model.Lesson
import com.example.data.model.SheikhData
import com.example.download.DownloadProgress
import com.example.download.DownloadStatus
import com.example.download.LessonDownloadManager
import com.example.ui.theme.NaturalAccentGold
import com.example.ui.theme.NaturalCardBg
import com.example.ui.theme.NaturalCharcoal
import com.example.ui.theme.NaturalItemBg
import com.example.ui.theme.NaturalMuted
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalOliveDark
import com.example.ui.theme.NaturalPillBg
import com.example.ui.theme.NaturalSandBg
import com.example.ui.theme.NaturalSandBorder
import com.example.ui.viewmodel.SheikhUiState

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadManagerSheet(
    uiState: SheikhUiState,
    onDismiss: () -> Unit,
    onPlayLesson: (Lesson) -> Unit,
    onDeleteDownload: (String) -> Unit,
    onDeleteAllDownloads: () -> Unit,
    onCancelDownload: (String) -> Unit,
    onExportDownloads: (android.net.Uri) -> Unit = {},
    onImportDownloads: (android.net.Uri) -> Unit = {}
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDeleteAllConfirmDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        uri?.let { onExportDownloads(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { onImportDownloads(it) }
    }

    val downloadedEntities = uiState.downloadedLessons
    val totalBytesUsed = remember(downloadedEntities) {
        downloadedEntities.sumOf { it.fileSizeBytes }
    }


    val activeDownloadingList = remember(uiState.activeDownloads) {
        uiState.activeDownloads.values.filter { it.status == DownloadStatus.DOWNLOADING }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NaturalSandBg,
        dragHandle = null,
        modifier = Modifier.testTag("download_manager_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NaturalPillBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.OfflinePin,
                            contentDescription = "مدير التحميلات",
                            tint = NaturalOlive,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "مدير الدروس الصوتية المحملة",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalCharcoal
                        )
                        Text(
                            text = "الاستماع بدون اتصال بالإنترنت",
                            fontSize = 12.sp,
                            color = NaturalMuted
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_download_manager")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = NaturalMuted
                    )
                }
            }

            // Storage Overview Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NaturalOlive.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SdCard,
                                contentDescription = "المساحة التخزينية",
                                tint = NaturalOlive,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "المساحة المستخدمة:${LessonDownloadManager.formatFileSize(totalBytesUsed)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalCharcoal
                            )
                            Text(
                                text = "${downloadedEntities.size}درس متاح في الذاكرة المحلية",
                                fontSize = 11.sp,
                                color = NaturalMuted
                            )
                        }
                    }

                    if (downloadedEntities.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = { exportLauncher.launch("samir_mustafa_backup.zip") },
                                colors = ButtonDefaults.textButtonColors(contentColor = NaturalOlive),
                                modifier = Modifier.testTag("export_downloads_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "تصدير",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            TextButton(
                                onClick = { showDeleteAllConfirmDialog = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFC0392B)),
                                modifier = Modifier.testTag("delete_all_downloads_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "حذف الكل",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        TextButton(
                            onClick = { importLauncher.launch(arrayOf("application/zip")) },
                            colors = ButtonDefaults.textButtonColors(contentColor = NaturalOlive),
                            modifier = Modifier.testTag("import_downloads_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderZip,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "استيراد ملف",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Ongoing Active Downloads
            if (activeDownloadingList.isNotEmpty()) {
                Text(
                    text = "جاري التحميل الآن (${activeDownloadingList.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NaturalOlive,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                activeDownloadingList.forEach { activeProgress ->
                    val lesson = SheikhData.allLessons.find { it.id == activeProgress.lessonId }
                    if (lesson != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = NaturalCardBg),
                            border = BorderStroke(1.dp, NaturalSandBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = lesson.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NaturalCharcoal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "${activeProgress.progressPercent}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NaturalOlive
                                        )

                                        IconButton(
                                            onClick = { onCancelDownload(lesson.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "إلغاء التحميل",
                                                tint = NaturalMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { activeProgress.progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = NaturalOlive,
                                    trackColor = NaturalSandBorder
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Downloaded list or empty state
            if (downloadedEntities.isEmpty() && activeDownloadingList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(NaturalPillBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Download,
                                contentDescription = null,
                                tint = NaturalOlive,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = "لا توجد دروس محملة بعد",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalCharcoal
                        )

                        Text(
                            text = "اضغط على أيقونة التحميل 📥 بجانب أي درس لحفظه في الذاكرة والاستماع إليه دون إنترنت.",
                            fontSize = 12.sp,
                            color = NaturalMuted,
                            modifier = Modifier.padding(horizontal = 24.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(downloadedEntities, key = { it.lessonId }) { entity ->
                        val lesson = SheikhData.allLessons.find { it.id == entity.lessonId }
                        if (lesson != null) {
                            DownloadedLessonRow(
                                lesson = lesson,
                                entity = entity,
                                onPlay = { onPlayLesson(lesson) },
                                onDelete = { onDeleteDownload(lesson.id) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Confirmation dialog for delete all
    if (showDeleteAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirmDialog = false },
            title = {
                Text(
                    text = "حذف جميع الدروس المحملة؟",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = NaturalCharcoal
                )
            },
            text = {
                Text(
                    text = "سيتم إفراغ الذاكرة المحلية وحذف جميع الملفات الصوتية (${downloadedEntities.size}درس). ستظل الدروس متاحة للاستماع عبر الإنترنت.",
                    fontSize = 13.sp,
                    color = NaturalMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAllDownloads()
                        showDeleteAllConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B))
                ) {
                    Text("نعم، حذف الكل", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteAllConfirmDialog = false }) {
                    Text("إلغاء", color = NaturalCharcoal)
                }
            },
            containerColor = NaturalCardBg,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
fun DownloadedLessonRow(
    lesson: Lesson,
    entity: DownloadedLessonEntity,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .testTag("downloaded_row_${lesson.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NaturalCardBg),
        border = BorderStroke(1.dp, NaturalSandBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NaturalItemBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "تشغيل بدون إنترنت",
                        tint = NaturalOlive,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lesson.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalCharcoal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${lesson.series} • ${LessonDownloadManager.formatFileSize(entity.fileSizeBytes)}",
                        fontSize = 11.sp,
                        color = NaturalMuted
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_download_${lesson.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف من الذاكرة",
                        tint = Color(0xFFC0392B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
