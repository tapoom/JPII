package klubi.plussipoisid.justputitin.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PuttSessionDao {
    @Insert
    suspend fun insertSession(session: PuttSession)

    @Query("SELECT * FROM putt_sessions ORDER BY date DESC")
    suspend fun getAllSessions(): List<PuttSession>

    @Query("SELECT * FROM putt_sessions WHERE distance = :distance ORDER BY date DESC")
    suspend fun getSessionsByDistance(distance: Int): List<PuttSession>

    @Query("SELECT * FROM putt_sessions WHERE distance = :distance ORDER BY date DESC LIMIT 10")
    suspend fun getAtLeastTenSessionsByDistance(distance: Int): List<PuttSession>
    @Query("""
    SELECT * FROM putt_sessions 
    WHERE distance = :d 
    ORDER BY date DESC
""")
    suspend fun allSessionsAt(d: Int): List<PuttSession>
} 