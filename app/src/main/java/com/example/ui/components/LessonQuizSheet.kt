package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Lesson
import com.example.data.model.QuizQuestion
import com.example.data.model.QuizType
import com.example.data.model.UnifiedQuiz
import com.example.data.repository.QuizDataProvider
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
import com.example.util.ShareHelper

enum class ResultReviewFilter {
    ALL,
    MISTAKES_ONLY,
    CORRECT_ONLY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonQuizSheet(
    lesson: Lesson,
    onDismiss: () -> Unit,
    onSubmitResult: (quizId: String, score: Int, totalQuestions: Int) -> Unit,
    modifier: Modifier = Modifier,
    onPlayLessonAtTimestamp: ((lessonId: String, timestampSeconds: Long) -> Unit)? = null
) {
    val unified: UnifiedQuiz = remember(lesson.id) { QuizDataProvider.getUnifiedQuizForLesson(lesson) }
    LessonQuizSheet(
        quiz = unified,
        onDismiss = onDismiss,
        onSubmitResult = onSubmitResult,
        modifier = modifier,
        onPlayLessonAtTimestamp = onPlayLessonAtTimestamp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonQuizSheet(
    quiz: UnifiedQuiz,
    onDismiss: () -> Unit,
    onSubmitResult: (quizId: String, score: Int, totalQuestions: Int) -> Unit,
    modifier: Modifier = Modifier,
    onPlayLessonAtTimestamp: ((lessonId: String, timestampSeconds: Long) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    // Active quiz question set (can be all questions or re-test mistakes only)
    var activeQuestions by remember(quiz.id) { mutableStateOf(quiz.questions) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    val userAnswers = remember { mutableStateListOf<Int>() }
    var isQuizCompleted by remember { mutableStateOf(false) }
    var reviewFilter by remember { mutableStateOf(ResultReviewFilter.ALL) }
    var isMistakesRetestMode by remember { mutableStateOf(false) }

    val currentQuestion = activeQuestions.getOrNull(currentQuestionIndex)
    val totalQuestions = activeQuestions.size
    val isSectionQuiz = quiz.quizType == QuizType.SECTION

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NaturalSandBg,
        dragHandle = null,
        modifier = modifier
            .fillMaxHeight(0.95f)
            .testTag("lesson_quiz_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isMistakesRetestMode) Color(0xFFFFE0B2)
                                else if (isSectionQuiz) NaturalAccentGold.copy(alpha = 0.2f)
                                else NaturalOlive.copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isMistakesRetestMode) "🎯"
                            else if (isSectionQuiz) (quiz.category?.iconEmoji ?: "🏆")
                            else "🧠",
                            fontSize = 19.sp
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isMistakesRetestMode) "تثبيت المسائل الخاطئة 🎯" else quiz.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalCharcoal,
                                maxLines = 1
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isMistakesRetestMode) Color(0xFFFFE0B2)
                                else if (isSectionQuiz) NaturalAccentGold.copy(alpha = 0.2f)
                                else NaturalOlive.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = if (isMistakesRetestMode) "إعادة تمكين" else quiz.badgeLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMistakesRetestMode) Color(0xFFE65100) else NaturalOliveDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isMistakesRetestMode) "إعادة مدارسة ما التبس عليك لترسيخ الفهم الحق" else quiz.subtitle,
                            fontSize = 11.sp,
                            color = NaturalMuted,
                            maxLines = 1
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp).testTag("close_quiz_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق الاختبار",
                        tint = NaturalMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (!isQuizCompleted && currentQuestion != null) {
                // Progress Indicator
                val progress = (currentQuestionIndex.toFloat()) / totalQuestions.toFloat()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "المسألة${currentQuestionIndex + 1}من أصل$totalQuestions",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSectionQuiz) NaturalOliveDark else NaturalOlive
                        )
                        Text(
                            text = if (isAnswerSubmitted) "تم التقييم والتوجيه" else "بانتظار اختيارك",
                            fontSize = 11.sp,
                            color = if (isAnswerSubmitted) NaturalAccentGold else NaturalMuted,
                            fontWeight = if (isAnswerSubmitted) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isSectionQuiz) NaturalOliveDark else NaturalOlive,
                        trackColor = NaturalSandBorder
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Question & Options List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Question Prompt
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = NaturalCardBg),
                            border = BorderStroke(1.dp, NaturalSandBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = NaturalAccentGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isSectionQuiz) "مسألة تأصيلية من قسم ${quiz.category?.displayName ?:""}:" else "مسألة تأصيلية وتدبرية:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NaturalOlive
                                    )
                                }

                                Text(
                                    text = currentQuestion.question,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NaturalCharcoal,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = if (isAnswerSubmitted) "تقييم الخيارات والتأصيل العلمي:" else "اختر الإجابة الأدق تأصيلا:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalCharcoal,
                            modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                        )
                    }

                    itemsIndexed(currentQuestion.options) { optionIndex, optionText ->
                        val isSelected = selectedOptionIndex == optionIndex
                        val isCorrectOption = currentQuestion.correctIndex == optionIndex
                        val isUserWrongPick = isAnswerSubmitted && isSelected && !isCorrectOption

                        val backgroundColor = when {
                            !isAnswerSubmitted && isSelected -> NaturalOliveLight.copy(alpha = 0.4f)
                            isAnswerSubmitted && isCorrectOption -> Color(0xFFE8F5E9)
                            isAnswerSubmitted && isUserWrongPick -> Color(0xFFFFEBEE)
                            else -> NaturalCardBg
                        }

                        val borderColor = when {
                            !isAnswerSubmitted && isSelected -> NaturalOlive
                            isAnswerSubmitted && isCorrectOption -> Color(0xFF4CAF50)
                            isAnswerSubmitted && isUserWrongPick -> Color(0xFFE57373)
                            else -> NaturalSandBorder
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isAnswerSubmitted) {
                                    selectedOptionIndex = optionIndex
                                }
                                .testTag("quiz_option_$optionIndex"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = backgroundColor),
                            border = BorderStroke(if (isAnswerSubmitted && (isCorrectOption || isUserWrongPick)) 2.dp else 1.2.dp, borderColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isAnswerSubmitted && isCorrectOption -> Color(0xFF4CAF50)
                                                isAnswerSubmitted && isUserWrongPick -> Color(0xFFE57373)
                                                isSelected -> NaturalOlive
                                                else -> NaturalPillBg
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isAnswerSubmitted && isCorrectOption) {
                                        Icon(Icons.Default.Check, contentDescription = "صحيحة", tint = Color.White, modifier = Modifier.size(16.dp))
                                    } else if (isAnswerSubmitted && isUserWrongPick) {
                                        Icon(Icons.Default.Close, contentDescription = "خطأ", tint = Color.White, modifier = Modifier.size(16.dp))
                                    } else {
                                        Text(
                                            text = when (optionIndex) {
                                                0 -> "أ"
                                                1 -> "ب"
                                                2 -> "ج"
                                                else -> "د"
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else NaturalCharcoal
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = optionText,
                                        fontSize = 13.sp,
                                        color = NaturalCharcoal,
                                        lineHeight = 19.sp,
                                        fontWeight = if (isAnswerSubmitted && isCorrectOption) FontWeight.Bold else FontWeight.Normal
                                    )

                                    if (isAnswerSubmitted) {
                                        if (isCorrectOption) {
                                            Text(
                                                text = "✓ الإجابة الصحيحة المعتمدة تأصيلا",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2E7D32),
                                                modifier = Modifier.padding(top = 3.dp)
                                            )
                                        } else if (isUserWrongPick) {
                                            Text(
                                                text = "✗ اختيارك (غير دقيق - انظر بيان التوجيه أدناه)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFC62828),
                                                modifier = Modifier.padding(top = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // PEDAGOGICAL GUIDANCE & ERROR CORRECTION BOX
                    if (isAnswerSubmitted) {
                        val isUserCorrect = selectedOptionIndex == currentQuestion.correctIndex
                        item {
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically()
                            ) {
                                if (isUserCorrect) {
                                    // Correct Answer Guidance Box
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                                        border = BorderStroke(1.2.dp, Color(0xFFC8E6C9))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color(0xFF2E7D32),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = "أحسنت! إجابة صحيحة وموفقة ✨",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF2E7D32)
                                                )
                                            }

                                            Text(
                                                text = "📖 الوجه الصواب والتأصيل العلمي:",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NaturalCharcoal
                                            )
                                            Text(
                                                text = currentQuestion.explanation,
                                                fontSize = 12.sp,
                                                color = NaturalCharcoal,
                                                lineHeight = 18.sp
                                            )

                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = NaturalCardBg,
                                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                                border = BorderStroke(1.dp, Color(0xFFC8E6C9))
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(
                                                        text = "💡 درة وتوجيه من الشيخ سمير مصطفى:",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = NaturalAccentGold
                                                    )
                                                    Text(
                                                        text = currentQuestion.scholarNote,
                                                        fontSize = 11.sp,
                                                        color = NaturalCharcoal,
                                                        lineHeight = 16.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // WRONG ANSWER: Comprehensive Guidance & Scientific Correction Box
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                                        border = BorderStroke(1.5.dp, Color(0xFFFFB74D))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // Header
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFFF9800)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.School,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = "ركن التوجيه والتصحيح العلمي 🧭",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFE65100)
                                                    )
                                                    Text(
                                                        text = "لا بأس! العلم ينال بتصحيح المفاهيم وتثبيت الحجة",
                                                        fontSize = 11.sp,
                                                        color = NaturalCharcoal.copy(alpha = 0.8f)
                                                    )
                                                }
                                            }

                                            // Comparison Pills
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color.White,
                                                border = BorderStroke(1.dp, Color(0xFFFFE0B2)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Text("❌", fontSize = 12.sp)
                                                        Column {
                                                            Text("اختيارك الذي وقع فيه الاشتباه:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                                            Text(
                                                                text = selectedOptionIndex?.let { currentQuestion.options.getOrNull(it) } ?: "",
                                                                fontSize = 11.sp,
                                                                color = NaturalCharcoal
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Text("✅", fontSize = 12.sp)
                                                        Column {
                                                            Text("الصواب المعتمد عند أهل التحقيق:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                                            Text(
                                                                text = currentQuestion.options[currentQuestion.correctIndex],
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFF1B5E20)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // 1. Why did confusion happen?
                                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(14.dp))
                                                    Text("لماذا وقع الاشتباه في هذه المسألة؟", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                                }
                                                Text(
                                                    text = currentQuestion.whyWrongExplanation,
                                                    fontSize = 11.sp,
                                                    color = NaturalCharcoal,
                                                    lineHeight = 17.sp
                                                )
                                            }

                                            // 2. The Detailed Scientific Explanation
                                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NaturalOliveDark, modifier = Modifier.size(14.dp))
                                                    Text("البيان الشافي ووجه الصواب العلمي:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NaturalOliveDark)
                                                }
                                                Text(
                                                    text = currentQuestion.explanation,
                                                    fontSize = 11.sp,
                                                    color = NaturalCharcoal,
                                                    lineHeight = 17.sp
                                                )
                                            }

                                            // 3. Sheikh's corrective guidance and rule
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = NaturalOlive.copy(alpha = 0.08f),
                                                border = BorderStroke(1.dp, NaturalOlive.copy(alpha = 0.25f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(
                                                        text = "🎯 وصية الشيخ سمير مصطفى لضبط المسألة:",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = NaturalOliveDark
                                                    )
                                                    Text(
                                                        text = currentQuestion.correctiveGuidance,
                                                        fontSize = 11.sp,
                                                        color = NaturalCharcoal,
                                                        lineHeight = 16.sp
                                                    )
                                                    Text(
                                                        text = "📌 ${currentQuestion.scholarNote}",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = NaturalAccentGold,
                                                        lineHeight = 15.sp,
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    )
                                                }
                                            }

                                            // 4. Direct Audio Timestamp Jump Button
                                            val targetLessonId = currentQuestion.lessonId ?: (quiz.id.takeIf { quiz.quizType == QuizType.LESSON })
                                            if (onPlayLessonAtTimestamp != null && targetLessonId != null) {
                                                Button(
                                                    onClick = {
                                                        onPlayLessonAtTimestamp(targetLessonId, currentQuestion.timestampSeconds)
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 4.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = NaturalOliveDark,
                                                        contentColor = Color.White
                                                    )
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Headphones,
                                                            contentDescription = null,
                                                            tint = NaturalAccentGold,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Text(
                                                            text = "استمع لشرح الشيخ في موضع المسألة (${currentQuestion.timestampLabel}) 🎧",
                                                            fontSize = 12.sp,
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
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Action Bottom Bar (Answer / Next Question)
                if (!isAnswerSubmitted) {
                    Button(
                        onClick = {
                            if (selectedOptionIndex != null) {
                                isAnswerSubmitted = true
                                userAnswers.add(selectedOptionIndex!!)
                            }
                        },
                        enabled = selectedOptionIndex != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_answer_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSectionQuiz) NaturalOliveDark else NaturalOlive,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "تحقق من الإجابة وتعلم التأصيل 🔍",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            if (currentQuestionIndex + 1 < totalQuestions) {
                                currentQuestionIndex++
                                selectedOptionIndex = null
                                isAnswerSubmitted = false
                            } else {
                                isQuizCompleted = true
                                // Calculate score
                                var score = 0
                                activeQuestions.forEachIndexed { i, q ->
                                    if (userAnswers.getOrNull(i) == q.correctIndex) {
                                        score++
                                    }
                                }
                                if (!isMistakesRetestMode) {
                                    onSubmitResult(quiz.id, score, totalQuestions)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("next_question_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSectionQuiz) NaturalOliveDark else NaturalOlive,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (currentQuestionIndex + 1 < totalQuestions) "المسألة التالية ⬅️" else "عرض نتائج التحصيل والتوجيهات الختامية 🏆",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // QUIZ COMPLETED: In-depth Review & Scientific Guidance Hub
                var score = 0
                val mistakenQuestions = mutableListOf<Pair<Int, QuizQuestion>>()
                val correctQuestions = mutableListOf<Pair<Int, QuizQuestion>>()

                activeQuestions.forEachIndexed { i, q ->
                    val userPick = userAnswers.getOrNull(i)
                    if (userPick == q.correctIndex) {
                        score++
                        correctQuestions.add(Pair(i, q))
                    } else {
                        mistakenQuestions.add(Pair(i, q))
                    }
                }

                val percentage = if (totalQuestions > 0) (score * 100) / totalQuestions else 0
                val isMastered = percentage >= 70
                val hasMistakes = mistakenQuestions.isNotEmpty()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("quiz_result_screen"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(
                                    if (percentage == 100) NaturalAccentGold.copy(alpha = 0.2f)
                                    else if (isMastered) NaturalOlive.copy(alpha = 0.15f)
                                    else Color(0xFFFFE0B2)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isMastered) Icons.Default.EmojiEvents else Icons.Default.School,
                                contentDescription = "نتيجة الاختبار",
                                tint = if (percentage == 100) NaturalAccentGold else if (isMastered) NaturalOlive else Color(0xFFE65100),
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = when {
                                percentage == 100 -> if (isSectionQuiz) "وسام الإتقان التام للقسم 👑" else "تاج الإتقان العلمي التام 👑"
                                percentage >= 70 -> if (isSectionQuiz) "شهادة إتمام القسم بامتياز 🌟" else "اجتياز مبارك بامتياز 🌟"
                                else -> "محطة مدارسة وتثبيت للمسائل 📖"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMastered) NaturalOliveDark else Color(0xFFD84315),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "أحرزت${score} من أصل${totalQuestions} درجات ($percentage%)",
                            fontSize = 13.sp,
                            color = NaturalOlive,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Assessment & Advice Banner
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = NaturalCardBg),
                            border = BorderStroke(1.dp, NaturalSandBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = if (isMastered) NaturalOlive else Color(0xFFE65100),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "التقييم والتوجيه التربوي الشامل:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NaturalCharcoal
                                    )
                                }

                                Text(
                                    text = if (percentage == 100) {
                                        "ما شاء الله تبارك الله! أحكمت المسائل واستوعبت دقائق العلم، ونوصيك ببذل الزكاة العلمية بمذاكرتها مع إخوانك والعمل بما علمت."
                                    } else if (isMastered) {
                                        "ما شاء الله! تحصيلك طيب وراسخ، ومعالجة المسائل التي التبست عليك في الأسفل تزيدك رسوخا وإتقانا إن شاء الله."
                                    } else {
                                        "الخطأ في بداية التعلم هو طريق التمكن؛ راجع التوجيهات والتعليلات العلمية للمسائل الخاطئة بالأسفل ثم أعد تثبيتها بضغطة زر."
                                    },
                                    fontSize = 12.sp,
                                    color = NaturalCharcoal,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // Quick Action: If there are mistakes, offer direct re-test of only the mistakes
                    if (hasMistakes) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFFFF3E0),
                                border = BorderStroke(1.2.dp, Color(0xFFFFB74D)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                                        Column {
                                            Text(
                                                text = "${mistakenQuestions.size}مسائل تحتاج منك إعادة تمكين وتثبيت",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFE65100)
                                            )
                                            Text(
                                                text = "يمكنك خوض اختبار فوري يقتصر على هذه المسائل فقط!",
                                                fontSize = 10.sp,
                                                color = NaturalCharcoal
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            activeQuestions = mistakenQuestions.map { it.second }
                                            currentQuestionIndex = 0
                                            selectedOptionIndex = null
                                            isAnswerSubmitted = false
                                            userAnswers.clear()
                                            isQuizCompleted = false
                                            isMistakesRetestMode = true
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("تثبيت الأخطاء 🎯", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // Review Section Header & Filters
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "مراجعة المسائل وبيان التوجيه العلمي لكل مسألة:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalCharcoal
                            )

                            // Filter Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (reviewFilter == ResultReviewFilter.ALL) NaturalOlive else NaturalCardBg,
                                    border = BorderStroke(1.dp, if (reviewFilter == ResultReviewFilter.ALL) NaturalOlive else NaturalSandBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { reviewFilter = ResultReviewFilter.ALL }
                                ) {
                                    Text(
                                        text = "الكل ($totalQuestions)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (reviewFilter == ResultReviewFilter.ALL) Color.White else NaturalCharcoal,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }

                                if (hasMistakes) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (reviewFilter == ResultReviewFilter.MISTAKES_ONLY) Color(0xFFE65100) else NaturalCardBg,
                                        border = BorderStroke(1.dp, if (reviewFilter == ResultReviewFilter.MISTAKES_ONLY) Color(0xFFE65100) else Color(0xFFFFCC80)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { reviewFilter = ResultReviewFilter.MISTAKES_ONLY }
                                    ) {
                                        Text(
                                            text = "الأخطاء (${mistakenQuestions.size}) 🧭",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (reviewFilter == ResultReviewFilter.MISTAKES_ONLY) Color.White else Color(0xFFE65100),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (reviewFilter == ResultReviewFilter.CORRECT_ONLY) NaturalOlive else NaturalCardBg,
                                    border = BorderStroke(1.dp, if (reviewFilter == ResultReviewFilter.CORRECT_ONLY) NaturalOlive else NaturalSandBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { reviewFilter = ResultReviewFilter.CORRECT_ONLY }
                                ) {
                                    Text(
                                        text = "المتقنة (${correctQuestions.size}) ✓",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (reviewFilter == ResultReviewFilter.CORRECT_ONLY) Color.White else NaturalCharcoal,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Filtered List of Questions in Review
                    val displayList = when (reviewFilter) {
                        ResultReviewFilter.ALL -> activeQuestions.mapIndexed { index, q -> Pair(index, q) }
                        ResultReviewFilter.MISTAKES_ONLY -> mistakenQuestions
                        ResultReviewFilter.CORRECT_ONLY -> correctQuestions
                    }

                    itemsIndexed(displayList) { _, (originalIndex, question) ->
                        val userAns = userAnswers.getOrNull(originalIndex)
                        val isCorrect = userAns == question.correctIndex

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCorrect) Color(0xFFF1F8E9) else Color(0xFFFFF8E1)
                            ),
                            border = BorderStroke(
                                1.2.dp,
                                if (isCorrect) Color(0xFFC8E6C9) else Color(0xFFFFB74D)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Question Header & Status
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "المسألة${originalIndex + 1}:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NaturalCharcoal
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                                            contentDescription = null,
                                            tint = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (isCorrect) "إجابة متقنة وموافقة" else "وقع فيها اشتباه وتحتاج تصحيحا",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                                        )
                                    }
                                }

                                Text(
                                    text = question.question,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NaturalCharcoal,
                                    lineHeight = 18.sp
                                )

                                // Answer Comparison
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = NaturalCardBg,
                                    border = BorderStroke(1.dp, if (isCorrect) Color(0xFFC8E6C9) else Color(0xFFFFE0B2)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (!isCorrect) {
                                            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text("❌", fontSize = 11.sp)
                                                Column {
                                                    Text("اختيارك غير الدقيق:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                                    Text(
                                                        text = userAns?.let { question.options.getOrNull(it) } ?: "لم يتم الاختيار",
                                                        fontSize = 11.sp,
                                                        color = NaturalCharcoal
                                                    )
                                                }
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("✅", fontSize = 11.sp)
                                            Column {
                                                Text("الصواب المعتمد:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                                Text(
                                                    text = question.options[question.correctIndex],
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1B5E20)
                                                )
                                            }
                                        }
                                    }
                                }

                                // If incorrect, show the why and how to fix
                                if (!isCorrect) {
                                    Text(
                                        text = "🔍 سبب الغلط والاشتباه:${question.whyWrongExplanation}",
                                        fontSize = 11.sp,
                                        color = Color(0xFFD84315),
                                        lineHeight = 16.sp
                                    )
                                }

                                Text(
                                    text = "📖 البيان التأصيلي:${question.explanation}",
                                    fontSize = 11.sp,
                                    color = NaturalCharcoal,
                                    lineHeight = 16.sp
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NaturalOlive.copy(alpha = 0.07f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "💡 نصيحة الشيخ وضابط التثبيت:",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NaturalOliveDark
                                        )
                                        Text(
                                            text = "${question.correctiveGuidance}\n📌 ${question.scholarNote}",
                                            fontSize = 10.sp,
                                            color = NaturalCharcoal,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }

                                // Audio Timestamp Remediation Button in Review
                                val reviewTargetLessonId = question.lessonId ?: (quiz.id.takeIf { quiz.quizType == QuizType.LESSON })
                                if (onPlayLessonAtTimestamp != null && reviewTargetLessonId != null) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isCorrect) NaturalOlive.copy(alpha = 0.1f) else Color(0xFFFFECB3),
                                        border = BorderStroke(1.dp, if (isCorrect) NaturalOlive.copy(alpha = 0.3f) else Color(0xFFFFB74D)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onPlayLessonAtTimestamp(reviewTargetLessonId, question.timestampSeconds)
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Headphones,
                                                contentDescription = null,
                                                tint = if (isCorrect) NaturalOliveDark else Color(0xFFE65100),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = if (isCorrect) "إعادة الاستماع لموضع المسألة في الدرس (${question.timestampLabel}) 🎧" else "استمع لشرح وتأصيل الشيخ عند الدقيقة (${question.timestampLabel}) 🎧",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCorrect) NaturalOliveDark else Color(0xFFE65100)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Final Action Buttons
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    activeQuestions = quiz.questions
                                    currentQuestionIndex = 0
                                    selectedOptionIndex = null
                                    isAnswerSubmitted = false
                                    userAnswers.clear()
                                    isQuizCompleted = false
                                    isMistakesRetestMode = false
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, NaturalOlive)
                            ) {
                                Icon(Icons.Default.Replay, contentDescription = null, tint = NaturalOlive, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("إعادة الاختبار كاملا", color = NaturalOlive, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val text = if (isSectionQuiz)
                                        "🏆 أتممت الامتحان الشامل لخاتمة قسم '${quiz.category?.displayName ?: quiz.title}' للشيخ سمير مصطفى بنتيجة$percentage% ($score/$totalQuestions) في تطبيق دروس الشيخ سمير مصطفى."
                                    else
                                        "🏆 أتممت اختبار درس '${quiz.title}' للشيخ سمير مصطفى بنتيجة$percentage% ($score/$totalQuestions) في تطبيق دروس الشيخ سمير مصطفى."
                                    ShareHelper.shareText(context, text)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (isSectionQuiz) NaturalOliveDark else NaturalOlive)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("مشاركة النتيجة", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NaturalItemBg)
                        ) {
                            Text("تم وحفظ الإنجاز", color = NaturalCharcoal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
