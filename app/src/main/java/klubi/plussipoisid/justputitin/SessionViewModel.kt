package klubi.plussipoisid.justputitin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import klubi.plussipoisid.justputitin.data.PuttDatabase
import klubi.plussipoisid.justputitin.data.PuttSession
import klubi.plussipoisid.justputitin.data.PuttSessionDao
import kotlin.math.ln
import kotlin.math.roundToInt

class SessionViewModel(application: Application) : AndroidViewModel(application) {
    private val _distance = MutableStateFlow(0)
    val distance: StateFlow<Int> = _distance

    private val _numPutts = MutableStateFlow(0)
    val numPutts: StateFlow<Int> = _numPutts

    private val _sessions = MutableStateFlow<List<PuttSession>>(emptyList())
    val sessions: StateFlow<List<PuttSession>> = _sessions.asStateFlow()

    private val _distances = MutableStateFlow<List<Int>>(emptyList())
    val distances: StateFlow<List<Int>> = _distances.asStateFlow()

    private val _stylesForDistance = MutableStateFlow<List<String>>(emptyList())
    val stylesForDistance: StateFlow<List<String>> = _stylesForDistance.asStateFlow()

    val styles = listOf(
        "Push Putt (Spin-Push Hybrid)",
        "Spin Putt",
        "Push-Putt (Traditional)",
        "Spush Putt (Spin + Push Hybrid)",
        "Turbo Putt",
        "Straddle Putt",
        "Staggered Stance Putt",
        "Jump Putt",
        "Step Putt",
        "Scoober or Overhand Putt",
        "Straddle Jump Putt"
    )
    private val _selectedStyle = MutableStateFlow(styles[1])
    val selectedStyle: StateFlow<String> = _selectedStyle.asStateFlow()
    fun setStyle(style: String) { _selectedStyle.value = style }

    // Sound effects toggle
    private val _soundOn = MutableStateFlow(true)
    val soundOn: StateFlow<Boolean> = _soundOn.asStateFlow()
    fun setSoundOn(enabled: Boolean) { _soundOn.value = enabled }

    // Average hit rate for selected distance
    private val _averageHitRate = MutableStateFlow<Double?>(null)
    val averageHitRate: StateFlow<Double?> = _averageHitRate.asStateFlow()
    fun loadAverageHitRate(distance: Int) {
        val db = PuttDatabase.getDatabase(getApplication())
        viewModelScope.launch {
            val sessions = db.puttSessionDao().getAtLeastTenSessionsByDistance(distance)
            if (sessions.isNotEmpty()) {
                val totalPutts = sessions.sumOf { it.numPutts }
                val totalMade = sessions.sumOf { it.madePutts }
                _averageHitRate.value = if (totalPutts > 0) totalMade.toDouble() / totalPutts else null
            } else {
                _averageHitRate.value = null
            }
        }
    }

    // Putting rating state
    private val _puttingRating = MutableStateFlow(0)
    val puttingRating: StateFlow<Int> = _puttingRating.asStateFlow()
    fun loadPuttingRating() {
        val db = PuttDatabase.getDatabase(getApplication())
        viewModelScope.launch {
            val rating = db.puttSessionDao().puttingIndex().toRating()
            _puttingRating.value = rating
        }
    }

    fun setDistance(value: Int) {
        _distance.value = value
    }

    fun setNumPutts(value: Int) {
        _numPutts.value = value
    }

    fun startSession(onStarted: (distance: Int, numPutts: Int) -> Unit) {
        onStarted(_distance.value, _numPutts.value)
    }

    fun saveSession(distance: Int, numPutts: Int, madePutts: Int, style: String = styles[0]) {
        val db = PuttDatabase.getDatabase(getApplication())
        val session = PuttSession(
            distance = distance,
            numPutts = numPutts,
            madePutts = madePutts,
            missedPutts = numPutts - madePutts,
            style = style
        )
        viewModelScope.launch {
            db.puttSessionDao().insertSession(session)
            loadPuttingRating() // Refresh rating after saving
        }
    }

    fun loadSessionsForDistance(distance: Int) {
        val db = PuttDatabase.getDatabase(getApplication())
        viewModelScope.launch {
            _sessions.value = db.puttSessionDao().getSessionsByDistance(distance)
        }
    }

    fun loadDistances() {
        val db = PuttDatabase.getDatabase(getApplication())
        viewModelScope.launch {
            _distances.value = db.puttSessionDao().getAllSessions().map { it.distance }.distinct().sorted()
        }
    }

    fun loadStylesForDistance(distance: Int) {
        val db = PuttDatabase.getDatabase(getApplication())
        viewModelScope.launch {
            _stylesForDistance.value = db.puttSessionDao().getSessionsByDistance(distance).map { it.style }.distinct().sorted()
        }
    }

    fun loadSessionsForDistanceAndRange(distance: Int, range: String) {
        val db = PuttDatabase.getDatabase(getApplication())
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            val all = db.puttSessionDao().getSessionsByDistance(distance)
            val filtered = when (range) {
                "Last week" -> all.filter { it.date >= now - 7 * 24 * 60 * 60 * 1000L }
                "Last month" -> all.filter { it.date >= now - 30 * 24 * 60 * 60 * 1000L }
                "Last year" -> all.filter { it.date >= now - 365 * 24 * 60 * 60 * 1000L }
                else -> all
            }
            _sessions.value = filtered
        }
    }

    fun loadSessionsForDistanceAndStyle(distance: Int, style: String?, range: String) {
        val db = PuttDatabase.getDatabase(getApplication())
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            val all = db.puttSessionDao().getSessionsByDistance(distance)
            val filteredByStyle = if (style == null || style == "All") all else all.filter { it.style == style }
            val filtered = when (range) {
                "Last week" -> filteredByStyle.filter { it.date >= now - 7 * 24 * 60 * 60 * 1000L }
                "Last month" -> filteredByStyle.filter { it.date >= now - 30 * 24 * 60 * 60 * 1000L }
                "Last year" -> filteredByStyle.filter { it.date >= now - 365 * 24 * 60 * 60 * 1000L }
                else -> filteredByStyle
            }
            _sessions.value = filtered
        }
    }

    // Calculate putt rating
    data class DistanceStats(
        val distance: Int,
        val attempts: Int,
        val made: Int,
        val pct: Double          // 0 – 1
    )

    private fun linearWeight(d: Int): Double {
        return d.toDouble() / 15.0
    }

    private fun quadraticWeight(d: Int): Double {
        val norm = d.toDouble() / 15.0
        return norm * norm   // (d / 15)^2
    }

    private fun logarithmicWeight(d: Int): Double {
        return ln((1 + d).toDouble()) / ln(16.0)  // ln(1+d) normalized to 0–1
    }

    suspend fun PuttSessionDao.puttingIndex(): Double {
        val rows = distanceStats()
        if (rows.isEmpty()) return 0.0          // no data yet

        val (num, den) = rows.fold(0.0 to 0.0) { (n, d), row ->
            val w = linearWeight(row.distance) * row.attempts   // more data ⇒ higher confidence
            (n + w * row.pct) to (d + w)
        }
        return if (den == 0.0) 0.0 else num / den        // 0 – 1
    }

    fun Double.toRating(): Int = (this * 100).roundToInt()   // 0 – 100 scale

    suspend fun PuttSessionDao.lastSessionsForPuttRange(
        distance: Int,
        minPutts: Int = 10,
        maxPutts: Int = 30
    ): List<PuttSession> {
        val sessions = allSessionsAt(distance)
        val result = mutableListOf<PuttSession>()
        var count = 0

        for (session in sessions) {
            if (count >= maxPutts) break
            result.add(session)
            count += session.numPutts
        }

        return if (count >= minPutts) result else emptyList()
    }

    suspend fun PuttSessionDao.distanceStats(
        minPutts: Int = 10,
        maxPutts: Int = 30
    ): List<DistanceStats> =
        (3..15).mapNotNull { d ->
            val s = lastSessionsForPuttRange(d, minPutts, maxPutts)
            if (s.isEmpty()) return@mapNotNull null

            val made  = s.sumOf { it.madePutts }
            val tries = s.sumOf { it.numPutts }
            DistanceStats(d, tries, made, made.toDouble() / tries)
        }
} 