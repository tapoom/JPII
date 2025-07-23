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
        }
    }

    fun loadSessionsForDistance(distance: Int) {
        val db = PuttDatabase.getDatabase(getApplication())
        viewModelScope.launch {
            _sessions.value = db.puttSessionDao().getSessionsByDistance(distance).take(10)
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
            _sessions.value = filtered.take(10)
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
            _sessions.value = filtered.take(10)
        }
    }
} 