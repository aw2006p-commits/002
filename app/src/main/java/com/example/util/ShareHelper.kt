package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.model.Lesson
import com.example.data.model.SheikhQuote

object ShareHelper {

    fun shareLesson(context: Context, lesson: Lesson) {
        val tagsFormatted = if (lesson.tags.isNotEmpty()) {
            "\nالتصنيفات:" + lesson.tags.joinToString(" ") { "#$it" }
        } else ""

        val takeawaysFormatted = if (lesson.keyTakeaways.isNotEmpty()) {
            "\n\n📌 من فوائد ومحاور الدرس:\n" + lesson.keyTakeaways.joinToString("\n") { "• $it" }
        } else ""

        val shareText = """
            🎧 درس مبارك للشيخ سمير مصطفى - حفظه الله
            
            🔹 العنوان: ${lesson.title}
            🔹 السلسلة: ${lesson.series}
            🔹 المدة: ${lesson.durationFormatted}
            $tagsFormatted
            
            📝 النبذة:
            ${lesson.description}$takeawaysFormatted
            
            ✨ شارك تؤجر - الدال على الخير كفاعله (تطبيق دروس الشيخ سمير مصطفى).
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_TITLE, lesson.title)
            type = "text/plain"
        }

        val chooserIntent = Intent.createChooser(sendIntent, "مشاركة درس:${lesson.title}")
        context.startActivity(chooserIntent)
    }

    fun shareQuote(context: Context, quote: SheikhQuote) {
        val tagsFormatted = if (quote.tags.isNotEmpty()) {
            "\n" + quote.tags.joinToString(" ") { "#$it" }
        } else ""

        val sourceFormatted = if (!quote.lessonTitle.isNullOrBlank()) {
            "\n📌 المصدر: ${quote.lessonTitle} (⏱️ الدقيقة ${quote.timestampFormatted})"
        } else if (quote.context.isNotBlank()) {
            "\n📌 المصدر: ${quote.context}"
        } else ""

        val shareText = """
            💎 درر وفوائد الشيخ سمير مصطفى
            
            « ${quote.quote} »
            $sourceFormatted$tagsFormatted
            
            ✨ تطبيق دروس الشيخ سمير مصطفى
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_TITLE, "فائدة للشيخ سمير مصطفى")
            type = "text/plain"
        }

        val chooserIntent = Intent.createChooser(sendIntent, "مشاركة الفائدة العلمية")
        context.startActivity(chooserIntent)
    }

    fun shareText(context: Context, text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val chooserIntent = Intent.createChooser(sendIntent, "مشاركة الفائدة")
        context.startActivity(chooserIntent)
    }
}
