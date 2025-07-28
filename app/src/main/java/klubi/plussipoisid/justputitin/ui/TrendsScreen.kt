package klubi.plussipoisid.justputitin.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import java.util.Calendar
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.toArgb

@Composable
fun TrendsScreen() {
    val viewModel: SessionViewModel = viewModel()
    val stylesForDistance = viewModel.stylesForDistance.collectAsState().value
    val sessions = viewModel.sessions.collectAsState().value
    val historyOptions = listOf("Last week", "Last month", "Last year", "All time")
    val expandedRange = remember { mutableStateOf(false) }
    // Remove distance picker state and effect
    // val selectedDistance = remember { mutableStateOf<Int?>(null) }
    // var previousDistance by remember { mutableStateOf<Int?>(null) }
    // LaunchedEffect(selectedDistance.value) {
    //     if (selectedDistance.value != null && selectedDistance.value != previousDistance) {
    //         selectedStyle.value = "All"
    //         previousDistance = selectedDistance.value
    //     }
    // }
    val selectedRange = remember { mutableStateOf(historyOptions[0]) }
    val selectedStyle = remember { mutableStateOf("All") }
    val expandedStyle = remember { mutableStateOf(false) }

    val customRange = remember { mutableStateOf<Pair<Long, Long>?>(null) }
    val showDatePicker = remember { mutableStateOf(false) }
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
                if (isStart) {
                    cal.set(year, month, day, 0, 0, 0)
                } else {
                    cal.set(year, month, day, 23, 59, 59)
                }
                val time = cal.timeInMillis
                val (start, end) = customRange.value ?: (time to time)
                customRange.value = if (isStart) time to end else start to time
            },
            now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    LaunchedEffect(Unit) {
        viewModel.loadAllStyles()
    }
    LaunchedEffect(selectedRange.value) {
        // selectedDistance.value?.let { viewModel.loadStylesForDistance(it) } // Removed distance picker
    }
    // Only load sessions when all filters are selected
    LaunchedEffect(selectedRange.value) {
        // selectedDistance.value?.let { viewModel.loadSessionsForDistanceAndStyle(it, if (selectedStyle.value == "All") null else selectedStyle.value, selectedRange.value) } // Removed distance picker
        if (selectedRange.value != null) {
            viewModel.loadSessionsByDateRange(
                if (customRange.value != null) customRange.value!!.first else 0L,
                if (customRange.value != null) customRange.value!!.second else System.currentTimeMillis()
            ) { sessions ->
                sessionsForRange.value = sessions
                hitRates.value = viewModel.getHitRatePerDistance(sessions)
            }
        }
    }

    // Remove these unused variables that cause @Composable invocation errors
    // val entries = sessions.sortedBy { it.date }.mapIndexed { idx, session ->
    //     val hitRate = if (session.numPutts > 0) (session.madePutts * 100f / session.numPutts) else 0f
    //     Entry(idx.toFloat(), hitRate)
    // }
    // val dates = sessions.sortedBy { it.date }.map { session ->
    //     java.text.SimpleDateFormat("MM-dd", java.util.Locale.getDefault()).format(java.util.Date(session.date))
    // }
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
        if (/*distances.isEmpty()*/ false) {
            Text("No recorded distances yet. Complete a session to see trends.", color = MaterialTheme.colorScheme.error)
        } else {
            // Distance picker removed
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
                        text = { Text("Custom range") },
                        onClick = {
                            selectedRange.value = "Custom range"
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
            if (stylesForDistance.isNotEmpty()) {
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
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Data loading for chart
        LaunchedEffect(selectedRange.value, customRange.value, selectedStyle.value) {
            val styleFilter = if (selectedStyle.value == "All") null else selectedStyle.value
            if (customRange.value != null) {
                viewModel.loadSessionsByDateRange(customRange.value!!.first, customRange.value!!.second) { sessions ->
                    val filtered = if (styleFilter == null) sessions else sessions.filter { it.style == styleFilter }
                    sessionsForRange.value = filtered
                    hitRates.value = viewModel.getHitRatePerDistance(filtered)
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
                    val filtered = if (styleFilter == null) sessions else sessions.filter { it.style == styleFilter }
                    sessionsForRange.value = filtered
                    hitRates.value = viewModel.getHitRatePerDistance(filtered)
                }
            }
        }
        if (hitRates.value.isEmpty()) {
            Text("No sessions found for this period.")
        } else if (hitRates.value.isNotEmpty()) {
            val primary = MaterialTheme.colorScheme.primary.toArgb()
            val secondary = MaterialTheme.colorScheme.secondary.toArgb()
            val background = MaterialTheme.colorScheme.surface.toArgb()
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    val entries = hitRates.value.map { (distance, hitRate) ->
                        Entry(distance.toFloat(), hitRate)
                    }
                    AndroidView(
                        factory = { ctx ->
                            val chart = LineChart(ctx)
                            val dataSet = LineDataSet(entries, "Hit Rate % by Distance")
                            dataSet.color = primary
                            dataSet.valueTextColor = secondary
                            dataSet.setDrawCircles(true)
                            dataSet.setCircleColor(primary)
                            dataSet.setDrawValues(true)
                            dataSet.lineWidth = 3f
                            dataSet.circleRadius = 5f
                            dataSet.setDrawFilled(true)
                            dataSet.fillAlpha = 120
                            dataSet.fillColor = primary
                            dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                            val lineData = LineData(dataSet)
                            chart.data = lineData
                            chart.axisLeft.axisMinimum = 0f
                            chart.axisLeft.axisMaximum = 100f
                            chart.axisLeft.textColor = secondary
                            chart.axisLeft.textSize = 14f
                            chart.axisLeft.gridColor = secondary
                            chart.axisRight.isEnabled = false
                            chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                            chart.xAxis.granularity = 1f
                            chart.xAxis.textColor = secondary
                            chart.xAxis.textSize = 14f
                            chart.xAxis.gridColor = secondary
                            chart.xAxis.valueFormatter = object : ValueFormatter() {
                                override fun getFormattedValue(value: Float): String {
                                    return value.toInt().toString() + "m"
                                }
                            }
                            chart.setBackgroundColor(background)
                            chart.description.isEnabled = false
                            chart.legend.isEnabled = false
                            chart.setDrawGridBackground(false)
                            chart.setPadding(16, 16, 16, 16)
                            chart.setExtraOffsets(8f, 8f, 8f, 8f)
                            chart.invalidate()
                            chart
                        },
                        update = { chart ->
                            val dataSet = LineDataSet(entries, "Hit Rate % by Distance")
                            dataSet.color = primary
                            dataSet.valueTextColor = secondary
                            dataSet.setDrawCircles(true)
                            dataSet.setCircleColor(primary)
                            dataSet.setDrawValues(true)
                            dataSet.lineWidth = 3f
                            dataSet.circleRadius = 5f
                            dataSet.setDrawFilled(true)
                            dataSet.fillAlpha = 120
                            dataSet.fillColor = primary
                            dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                            val lineData = LineData(dataSet)
                            chart.data = lineData
                            chart.axisLeft.textColor = secondary
                            chart.axisLeft.textSize = 14f
                            chart.axisLeft.gridColor = secondary
                            chart.xAxis.textColor = secondary
                            chart.xAxis.textSize = 14f
                            chart.xAxis.gridColor = secondary
                            chart.xAxis.valueFormatter = object : ValueFormatter() {
                                override fun getFormattedValue(value: Float): String {
                                    return value.toInt().toString() + "m"
                                }
                            }
                            chart.setBackgroundColor(background)
                            chart.invalidate()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }
            }
        }
    }
} 