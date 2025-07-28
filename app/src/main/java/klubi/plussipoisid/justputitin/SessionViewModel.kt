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
import klubi.plussipoisid.justputitin.data.TrainingRun
import klubi.plussipoisid.justputitin.data.TrainingStep
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.random.Random

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

    // Training Run state
    private val _trainingRuns = MutableStateFlow<List<TrainingRun>>(emptyList())
    val trainingRuns: StateFlow<List<TrainingRun>> = _trainingRuns.asStateFlow()

    private val _currentTrainingRun = MutableStateFlow<TrainingRun?>(null)
    val currentTrainingRun: StateFlow<TrainingRun?> = _currentTrainingRun.asStateFlow()

    private val _trainingSteps = MutableStateFlow<List<TrainingStep>>(emptyList())
    val trainingSteps: StateFlow<List<TrainingStep>> = _trainingSteps.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex.asStateFlow()

    // Training setup state
    private val _minDistance = MutableStateFlow(3)
    val minDistance: StateFlow<Int> = _minDistance.asStateFlow()

    private val _maxDistance = MutableStateFlow(10)
    val maxDistance: StateFlow<Int> = _maxDistance.asStateFlow()

    private val _maxPuttsPerDistance = MutableStateFlow(10)
    val maxPuttsPerDistance: StateFlow<Int> = _maxPuttsPerDistance.asStateFlow()

    // Tracking mode state
    private val _trackingMode = MutableStateFlow("Per Putt")
    val trackingMode: StateFlow<String> = _trackingMode.asStateFlow()
    fun setTrackingMode(mode: String) { _trackingMode.value = mode }

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

    // Training Run functions
    fun setMinDistance(value: Int) { _minDistance.value = value }
    fun setMaxDistance(value: Int) { _maxDistance.value = value }
    fun setMaxPuttsPerDistance(value: Int) { _maxPuttsPerDistance.value = value }

    fun createTrainingRun(): TrainingRun {
        val minDist = _minDistance.value
        val maxDist = _maxDistance.value
        val maxPutts = _maxPuttsPerDistance.value
        
        // Generate random training steps
        val steps = mutableListOf<TrainingStep>()
        val distances = (minDist..maxDist).toList()
        var totalPutts = 0
        
        // Ensure we have at least one step
        if (distances.isNotEmpty()) {
            // Randomly select distances and number of putts
            // Generate random sessions
            for (i in 0 until ((maxDist - minDist) * 2)) {
                if (totalPutts >= 50) break // Limit total putts
                val distance = distances.random()
                val numPutts = Random.nextInt(2, maxPutts + 1)
                steps.add(TrainingStep(distance = distance, numPutts = numPutts))
                totalPutts += numPutts
            }
        }
        
        // Ensure we have at least one step
        if (steps.isEmpty()) {
            steps.add(TrainingStep(distance = minDist, numPutts = 5))
            totalPutts = 5
        }
        
        _trainingSteps.value = steps.shuffled() // Shuffle the steps for randomness
        
        val trainingRun = TrainingRun(
            minDistance = minDist,
            maxDistance = maxDist,
            maxPuttsPerDistance = maxPutts,
            totalPutts = totalPutts,
            style = _selectedStyle.value,
            trackingMode = _trackingMode.value
        )
        
        _currentTrainingRun.value = trainingRun
        _currentStepIndex.value = 0
        
        // Update current step and progress
        _currentStep.value = _trainingSteps.value.firstOrNull()
        _trainingProgress.value = 0f
        
        // Debug logging
        android.util.Log.d("TrainingRun", "Created training run: ${trainingRun.totalPutts} total putts")
        android.util.Log.d("TrainingRun", "Training steps: ${_trainingSteps.value.size} steps")
        android.util.Log.d("TrainingRun", "Current step: ${_currentStep.value}")
        
        // Save the training run to database
        saveTrainingRun(trainingRun)
        
        return trainingRun
    }

    fun saveTrainingRun(trainingRun: TrainingRun) {
        val db = PuttDatabase.getDatabase(getApplication())
        viewModelScope.launch {
            db.puttSessionDao().insertTrainingRun(trainingRun)
            loadTrainingRuns()
        }
    }

    fun loadTrainingRuns() {
        val db = PuttDatabase.getDatabase(getApplication())
        viewModelScope.launch {
            _trainingRuns.value = db.puttSessionDao().getAllTrainingRuns()
        }
    }

    fun updateTrainingRun(trainingRun: TrainingRun) {
        val db = PuttDatabase.getDatabase(getApplication())
        viewModelScope.launch {
            db.puttSessionDao().updateTrainingRun(trainingRun)
            _currentTrainingRun.value = trainingRun
        }
    }

    fun recordTrainingPutt(made: Boolean) {
        val currentRun = _currentTrainingRun.value ?: return
        val currentStep = _trainingSteps.value.getOrNull(_currentStepIndex.value) ?: return
        
        // Update current step
        val updatedSteps = _trainingSteps.value.toMutableList()
        val updatedStep = currentStep.copy(
            completedPutts = currentStep.completedPutts + 1,
            madePutts = currentStep.madePutts + if (made) 1 else 0
        )
        updatedSteps[_currentStepIndex.value] = updatedStep
        _trainingSteps.value = updatedSteps
        
        // Update current step state
        _currentStep.value = updatedStep
        
        // Update training run
        val updatedRun = currentRun.copy(
            completedPutts = currentRun.completedPutts + 1,
            madePutts = currentRun.madePutts + if (made) 1 else 0
        )
        _currentTrainingRun.value = updatedRun
        
        // Check if current step is complete
        if (updatedStep.completedPutts >= updatedStep.numPutts) {
            if (_currentStepIndex.value < _trainingSteps.value.size - 1) {
                _currentStepIndex.value += 1
                // Update current step
                _currentStep.value = _trainingSteps.value.getOrNull(_currentStepIndex.value)
            } else {
                // Training run complete
                val completedRun = updatedRun.copy(isCompleted = true)
                _currentTrainingRun.value = completedRun
                updateTrainingRun(completedRun)
                // Save individual sessions for each step
                saveTrainingRunSessions(completedRun, updatedSteps)
            }
        }
        
        // Update progress
        _trainingProgress.value = if (updatedRun.totalPutts > 0) {
            updatedRun.completedPutts.toFloat() / updatedRun.totalPutts
        } else 0f
    }

    private fun saveTrainingRunSessions(trainingRun: TrainingRun, steps: List<TrainingStep>) {
        viewModelScope.launch {
            steps.forEach { step ->
                if (step.completedPutts > 0) {
                    val session = PuttSession(
                        distance = step.distance,
                        numPutts = step.completedPutts,
                        madePutts = step.madePutts,
                        missedPutts = step.completedPutts - step.madePutts,
                        style = trainingRun.style
                    )
                    val db = PuttDatabase.getDatabase(getApplication())
                    db.puttSessionDao().insertSession(session)
                }
            }
            loadPuttingRating() // Refresh rating after saving sessions
        }
    }

    private val _currentStep = MutableStateFlow<TrainingStep?>(null)
    val currentStep: StateFlow<TrainingStep?> = _currentStep.asStateFlow()

    private val _trainingProgress = MutableStateFlow(0f)
    val trainingProgress: StateFlow<Float> = _trainingProgress.asStateFlow()

    fun getCurrentStep(): TrainingStep? {
        val step = _trainingSteps.value.getOrNull(_currentStepIndex.value)
        _currentStep.value = step
        return step
    }

    fun getTrainingProgress(): Float {
        val currentRun = _currentTrainingRun.value ?: return 0f
        val progress = if (currentRun.totalPutts > 0) {
            currentRun.completedPutts.toFloat() / currentRun.totalPutts
        } else 0f
        _trainingProgress.value = progress
        return progress
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

    fun loadAllStyles() {
        val db = PuttDatabase.getDatabase(getApplication())
        viewModelScope.launch {
            _stylesForDistance.value = db.puttSessionDao().getAllSessions().map { it.style }.distinct().sorted()
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

    // New: Load sessions for all distances in a custom date range
    fun loadSessionsByDateRange(startDate: Long, endDate: Long, onResult: (List<PuttSession>) -> Unit) {
        val db = PuttDatabase.getDatabase(getApplication())
        viewModelScope.launch {
            val sessions = db.puttSessionDao().getSessionsByDateRange(startDate, endDate)
            onResult(sessions)
        }
    }

    // New: Aggregate hit rate per distance for a given date range (1-30m)
    fun getHitRatePerDistance(sessions: List<PuttSession>): List<Pair<Int, Float>> {
        val result = mutableListOf<Pair<Int, Float>>()
        for (distance in 1..30) {
            val filtered = sessions.filter { it.distance == distance }
            val totalPutts = filtered.sumOf { it.numPutts }
            val totalMade = filtered.sumOf { it.madePutts }
            val hitRate = if (totalPutts > 0) (totalMade * 100f / totalPutts) else 0f
            result.add(distance to hitRate)
        }
        return result
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
        (3..15).map { d ->
            val s = lastSessionsForPuttRange(d, minPutts, maxPutts)
            if (s.isEmpty()) {
                // No data for this distance, count as 0%
                DistanceStats(d, minPutts, 0, 0.0)
            } else {
                val made  = s.sumOf { it.madePutts }
                val tries = s.sumOf { it.numPutts }
                DistanceStats(d, tries, made, made.toDouble() / tries)
            }
        }

    suspend fun getLastSession(distance: Int, style: String): PuttSession? {
        val db = PuttDatabase.getDatabase(getApplication())
        val all = db.puttSessionDao().getSessionsByDistance(distance)
        return all.filter { it.style == style }.maxByOrNull { it.date }
    }

    suspend fun getAverageHitRate(distance: Int, style: String): Double? {
        val db = PuttDatabase.getDatabase(getApplication())
        val all = db.puttSessionDao().getSessionsByDistance(distance).filter { it.style == style }
        val totalPutts = all.sumOf { it.numPutts }
        val totalMade = all.sumOf { it.madePutts }
        return if (totalPutts > 0) totalMade.toDouble() / totalPutts else null
    }

    fun deleteSession(session: PuttSession) {
        val db = PuttDatabase.getDatabase(getApplication())
        viewModelScope.launch {
            db.puttSessionDao().deleteSessionById(session.id)
            loadSessionsForDistance(session.distance)
            loadPuttingRating()
            loadAverageHitRate(session.distance)
        }
    }
} 