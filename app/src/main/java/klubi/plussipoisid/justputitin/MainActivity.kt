package klubi.plussipoisid.justputitin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import klubi.plussipoisid.justputitin.ui.TrendsScreen
import android.media.MediaPlayer
import androidx.compose.ui.platform.LocalContext

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
    val viewModel: SessionViewModel = viewModel()
    val currentScreen = remember { mutableStateOf("main_menu") }
    NavHost(
        navController = navController,
        startDestination = "main_menu",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("main_menu") {
            currentScreen.value = "main_menu"
            Surface(color = MaterialTheme.colorScheme.background, tonalElevation = 0.dp) {
                AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                    MainMenuScreen(
                        onStartSession = { navController.navigate("session_setup") },
                        onCheckStats = { navController.navigate("statistics") },
                        onTrends = { navController.navigate("trends") }
                    )
                }
            }
        }
        composable("session_setup") {
            currentScreen.value = "session_setup"
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, tonalElevation = 4.dp) {
                AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                    SessionSetupScreen(onStartSession = { distance, numPutts, style ->
                        navController.navigate("result_entry/$distance/$numPutts/$style")
                    })
                }
            }
        }
        composable("statistics") {
            currentScreen.value = "statistics"
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, tonalElevation = 2.dp) {
                AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                    StatisticsScreen()
                }
            }
        }
        composable("trends") {
            currentScreen.value = "trends"
            Surface(color = MaterialTheme.colorScheme.background, tonalElevation = 0.dp) {
                AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                    TrendsScreen()
                }
            }
        }
        composable("result_entry/{distance}/{numPutts}/{style}") { backStackEntry ->
            currentScreen.value = "result_entry"
            val distance = backStackEntry.arguments?.getString("distance")?.toIntOrNull() ?: 0
            val numPutts = backStackEntry.arguments?.getString("numPutts")?.toIntOrNull() ?: 0
            val style = backStackEntry.arguments?.getString("style") ?: viewModel.styles[0]
            Surface(color = MaterialTheme.colorScheme.primaryContainer, tonalElevation = 8.dp) {
                AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                    ResultEntryScreen(distance = distance, numPutts = numPutts,
                        onRepeat = { navController.navigate("result_entry/$distance/$numPutts/$style") },
                        onAdjust = { navController.popBackStack("session_setup", inclusive = false) },
                        navController = navController,
                        style = style
                    )
                }
            }
        }
    }
}

@Composable
fun MainMenuScreen(onStartSession: () -> Unit, onCheckStats: () -> Unit, onTrends: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Just put it in!", modifier = Modifier.padding(bottom = 32.dp))
        Button(onClick = onStartSession, modifier = Modifier.fillMaxWidth()) {
            Text("New session")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onCheckStats, modifier = Modifier.fillMaxWidth()) {
            Text("History")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onTrends, modifier = Modifier.fillMaxWidth()) {
            Text("Line Trends")
        }
    }
}

@Composable
fun StatisticsScreen() {
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

    LaunchedEffect(Unit) {
        viewModel.loadDistances()
    }
    LaunchedEffect(selectedDistance.value) {
        selectedDistance.value?.let { viewModel.loadStylesForDistance(it) }
    }
    LaunchedEffect(selectedDistance.value, selectedStyle.value, selectedRange.value) {
        selectedDistance.value?.let { viewModel.loadSessionsForDistanceAndStyle(it, if (selectedStyle.value == "All") null else selectedStyle.value, selectedRange.value) }
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
                                selectedStyle.value = "All"
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
                                expandedRange.value = false
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (sessions.isEmpty() && distances.isNotEmpty()) {
            Text("No sessions found for this distance, style, and range.")
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
                    Text("Style: ${lastSession.style}")
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
                                Text("Style: ${session.style}")
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
fun ResultEntryScreen(distance: Int, numPutts: Int, onRepeat: () -> Unit, onAdjust: () -> Unit, navController: androidx.navigation.NavHostController, style: String) {
    val viewModel: SessionViewModel = viewModel()
    var successful = remember(numPutts) { mutableStateOf(numPutts) }
    val hitRate = if (numPutts > 0) (successful.value * 100 / numPutts) else 0
    var saved = remember { mutableStateOf(false) }
    val selectedStyle = remember { mutableStateOf(style) }
    val context = LocalContext.current
    val soundOn by viewModel.soundOn.collectAsState()

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
                    text = "Enter Session Results",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
                Text("Distance: $distance meters", style = MaterialTheme.typography.bodyLarge)
                Text("Throws: $numPutts", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Style: " + selectedStyle.value, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Successful Putts", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                NumberPickerRow(
                    range = 0..numPutts,
                    selected = successful.value,
                    onSelected = { successful.value = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Hit Rate: $hitRate%", modifier = Modifier.padding(bottom = 24.dp), style = MaterialTheme.typography.bodyLarge)
                // Animated Save Button
                val buttonColor by animateColorAsState(
                    targetValue = if (saved.value) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    animationSpec = tween(durationMillis = 400), label = ""
                )
                val shadowColor by animateColorAsState(
                    targetValue = if (saved.value) Color(0x804CAF50) else Color.Transparent,
                    animationSpec = tween(durationMillis = 400), label = ""
                )
                Box(contentAlignment = Alignment.Center) {
                    Button(
                        onClick = {
                            val puttsMade = successful.value
                            viewModel.saveSession(distance, numPutts, puttsMade, selectedStyle.value)
                            saved.value = true
                            if (puttsMade == numPutts && numPutts > 0 && soundOn) {
                                playKawaiiSound(context)
                            }
                            successful.value = numPutts
                        },
                        enabled = successful.value in 0..numPutts && !saved.value,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        modifier = Modifier
                            .width(120.dp)
                            .height(56.dp)
                            .shadow(
                                elevation = if (saved.value) 24.dp else 4.dp,
                                shape = CircleShape,
                                ambientColor = shadowColor,
                                spotColor = shadowColor
                            )
                    ) {
                        Crossfade(targetState = saved.value, label = "") { isSaved ->
                            if (isSaved) {
                                Icon(Icons.Default.Check, contentDescription = "Saved", tint = Color.White)
                            } else {
                                Text("Save", color = Color.White)
                            }
                        }
                    }
                }
                if (saved.value) {
                    // Reset saved state after a short delay or on next input
                    LaunchedEffect(saved.value) {
                        if (saved.value) {
                            kotlinx.coroutines.delay(1200)
                            saved.value = false
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Sound effects toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sound effects", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.material3.Switch(
                        checked = soundOn,
                        onCheckedChange = { viewModel.setSoundOn(it) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onAdjust) {
                    Text("Adjust Session Settings")
                }
            }
        }
    }
}

fun playKawaiiSound(context: android.content.Context) {
    try {
        val mediaPlayer = MediaPlayer.create(context, R.raw.kawaii)
        mediaPlayer?.setOnCompletionListener { mp ->
            mp.release()
        }
        mediaPlayer?.start()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun SessionSetupScreen(onStartSession: (Int, Int, String) -> Unit) {
    val viewModel: SessionViewModel = viewModel()
    val distance = viewModel.distance.collectAsState().value
    val numPutts = viewModel.numPutts.collectAsState().value
    val styles = viewModel.styles
    val selectedStyle = viewModel.selectedStyle.collectAsState().value
    val expandedStyle = remember { mutableStateOf(false) }

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
                    text = "Setup Putting Session",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
                Text("Select Distance (meters)", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                NumberPickerRow(
                    range = 1..30,
                    selected = if (distance in 1..30) distance else null,
                    onSelected = { viewModel.setDistance(it) }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text("Select Number of Putts", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                NumberPickerRow(
                    range = 1..20,
                    selected = if (numPutts in 1..20) numPutts else null,
                    onSelected = { viewModel.setNumPutts(it) }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text("Select Putting Style", style = MaterialTheme.typography.bodyLarge)
                Box {
                    Button(
                        onClick = { expandedStyle.value = true },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Text(selectedStyle)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Pick Style")
                    }
                    DropdownMenu(
                        expanded = expandedStyle.value,
                        onDismissRequest = { expandedStyle.value = false },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        styles.forEach { style ->
                            DropdownMenuItem(
                                text = { Text(style) },
                                onClick = {
                                    viewModel.setStyle(style)
                                    expandedStyle.value = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { viewModel.startSession { d, n -> onStartSession(d, n, selectedStyle) } },
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
fun NumberPickerRow(range: IntRange, selected: Int?, onSelected: (Int) -> Unit) {
    val itemSize = 56.dp
    val selectedItemSize = 72.dp
    val contentPadding = (itemSize / 2)
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll to selected on first show and whenever selected changes
    LaunchedEffect(selected) {
        if (selected != null) {
            val idx = range.indexOf(selected)
            if (idx >= 0) {
                coroutineScope.launch {
                    listState.animateScrollToItem(idx)
                }
            }
        }
    }

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = contentPadding),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        items(range.toList()) { value ->
            val isSelected = selected != null && value == selected
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