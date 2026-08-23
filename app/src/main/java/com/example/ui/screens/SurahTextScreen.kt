package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.NaturalCardBg
import com.example.ui.theme.NaturalCharcoal
import com.example.ui.theme.NaturalMuted
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalSandBg
import com.example.ui.viewmodel.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahTextScreen(
    surahId: Int,
    surahName: String,
    viewModel: QuranViewModel,
    onClose: () -> Unit,
    onPlayAyahAudio: (String) -> Unit
) {
    val surahDetails by viewModel.surahDetails.collectAsStateWithLifecycle()
    
    LaunchedEffect(surahId) {
        viewModel.fetchSurahDetails(surahId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سورة $surahName", fontWeight = FontWeight.Bold, color = GoldenTextMain) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Text("✖️", fontSize = 16.sp, color = GoldenTextMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GoldenBackground,
                    titleContentColor = GoldenTextMain
                )
            )
        },
        containerColor = GoldenBackground
    ) { innerPadding ->
        val ayahs = surahDetails[surahId]

        if (ayahs == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldenAccent)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(ayahs) { ayah ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = GoldenCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Arabic Text
                            Text(
                                text = "${ayah.text} ﴿${ayah.numberInSurah}﴾",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldenTextMain,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth(),
                                lineHeight = 40.sp
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Divider
                            Divider(color = GoldenAccent.copy(alpha = 0.2f))
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Translation
                            Text(
                                text = ayah.translation,
                                fontSize = 16.sp,
                                color = GoldenTextSub,
                                textAlign = TextAlign.Left,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Play Ayah button
                            if (ayah.audioUrl != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = { onPlayAyahAudio(ayah.audioUrl) }
                                    ) {
                                        Text("🔊", fontSize = 20.sp)
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
