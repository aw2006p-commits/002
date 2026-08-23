package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SheikhQuote
import com.example.ui.theme.NaturalAccentGold
import com.example.ui.theme.NaturalCardBg
import com.example.ui.theme.NaturalCharcoal
import com.example.ui.theme.NaturalMuted
import com.example.ui.theme.NaturalOlive
import com.example.ui.theme.NaturalOliveDark
import com.example.ui.theme.NaturalPillBg
import com.example.ui.theme.NaturalSandBg
import com.example.ui.theme.NaturalSandBorder
import com.example.util.ShareHelper

@Composable
fun ShareQuoteDialog(
    quote: SheikhQuote,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = NaturalSandBg
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مشاركة الفائدة العلمية",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalOliveDark
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(NaturalPillBg, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = NaturalCharcoal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card Preview
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NaturalSandBorder, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = NaturalCardBg),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(NaturalOlive.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                tint = NaturalOliveDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "« ${quote.quote} »",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalCharcoal,
                            textAlign = TextAlign.Center,
                            lineHeight = 26.sp
                        )

                        if (!quote.context.isNotBlank() || !quote.lessonTitle.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            val sourceText = when {
                                !quote.lessonTitle.isNullOrBlank() -> "📌 المصدر: ${quote.lessonTitle}"
                                else -> "📌 المصدر: ${quote.context}"
                            }
                            Text(
                                text = sourceText,
                                fontSize = 13.sp,
                                color = NaturalMuted,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (quote.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = quote.tags.joinToString(" ") { "#$it" },
                                fontSize = 12.sp,
                                color = NaturalAccentGold,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "الشيخ سمير مصطفى - حفظه الله",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalOliveDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val textToCopy = "« ${quote.quote} »\nالشيخ سمير مصطفى"
                            clipboardManager.setText(AnnotatedString(textToCopy))
                            Toast.makeText(context, "تم نسخ النص بالحافظة", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NaturalOliveDark)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "نسخ النص", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            ShareHelper.shareQuote(context, quote)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NaturalOlive,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "مشاركة", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
