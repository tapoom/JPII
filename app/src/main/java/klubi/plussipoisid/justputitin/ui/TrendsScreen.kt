package klubi.plussipoisid.justputitin.ui

import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import klubi.plussipoisid.justputitin.SessionViewModel
import android.app.DatePickerDialog
import java.util.Calendar
import androidx.compose.ui.platform.LocalContext

@Composable
fun TrendsScreen() {
    val viewModel: SessionViewModel = viewModel()
    val distances = viewModel.distances.collectAsState().value
    val stylesForDistance = viewModel.stylesForDistance.collectAsState().value
    val sessions = viewModel.sessions.collectAsState().value
    val historyOptions = listOf("Last week", "Last month", "Last year", "All time")
    val expandedDistance = remember { mutableStateOf(false) }
    val expandedRange = remember { mutableStateOf(false) }
    val expandedStyle = remember { mutableStateOf(false) }
    val selectedDistance = remember { mutableStateOf<Int?>(null) }
    val selectedRange = remember { mutableStateOf(historyOptions[0]) }
    val selectedStyle = remember { mutableStateOf("All") }

    // Track previous distance to only reset style when distance actually changes
    var previousDistance by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(selectedDistance.value) {
        if (selectedDistance.value != null && selectedDistance.value != previousDistance) {
            selectedStyle.value = "All"
            previousDistance = selectedDistance.value
        }
    }

    val customRange = remember { mutableStateOf<Pair<Long, Long>?>(null) }
    val showDatePicker = remember { mutableStateOf(false) }
    val isPickingStart = remember { mutableStateOf(true) }
    val calendar = Calendar.getInstance()
    val context = LocalContext.current
    val sessionsForRange = remember { mutableStateOf<List<klubi.plussipoisid.justputitin.data.PuttSession>>(emptyList()) }
    val hitRates = remember { mutableStateOf<List<Pair<Int, Float>>>(emptyList()) }

    // Date picker dialog logic
    fun showPicker(isStart: Boolean) {
        val now = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day, 0, 0, 0)
                val time = cal.timeInMillis
                if (isStart) {
                    customRange.value = time to (customRange.value?.second ?: time)
                } else {
                    customRange.value = (customRange.value?.first ?: time) to time
                }
            },
            now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    LaunchedEffect(Unit) {
        viewModel.loadDistances()
    }
    LaunchedEffect(selectedDistance.value) {
        selectedDistance.value?.let { viewModel.loadStylesForDistance(it) }
    }
    // Remove default selectedDistance logic
    // Only load sessions when all filters are selected
    LaunchedEffect(selectedDistance.value, selectedStyle.value, selectedRange.value) {
        if (selectedDistance.value != null) {
            viewModel.loadSessionsForDistanceAndStyle(
                selectedDistance.value!!,
                if (selectedStyle.value == "All") null else selectedStyle.value,
                selectedRange.value
            )
        }
    }

    val entries = sessions.sortedBy { it.date }.mapIndexed { idx, session ->
        val hitRate = if (session.numPutts > 0) (session.madePutts * 100f / session.numPutts) else 0f
        Entry(idx.toFloat(), hitRate)
    }
    val dates = sessions.sortedBy { it.date }.map { session ->
        java.text.SimpleDateFormat("MM-dd", java.util.Locale.getDefault()).format(java.util.Date(session.date))
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Hit Rate Trends", style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), modifier = Modifier.padding(bottom = 16.dp))
        if (distances.isEmpty()) {
            Text("No recorded distances yet. Complete a session to see trends.", color = MaterialTheme.colorScheme.error)
        } else {
            Box {
                Button(
                    onClick = { expandedDistance.value = true },
                    enabled = distances.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text(selectedDistance.value?.toString() ?: "Pick Distance")
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pick Distance")
                }
                DropdownMenu(
                    expanded = expandedDistance.value,
                    onDismissRequest = { expandedDistance.value = false },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    distances.forEach { d ->
                        DropdownMenuItem(
                            text = { Text(d.toString()) },
                            onClick = {
                                selectedDistance.value = d
                                expandedDistance.value = false
                                // Style reset handled by LaunchedEffect
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box {
                Button(
                    onClick = { expandedStyle.value = true },
                    enabled = stylesForDistance.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text(selectedStyle.value)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pick Style")
                }
                DropdownMenu(
                    expanded = expandedStyle.value,
                    onDismissRequest = { expandedStyle.value = false },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    DropdownMenuItem(
                        text = { Text("All") },
                        onClick = {
                            selectedStyle.value = "All"
                            expandedStyle.value = false
                        }
                    )
                    stylesForDistance.forEach { style ->
                        DropdownMenuItem(
                            text = { Text(style) },
                            onClick = {
                                selectedStyle.value = style
                                expandedStyle.value = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box {
                Button(
                    onClick = { expandedRange.value = true },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text(selectedRange.value)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pick Range")
                }
                DropdownMenu(
                    expanded = expandedRange.value,
                    onDismissRequest = { expandedRange.value = false },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    historyOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedRange.value = option
                                customRange.value = null
                                expandedRange.value = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Custom Range") },
                        onClick = {
                            expandedRange.value = false
                            showDatePicker.value = true
                        }
                    )
                }
            }
            if (showDatePicker.value) {
                // Show two date pickers for start and end
                LaunchedEffect(Unit) {
                    showPicker(true)
                    showPicker(false)
                    showDatePicker.value = false
                }
            }
            if (customRange.value != null) {
                val start = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date(customRange.value!!.first))
                val end = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date(customRange.value!!.second))
                Text("Custom: $start to $end", style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Data loading for chart
        LaunchedEffect(selectedRange.value, customRange.value) {
            if (customRange.value != null) {
                viewModel.loadSessionsByDateRange(customRange.value!!.first, customRange.value!!.second) { sessions ->
                    sessionsForRange.value = sessions
                    hitRates.value = viewModel.getHitRatePerDistance(sessions)
                }
            } else {
                // Use default periods
                val now = System.currentTimeMillis()
                val (start, end) = when (selectedRange.value) {
                    "Last week" -> now - 7 * 24 * 60 * 60 * 1000L to now
                    "Last month" -> now - 30 * 24 * 60 * 60 * 1000L to now
                    "Last year" -> now - 365 * 24 * 60 * 60 * 1000L to now
                    else -> 0L to now
                }
                viewModel.loadSessionsByDateRange(start, end) { sessions ->
                    sessionsForRange.value = sessions
                    hitRates.value = viewModel.getHitRatePerDistance(sessions)
                }
            }
        }
        if (hitRates.value.isEmpty() && distances.isNotEmpty()) {
            Text("No sessions found for this period.")
        } else if (hitRates.value.isNotEmpty()) {
            // Show line chart: x = distance, y = hit rate
            val entries = hitRates.value.map { (distance, hitRate) ->
                Entry(distance.toFloat(), hitRate)
            }
            AndroidView(
                factory = { ctx ->
                    val chart = LineChart(ctx)
                    val dataSet = LineDataSet(entries, "Hit Rate % by Distance")
                    dataSet.color = android.graphics.Color.BLUE
                    dataSet.valueTextColor = android.graphics.Color.BLACK
                    dataSet.setDrawCircles(true)
                    dataSet.setDrawValues(true)
                    dataSet.lineWidth = 2f
                    dataSet.circleRadius = 4f
                    dataSet.setDrawFilled(true)
                    dataSet.fillAlpha = 50
                    val lineData = LineData(dataSet)
                    chart.data = lineData
                    chart.axisLeft.axisMinimum = 0f
                    chart.axisLeft.axisMaximum = 100f
                    chart.axisRight.isEnabled = false
                    chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                    chart.xAxis.granularity = 1f
                    chart.xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return value.toInt().toString() + "m"
                        }
                    }
                    chart.description.isEnabled = false
                    chart.legend.isEnabled = true
                    chart.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 600)
                    chart.invalidate()
                    chart
                },
                update = { chart ->
                    val dataSet = LineDataSet(entries, "Hit Rate % by Distance")
                    dataSet.color = android.graphics.Color.BLUE
                    dataSet.valueTextColor = android.graphics.Color.BLACK
                    dataSet.setDrawCircles(true)
                    dataSet.setDrawValues(true)
                    dataSet.lineWidth = 2f
                    dataSet.circleRadius = 4f
                    dataSet.setDrawFilled(true)
                    dataSet.fillAlpha = 50
                    val lineData = LineData(dataSet)
                    chart.data = lineData
                    chart.xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return value.toInt().toString() + "m"
                        }
                    }
                    chart.invalidate()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        }
    }
} 