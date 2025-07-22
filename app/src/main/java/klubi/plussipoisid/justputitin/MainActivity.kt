package klubi.plussipoisid.justputitin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import klubi.plussipoisid.justputitin.data.PuttSession
import klubi.plussipoisid.justputitin.ui.theme.JPIITheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.Brush

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JPIITheme {
                AppNavHost()
            }
        }
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "main_menu",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("main_menu") {
                MainMenuScreen(
                    onStartSession = { navController.navigate("session_setup") },
                    onCheckStats = { navController.navigate("statistics") }
                )
            }
            composable("session_setup") {
                SessionSetupScreen(onStartSession = { distance, numPutts ->
                    navController.navigate("result_entry/$distance/$numPutts")
                })
            }
            composable("statistics") {
                // TODO: Implement statistics screen
                StatisticsScreen()
            }
            composable("result_entry/{distance}/{numPutts}") { backStackEntry ->
                val distance = backStackEntry.arguments?.getString("distance")?.toIntOrNull() ?: 0
                val numPutts = backStackEntry.arguments?.getString("numPutts")?.toIntOrNull() ?: 0
                ResultEntryScreen(distance = distance, numPutts = numPutts,
                    onRepeat = { navController.navigate("result_entry/$distance/$numPutts") },
                    onAdjust = { navController.popBackStack("session_setup", inclusive = false) },
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun MainMenuScreen(onStartSession: () -> Unit, onCheckStats: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Disc Golf Putting Tracker", modifier = Modifier.padding(bottom = 32.dp))
        Button(onClick = onStartSession, modifier = Modifier.fillMaxWidth()) {
            Text("Start New Putting Session")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onCheckStats, modifier = Modifier.fillMaxWidth()) {
            Text("Check Historical Statistics")
        }
    }
}

@Composable
fun StatisticsScreen() {
    val viewModel: SessionViewModel = viewModel()
    val distances = viewModel.distances.collectAsState().value
    val sessions = viewModel.sessions.collectAsState().value
    val historyOptions = listOf("Last week", "Last month", "Last year", "All time")
    val expandedDistance = remember { mutableStateOf(false) }
    val expandedRange = remember { mutableStateOf(false) }
    val selectedDistance = remember { mutableStateOf<Int?>(null) }
    val selectedRange = remember { mutableStateOf(historyOptions[0]) }

    LaunchedEffect(Unit) {
        viewModel.loadDistances()
    }
    LaunchedEffect(selectedDistance.value, selectedRange.value) {
        selectedDistance.value?.let { viewModel.loadSessionsForDistanceAndRange(it, selectedRange.value) }
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
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Statistics", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 16.dp))
        if (distances.isEmpty()) {
            Text("No recorded distances yet. Complete a session to see stats.", color = MaterialTheme.colorScheme.error)
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
                                expandedRange.value = false
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (sessions.isEmpty() && distances.isNotEmpty()) {
            Text("No sessions found for this distance and range.")
        } else if (sessions.isNotEmpty()) {
            val lastSession = sessions.first()
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(lastSession.date))
            val hitRate = if (lastSession.numPutts > 0) (lastSession.madePutts * 100 / lastSession.numPutts) else 0
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Last Session", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("$date", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Throws: ${lastSession.numPutts}")
                    Text("Hits: ${lastSession.madePutts}")
                    Text("Hit Rate: $hitRate%", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            BarChart(sessions = sessions)
            Spacer(modifier = Modifier.height(24.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions.drop(1)) { session ->
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(session.date))
                    val hitRate = if (session.numPutts > 0) (session.madePutts * 100 / session.numPutts) else 0
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("$date", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Throws: ${session.numPutts}")
                                Text("Hits: ${session.madePutts}")
                            }
                            Text("$hitRate%", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.align(Alignment.CenterVertically))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BarChart(sessions: List<PuttSession>) {
    val maxRate = (sessions.maxOfOrNull { if (it.numPutts > 0) (it.madePutts * 100 / it.numPutts) else 0 } ?: 100).coerceAtLeast(100)
    val barWidth = 32.dp
    val chartHeight = 160.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(chartHeight)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        sessions.forEach { session ->
            val hitRate = if (session.numPutts > 0) (session.madePutts * 100 / session.numPutts) else 0
            val barColor = if (hitRate >= 70) MaterialTheme.colorScheme.primary else if (hitRate >= 40) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
            Box(
                modifier = Modifier
                    .height((chartHeight * hitRate / maxRate).coerceAtLeast(8.dp))
                    .width(barWidth)
                    .background(barColor, RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text("$hitRate%", color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

@Composable
fun ResultEntryScreen(distance: Int, numPutts: Int, onRepeat: () -> Unit, onAdjust: () -> Unit, navController: androidx.navigation.NavHostController) {
    val viewModel: SessionViewModel = viewModel()
    var successful = remember { mutableStateOf(0) }
    val hitRate = if (numPutts > 0) (successful.value * 100 / numPutts) else 0
    var saved = remember { mutableStateOf(false) }

    // Intercept system back and go to main menu
    BackHandler {
        navController.popBackStack("main_menu", inclusive = false)
        navController.navigate("main_menu")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "\uD83C\uDFBE Enter Session Results",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
                Text("Distance: $distance meters", style = MaterialTheme.typography.bodyLarge)
                Text("Throws: $numPutts", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Successful Putts", style = MaterialTheme.typography.bodyLarge)
                NumberPickerRow(
                    range = 0..numPutts,
                    selected = successful.value,
                    onSelected = { successful.value = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Hit Rate: $hitRate%", modifier = Modifier.padding(bottom = 24.dp), style = MaterialTheme.typography.bodyLarge)
                Button(
                    onClick = {
                        viewModel.saveSession(distance, numPutts, successful.value)
                        saved.value = true
                    },
                    enabled = successful.value in 0..numPutts && !saved.value,
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Save Results")
                }
                if (saved.value) {
                    Text("Session saved!", modifier = Modifier.padding(top = 16.dp))
                    Button(onClick = onRepeat, modifier = Modifier.padding(top = 16.dp)) {
                        Text("Repeat Session")
                    }
                    Button(onClick = onAdjust, modifier = Modifier.padding(top = 16.dp)) {
                        Text("Adjust Session Settings")
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRepeat, enabled = false) {
                        Text("Repeat Session")
                    }
                    Button(onClick = onAdjust, enabled = false) {
                        Text("Adjust Session Settings")
                    }
                }
            }
        }
    }
}

@Composable
fun SessionSetupScreen(onStartSession: (Int, Int) -> Unit) {
    val viewModel: SessionViewModel = viewModel()
    val distance = viewModel.distance.collectAsState().value
    val numPutts = viewModel.numPutts.collectAsState().value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "\uD83C\uDFBE New Putting Session",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
                Text("Select Distance (meters)", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                NumberPickerRow(
                    range = 1..30,
                    selected = distance.takeIf { it in 1..30 } ?: 4,
                    onSelected = { viewModel.setDistance(it) }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text("Select Number of Putts", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                NumberPickerRow(
                    range = 1..20,
                    selected = numPutts.takeIf { it in 1..20 } ?: 10,
                    onSelected = { viewModel.setNumPutts(it) }
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { viewModel.startSession(onStartSession) },
                    enabled = distance in 1..30 && numPutts in 1..20,
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Start Session")
                }
            }
        }
    }
}

@Composable
fun NumberPickerRow(range: IntRange, selected: Int, onSelected: (Int) -> Unit) {
    val itemSize = 56.dp
    val selectedItemSize = 72.dp
    val visibleItems = 4 // Number of items to show at once (including partials)
    val contentPadding = (itemSize / 2)
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = contentPadding),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        items(range.toList()) { value ->
            val isSelected = value == selected
            val animatedColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                animationSpec = tween(durationMillis = 300), label = ""
            )
            val animatedElevation by animateDpAsState(
                targetValue = if (isSelected) 16.dp else 2.dp,
                animationSpec = tween(durationMillis = 300), label = ""
            )
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = animatedColor),
                elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation),
                modifier = Modifier
                    .size(if (isSelected) selectedItemSize else itemSize)
                    .shadow(if (isSelected) 12.dp else 2.dp, CircleShape)
                    .clickable { onSelected(value) }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = value.toString(),
                        style = if (isSelected) MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun PuttTrackingScreen(distance: Int, numPutts: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Putt Tracking Screen", modifier = Modifier.padding(bottom = 24.dp))
        Text("Distance: $distance meters")
        Text("Number of Putts: $numPutts")
        // TODO: Add putt tracking UI (made/missed buttons, progress, etc.)
    }
}