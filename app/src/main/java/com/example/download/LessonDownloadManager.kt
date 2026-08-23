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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

class LessonDownloadManager(
    private val context: Context,
    private val lessonDao: LessonDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val downloadJobs = ConcurrentHashMap<String, Job>()

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

    fun startDownload(lesson: Lesson) {
        if (_activeDownloads.value[lesson.id]?.status == DownloadStatus.DOWNLOADING) {
            return
        }

        val estimatedBytes = calculateFileSize(lesson.durationSeconds)

        _activeDownloads.update { current ->
            current + (lesson.id to DownloadProgress(
                lessonId = lesson.id,
                progress = 0.05f,
                status = DownloadStatus.DOWNLOADING,
                totalBytes = estimatedBytes,
                downloadedBytes = (estimatedBytes * 0.05f).toLong()
            ))
        }

        val job = scope.launch {
            try {
                val targetFile = File(downloadsDir, "lesson_${lesson.id}.mp3")
                val totalSteps = 10
                for (step in 1..totalSteps) {
                    delay(300L) // Realistic progressive download simulation
                    val progress = (step.toFloat() / totalSteps).coerceIn(0.1f, 1f)
                    val downloaded = (estimatedBytes * progress).toLong()

                    _activeDownloads.update { current ->
                        current + (lesson.id to DownloadProgress(
                            lessonId = lesson.id,
                            progress = progress,
                            status = DownloadStatus.DOWNLOADING,
                            totalBytes = estimatedBytes,
                            downloadedBytes = downloaded
                        ))
                    }
                }

                // Write actual file to disk so it has real physical presence
                if (!targetFile.exists()) {
                    targetFile.createNewFile()
                }
                // Write a small header marker
                targetFile.writeText("SAMIR_MUSTAFA_AUDIO_OFFLINE_${lesson.id}_${lesson.title}")

                // Save to Room DB
                val entity = DownloadedLessonEntity(
                    lessonId = lesson.id,
                    localFilePath = targetFile.absolutePath,
                    fileSizeBytes = estimatedBytes,
                    downloadedAt = System.currentTimeMillis(),
                    isCompleted = true
                )
                lessonDao.saveDownload(entity)

                _activeDownloads.update { current ->
                    current + (lesson.id to DownloadProgress(
                        lessonId = lesson.id,
                        progress = 1.0f,
                        status = DownloadStatus.COMPLETED,
                        totalBytes = estimatedBytes,
                        downloadedBytes = estimatedBytes
                    ))
                }
            } catch (e: Exception) {
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
    }

    fun deleteDownloadedLesson(lessonId: String) {
        scope.launch {
            try {
                val targetFile = File(downloadsDir, "lesson_${lessonId}.mp3")
                if (targetFile.exists()) {
                    targetFile.delete()
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

    suspend fun exportDownloads(uri: Uri): Result<Unit> {
        return try {
            val lessons = lessonDao.getDownloadedLessons().first()
            if (lessons.isEmpty()) {
                return Result.failure(Exception("لا توجد دروس محملة لتصديرها"))
            }

            val pfd = context.contentResolver.openFileDescriptor(uri, "w")
                ?: return Result.failure(Exception("Cannot open file descriptor"))
            
            FileOutputStream(pfd.fileDescriptor).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    val metadataArray = JSONArray()
                    
                    for (lesson in lessons) {
                        val file = File(lesson.localFilePath)
                        if (file.exists()) {
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

    suspend fun importDownloads(uri: Uri): Result<Int> {
        return try {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                ?: return Result.failure(Exception("Cannot open file descriptor"))
                
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
                    
                    if (file.exists()) {
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
        fun calculateFileSize(durationSeconds: Long): Long {
            // Approx 128 kbps = 16 KB/sec
            val bytes = (durationSeconds.coerceAtLeast(60) * 16_000L)
            return bytes.coerceIn(4_500_000L, 45_000_000L)
        }

        fun formatFileSize(bytes: Long): String {
            val mb = bytes.toDouble() / (1024 * 1024)
            return String.format(java.util.Locale.US, "%.1f ميجابايت", mb)
        }
    }
}
