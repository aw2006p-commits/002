package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Lesson
import com.example.data.model.LessonCategory
import com.example.data.model.SeriesInfo
import com.example.data.model.SheikhData
import com.example.ui.components.LessonItemCard
import com.example.ui.components.ShareQuoteDialog
import com.example.ui.theme.GoldGradient
import com.example.ui.theme.OliveGradient
import com.example.ui.theme.RoyalCardGradient
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
import com.example.util.ShareHelper

@Composable
fun HomeScreen(
    uiState: SheikhUiState,
    onNavigateToLessons: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onOpenAchievements: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onStartQuiz: (Lesson) -> Unit = {},
    onStartCategoryQuiz: ((LessonCategory) -> Unit)? = null,
    currentPlayingLessonId: String? = null,
    onPlayLesson: ((Lesson) -> Unit)? = null,
    onToggleFavorite: ((String) -> Unit)? = null,
    onDownloadLesson: ((Lesson) -> Unit)? = null,
    onDeleteDownload: ((String) -> Unit)? = null,
    onSelectSeries: ((SeriesInfo) -> Unit)? = null,
    onToggleThemeMode: (() -> Unit)? = null,
    onPlayLessonAtTimestamp: ((String, Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val dailyQuote = SheikhData.quotes.firstOrNull()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTab by remember { mutableIntStateOf(0) } // 0: الكل, 1: السلاسل, 2: الدروس
    var shareQuoteData by remember { mutableStateOf<com.example.data.model.SheikhQuote?>(null) }

    val trimmedQuery = searchQuery.trim()
    val isSearching = trimmedQuery.isNotEmpty()

    // Filter Series & Lessons based on search query
    val matchingSeries = remember(trimmedQuery) {
        if (trimmedQuery.isEmpty()) emptyList()
        else {
            SheikhData.seriesList.filter { series ->
                series.title.contains(trimmedQuery, ignoreCase = true) ||
                        series.description.contains(trimmedQuery, ignoreCase = true)
            }
        }
    }

    val matchingLessons = remember(trimmedQuery) {
        if (trimmedQuery.isEmpty()) emptyList()
        else {
            SheikhData.allLessons.filter { lesson ->
                lesson.title.contains(trimmedQuery, ignoreCase = true) ||
                        lesson.series.contains(trimmedQuery, ignoreCase = true) ||
                        lesson.description.contains(trimmedQuery, ignoreCase = true) ||
                        lesson.tags.any { tag -> tag.contains(trimmedQuery, ignoreCase = true) }
            }
        }
    }

    val totalResultsCount = matchingSeries.size + matchingLessons.size

    val quickSearchTags = listOf(
        "سيرة النبي ﷺ",
        "المعارك الكبرى",
        "شرح الوابل الصيب",
        "سورة الكهف",
        "عقائد البلاء",
        "صياغة الطريق",
        "سورة مريم",
        "قيام الليل",
        "فقه القلوب",
        "التوكل"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NaturalSandBg)
            .padding(horizontal = 20.dp)
            .testTag("home_screen_lazy_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Welcome Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "منصة المحتوى العلمي والدعوي",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalOlive,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "الشيخ سمير مصطفى",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalCharcoal,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Right side: Theme Toggle, Settings, Achievements & Avatar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onToggleThemeMode != null) {
                        val isDarkMode = uiState.userPreferences.themeMode == com.example.data.local.AppThemeMode.DARK
                        IconButton(
                            onClick = onToggleThemeMode,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(NaturalItemBg)
                                .testTag("home_theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDarkMode) "تفعيل الوضع النهاري" else "تفعيل الوضع الليلي",
                                tint = if (isDarkMode) NaturalAccentGold else NaturalOlive,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NaturalItemBg)
                            .testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "الإعدادات والتخصيص",
                            tint = NaturalCharcoal,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenAchievements,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NaturalItemBg)
                            .testTag("home_achievements_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "إنجازي",
                            tint = NaturalOlive,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Sheikh Avatar
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(NaturalPillBg)
                            .border(2.dp, NaturalOlive, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.sheikh_samir_real_avatar),
                            contentDescription = "الشيخ سمير مصطفى",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // =========================================================================
        // SEARCH BAR: شريط البحث في عناوين الدروس والسلاسل العلمية
        // =========================================================================
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_bar_input"),
                    placeholder = {
                        Text(
                            text = "ابحث في عناوين الدروس والسلاسل العلمية...",
                            fontSize = 13.sp,
                            color = NaturalMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = NaturalOlive,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    searchQuery = ""
                                    focusManager.clearFocus()
                                },
                                modifier = Modifier.testTag("home_search_clear_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "مسح البحث",
                                    tint = NaturalMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NaturalCardBg,
                        unfocusedContainerColor = NaturalCardBg,
                        focusedBorderColor = NaturalOlive,
                        unfocusedBorderColor = NaturalSandBorder,
                        cursorColor = NaturalOlive,
                        focusedTextColor = NaturalCharcoal,
                        unfocusedTextColor = NaturalCharcoal
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                )

                // Quick Topic Suggestions Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مقترحات:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalMuted
                    )

                    quickSearchTags.forEach { tag ->
                        val isSelected = searchQuery == tag
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) NaturalOlive else NaturalItemBg,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) NaturalOlive else NaturalSandBorder
                            ),
                            modifier = Modifier
                                .clickable {
                                    if (searchQuery == tag) {
                                        searchQuery = ""
                                    } else {
                                        searchQuery = tag
                                    }
                                }
                                .testTag("quick_search_tag_$tag")
                        ) {
                            Text(
                                text = tag,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else NaturalCharcoal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // SEARCH RESULTS VIEW (When query is active)
        // =========================================================================
        if (isSearching) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Result Header & Count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "نتائج البحث عن «$trimmedQuery»",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalCharcoal
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (totalResultsCount > 0) NaturalOlive.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "$totalResultsCount نتيجة",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalResultsCount > 0) NaturalOliveDark else Color(0xFFC62828),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Filter sub-tabs (الكل / السلاسل / الدروس)
                    if (totalResultsCount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val tabs = listOf(
                                "الكل ($totalResultsCount)" to 0,
                                "السلاسل (${matchingSeries.size})" to 1,
                                "الدروس (${matchingLessons.size})" to 2
                            )
                            tabs.forEach { (title, tabIdx) ->
                                val isSelected = selectedFilterTab == tabIdx
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) NaturalOlive else NaturalItemBg,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) NaturalOlive else NaturalSandBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedFilterTab = tabIdx }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else NaturalCharcoal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // If No Results Found
            if (totalResultsCount == 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("home_search_no_results"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = NaturalCardBg),
                        border = BorderStroke(1.dp, NaturalSandBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "🔍", fontSize = 36.sp)
                            Text(
                                text = "لم يتم العثور على أي نتائج ل «$trimmedQuery»",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalCharcoal
                            )
                            Text(
                                text = "جرب البحث بكلمات أخرى ك 'السيرة'، 'الكهف'، 'الوابل الصيب'، أو 'التوكل'",
                                fontSize = 12.sp,
                                color = NaturalMuted,
                                lineHeight = 18.sp
                            )
                            Button(
                                onClick = { searchQuery = "" },
                                colors = ButtonDefaults.buttonColors(containerColor = NaturalOlive),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "إلغاء البحث وعرض الصفحة الرئيسية",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            } else {
                // Section 1: Matching Series
                if ((selectedFilterTab == 0 || selectedFilterTab == 1) && matchingSeries.isNotEmpty()) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(text = "📚", fontSize = 16.sp)
                            Text(
                                text = "السلاسل العلمية المطابقة (${matchingSeries.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalCharcoal
                            )
                        }
                    }

                    items(matchingSeries, key = { "series_${it.title}" }) { series ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (onSelectSeries != null) {
                                        onSelectSeries(series)
                                    } else {
                                        onNavigateToLibrary()
                                    }
                                }
                                .testTag("home_search_series_${series.title}"),
                            shape = RoundedCornerShape(18.dp),
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
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(NaturalOlive.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = series.iconEmoji, fontSize = 18.sp)
                                        }

                                        Column {
                                            Text(
                                                text = series.title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NaturalCharcoal,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${series.lessonsCount}درس •${series.totalDuration}",
                                                fontSize = 11.sp,
                                                color = NaturalOliveDark,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = NaturalOlive.copy(alpha = 0.1f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "استعراض السلسلة",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NaturalOliveDark
                                            )
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                tint = NaturalOliveDark,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = series.description,
                                    fontSize = 12.sp,
                                    color = NaturalMuted,
                                    lineHeight = 17.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Section 2: Matching Lessons
                if ((selectedFilterTab == 0 || selectedFilterTab == 2) && matchingLessons.isNotEmpty()) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Text(text = "🎙️", fontSize = 16.sp)
                            Text(
                                text = "الدروس والخطب المطابقة (${matchingLessons.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalCharcoal
                            )
                        }
                    }

                    items(matchingLessons, key = { "lesson_${it.id}" }) { lesson ->
                        LessonItemCard(
                            lesson = lesson,
                            isCurrentlyPlaying = currentPlayingLessonId == lesson.id,
                            isFavorite = uiState.favoriteIds.contains(lesson.id),
                            onLessonClick = { onPlayLesson?.invoke(lesson) ?: onNavigateToLessons() },
                            onToggleFavorite = { onToggleFavorite?.invoke(it) },
                            isDownloaded = uiState.downloadedIds.contains(lesson.id),
                            downloadProgress = uiState.activeDownloads[lesson.id],
                            onDownloadClick = onDownloadLesson,
                            onDeleteDownloadClick = onDeleteDownload,
                            quizResult = uiState.quizResults[lesson.id],
                            onQuizClick = onStartQuiz
                        )
                    }
                }
            }
        }

        // =========================================================================
        // DEFAULT HOME SECTIONS (When not searching)
        // =========================================================================
        if (!isSearching) {
            // Hero Card 1: Sheikh Biography Banner Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sheikh_bio_card"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, NaturalSandBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().background(RoyalCardGradient)) {
                        // Visual Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.sheikh_samir_real_banner),
                                contentDescription = "صورة الشيخ سمير مصطفى",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Gradient Overlay at the bottom of the image for smooth transition
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, NaturalCardBg.copy(alpha = 0.9f), NaturalCardBg)
                                        )
                                    )
                            )

                            // Top Pill Tag
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = NaturalOlive.copy(alpha = 0.95f),
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "✨ ترجمة الشيخ وسيرته",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // Content details
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "فضيلة الشيخ سمير مصطفى",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NaturalCharcoal
                            )

                            Text(
                                text = "داعية ومرب إسلامي مصري، ولد عام 1976م بمحافظة أسيوط وعاش معظم حياته بمدينة حلوان. عرف باهتمامه الكبير بعلوم الشريعة وتأصيل العقيدة الصحيحة، وتفرده بأسلوب وعظي مؤثر يجمع بين قوة الحجة والعمق الإيماني وإحياء القلوب.",
                                fontSize = 13.sp,
                                color = NaturalCharcoal.copy(alpha = 0.85f),
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            // Section: Sheikh Scientific Journey & Teachers (الشيوخ والمنهج)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sheikh_teachers_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = NaturalCardBg),
                    border = BorderStroke(1.dp, NaturalSandBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = NaturalOlive,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "شيوخه ومنهجه العلمي",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalCharcoal
                            )
                        }

                        Text(
                            text = "تلقى الشيخ العلم على أيدي كبار علماء أهل السنة والجماعة، ومن أبرزهم:",
                            fontSize = 12.sp,
                            color = NaturalMuted
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            TeacherItem(
                                name = "الشيخ حسن أبو الأشبال الزهيري",
                                field = "لازمه واستفاد منه في علوم الحديث والفقه والأصول."
                            )
                            TeacherItem(
                                name = "الشيخ أبو إسحاق الحويني",
                                field = "استفاد من منهجه في التحقيق وضبط الأسانيد وعلوم السنة."
                            )
                            TeacherItem(
                                name = "الشيخ محمد حسين يعقوب",
                                field = "تأثر بمنهجه في التربية والسلوك والرقائق وتزكية النفوس."
                            )
                        }
                    }
                }
            }

            // Quick Navigation Action to "الدروس والخطب"
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToLessons() }
                        .testTag("cta_lessons_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OliveGradient)
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Headphones,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "استكشف الدروس والخطب",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "آلاف الساعات من الدروس العلمية المبوبة",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "انتقال",
                                    tint = NaturalOliveDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Hero Card 2: App Overview & Objectives (تعريف بالتطبيق)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("app_overview_card"),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, NaturalSandBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().background(RoyalCardGradient)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.sheikh_mosque_banner_1787418040016),
                                contentDescription = "صورة المسجد والمنبر",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Gradient Overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(70.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, NaturalCardBg.copy(alpha = 0.95f), NaturalCardBg)
                                        )
                                    )
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = NaturalCharcoal.copy(alpha = 0.85f),
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "📱 عن التطبيق ورسالته",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "رسالة التطبيق وأهدافه",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NaturalCharcoal
                            )

                            Text(
                                text = "تم بناء هذا التطبيق بهدف تيسير الوصول إلى التراث العلمي والدعوي لفضيلة الشيخ سمير مصطفى، وتوفير تجربة استماع قرآنية وإيمانية هادئة بدون أي إعلانات، مع مشغل صوتي متقدم وسلاسل علمية مبوبة ومفهرسة.",
                                fontSize = 13.sp,
                                color = NaturalCharcoal.copy(alpha = 0.85f),
                                lineHeight = 22.sp
                            )

                            // Feature Badges Grid
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AppFeatureChip("🎙️ مشغل متقدم", modifier = Modifier.weight(1f))
                                AppFeatureChip("📚 سلاسل كاملة", modifier = Modifier.weight(1f))
                                AppFeatureChip("💾 بدون إنترنت", modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Section Exams Card (الاختبارات الشاملة لنهاية الأقسام)
            if (onStartCategoryQuiz != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_section_exams_card"),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = NaturalCardBg),
                        border = BorderStroke(1.dp, NaturalSandBorder)
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
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = "🏆", fontSize = 20.sp)
                                    Column {
                                        Text(
                                            text = "امتحانات نهاية الأقسام العلمية",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NaturalCharcoal
                                        )
                                        Text(
                                            text = "اختبارات ختامية تأصيلية شاملة لكل قسم",
                                            fontSize = 11.sp,
                                            color = NaturalMuted
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = NaturalAccentGold.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "5 امتحانات",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NaturalOliveDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Categories Quiz Pills Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val examCategories = listOf(
                                    LessonCategory.SEERAH,
                                    LessonCategory.TAFSEER,
                                    LessonCategory.KHAWATIR,
                                    LessonCategory.FIQH,
                                    LessonCategory.KHUTBAH
                                )
                                examCategories.forEach { category ->
                                    val catQuizId = "category_${category.name.lowercase()}"
                                    val catResult = uiState.quizResults[catQuizId]
                                    val isPassed = catResult?.isPassed == true

                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isPassed) NaturalOlive.copy(alpha = 0.12f) else NaturalItemBg,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isPassed) NaturalOlive else NaturalSandBorder
                                        ),
                                        modifier = Modifier
                                            .clickable { onStartCategoryQuiz(category) }
                                            .testTag("home_category_quiz_${category.name}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(text = category.iconEmoji, fontSize = 14.sp)
                                            Column {
                                                Text(
                                                    text = category.displayName,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = NaturalCharcoal
                                                )
                                                Text(
                                                    text = if (isPassed) "مجتاز (${catResult?.percentage}%) ✓" else "خوض الاختبار ⬅️",
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isPassed) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isPassed) NaturalOliveDark else NaturalMuted
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quiz Spotlight Card (ركن التمكين وتثبيت المحفوظ)
            item {
                val featuredLesson = SheikhData.allLessons.firstOrNull()
                if (featuredLesson != null) {
                    val quizRes = uiState.quizResults[featuredLesson.id]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStartQuiz(featuredLesson) }
                            .testTag("home_quiz_spotlight_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (quizRes?.isPassed == true) NaturalOlive.copy(alpha = 0.08f) else NaturalItemBg
                        ),
                        border = BorderStroke(1.dp, if (quizRes?.isPassed == true) NaturalOlive else NaturalSandBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(NaturalOlive.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🧠", fontSize = 22.sp)
                                }

                                Column {
                                    Text(
                                        text = "اختبار ترسيخ الفوائد والتمكين",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NaturalCharcoal
                                    )
                                    Text(
                                        text = if (quizRes != null)
                                            "نتيجة اختبارك السابق:${quizRes.percentage}% • اضغط للإعادة والتثبيت"
                                        else
                                            "أسئلة دقيقة في نهاية الدروس لتثبيت الفوائد والمسائل العلمية",
                                        fontSize = 11.sp,
                                        color = NaturalMuted,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "بدء الاختبار",
                                tint = NaturalOlive,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Daily Benefit / Quote Card
            if (dailyQuote != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("daily_quote_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = NaturalCardBg),
                        border = BorderStroke(1.dp, NaturalSandBorder)
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FormatQuote,
                                        contentDescription = "اقتباس",
                                        tint = NaturalAccentGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "فائدة إيمانية من كلام الشيخ",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NaturalOlive
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { shareQuoteData = dailyQuote },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "مشاركة الفائدة",
                                            tint = NaturalMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val copyContent = "« ${dailyQuote.quote} »\n📌 المصدر: ${dailyQuote.lessonTitle ?: dailyQuote.context} (⏱️ الدقيقة ${dailyQuote.timestampFormatted})\nالشيخ سمير مصطفى"
                                            clipboardManager.setText(AnnotatedString(copyContent))
                                            Toast.makeText(context, "تم نسخ الفائدة ومصدرها إلى الحافظة", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "نسخ الفائدة",
                                            tint = NaturalMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "« ${dailyQuote.quote} »",
                                fontSize = 13.sp,
                                color = NaturalCharcoal,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.Medium
                            )

                            // Lesson Source & Timestamp Action
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = NaturalItemBg,
                                border = BorderStroke(0.5.dp, NaturalSandBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = dailyQuote.lessonId != null && onPlayLessonAtTimestamp != null) {
                                        dailyQuote.lessonId?.let { lid ->
                                            onPlayLessonAtTimestamp?.invoke(lid, dailyQuote.timestampSeconds)
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "📌 المصدر: ${dailyQuote.lessonTitle ?: dailyQuote.context}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NaturalCharcoal,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "⏱️ موضع الفائدة: الدقيقة ${dailyQuote.timestampFormatted}",
                                            fontSize = 10.sp,
                                            color = NaturalMuted
                                        )
                                    }

                                    if (dailyQuote.lessonId != null && onPlayLessonAtTimestamp != null) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = NaturalOlive.copy(alpha = 0.15f),
                                            modifier = Modifier.padding(start = 6.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = NaturalOlive,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "استمع للفائدة",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = NaturalOlive
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    shareQuoteData?.let { quote: com.example.data.model.SheikhQuote ->
        ShareQuoteDialog(
            quote = quote,
            onDismiss = { shareQuoteData = null }
        )
    }
}

@Composable
fun TeacherItem(
    name: String,
    field: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(NaturalItemBg, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(NaturalOlive)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NaturalCharcoal
            )
            Text(
                text = field,
                fontSize = 11.sp,
                color = NaturalMuted,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun AppFeatureChip(
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = NaturalItemBg,
        border = BorderStroke(0.5.dp, NaturalSandBorder),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NaturalOlive
            )
        }
    }
}

