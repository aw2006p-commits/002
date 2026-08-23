package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Lesson
import com.example.ui.theme.NaturalAccentGold
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalOliveDark

@Composable
fun HeroBanner(
    lesson: Lesson,
    onPlayClick: (Lesson) -> Unit,
    modifier: Modifier = Modifier
) {
    val heroGradient = Brush.horizontalGradient(
        colors = listOf(
            NaturalOliveDark,
            NaturalOlive
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_banner_card"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(heroGradient)
                .clip(RoundedCornerShape(26.dp))
        ) {
            // Decorative background glowing accents
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .offset(x = (-30).dp, y = 50.dp)
                    .background(
                        color = NaturalAccentGold.copy(alpha = 0.12f),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 25.dp, y = (-25).dp)
                    .background(
                        color = Color.White.copy(alpha = 0.08f),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Badge with Gold accent
                Surface(
                    shape = CircleShape,
                    color = NaturalAccentGold.copy(alpha = 0.25f),
                    modifier = Modifier.testTag("hero_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "✨",
                            fontSize = 11.sp
                        )
                        Text(
                            text = "جديد السلاسل والدروس",
                            color = Color(0xFFFFF3D6),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Series Name
                Text(
                    text = lesson.series,
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )

                // Subtitle
                Text(
                    text = lesson.title,
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Play Button matching Natural Tones
                Button(
                    onClick = { onPlayClick(lesson) },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = NaturalOliveDark
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier.testTag("hero_play_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "استماع الآن",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalOliveDark
                        )
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "استماع",
                            tint = NaturalOliveDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
