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

    fun setDistance(value: Int) {
        _distance.value = value
    }

    fun setNumPutts(value: Int) {
        _numPutts.value = value
    }

    fun startSession(onStarted: (distance: Int, numPutts: Int) -> Unit) {
        onStarted(_distance.value, _numPutts.value)
    }

    fun saveSession(distance: Int, numPutts: Int, madePutts: Int) {
        val db = PuttDatabase.getDatabase(getApplication())
        val session = PuttSession(
            distance = distance,
            numPutts = numPutts,
            madePutts = madePutts,
            missedPutts = numPutts - madePutts
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
} 