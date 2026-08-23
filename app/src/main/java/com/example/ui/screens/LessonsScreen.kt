package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
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
import com.example.data.model.Lesson
import com.example.data.model.LessonCategory
import com.example.data.model.SheikhData
import com.example.ui.components.HeroBanner
import com.example.ui.components.LessonItemCard
import com.example.ui.theme.NaturalAccentGold
import com.example.ui.theme.NaturalCardBg
import com.example.ui.theme.NaturalCharcoal
import com.example.ui.theme.NaturalItemBg
import com.example.ui.theme.NaturalMuted
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalOliveDark
import com.example.ui.theme.NaturalSandBg
import com.example.ui.theme.NaturalSandBorder
import com.example.ui.viewmodel.SheikhUiState
import java.util.TreeMap

enum class LessonsViewMode(val title: String, val iconText: String) {
    FOLDERS("مجلدات الأقسام", "📁"),
    ALPHABETICAL("الترتيب الأبجدي", "🔤"),
    ALL_LIST("قائمة الدروس", "📜")
}

data class LessonFolder(
    val category: LessonCategory,
    val name: String,
    val iconEmoji: String,
    val description: String
)

private fun getArabicSortKey(title: String): Char {
    val trimmed = title.trim()
    val cleaned = if (trimmed.startsWith("الدرس")) {
        trimmed.removePrefix("الدرس").trim()
    } else if (trimmed.startsWith("خطبة:")) {
        trimmed.removePrefix("خطبة:").trim()
    } else if (trimmed.startsWith("سلسلة")) {
        trimmed.removePrefix("سلسلة").trim()
    } else {
        trimmed
    }

    val firstChar = cleaned.firstOrNull() ?: 'أ'
    return when (firstChar) {
        'إ', 'أ', 'آ', 'ا' -> 'أ'
        'ة' -> 'ت'
        'ى', 'ئ', 'ي' -> 'ي'
        else -> firstChar
    }
}

@Composable
fun LessonsScreen(
    uiState: SheikhUiState,
    currentPlayingLessonId: String?,
    onPlayLesson: (Lesson) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onSelectCategory: (LessonCategory) -> Unit,
    onNavigateToLibrary: () -> Unit,
    onDownloadLesson: (Lesson) -> Unit = {},
    onDeleteDownload: (String) -> Unit = {},
    onOpenDownloadManager: () -> Unit = {},
    onStartQuiz: ((Lesson) -> Unit)? = null,
    onStartCategoryQuiz: ((LessonCategory) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(LessonsViewMode.FOLDERS) }
    var selectedFolderCategory by remember { mutableStateOf<LessonCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedAlphabetLetter by remember { mutableStateOf<Char?>(null) }
    var selectedTag by remember { mutableStateOf<String?>(null) }

    val folders = remember {
        listOf(
            LessonFolder(LessonCategory.SEERAH, "مجلد السيرة النبوية والمغازي", "🕌", "أحداث السيرة النبوية العطرة والغزوات والدروس والعبر"),
            LessonFolder(LessonCategory.TAFSEER, "مجلد التفسير والتدبر", "📖", "وقفات تدبرية وغوص في أسرار ومعاني السور والآيات القرآنية"),
            LessonFolder(LessonCategory.KHAWATIR, "مجلد الرقائق وتزكية النفوس", "💧", "مواعظ إيمانية، شرح الوابل الصيب، وإيقاظ القلوب"),
            LessonFolder(LessonCategory.FIQH, "مجلد الفقه وأعمال القلوب", "⚖️", "أحكام العبادات، فقه الابتلاء، وتجريد الإخلاص لله"),
            LessonFolder(LessonCategory.KHUTBAH, "مجلد خطب ومواعظ الجمعة", "🎙️", "مختارات من خطب الجمعة والمناسبات الإيمانية المؤثرة")
        )
    }

    val allLessonsList: List<Lesson> = SheikhData.allLessons

    val popularTags = remember {
        listOf("فقه", "تزكية", "عقيدة", "سيرة", "تفسير", "رقائق", "غزوات", "أذكار", "توكل", "دعاء", "أخلاق")
    }

    // Filtered by Search Query AND Selected Tag
    val filteredLessons: List<Lesson> = remember(searchQuery, selectedTag) {
        var list = allLessonsList
        if (selectedTag != null) {
            list = list.filter { it.tags.contains(selectedTag) }
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim()
            list = list.filter { lesson ->
                lesson.title.contains(q, ignoreCase = true) ||
                lesson.series.contains(q, ignoreCase = true) ||
                lesson.description.contains(q, ignoreCase = true) ||
                lesson.tags.any { it.contains(q, ignoreCase = true) }
            }
        }
        list
    }

    val groupedAlphabetical: Map<Char, List<Lesson>> = remember(filteredLessons) {
        val map = TreeMap<Char, MutableList<Lesson>>()
        for (lesson in filteredLessons.sortedBy { it.title }) {
            val key = getArabicSortKey(lesson.title)
            val list = map.getOrPut(key) { mutableListOf() }
            list.add(lesson)
        }
        map
    }

    val availableLetters: List<Char> = remember(groupedAlphabetical) {
        groupedAlphabetical.keys.toList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NaturalSandBg)
            .padding(horizontal = 20.dp)
            .testTag("lessons_screen_root")
    ) {
        // Top Header Section
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "قسم الدروس والخطب",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalCharcoal
                    )
                    Text(
                        text = if (selectedFolderCategory != null) "استعراض محتويات المجلد المحدد" else "فهرس منظم بالمجلدات، التصنيفات، والترتيب الأبجدي",
                        fontSize = 12.sp,
                        color = NaturalMuted
                    )
                }

                if (selectedFolderCategory != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NaturalOlive,
                        modifier = Modifier
                            .clickable { selectedFolderCategory = null }
                            .testTag("back_to_folders_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "المجلدات",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    IconButton(
                        onClick = onOpenDownloadManager,
                        modifier = Modifier.testTag("lessons_open_download_manager")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OfflinePin,
                            contentDescription = "مدير التحميلات",
                            tint = if (uiState.downloadedIds.isNotEmpty()) NaturalOlive else NaturalMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lessons_search_input"),
                placeholder = {
                    Text("ابحث في الدروس أو بالتصنيف (مثل: فقه، تزكية، عقيدة)...", fontSize = 13.sp, color = NaturalMuted)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = NaturalOlive,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "مسح",
                                tint = NaturalMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = NaturalCardBg,
                    unfocusedContainerColor = NaturalCardBg,
                    focusedBorderColor = NaturalOlive,
                    unfocusedBorderColor = NaturalSandBorder,
                    cursorColor = NaturalOlive
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal Tags Filter Bar (الفلترة بالتصنيفات)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // All Tags Chip
                val isAllSelected = selectedTag == null
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isAllSelected) NaturalOlive else NaturalCardBg,
                    border = BorderStroke(1.dp, if (isAllSelected) NaturalOlive else NaturalSandBorder),
                    modifier = Modifier
                        .clickable { selectedTag = null }
                        .testTag("tag_filter_all")
                ) {
                    Text(
                        text = "🏷️ كل التصنيفات",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAllSelected) Color.White else NaturalCharcoal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                popularTags.forEach { tag ->
                    val isSelected = selectedTag == tag
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) NaturalOlive else NaturalCardBg,
                        border = BorderStroke(1.dp, if (isSelected) NaturalOlive else NaturalSandBorder),
                        modifier = Modifier
                            .clickable { selectedTag = if (isSelected) null else tag }
                            .testTag("tag_filter_$tag")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else NaturalCharcoal
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إلغاء التصفية",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // View Mode Selector (Only when not inside single folder and no active search)
            if (selectedFolderCategory == null && searchQuery.isBlank() && selectedTag == null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LessonsViewMode.entries.forEach { mode ->
                        val isSelected = viewMode == mode
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) NaturalOlive else NaturalCardBg,
                            border = BorderStroke(1.dp, if (isSelected) NaturalOlive else NaturalSandBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewMode = mode }
                                .testTag("view_mode_${mode.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mode.iconText,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(
                                    text = mode.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else NaturalCharcoal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Screen Body
        if (selectedFolderCategory != null) {
            val folderLessons = filteredLessons.filter { it.category == selectedFolderCategory }
            val currentFolderInfo = folders.find { it.category == selectedFolderCategory }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("single_folder_lazy_column"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = NaturalCardBg),
                        border = BorderStroke(1.dp, NaturalSandBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(NaturalItemBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentFolderInfo?.iconEmoji ?: "📁",
                                    fontSize = 24.sp
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentFolderInfo?.name ?: "",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NaturalCharcoal
                                )
                                Text(
                                    text = "${folderLessons.size}دروس متاحة في هذا المجلد",
                                    fontSize = 12.sp,
                                    color = NaturalOlive,
                                    fontWeight = FontWeight.Bold
                                )
                                if (currentFolderInfo != null) {
                                    Text(
                                        text = currentFolderInfo.description,
                                        fontSize = 11.sp,
                                        color = NaturalMuted,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section Quiz Banner inside folder
                if (selectedFolderCategory != null && onStartCategoryQuiz != null) {
                    val categoryQuizId = "category_${selectedFolderCategory!!.name.lowercase()}"
                    val categoryQuizResult = uiState.quizResults[categoryQuizId]
                    item {
                        SectionQuizBannerCard(
                            category = selectedFolderCategory!!,
                            quizResult = categoryQuizResult,
                            onStartCategoryQuiz = onStartCategoryQuiz
                        )
                    }
                }

                if (folderLessons.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد نتائج مطابقة في هذا المجلد",
                                fontSize = 13.sp,
                                color = NaturalMuted
                            )
                        }
                    }
                } else {
                    items(folderLessons, key = { it.id }) { lesson ->
                        LessonItemCard(
                            lesson = lesson,
                            isCurrentlyPlaying = currentPlayingLessonId == lesson.id,
                            isFavorite = uiState.favoriteIds.contains(lesson.id),
                            onLessonClick = onPlayLesson,
                            onToggleFavorite = onToggleFavorite,
                            onTagClick = { tag -> selectedTag = if (selectedTag == tag) null else tag }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        } else if (selectedTag != null || searchQuery.isNotBlank()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("search_lessons_lazy_column"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedTag != null) "دروس مصنفة ك [#$selectedTag] (${filteredLessons.size}درس)" else "نتائج البحث (${filteredLessons.size}درس)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalOlive
                        )

                        if (selectedTag != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NaturalItemBg,
                                modifier = Modifier.clickable { selectedTag = null }
                            ) {
                                Text(
                                    text = "إلغاء التصفية ✕",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NaturalOlive,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                if (filteredLessons.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لم يتم العثور على دروس مطابقة للتصنيف أو البحث المحدد",
                                fontSize = 13.sp,
                                color = NaturalMuted
                            )
                        }
                    }
                } else {
                    items(filteredLessons, key = { it.id }) { lesson ->
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
                            onTagClick = { tag -> selectedTag = if (selectedTag == tag) null else tag },
                            quizResult = uiState.quizResults[lesson.id],
                            onQuizClick = onStartQuiz
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        } else {
            when (viewMode) {
                LessonsViewMode.FOLDERS -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("folders_grid_column"),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "مجلدات الدروس بحسب الأبواب العلمية",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalCharcoal,
                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                            )
                        }

                        items(folders, key = { it.category.name }) { folder ->
                            val count = allLessonsList.count { it.category == folder.category }
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = NaturalCardBg),
                                border = BorderStroke(1.dp, NaturalSandBorder),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedFolderCategory = folder.category }
                                    .testTag("folder_card_${folder.category.name}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(NaturalItemBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = folder.iconEmoji,
                                            fontSize = 26.sp
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = folder.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NaturalCharcoal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Text(
                                            text = folder.description,
                                            fontSize = 11.sp,
                                            color = NaturalMuted,
                                            lineHeight = 16.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = NaturalItemBg
                                            ) {
                                                Text(
                                                    text = "${count} درس مسجل",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = NaturalOlive,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }

                                            if (onStartCategoryQuiz != null) {
                                                val catQuizId = "category_${folder.category.name.lowercase()}"
                                                val catResult = uiState.quizResults[catQuizId]
                                                val isCatPassed = catResult?.isPassed == true
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isCatPassed) NaturalOlive.copy(alpha = 0.15f) else NaturalAccentGold.copy(alpha = 0.18f),
                                                    modifier = Modifier.clickable { onStartCategoryQuiz(folder.category) }
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = if (isCatPassed) "👑 امتحان القسم (${catResult?.percentage}%)" else "🏆 امتحان القسم",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = NaturalOliveDark
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "فتح المجلد",
                                        tint = NaturalOlive,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(90.dp))
                        }
                    }
                }

                LessonsViewMode.ALPHABETICAL -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Alphabet Quick Filter Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedAlphabetLetter == null) NaturalOlive else NaturalCardBg,
                                border = BorderStroke(1.dp, if (selectedAlphabetLetter == null) NaturalOlive else NaturalSandBorder),
                                modifier = Modifier.clickable { selectedAlphabetLetter = null }
                            ) {
                                Text(
                                    text = "الكل",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedAlphabetLetter == null) Color.White else NaturalCharcoal,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            availableLetters.forEach { letter ->
                                val isSelected = selectedAlphabetLetter == letter
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) NaturalOlive else NaturalCardBg,
                                    border = BorderStroke(1.dp, if (isSelected) NaturalOlive else NaturalSandBorder),
                                    modifier = Modifier
                                        .clickable { selectedAlphabetLetter = letter }
                                        .testTag("alpha_pill_$letter")
                                ) {
                                    Text(
                                        text = "حرف$letter",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else NaturalCharcoal,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Alphabetical Lessons
                        val entriesToDisplay = groupedAlphabetical.entries.filter {
                            selectedAlphabetLetter == null || selectedAlphabetLetter == it.key
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("alphabetical_lazy_column"),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            entriesToDisplay.forEach { entry ->
                                val letter = entry.key
                                val lessons = entry.value

                                item(key = "header_letter_$letter") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp, bottom = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(NaturalOlive),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = letter.toString(),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Text(
                                            text = "حرف (${letter})",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NaturalCharcoal
                                        )
                                        Text(
                                            text = "(${lessons.size}دروس)",
                                            fontSize = 12.sp,
                                            color = NaturalMuted
                                        )
                                    }
                                }

                                items(lessons, key = { it.id }) { lesson ->
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
                                        onTagClick = { tag -> selectedTag = if (selectedTag == tag) null else tag },
                                        quizResult = uiState.quizResults[lesson.id],
                                        onQuizClick = onStartQuiz
                                    )
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(90.dp))
                            }
                        }
                    }
                }

                LessonsViewMode.ALL_LIST -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("all_lessons_list_column"),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            HeroBanner(
                                lesson = SheikhData.featuredHeroLesson,
                                onPlayClick = onPlayLesson
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LessonCategory.entries.forEach { category ->
                                    val isSelected = uiState.selectedCategory == category
                                    Surface(
                                        shape = RoundedCornerShape(18.dp),
                                        color = if (isSelected) NaturalOlive else NaturalCardBg,
                                        border = BorderStroke(1.dp, if (isSelected) NaturalOlive else NaturalSandBorder),
                                        modifier = Modifier
                                            .clickable { onSelectCategory(category) }
                                            .testTag("lessons_category_pill_${category.name}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(text = category.iconEmoji, fontSize = 14.sp)
                                            Text(
                                                text = category.displayName,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else NaturalCharcoal
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (uiState.selectedCategory == LessonCategory.ALL) "جميع الدروس المسجلة" else uiState.selectedCategory.displayName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NaturalCharcoal
                                )
                                Text(
                                    text = "${uiState.filteredLessons.size}درسا",
                                    fontSize = 12.sp,
                                    color = NaturalOlive,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (uiState.selectedCategory != LessonCategory.ALL && onStartCategoryQuiz != null) {
                            val categoryQuizId = "category_${uiState.selectedCategory.name.lowercase()}"
                            val categoryQuizResult = uiState.quizResults[categoryQuizId]
                            item {
                                SectionQuizBannerCard(
                                    category = uiState.selectedCategory,
                                    quizResult = categoryQuizResult,
                                    onStartCategoryQuiz = onStartCategoryQuiz
                                )
                            }
                        }

                        items(uiState.filteredLessons, key = { it.id }) { lesson ->
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
                                onTagClick = { tag -> selectedTag = if (selectedTag == tag) null else tag },
                                quizResult = uiState.quizResults[lesson.id],
                                onQuizClick = onStartQuiz
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(90.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionQuizBannerCard(
    category: LessonCategory,
    quizResult: com.example.data.local.QuizResultEntity?,
    onStartCategoryQuiz: (LessonCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPassed = quizResult?.isPassed == true
    val percentage = quizResult?.percentage ?: 0
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onStartCategoryQuiz(category) }
            .testTag("section_quiz_card_${category.name}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPassed) NaturalOlive.copy(alpha = 0.08f) else NaturalCardBg
        ),
        border = BorderStroke(
            1.5.dp,
            if (isPassed) NaturalOlive else NaturalAccentGold.copy(alpha = 0.6f)
        )
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isPassed) NaturalAccentGold.copy(alpha = 0.2f) else NaturalItemBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPassed) "👑" else "🏆",
                            fontSize = 22.sp
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "الامتحان الشامل لخاتمة القسم",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalCharcoal
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NaturalAccentGold.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "امتحان نهاية القسم",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NaturalOliveDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = if (quizResult != null)
                                "نتيجة الاختبار السابق:$percentage% (${if (isPassed) "تم الاجتياز والتمكين 🌟" else "فرصة للإعادة"})"
                            else
                                "اختبار نهائي شامل لجميع مسائل وفوائد قسم${category.displayName}",
                            fontSize = 11.sp,
                            color = if (isPassed) NaturalOliveDark else NaturalMuted,
                            fontWeight = if (isPassed) FontWeight.Bold else FontWeight.Normal,
                            lineHeight = 16.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isPassed) NaturalOlive else NaturalAccentGold,
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (quizResult != null) "إعادة الامتحان" else "بدء الامتحان",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
