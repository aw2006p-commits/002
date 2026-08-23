package com.example.download

import android.content.Context
import android.net.Uri
import com.example.data.local.DownloadedLessonEntity
import com.example.data.local.LessonDao
import com.example.data.model.Lesson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class LessonDownloadManager(
    private val context: Context,
    private val lessonDao: LessonDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val downloadJobs = ConcurrentHashMap<String, Job>()

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()
    }

    private val _activeDownloads = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
    val activeDownloads: StateFlow<Map<String, DownloadProgress>> = _activeDownloads.asStateFlow()

    val downloadedLessons: Flow<List<DownloadedLessonEntity>> = lessonDao.getDownloadedLessons()
    val downloadedLessonIds: Flow<Set<String>> = lessonDao.getDownloadedLessonIds().map { it.toSet() }

    private val downloadsDir: File
        get() {
            val dir = File(context.filesDir, "offline_lessons")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    fun getLocalPath(lessonId: String): String? {
        val file = File(downloadsDir, "lesson_$lessonId.mp3")
        return if (file.exists() && file.length() > 1024L) file.absolutePath else null
    }

    fun startDownload(lesson: Lesson) {
        if (_activeDownloads.value[lesson.id]?.status == DownloadStatus.DOWNLOADING) {
            return
        }

        val audioUrl = lesson.audioUrl.trim()
        if (audioUrl.isBlank()) {
            _activeDownloads.update { current ->
                current + (lesson.id to DownloadProgress(
                    lessonId = lesson.id,
                    progress = 0f,
                    status = DownloadStatus.FAILED
                ))
            }
            return
        }

        val estimatedBytes = calculateFileSize(lesson.durationSeconds)

        _activeDownloads.update { current ->
            current + (lesson.id to DownloadProgress(
                lessonId = lesson.id,
                progress = 0.02f,
                status = DownloadStatus.DOWNLOADING,
                totalBytes = estimatedBytes,
                downloadedBytes = 0L
            ))
        }

        val job = scope.launch {
            val targetFile = File(downloadsDir, "lesson_${lesson.id}.mp3")
            val tempFile = File(downloadsDir, "lesson_${lesson.id}.mp3.part")

            try {
                if (tempFile.exists()) {
                    tempFile.delete()
                }

                val request = Request.Builder()
                    .url(audioUrl)
                    .header("User-Agent", "QabasAndroid/1.0")
                    .get()
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IllegalStateException("HTTP ${response.code}")
                    }

                    val body = response.body ?: throw IllegalStateException("Empty body")
                    val totalFromServer = body.contentLength().takeIf { it > 0 } ?: estimatedBytes

                    body.byteStream().use { input ->
                        FileOutputStream(tempFile).use { output ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            var downloaded = 0L
                            var lastEmitAt = 0L

                            while (true) {
                                val read = input.read(buffer)
                                if (read == -1) break
                                output.write(buffer, 0, read)
                                downloaded += read

                                val now = System.currentTimeMillis()
                                if (now - lastEmitAt >= 250L || downloaded >= totalFromServer) {
                                    lastEmitAt = now
                                    val progress = (downloaded.toFloat() / totalFromServer.toFloat())
                                        .coerceIn(0.02f, 0.99f)
                                    _activeDownloads.update { current ->
                                        current + (lesson.id to DownloadProgress(
                                            lessonId = lesson.id,
                                            progress = progress,
                                            status = DownloadStatus.DOWNLOADING,
                                            totalBytes = totalFromServer,
                                            downloadedBytes = downloaded
                                        ))
                                    }
                                }
                            }
                            output.flush()
                        }
                    }

                    if (!tempFile.exists() || tempFile.length() < 1024L) {
                        throw IllegalStateException("Downloaded file too small")
                    }

                    if (targetFile.exists()) {
                        targetFile.delete()
                    }
                    if (!tempFile.renameTo(targetFile)) {
                        tempFile.copyTo(targetFile, overwrite = true)
                        tempFile.delete()
                    }

                    val finalSize = targetFile.length()
                    val entity = DownloadedLessonEntity(
                        lessonId = lesson.id,
                        localFilePath = targetFile.absolutePath,
                        fileSizeBytes = finalSize,
                        downloadedAt = System.currentTimeMillis(),
                        isCompleted = true
                    )
                    lessonDao.saveDownload(entity)

                    _activeDownloads.update { current ->
                        current + (lesson.id to DownloadProgress(
                            lessonId = lesson.id,
                            progress = 1.0f,
                            status = DownloadStatus.COMPLETED,
                            totalBytes = finalSize,
                            downloadedBytes = finalSize
                        ))
                    }
                }
            } catch (e: Exception) {
                runCatching { tempFile.delete() }
                _activeDownloads.update { current ->
                    current + (lesson.id to DownloadProgress(
                        lessonId = lesson.id,
                        progress = 0f,
                        status = DownloadStatus.FAILED,
                        totalBytes = estimatedBytes,
                        downloadedBytes = 0L
                    ))
                }
            } finally {
                downloadJobs.remove(lesson.id)
            }
        }

        downloadJobs[lesson.id] = job
    }

    fun cancelDownload(lessonId: String) {
        val job = downloadJobs.remove(lessonId)
        job?.cancel()
        _activeDownloads.update { current ->
            current - lessonId
        }
        scope.launch {
            val tempFile = File(downloadsDir, "lesson_$lessonId.mp3.part")
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }

    fun deleteDownloadedLesson(lessonId: String) {
        scope.launch {
            try {
                val targetFile = File(downloadsDir, "lesson_$lessonId.mp3")
                if (targetFile.exists()) {
                    targetFile.delete()
                }
                val tempFile = File(downloadsDir, "lesson_$lessonId.mp3.part")
                if (tempFile.exists()) {
                    tempFile.delete()
                }
                lessonDao.deleteDownload(lessonId)
                _activeDownloads.update { current ->
                    current - lessonId
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteAllDownloads() {
        scope.launch {
            try {
                downloadsDir.listFiles()?.forEach { file ->
                    file.delete()
                }
                lessonDao.deleteAllDownloads()
                _activeDownloads.update { emptyMap() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun exportDownloads(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val lessons = lessonDao.getDownloadedLessons().first()
            if (lessons.isEmpty()) {
                return@withContext Result.failure(Exception("لا توجد دروس محملة لتصديرها"))
            }

            val pfd = context.contentResolver.openFileDescriptor(uri, "w")
                ?: return@withContext Result.failure(Exception("Cannot open file descriptor"))

            FileOutputStream(pfd.fileDescriptor).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    val metadataArray = JSONArray()

                    for (lesson in lessons) {
                        val file = File(lesson.localFilePath)
                        if (file.exists() && file.length() > 0L) {
                            val entry = ZipEntry(file.name)
                            zos.putNextEntry(entry)
                            FileInputStream(file).use { fis ->
                                fis.copyTo(zos)
                            }
                            zos.closeEntry()

                            val lessonObj = JSONObject().apply {
                                put("lessonId", lesson.lessonId)
                                put("fileName", file.name)
                                put("fileSizeBytes", lesson.fileSizeBytes)
                                put("downloadedAt", lesson.downloadedAt)
                                put("isCompleted", lesson.isCompleted)
                            }
                            metadataArray.put(lessonObj)
                        }
                    }

                    val metadataEntry = ZipEntry("metadata.json")
                    zos.putNextEntry(metadataEntry)
                    zos.write(metadataArray.toString(2).toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                }
            }
            pfd.close()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun importDownloads(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                ?: return@withContext Result.failure(Exception("Cannot open file descriptor"))

            var importedCount = 0
            val metadataString = StringBuilder()

            FileInputStream(pfd.fileDescriptor).use { fis ->
                ZipInputStream(fis).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        if (entry.name == "metadata.json") {
                            val bytes = zis.readBytes()
                            metadataString.append(String(bytes, Charsets.UTF_8))
                        } else if (entry.name.endsWith(".mp3")) {
                            val targetFile = File(downloadsDir, entry.name)
                            FileOutputStream(targetFile).use { fos ->
                                zis.copyTo(fos)
                            }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }
            pfd.close()

            if (metadataString.isNotEmpty()) {
                val metadataArray = JSONArray(metadataString.toString())
                for (i in 0 until metadataArray.length()) {
                    val obj = metadataArray.getJSONObject(i)
                    val lessonId = obj.getString("lessonId")
                    val fileName = obj.getString("fileName")
                    val file = File(downloadsDir, fileName)

                    if (file.exists() && file.length() > 0L) {
                        val entity = DownloadedLessonEntity(
                            lessonId = lessonId,
                            localFilePath = file.absolutePath,
                            fileSizeBytes = obj.optLong("fileSizeBytes", file.length()),
                            downloadedAt = obj.optLong("downloadedAt", System.currentTimeMillis()),
                            isCompleted = obj.optBoolean("isCompleted", true)
                        )
                        lessonDao.saveDownload(entity)
                        importedCount++
                    }
                }
            }

            Result.success(importedCount)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 64 * 1024

        fun calculateFileSize(durationSeconds: Long): Long {
            val bytes = (durationSeconds.coerceAtLeast(60) * 16_000L)
            return bytes.coerceIn(4_500_000L, 45_000_000L)
        }

        fun formatFileSize(bytes: Long): String {
            val mb = bytes.toDouble() / (1024 * 1024)
            return String.format(java.util.Locale.US, "%.1f ميجابايت", mb)
        }
    }
}
