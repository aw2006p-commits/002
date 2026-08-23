package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.Surah
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Golden Quran specific colors
val GoldenBackground = Color(0xFFF9F9F9)
val GoldenCard = Color(0xFFFFFFFF)
val GoldenAccent = Color(0xFFC79E53)
val GoldenTextMain = Color(0xFF222222)
val GoldenTextSub = Color(0xFF888888)

// Mock data for Surahs
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    viewModel: QuranViewModel = viewModel(),
    onSurahSelected: (Surah) -> Unit = {},
    onReadText: (Surah) -> Unit = {},
    onPlayAudio: (Surah) -> Unit = {}
) {
    val surahs by viewModel.surahs.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    val tabs = listOf("الفهرس", "الأجزاء", "المرجعيات")

    val filteredSurahs = remember(searchQuery, surahs) {
        surahs.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.id.toString() == searchQuery ||
            it.ayahs.toString() == searchQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الفهرس", fontWeight = FontWeight.Bold, color = GoldenTextMain) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GoldenBackground,
                    titleContentColor = GoldenTextMain
                )
            )
        },
        containerColor = GoldenBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header / Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = GoldenBackground,
                contentColor = GoldenAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = GoldenAccent
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) GoldenAccent else GoldenTextSub
                            )
                        }
                    )
                }
            }

            if (selectedTabIndex == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // Bookmark Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(GoldenCard)
                                .border(1.dp, GoldenAccent.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📖", fontSize = 24.sp) // Can be replaced with a proper icon later
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("فاصل الصفحة", fontWeight = FontWeight.Bold, color = GoldenTextMain)
                                Text("النساء - الآية ١ - صفحة ٧٧", fontSize = 12.sp, color = GoldenTextSub)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            placeholder = { Text("بحث في أسماء السور أو عدد الآيات...", color = GoldenTextSub) },
                            leadingIcon = { Text("🔍", fontSize = 18.sp) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldenAccent,
                                unfocusedBorderColor = GoldenTextMain.copy(alpha = 0.1f),
                                focusedContainerColor = GoldenCard,
                                unfocusedContainerColor = GoldenCard,
                                focusedTextColor = GoldenTextMain,
                                unfocusedTextColor = GoldenTextMain
                            ),
                            singleLine = true
                        )
                    }

                    if (isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = GoldenAccent)
                            }
                        }
                    } else {
                        items(filteredSurahs) { surah ->
                            SurahItem(
                                surah = surah, 
                                onClick = { onSurahSelected(surah) },
                                onReadText = { onReadText(surah) },
                                onPlayAudio = { onPlayAudio(surah) }
                            )
                        }
                    }
                }
            } else {
                // Placeholder for other tabs
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("قريباً إن شاء الله", color = NaturalMuted)
                }
            }
        }
    }
}

@Composable
fun SurahItem(surah: Surah, onClick: () -> Unit, onReadText: () -> Unit, onPlayAudio: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GoldenCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Surah Number with Golden Circle Motif
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(2.dp, GoldenAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${surah.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = GoldenTextMain
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Surah Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = surah.name, // Golden Quran just says the name like "النبأ"
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = GoldenTextMain
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "آياتها ${surah.ayahs} - ${surah.type}",
                    fontSize = 13.sp,
                    color = GoldenTextSub
                )
            }
            
            // Page Number
            Text(
                text = "${surah.page}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = GoldenTextMain
            )

            // Text/Translation Button
            IconButton(
                onClick = onReadText,
                modifier = Modifier
                    .size(40.dp)
                    .padding(start = 8.dp)
            ) {
                Text("📝", fontSize = 16.sp)
            }

            // Minimal Play Button (Added for feature parity, keeping it subtle)
            IconButton(
                onClick = onPlayAudio,
                modifier = Modifier
                    .size(40.dp)
            ) {
                Text("▶️", fontSize = 16.sp)
            }
        }
    }
}
