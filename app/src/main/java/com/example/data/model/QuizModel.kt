package com.example.data.model

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val scholarNote: String = "الفائدة: التحقيق العلمي الدقيق يثبت حفظ المسائل ويمنع الاشتباه.",
    val whyWrongExplanation: String = "سبب وقوع الاشتباه: قد يلتبس المعنى المتبادر أو الفرعي بالأصل التأصيلي، والصواب يتقرر بجمع الأدلة واستصحاب مقاصد الشريعة.",
    val correctiveGuidance: String = "التوجيه والتصحيح العلمي: ينبغي لطالب العلم ربط المسألة بضابطها الكلي، وتكرار الاستماع لمواضع التحقيق في الدرس لتستقر الحجة في القلب.",
    val timestampSeconds: Long = 0L,
    val timestampLabel: String = "02:30",
    val lessonId: String? = null
)

enum class QuizType {
    LESSON,
    SECTION
}

data class UnifiedQuiz(
    val id: String,
    val title: String,
    val subtitle: String,
    val badgeLabel: String,
    val quizType: QuizType,
    val category: LessonCategory? = null,
    val difficulty: String = "مستوى متقدم • ترسيخ الفوائد",
    val questions: List<QuizQuestion>
)

data class LessonQuiz(
    val lessonId: String,
    val lessonTitle: String,
    val series: String,
    val difficulty: String = "مستوى متقدم • ترسيخ الفوائد",
    val questions: List<QuizQuestion>
)

