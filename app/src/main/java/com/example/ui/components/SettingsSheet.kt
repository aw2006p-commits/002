package com.example.ui.components

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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppThemeMode
import com.example.data.local.UserPreferences
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.NaturalAccentGold
import com.example.ui.theme.NaturalCharcoal
import com.example.ui.theme.NaturalItemBg
import com.example.ui.theme.NaturalMuted
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalOliveDark
import com.example.ui.theme.NaturalSandBg
import com.example.ui.theme.NaturalSandBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    preferences: UserPreferences,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onSetFontScale: (Float) -> Unit,
    onSetDailyGoal: (Int) -> Unit,
    onSetSmartRewind: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val appColors = LocalAppColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = appColors.background,
        modifier = Modifier.testTag("settings_modal_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(appColors.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = appColors.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "إعدادات وتخصيص التطبيق",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary
                    )
                    Text(
                        text = "تحكم في المظهر، خط القراءة، والورد اليومي",
                        fontSize = 12.sp,
                        color = appColors.textMuted
                    )
                }
            }

            // 1. Theme Selection Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = appColors.surface),
                border = BorderStroke(1.dp, appColors.border)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "مظهر التطبيق (النمط الليلي / النهاري)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple(AppThemeMode.LIGHT, "نهاري ☀️", Icons.Default.LightMode),
                            Triple(AppThemeMode.DARK, "ليلي 🌙", Icons.Default.DarkMode),
                            Triple(AppThemeMode.SYSTEM, "تلقائي 📱", Icons.Default.Settings)
                        ).forEach { (mode, title, icon) ->
                            val isSelected = preferences.themeMode == mode
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) appColors.primary else appColors.itemBg,
                                border = BorderStroke(1.dp, if (isSelected) appColors.primary else appColors.border),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSetThemeMode(mode) }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else appColors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Font Scaling Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = appColors.surface),
                border = BorderStroke(1.dp, appColors.border)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.FormatSize, contentDescription = null, tint = appColors.primary, modifier = Modifier.size(18.dp))
                            Text(
                                text = "حجم الخط ونصوص الفوائد",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimary
                            )
                        }
                        Text(
                            text = "${(preferences.fontScale * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = appColors.primary
                        )
                    }

                    Slider(
                        value = preferences.fontScale,
                        onValueChange = { onSetFontScale(it) },
                        valueRange = 0.85f..1.35f,
                        steps = 4,
                        colors = SliderDefaults.colors(
                            thumbColor = appColors.primary,
                            activeTrackColor = appColors.primary,
                            inactiveTrackColor = appColors.border
                        )
                    )

                    // Preview text box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(appColors.itemBg)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "معاينة: «العلم إن لم يورثك خشية وإخباتاً لله؛ فراجع نيتك وطريقك»",
                            fontSize = (13 * preferences.fontScale).sp,
                            color = appColors.textPrimary,
                            lineHeight = (20 * preferences.fontScale).sp
                        )
                    }
                }
            }

            // 3. Daily Listening Goal (الورد اليومي)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = appColors.surface),
                border = BorderStroke(1.dp, appColors.border)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = NaturalAccentGold, modifier = Modifier.size(18.dp))
                            Text(
                                text = "الورد اليومي للاستماع",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimary
                            )
                        }
                        Text(
                            text = "${preferences.dailyGoalMinutes} دقيقة يومياً",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalAccentGold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 30, 45, 60).forEach { goal ->
                            val isSelected = preferences.dailyGoalMinutes == goal
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) NaturalAccentGold else appColors.itemBg,
                                border = BorderStroke(1.dp, if (isSelected) NaturalAccentGold else appColors.border),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSetDailyGoal(goal) }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$goal د",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else appColors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Smart Rewind on Resume
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = appColors.surface),
                border = BorderStroke(1.dp, appColors.border)
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(appColors.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = null, tint = appColors.primary, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = "الترجيع الذكي عند الاستئناف",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimary
                            )
                            Text(
                                text = "إرجاع 5 ثوانٍ تلقائياً لتذكر سياق الكلام",
                                fontSize = 11.sp,
                                color = appColors.textMuted
                            )
                        }
                    }

                    Switch(
                        checked = preferences.smartRewindEnabled,
                        onCheckedChange = onSetSmartRewind,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = appColors.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
