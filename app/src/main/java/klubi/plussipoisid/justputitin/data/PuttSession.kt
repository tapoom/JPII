package klubi.plussipoisid.justputitin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "putt_sessions")
data class PuttSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long = System.currentTimeMillis(),
    val distance: Int,
    val numPutts: Int,
    val madePutts: Int,
    val missedPutts: Int,
    val style: String = ""
) 