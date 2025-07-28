package klubi.plussipoisid.justputitin.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PuttSessionDao {
    @Insert
    suspend fun insertSession(session: PuttSession)

    @Query("SELECT * FROM putt_sessions ORDER BY date DESC")
    suspend fun getAllSessions(): List<PuttSession>

    @Query("SELECT * FROM putt_sessions WHERE distance = :distance ORDER BY date DESC")
    suspend fun getSessionsByDistance(distance: Int): List<PuttSession>

    @Query("SELECT * FROM putt_sessions WHERE distance = :distance AND style = :style ORDER BY date DESC")
    suspend fun getSessionsByDistanceAndStyle(distance: Int, style: String): List<PuttSession>

    @Query("SELECT * FROM putt_sessions WHERE distance = :distance ORDER BY date DESC LIMIT 10")
    suspend fun getAtLeastTenSessionsByDistance(distance: Int): List<PuttSession>

    @Query("""
    SELECT * FROM putt_sessions 
    WHERE distance = :d 
    ORDER BY date DESC
    """)
    suspend fun allSessionsAt(d: Int): List<PuttSession>

    @Query("SELECT * FROM putt_sessions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getSessionsByDateRange(startDate: Long, endDate: Long): List<PuttSession>

    @Query("DELETE FROM putt_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Int)

    // Training Run methods
    @Insert
    suspend fun insertTrainingRun(trainingRun: TrainingRun)

    @Update
    suspend fun updateTrainingRun(trainingRun: TrainingRun)

    @Query("SELECT * FROM training_runs ORDER BY date DESC")
    suspend fun getAllTrainingRuns(): List<TrainingRun>

    @Query("SELECT * FROM training_runs WHERE id = :runId")
    suspend fun getTrainingRunById(runId: Int): TrainingRun?

    @Query("DELETE FROM training_runs WHERE id = :runId")
    suspend fun deleteTrainingRunById(runId: Int)
} 