package klubi.plussipoisid.justputitin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_runs")
data class TrainingRun(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long = System.currentTimeMillis(),
    val minDistance: Int,
    val maxDistance: Int,
    val maxPuttsPerDistance: Int,
    val totalPutts: Int,
    val completedPutts: Int = 0,
    val madePutts: Int = 0,
    val style: String = "",
    val trackingMode: String = "Per Putt",
    val isCompleted: Boolean = false
)

data class TrainingStep(
    val distance: Int,
    val numPutts: Int,
    val completedPutts: Int = 0,
    val madePutts: Int = 0
) 