package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.NaturalCharcoal
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalSandBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(
    initialPage: Int,
    onClose: () -> Unit
) {
    // 604 pages in the standard Madani Mushaf
    val pagerState = rememberPagerState(
        initialPage = initialPage - 1, // Page 1 is index 0
        pageCount = { 604 }
    )

    // For tap to toggle overlay UI (top bar, page number)
    var showOverlay by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Color(0xFFF9F0D9), // Match the paper tint for seamless edges
        topBar = {
            if (showOverlay) {
                TopAppBar(
                    title = { Text("المصحف الشريف", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Text("✖️", fontSize = 16.sp, color = Color(0xFF222222))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFF9F9F9).copy(alpha = 0.95f),
                        titleContentColor = Color(0xFF222222)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F0D9))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    showOverlay = !showOverlay
                },
            contentAlignment = Alignment.Center
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                reverseLayout = false // Since app is RTL, index 0 is on the right, swiping left goes to index 1 (page 2), which is perfect for Arabic books.
            ) { pageIndex ->
                val pageNumber = pageIndex + 1
                val imageUrl = String.format("https://files.quran.app/hafs/madani/width_1024/page%03d.png", pageNumber)

                // Add zoom support (pan & zoom)
                var scale by remember { mutableFloatStateOf(1f) }
                var offsetX by remember { mutableFloatStateOf(0f) }
                var offsetY by remember { mutableFloatStateOf(0f) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 3f)
                                if (scale > 1f) {
                                    offsetX += pan.x
                                    offsetY += pan.y
                                } else {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        }
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Page $pageNumber",
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(Color(0xFFF9F0D9), BlendMode.Multiply), // Golden Paper Tint
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY
                            )
                    )
                }
            }

            // Bottom Overlay for Page Number
            if (showOverlay) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF222222).copy(alpha = 0.8f))
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "صفحة ${pagerState.currentPage + 1}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
