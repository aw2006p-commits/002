package com.example.ui.components

import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BookmarkEntity
import com.example.data.model.Lesson
import com.example.player.PlayerState
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.LocalFontScale
import com.example.ui.theme.NaturalAccentGold
import com.example.ui.theme.NaturalOlive

@Composable
fun AddBookmarkDialog(
    currentTime: Long,
    lessonTitle: String = "",
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var note by remember { mutableStateOf("") }
    val formattedTime = PlayerState.formatSeconds(currentTime)
    val appColors = LocalAppColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Bookmark, contentDescription = null, tint = appColors.primary, modifier = Modifier.size(22.dp))
                Column {
                    Text(
                        text = "تدوين فائدة علمية",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = appColors.textPrimary
                    )
                    Text(
                        text = "عند التوقيت: $formattedTime",
                        fontSize = 12.sp,
                        color = appColors.primary
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (lessonTitle.isNotBlank()) {
                    Text(
                        text = "الدرس: $lessonTitle",
                        fontSize = 12.sp,
                        color = appColors.textMuted
                    )
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("اكتب فائدتك أو استنباطك من كلام الشيخ...", fontSize = 13.sp, color = appColors.textMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = appColors.itemBg,
                        unfocusedContainerColor = appColors.itemBg,
                        focusedBorderColor = appColors.primary,
                        unfocusedBorderColor = appColors.border,
                        focusedTextColor = appColors.textPrimary,
                        unfocusedTextColor = appColors.textPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(note) },
                colors = ButtonDefaults.buttonColors(containerColor = appColors.primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("حفظ الفائدة", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = appColors.textMuted)
            }
        },
        containerColor = appColors.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun BookmarksList(
    bookmarks: List<BookmarkEntity>,
    onPlayBookmark: (Long) -> Unit,
    onShareBookmark: (BookmarkEntity) -> Unit,
    onDeleteBookmark: ((String) -> Unit)? = null
) {
    if (bookmarks.isEmpty()) return

    val appColors = LocalAppColors.current
    val fontScale = LocalFontScale.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("📝", fontSize = 16.sp)
                Text(
                    text = "فوائدي وملاحظاتي الزمنية (${bookmarks.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary
                )
            }

            // Export all notes button
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = appColors.primary.copy(alpha = 0.1f),
                modifier = Modifier.clickable {
                    exportAllBookmarksText(context, bookmarks)
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = appColors.primary, modifier = Modifier.size(14.dp))
                    Text(
                        text = "تصدير الفوائد",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.primary
                    )
                }
            }
        }

        bookmarks.forEach { bookmark ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = appColors.surface),
                border = BorderStroke(1.dp, appColors.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlayBookmark(bookmark.timestampSeconds) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Jump timestamp button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = appColors.primary.copy(alpha = 0.12f),
                        modifier = Modifier.clickable { onPlayBookmark(bookmark.timestampSeconds) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = appColors.primary, modifier = Modifier.size(14.dp))
                            Text(
                                text = PlayerState.formatSeconds(bookmark.timestampSeconds),
                                color = appColors.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bookmark.note.ifBlank { "فائدة مسجلة عند هذا التوقيت" },
                            fontSize = (13 * fontScale).sp,
                            color = appColors.textPrimary,
                            lineHeight = (19 * fontScale).sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { onShareBookmark(bookmark) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "مشاركة",
                                tint = appColors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (onDeleteBookmark != null) {
                            IconButton(
                                onClick = { onDeleteBookmark(bookmark.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف",
                                    tint = Color.Red.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun exportAllBookmarksText(context: Context, bookmarks: List<BookmarkEntity>) {
    val builder = StringBuilder()
    builder.append("📖 فوائد وملاحظات من دروس الشيخ سمير مصطفى:\n\n")
    bookmarks.forEachIndexed { index, b ->
        builder.append("${index + 1}. [${PlayerState.formatSeconds(b.timestampSeconds)}]: ${b.note}\n\n")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "فوائد دروس الشيخ سمير مصطفى")
        putExtra(Intent.EXTRA_TEXT, builder.toString())
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة وتصدير الفوائد"))
}
