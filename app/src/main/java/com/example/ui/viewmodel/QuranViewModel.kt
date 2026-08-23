package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

data class Surah(
    val id: Int, 
    val name: String, 
    val type: String, 
    val ayahs: Int, 
    val page: Int,
    val englishName: String = "",
    val translatedName: String = ""
)

data class Ayah(
    val numberInSurah: Int,
    val text: String,
    val translation: String,
    val audioUrl: String? = null
)

class QuranViewModel : ViewModel() {
    private val client = OkHttpClient()

    private val _surahs = MutableStateFlow<List<Surah>>(emptyList())
    val surahs = _surahs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _surahDetails = MutableStateFlow<Map<Int, List<Ayah>>>(emptyMap())
    val surahDetails = _surahDetails.asStateFlow()

    init {
        fetchSurahs()
    }

    private fun fetchSurahs() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            try {
                val request = Request.Builder()
                    .url("https://api.quran.com/api/v4/chapters")
                    .build()
                val response = client.newCall(request).execute()
                val body = response.body?.string()
                
                if (response.isSuccessful && body != null) {
                    val json = JSONObject(body)
                    val chapters = json.getJSONArray("chapters")
                    val parsedSurahs = mutableListOf<Surah>()
                    
                    for (i in 0 until chapters.length()) {
                        val chapter = chapters.getJSONObject(i)
                        parsedSurahs.add(
                            Surah(
                                id = chapter.getInt("id"),
                                name = chapter.getString("name_arabic"),
                                type = if (chapter.getString("revelation_place") == "makkah") "مكية" else "مدنية",
                                ayahs = chapter.getInt("verses_count"),
                                page = chapter.getJSONArray("pages").getInt(0),
                                englishName = chapter.getString("name_simple"),
                                translatedName = chapter.getJSONObject("translated_name").getString("name")
                            )
                        )
                    }
                    _surahs.value = parsedSurahs
                } else {
                    _error.value = "Failed to load surahs: ${response.code}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchSurahDetails(surahId: Int) {
        if (_surahDetails.value.containsKey(surahId)) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetching Arabic text and audio
                val request = Request.Builder()
                    .url("https://api.alquran.cloud/v1/surah/$surahId/editions/quran-uthmani,en.asad,ar.alafasy")
                    .build()
                val response = client.newCall(request).execute()
                val body = response.body?.string()
                
                if (response.isSuccessful && body != null) {
                    val json = JSONObject(body)
                    val data = json.getJSONArray("data")
                    
                    val arabicEdition = data.getJSONObject(0).getJSONArray("ayahs")
                    val translationEdition = data.getJSONObject(1).getJSONArray("ayahs")
                    val audioEdition = data.getJSONObject(2).getJSONArray("ayahs")
                    
                    val ayahs = mutableListOf<Ayah>()
                    for (i in 0 until arabicEdition.length()) {
                        val arAyah = arabicEdition.getJSONObject(i)
                        val trAyah = translationEdition.getJSONObject(i)
                        val auAyah = audioEdition.getJSONObject(i)
                        
                        ayahs.add(
                            Ayah(
                                numberInSurah = arAyah.getInt("numberInSurah"),
                                text = arAyah.getString("text"),
                                translation = trAyah.getString("text"),
                                audioUrl = auAyah.getString("audio")
                            )
                        )
                    }
                    
                    val currentMap = _surahDetails.value.toMutableMap()
                    currentMap[surahId] = ayahs
                    _surahDetails.value = currentMap
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
