package com.example.data.repository

import android.content.Context
import com.example.data.model.BookReference
import com.example.data.model.DetailedLesson
import com.example.data.model.Lesson
import com.example.data.model.LessonSeries
import com.example.data.model.LessonTimestampChapter
import com.example.data.model.ScholarDatabase
import com.example.data.model.ScholarProfile
import com.example.data.model.ScholarQuoteDetail
import com.example.data.model.SubjectTopic
import com.example.data.model.toLesson
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * محمل ومحلل قاعدة بيانات دروس وسلاسل الشيخ سمير مصطفى من ملفات ال JSON
 */
object ScholarJsonDataLoader {

    private var cachedDatabase: ScholarDatabase? = null

    /**
     * تحميل قاعدة البيانات كاملة من assets/data/sheikh_samir_database.json
     */
    fun loadDatabase(context: Context): ScholarDatabase {
        cachedDatabase?.let { return it }

        return try {
            val inputStream = context.assets.open("data/sheikh_samir_database.json")
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val jsonString = reader.use { it.readText() }
            val rootJson = JSONObject(jsonString)

            val parsedDb = parseScholarDatabase(rootJson)
            cachedDatabase = parsedDb
            parsedDb
        } catch (e: Exception) {
            e.printStackTrace()
            // في حالة حدوث استثناء، نعيد قاعدة بيانات فارغة أو نموذجية
            fallbackDatabase()
        }
    }

    /**
     * جلب كافة الدروس كقائمة Lesson جاهزة لواجهة المستخدم
     */
    fun getAllLessonsAsUiModel(context: Context): List<Lesson> {
        val db = loadDatabase(context)
        return db.seriesList.flatMap { it.lessons }.map { it.toLesson() }
    }

    /**
     * جلب كافة السلاسل المرتبطة بكتاب معين
     */
    fun getSeriesByBookId(context: Context, bookId: String): List<LessonSeries> {
        val db = loadDatabase(context)
        return db.seriesList.filter { it.bookId == bookId }
    }

    /**
     * جلب كافة السلاسل المرتبطة بموضوع / تصنيف محدد
     */
    fun getSeriesByTopicId(context: Context, topicId: String): List<LessonSeries> {
        val db = loadDatabase(context)
        return db.seriesList.filter { it.topicId == topicId }
    }

    /**
     * جلب تفاصيل درس معين بجميع فصوله ومحاوره
     */
    fun getLessonById(context: Context, lessonId: String): DetailedLesson? {
        val db = loadDatabase(context)
        return db.seriesList.flatMap { it.lessons }.find { it.id == lessonId }
    }

    private fun parseScholarDatabase(json: JSONObject): ScholarDatabase {
        val version = json.optString("version", "1.0.0")
        val lastUpdated = json.optString("lastUpdated", "")

        // Profile
        val profileJson = json.getJSONObject("scholarProfile")
        val linksJson = profileJson.optJSONObject("officialLinks")
        val linksMap = mutableMapOf<String, String>()
        linksJson?.keys()?.forEach { key ->
            linksMap[key] = linksJson.optString(key)
        }

        val profile = ScholarProfile(
            id = profileJson.optString("id"),
            name = profileJson.optString("name"),
            title = profileJson.optString("title"),
            kunya = profileJson.optString("kunya"),
            biography = profileJson.optString("biography"),
            birthPlace = profileJson.optString("birthPlace"),
            primaryFields = profileJson.optJSONArray("primaryFields")?.let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            } ?: emptyList(),
            totalRecordedLessons = profileJson.optInt("totalRecordedLessons"),
            totalSeries = profileJson.optInt("totalSeries"),
            officialLinks = linksMap
        )

        // Topics
        val topicsArr = json.getJSONArray("topics")
        val topicsList = (0 until topicsArr.length()).map { i ->
            val t = topicsArr.getJSONObject(i)
            SubjectTopic(
                id = t.getString("id"),
                name = t.getString("name"),
                nameEnglish = t.optString("nameEnglish"),
                description = t.optString("description"),
                iconEmoji = t.optString("iconEmoji"),
                colorHex = t.optString("colorHex"),
                seriesCount = t.optInt("seriesCount")
            )
        }

        // Books
        val booksArr = json.getJSONArray("bookExplanations")
        val booksList = (0 until booksArr.length()).map { i ->
            val b = booksArr.getJSONObject(i)
            BookReference(
                id = b.getString("id"),
                bookTitle = b.getString("bookTitle"),
                originalAuthor = b.getString("originalAuthor"),
                field = b.getString("field"),
                summary = b.getString("summary"),
                explanationSeriesId = b.getString("explanationSeriesId")
            )
        }

        // Series & Lessons
        val seriesArr = json.getJSONArray("seriesList")
        val seriesList = (0 until seriesArr.length()).map { i ->
            val s = seriesArr.getJSONObject(i)
            val lessonsArr = s.getJSONArray("lessons")
            val lessonsList = (0 until lessonsArr.length()).map { j ->
                val l = lessonsArr.getJSONObject(j)

                val chaptersArr = l.optJSONArray("chapters")
                val chapters = if (chaptersArr != null) {
                    (0 until chaptersArr.length()).map { k ->
                        val c = chaptersArr.getJSONObject(k)
                        LessonTimestampChapter(
                            startSeconds = c.optLong("startSeconds", 0L),
                            title = c.optString("title", "")
                        )
                    }
                } else emptyList()

                val takeawaysArr = l.optJSONArray("keyTakeaways")
                val keyTakeaways = takeawaysArr?.let { arr ->
                    (0 until arr.length()).map { arr.getString(it) }
                } ?: emptyList()

                val tagsArr = l.optJSONArray("tags")
                val tags = tagsArr?.let { arr ->
                    (0 until arr.length()).map { arr.getString(it) }
                } ?: emptyList()

                val versesArr = l.optJSONArray("relatedVerses")
                val verses = versesArr?.let { arr ->
                    (0 until arr.length()).map { arr.getString(it) }
                } ?: emptyList()

                val hadithsArr = l.optJSONArray("relatedHadiths")
                val hadiths = hadithsArr?.let { arr ->
                    (0 until arr.length()).map { arr.getString(it) }
                } ?: emptyList()

                DetailedLesson(
                    id = l.getString("id"),
                    episodeNumber = l.optInt("episodeNumber", 1),
                    title = l.getString("title"),
                    seriesId = l.optString("seriesId", s.getString("id")),
                    seriesTitle = l.optString("seriesTitle", s.getString("title")),
                    topicId = l.optString("topicId", s.getString("topicId")),
                    topicDisplayName = l.optString("topicDisplayName", ""),
                    durationSeconds = l.optLong("durationSeconds", 0L),
                    durationFormatted = l.optString("durationFormatted", ""),
                    audioUrl = l.optString("audioUrl", ""),
                    recordingDate = l.optString("recordingDate", ""),
                    recordingLocation = l.optString("recordingLocation", ""),
                    summary = l.optString("summary", ""),
                    keyTakeaways = keyTakeaways,
                    tags = tags,
                    chapters = chapters,
                    relatedVerses = verses,
                    relatedHadiths = hadiths
                )
            }

            LessonSeries(
                id = s.getString("id"),
                title = s.getString("title"),
                topicId = s.getString("topicId"),
                bookId = if (s.has("bookId") && !s.isNull("bookId")) s.getString("bookId") else null,
                description = s.optString("description", ""),
                totalEpisodes = s.optInt("totalEpisodes", lessonsList.size),
                isCompleted = s.optBoolean("isCompleted", true),
                coverEmoji = s.optString("coverEmoji", "🎙️"),
                recordingPeriod = s.optString("recordingPeriod", ""),
                primaryLocation = s.optString("primaryLocation", ""),
                lessons = lessonsList
            )
        }

        // Quotes
        val quotesArr = json.optJSONArray("quotes")
        val quotesList = if (quotesArr != null) {
            (0 until quotesArr.length()).map { i ->
                val q = quotesArr.getJSONObject(i)
                ScholarQuoteDetail(
                    id = q.getString("id"),
                    quote = q.getString("quote"),
                    sourceSeries = q.optString("sourceSeries", ""),
                    lessonId = q.optString("lessonId", ""),
                    tags = q.optJSONArray("tags")?.let { arr ->
                        (0 until arr.length()).map { arr.getString(it) }
                    } ?: emptyList()
                )
            }
        } else emptyList()

        return ScholarDatabase(
            version = version,
            lastUpdated = lastUpdated,
            scholarProfile = profile,
            topics = topicsList,
            bookExplanations = booksList,
            seriesList = seriesList,
            quotes = quotesList
        )
    }

    private fun fallbackDatabase(): ScholarDatabase {
        return ScholarDatabase(
            version = "1.0.0",
            lastUpdated = "2026-08-22",
            scholarProfile = ScholarProfile(
                id = "sheikh_samir_mustafa",
                name = "الشيخ سمير مصطفى",
                title = "الداعية والمربي الإسلامي",
                kunya = "أبو أنس",
                biography = "داعية ومرب إسلامي",
                birthPlace = "القاهرة، مصر",
                primaryFields = listOf("السيرة", "الرقائق"),
                totalRecordedLessons = 49,
                totalSeries = 9,
                officialLinks = emptyMap()
            ),
            topics = emptyList(),
            bookExplanations = emptyList(),
            seriesList = emptyList(),
            quotes = emptyList()
        )
    }
}
