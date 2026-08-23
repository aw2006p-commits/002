package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BookmarkEntity
import com.example.data.model.Lesson
import com.example.data.model.LessonCategory
import com.example.data.model.SheikhData
import com.example.data.model.SheikhQuote
import com.example.player.PlayerState
import com.example.ui.components.LessonItemCard
import com.example.ui.components.ShareQuoteDialog
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.LocalFontScale
import com.example.ui.theme.NaturalAccentGold
import com.example.ui.viewmodel.SheikhUiState
import com.example.util.ShareHelper

@Composable
fun SearchQuotesScreen(
    uiState: SheikhUiState,
    currentPlayingLessonId: String?,
    allBookmarks: List<BookmarkEntity> = emptyList(),
    onSearchQueryChanged: (String) -> Unit,
    onPlayLesson: (Lesson) -> Unit,
    onPlayLessonAtTimestamp: ((String, Long) -> Unit)? = null,
    onToggleFavorite: (String) -> Unit,
    onDownloadLesson: (Lesson) -> Unit = {},
    onDeleteDownload: (String) -> Unit = {},
    onStartQuiz: ((Lesson) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedSearchTab by remember { mutableIntStateOf(0) } // 0: الدروس, 1: الفوائد والأقوال, 2: قيودي وملاحظاتي
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val appColors = LocalAppColors.current
    val fontScale = LocalFontScale.current

    val filteredBookmarks = remember(uiState.searchQuery, allBookmarks) {
        if (uiState.searchQuery.isBlank()) allBookmarks
        else allBookmarks.filter { it.note.contains(uiState.searchQuery, ignoreCase = true) }
    }

    val displayLessons = remember(uiState.filteredLessons, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) uiState.filteredLessons
        else uiState.filteredLessons.filter { it.category.displayName == selectedCategoryFilter }
    }

    val categories = remember {
        listOf("الكل") + LessonCategory.values().filter { it != LessonCategory.ALL }.map { it.displayName }
    }
    
    var shareQuoteData by remember { mutableStateOf<com.example.data.model.SheikhQuote?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(appColors.background)
            .padding(horizontal = 20.dp)
            .testTag("search_quotes_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
                Text(
                    text = "البحث والفوائد",
                    fontSize = (22 * fontScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary
                )
                Text(
                    text = "ابحث في محتوى الدروس، درر واقتباسات الشيخ، أو قيودك وملاحظاتك المكتوبة",
                    fontSize = (13 * fontScale).sp,
                    color = appColors.textMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Search Input Bar
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = {
                    Text(
                        text = "ابحث بالاسم، الكلمات الدلالية، أو محتوى الملاحظات...",
                        fontSize = (13 * fontScale).sp,
                        color = appColors.textMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = appColors.primary
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "مسح",
                                tint = appColors.textMuted
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = appColors.surface,
                    unfocusedContainerColor = appColors.surface,
                    focusedBorderColor = appColors.primary,
                    unfocusedBorderColor = appColors.border,
                    focusedTextColor = appColors.textPrimary,
                    unfocusedTextColor = appColors.textPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input_field")
            )
        }

        // Search Tabs (الدروس, الفوائد, ملاحظاتي وقيودي)
        item {
            TabRow(
                selectedTabIndex = selectedSearchTab,
                containerColor = appColors.surface,
                contentColor = appColors.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSearchTab]),
                        color = appColors.primary
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, appColors.border, RoundedCornerShape(16.dp))
            ) {
                Tab(
                    selected = selectedSearchTab == 0,
                    onClick = { selectedSearchTab = 0 },
                    text = {
                        Text(
                            text = "الدروس (${displayLessons.size})",
                            fontWeight = if (selectedSearchTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = (13 * fontScale).sp
                        )
                    }
                )
                Tab(
                    selected = selectedSearchTab == 1,
                    onClick = { selectedSearchTab = 1 },
                    text = {
                        Text(
                            text = "الفوائد (${uiState.filteredQuotes.size})",
                            fontWeight = if (selectedSearchTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = (13 * fontScale).sp
                        )
                    }
                )
                Tab(
                    selected = selectedSearchTab == 2,
                    onClick = { selectedSearchTab = 2 },
                    text = {
                        Text(
                            text = "قيودي (${filteredBookmarks.size})",
                            fontWeight = if (selectedSearchTab == 2) FontWeight.Bold else FontWeight.Normal,
                            fontSize = (13 * fontScale).sp
                        )
                    }
                )
            }
        }

        // Filter chips when on Lessons tab
        if (selectedSearchTab == 0) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "تصفية",
                        tint = appColors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )

                    categories.forEach { cat ->
                        val isSelected = (cat == "الكل" && selectedCategoryFilter == null) || (cat == selectedCategoryFilter)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) appColors.primary else appColors.surface,
                            border = BorderStroke(1.dp, if (isSelected) appColors.primary else appColors.border),
                            modifier = Modifier.clickable {
                                selectedCategoryFilter = if (cat == "الكل") null else cat
                            }
                        ) {
                            Text(
                                text = cat,
                                fontSize = (11 * fontScale).sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else appColors.textPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // TAB 0: LESSONS
        if (selectedSearchTab == 0) {
            if (displayLessons.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🔍", fontSize = 36.sp)
                        Text(
                            text = "لم يتم العثور على دروس مطابقة",
                            fontSize = (14 * fontScale).sp,
                            color = appColors.textMuted,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                items(displayLessons, key = { it.id }) { lesson ->
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
        // TAB 1: QUOTES
        else if (selectedSearchTab == 1) {
            if (uiState.filteredQuotes.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "📜", fontSize = 36.sp)
                        Text(
                            text = "لم يتم العثور على فوائد مطابقة",
                            fontSize = (14 * fontScale).sp,
                            color = appColors.textMuted,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                items(uiState.filteredQuotes, key = { it.id }) { quote ->
                    QuoteCard(
                        quote = quote,
                        onPlayAtTimestamp = onPlayLessonAtTimestamp,
                        onShare = { shareQuoteData = it },
                        onCopy = {
                            val copyContent = "« ${quote.quote} »\n📌 المصدر: ${quote.lessonTitle ?: quote.context} (⏱️ الدقيقة ${quote.timestampFormatted})\nالشيخ سمير مصطفى"
                            clipboardManager.setText(AnnotatedString(copyContent))
                            Toast.makeText(context, "تم نسخ الفائدة ومصدرها إلى الحافظة", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
        // TAB 2: MY NOTES / BOOKMARKS
        else {
            if (filteredBookmarks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "✍️", fontSize = 36.sp)
                        Text(
                            text = if (uiState.searchQuery.isNotBlank()) "لم يتم العثور على قيود مطابقة لبحثك" else "لم تقم بتدوين قيود وملاحظات زمنية بعد",
                            fontSize = (14 * fontScale).sp,
                            color = appColors.textMuted,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "أثناء استماعك لأي درس، اضغط على زر تدوين الملاحظات لحفظ الفائدة وتوقيتها.",
                            fontSize = (12 * fontScale).sp,
                            color = appColors.textMuted,
                            modifier = Modifier.padding(top = 4.dp, start = 20.dp, end = 20.dp)
                        )
                    }
                }
            } else {
                items(filteredBookmarks, key = { it.id }) { bookmark ->
                    val relatedLesson = SheikhData.allLessons.find { it.id == bookmark.lessonId }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = appColors.surface),
                        border = BorderStroke(1.dp, appColors.border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onPlayLessonAtTimestamp?.invoke(bookmark.lessonId, bookmark.timestampSeconds)
                            }
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
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = appColors.primary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = PlayerState.formatSeconds(bookmark.timestampSeconds),
                                            fontSize = (11 * fontScale).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = appColors.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = relatedLesson?.title ?: "درس",
                                        fontSize = (12 * fontScale).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = appColors.textPrimary
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val quote = com.example.data.model.SheikhQuote(
                                            id = "bookmark_${bookmark.id}",
                                            quote = bookmark.note,
                                            context = relatedLesson?.title ?: "درس",
                                            tags = emptyList(),
                                            lessonTitle = relatedLesson?.title,
                                            lessonId = bookmark.lessonId,
                                            timestampSeconds = bookmark.timestampSeconds
                                        )
                                        shareQuoteData = quote
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = appColors.textMuted, modifier = Modifier.size(16.dp))
                                }
                            }

                            Text(
                                text = "« ${bookmark.note} »",
                                fontSize = (13 * fontScale).sp,
                                color = appColors.textPrimary,
                                lineHeight = (20 * fontScale).sp
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

    shareQuoteData?.let { quote: SheikhQuote ->
        ShareQuoteDialog(
            quote = quote,
            onDismiss = { shareQuoteData = null }
        )
    }
}

@Composable
fun QuoteCard(
    quote: SheikhQuote,
    onCopy: () -> Unit,
    onShare: (SheikhQuote) -> Unit,
    onPlayAtTimestamp: ((String, Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    val fontScale = LocalFontScale.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quote_card_${quote.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.surface),
        border = BorderStroke(1.dp, appColors.border)
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = NaturalAccentGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = quote.context,
                        fontSize = (12 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.primary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val cardContext = LocalContext.current
                    IconButton(
                        onClick = { onShare(quote) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("quote_share_${quote.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة الفائدة",
                            tint = appColors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("quote_copy_${quote.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "نسخ",
                            tint = appColors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = "« ${quote.quote} »",
                fontSize = (14 * fontScale).sp,
                color = appColors.textPrimary,
                lineHeight = (22 * fontScale).sp,
                fontWeight = FontWeight.Medium
            )

            // Tags
            if (quote.tags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    quote.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = appColors.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = (11 * fontScale).sp,
                                color = appColors.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Source Lesson & Timestamp Info Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = appColors.itemBg,
                border = BorderStroke(1.dp, appColors.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = quote.lessonId != null && onPlayAtTimestamp != null) {
                        quote.lessonId?.let { lid ->
                            onPlayAtTimestamp?.invoke(lid, quote.timestampSeconds)
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LibraryBooks,
                            contentDescription = null,
                            tint = appColors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "المصدر: ${quote.lessonTitle ?: quote.context}",
                                fontSize = (12 * fontScale).sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = appColors.accent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "الموضع في الدرس: الدقيقة ${quote.timestampFormatted}",
                                    fontSize = (11 * fontScale).sp,
                                    color = appColors.textMuted
                                )
                            }
                        }
                    }

                    if (quote.lessonId != null && onPlayAtTimestamp != null) {
                        FilledTonalButton(
                            onClick = {
                                onPlayAtTimestamp(quote.lessonId, quote.timestampSeconds)
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = appColors.primary.copy(alpha = 0.15f),
                                contentColor = appColors.primary
                            ),
                            modifier = Modifier
                                .height(34.dp)
                                .padding(start = 6.dp)
                                .testTag("quote_listen_${quote.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "استمع للفائدة",
                                    fontSize = (11 * fontScale).sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
