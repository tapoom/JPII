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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import klubi.plussipoisid.justputitin.ui.MainMenuScreen
import klubi.plussipoisid.justputitin.ui.TrainingSetupScreen
import klubi.plussipoisid.justputitin.ui.TrainingSessionScreen
import klubi.plussipoisid.justputitin.ui.NumberPickerRow
import android.media.MediaPlayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.withContext
import klubi.plussipoisid.justputitin.data.TrainingRun
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.RadioButton

@Composable
fun FadingAppNavHost() {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 2000)
    )
    LaunchedEffect(Unit) {
        visible = true
    }
    Box(modifier = Modifier.fillMaxSize().alpha(alpha)) {
        AppNavHost()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JPIITheme(dynamicColor = false) {
                FadingAppNavHost()
            }
        }
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val viewModel: SessionViewModel = viewModel()
    val currentScreen = remember { mutableStateOf("main_menu") }
    
    // Debug logging for ViewModel instance
    LaunchedEffect(Unit) {
        android.util.Log.d("AppNavHost", "ViewModel instance: $viewModel")
    }
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
                        viewModel = viewModel,
                        onStartSession = { navController.navigate("session_setup") },
                        onStartTraining = { navController.navigate("training_setup") },
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
        composable("training_setup") {
            currentScreen.value = "training_setup"
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, tonalElevation = 4.dp) {
                AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                    TrainingSetupScreen(
                        viewModel = viewModel,
                        onStartTraining = { trainingRun ->
                            navController.navigate("training_session")
                        }
                    )
                }
            }
        }
        composable("training_session") {
            currentScreen.value = "training_session"
            Surface(color = MaterialTheme.colorScheme.primaryContainer, tonalElevation = 8.dp) {
                AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                    TrainingSessionScreen(
                        viewModel = viewModel,
                        onComplete = { navController.popBackStack("main_menu", inclusive = false) },
                        navController = navController
                    )
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
fun MainMenuScreen(viewModel: SessionViewModel, onStartSession: () -> Unit, onStartTraining: () -> Unit, onCheckStats: () -> Unit, onTrends: () -> Unit) {
    val puttingRating by viewModel.puttingRating.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadPuttingRating()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.jpii_foreground),
            contentDescription = "App Icon",
            modifier = Modifier
                .size(260.dp)
                .padding(top = 16.dp, bottom = 16.dp),
            tint = Color.Unspecified
        )
        Text("Putting Rating: $puttingRating", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
        Text("Just putt it in!", modifier = Modifier.padding(bottom = 32.dp))
        Button(onClick = onStartSession, modifier = Modifier.fillMaxWidth()) {
            Text("New session")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onStartTraining, modifier = Modifier.fillMaxWidth()) {
            Text("Training Run")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onCheckStats, modifier = Modifier.fillMaxWidth()) {
            Text("History")
        }
        Spacer(modifier = Modifier.height(16.dp))
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
    val averageHitRate = viewModel.averageHitRate.collectAsState().value

    LaunchedEffect(Unit) {
        viewModel.loadDistances()
    }
    LaunchedEffect(selectedDistance.value) {
        selectedDistance.value?.let {
            viewModel.loadStylesForDistance(it)
            viewModel.loadAverageHitRate(it)
        }
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
            if (averageHitRate != null && selectedDistance.value != null) {
                Text(
                    text = "Average hit rate for ${selectedDistance.value}m: ${(averageHitRate * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
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
            Spacer(modifier = Modifier.height(8.dp))
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
        Spacer(modifier = Modifier.height(12.dp))
        if (sessions.isEmpty() && distances.isNotEmpty()) {
            Text("No sessions found for this distance, style, and range.")
        } else if (sessions.isNotEmpty()) {
            var showDeleteDialog by remember { mutableStateOf(false) }
            val lastSession = sessions.first()
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(lastSession.date))
            val hitRate = if (lastSession.numPutts > 0) (lastSession.madePutts * 100 / lastSession.numPutts) else 0
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Last Session - $date", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Throws: ${lastSession.numPutts} / Hits: ${lastSession.madePutts}")
                        Text("Style: ${lastSession.style}")
                    }
                    Text("$hitRate%", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.align(Alignment.CenterVertically))
                    IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.align(Alignment.CenterVertically)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Session",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Session") },
                    text = { Text("Are you sure you want to delete this session?") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.deleteSession(lastSession)
                            showDeleteDialog = false
                        }) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            BarChart(sessions = sessions)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions.drop(1)) { session ->
                    var showDeleteDialog by remember { mutableStateOf(false) }
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
                                Text("Throws: ${session.numPutts} / Hits: ${session.madePutts}")
                                Text("Style: ${session.style}")
                            }
                            Text("$hitRate%", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.align(Alignment.CenterVertically))
                            IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.align(Alignment.CenterVertically)) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Session",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Delete Session") },
                                text = { Text("Are you sure you want to delete this session?") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        viewModel.deleteSession(session)
                                        showDeleteDialog = false
                                    }) {
                                        Text("Delete")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
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
    val chartHeight = 80.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(chartHeight)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                Text("$hitRate%", color = MaterialTheme.colorScheme.onPrimary, fontSize = 10.sp, modifier = Modifier.padding(bottom = 4.dp))
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

    // Add state for last session and average hitrate
    val lastSessionHitrate = remember { mutableStateOf<Int?>(null) }
    val averageHitrate = remember { mutableStateOf<Double?>(null) }

    LaunchedEffect(distance, selectedStyle.value) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val last = viewModel.getLastSession(distance, selectedStyle.value)
            val avg = viewModel.getAverageHitRate(distance, selectedStyle.value)
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                lastSessionHitrate.value = last?.let { if (it.numPutts > 0) (it.madePutts * 100 / it.numPutts) else null }
                averageHitrate.value = avg
            }
        }
    }

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
                // Make distance and throws more prominent
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(vertical = 12.dp, horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Distance",
                        tint = Color(0xffee632c),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "$distance m",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xffee632c),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Throws",
                        tint = Color(0xffee632c),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "$numPutts throws",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xffee632c),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

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
                            // Update last/average hitrate after saving
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                val last = viewModel.getLastSession(distance, selectedStyle.value)
                                val avg = viewModel.getAverageHitRate(distance, selectedStyle.value)
                                withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    lastSessionHitrate.value = last?.let { if (it.numPutts > 0) (it.madePutts * 100 / it.numPutts) else null }
                                    averageHitrate.value = avg
                                }
                            }
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

                // Show last attempt and average hitrate
                if (lastSessionHitrate.value != null) {
                    Text("Last attempt hit rate: ${lastSessionHitrate.value}%", style = MaterialTheme.typography.bodyMedium)
                }
                if (averageHitrate.value != null) {
                    Text("Average hit rate: ${(averageHitrate.value!! * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))

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
    val averageHitRate = viewModel.averageHitRate.collectAsState().value

    LaunchedEffect(distance) {
        if (distance in 1..30) {
            viewModel.loadAverageHitRate(distance)
        }
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
                if (averageHitRate != null && distance in 1..30) {
                    Text(
                        text = "Average hit rate for ${distance}m: ${(averageHitRate * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                } else if (distance in 1..30) {
                    Text(
                        text = "No sessions recorded",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }
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
fun TrainingSetupScreen(viewModel: SessionViewModel, onStartTraining: (TrainingRun) -> Unit) {
    val minDistance by viewModel.minDistance.collectAsState()
    val maxDistance by viewModel.maxDistance.collectAsState()
    val maxPuttsPerDistance by viewModel.maxPuttsPerDistance.collectAsState()
    val selectedStyle by viewModel.selectedStyle.collectAsState()
    val expandedStyle = remember { mutableStateOf(false) }
    
    // Debug logging
    LaunchedEffect(Unit) {
        android.util.Log.d("TrainingSetup", "Training setup screen loaded")
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
                    text = "Training Run Setup",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
                
                //Text("Distance Range", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                /*
                Text(
                    text = "Select the range of distances for your training session",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                 */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Min Distance", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                        NumberPickerRow(
                            range = 1..29,
                            selected = minDistance,
                            onSelected = { 
                                viewModel.setMinDistance(it)
                                // Ensure max distance is at least one more than min distance
                                if (maxDistance <= it) {
                                    viewModel.setMaxDistance(it + 1)
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Max Distance", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                        NumberPickerRow(
                            range = (minDistance + 1)..30,
                            selected = maxDistance,
                            onSelected = {
                                viewModel.setMaxDistance(it)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                // Show selected range
                /*
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (maxDistance > minDistance) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Range",
                            tint = if (maxDistance > minDistance) 
                                MaterialTheme.colorScheme.onPrimaryContainer 
                            else 
                                MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (maxDistance > minDistance) 
                                "Training Range: ${minDistance}-${maxDistance}m"
                            else 
                                "Invalid Range: Max must be > Min",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (maxDistance > minDistance) 
                                MaterialTheme.colorScheme.onPrimaryContainer 
                            else 
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                 */
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Max Putts per Distance", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                NumberPickerRow(
                    range = 3..15,
                    selected = maxPuttsPerDistance,
                    onSelected = { viewModel.setMaxPuttsPerDistance(it) }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Tracking Mode", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val trackingMode by viewModel.trackingMode.collectAsState()
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        RadioButton(
                            selected = trackingMode == "Per Putt",
                            onClick = { viewModel.setTrackingMode("Per Putt") }
                        )
                        Text("Per Putt", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Mark each putt individually",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        RadioButton(
                            selected = trackingMode == "Per Distance Total",
                            onClick = { viewModel.setTrackingMode("Per Distance Total") }
                        )
                        Text("Per Distance Total", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Enter total made/missed",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Putting Style", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
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
                        viewModel.styles.forEach { style ->
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
                val trackingMode by viewModel.trackingMode.collectAsState()
                Text(
                    text = "The app will create a randomized training session with distances between ${minDistance}-${maxDistance}m and up to ${maxPuttsPerDistance} putts per distance. You'll track your putts ${if (trackingMode == "Per Putt") "individually" else "by entering totals for each distance"}.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Button(
                    onClick = { 
                        android.util.Log.d("TrainingSetup", "Start Training Run button clicked")
                        val trainingRun = viewModel.createTrainingRun()
                        android.util.Log.d("TrainingSetup", "Created training run: $trainingRun")
                        onStartTraining(trainingRun)
                    },
                    enabled = maxDistance > minDistance,
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Start Training Run")
                }
            }
        }
    }
}

@Composable
fun TrainingSessionScreen(viewModel: SessionViewModel, onComplete: () -> Unit, navController: androidx.navigation.NavHostController) {
    val currentTrainingRun by viewModel.currentTrainingRun.collectAsState()
    val currentStepIndex by viewModel.currentStepIndex.collectAsState()
    val trainingSteps by viewModel.trainingSteps.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val progress by viewModel.trainingProgress.collectAsState()
    val context = LocalContext.current
    val soundOn by viewModel.soundOn.collectAsState()

    // Debug logging
    LaunchedEffect(currentTrainingRun, currentStep) {
        android.util.Log.d("TrainingSession", "Training run: $currentTrainingRun")
        android.util.Log.d("TrainingSession", "Current step: $currentStep")
        android.util.Log.d("TrainingSession", "Training steps: ${trainingSteps.size}")
    }

    // Intercept system back and go to main menu
    BackHandler {
        navController.popBackStack("main_menu", inclusive = false)
        navController.navigate("main_menu")
    }

    if (currentTrainingRun == null || currentStep == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No training run in progress")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Training run: $currentTrainingRun")
                Text("Current step: $currentStep")
                Text("Training steps: ${trainingSteps.size}")
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        android.util.Log.d("TrainingSession", "Manual check - Training run: $currentTrainingRun")
                        android.util.Log.d("TrainingSession", "Manual check - Current step: $currentStep")
                        android.util.Log.d("TrainingSession", "Manual check - Training steps: ${trainingSteps.size}")
                    }
                ) {
                    Text("Check State")
                }
            }
        }
        return
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
                    text = "Training Run",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )

                // Progress indicator
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "${(progress * 100).toInt()}% Complete",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Current step info
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Step ${currentStepIndex + 1} of ${trainingSteps.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${currentStep!!.distance}m",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${currentStep!!.completedPutts}/${currentStep!!.numPutts} putts completed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (currentStep!!.completedPutts > 0) {
                            val stepHitRate = if (currentStep!!.completedPutts > 0) {
                                (currentStep!!.madePutts * 100 / currentStep!!.completedPutts)
                            } else 0
                            Text(
                                text = "Step hit rate: $stepHitRate%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Text(
                                text = "Step hit rate: na",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Overall stats
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Overall Progress",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "${currentTrainingRun!!.completedPutts}/${currentTrainingRun!!.totalPutts} putts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        if (currentTrainingRun!!.completedPutts > 0) {
                            val overallHitRate = (currentTrainingRun!!.madePutts * 100 / currentTrainingRun!!.completedPutts)
                            Text(
                                text = "Overall hit rate: $overallHitRate%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        } else {
                            Text(
                                text = "Overall hit rate: na",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Putt buttons - different UI based on tracking mode
                val trackingMode = currentTrainingRun!!.trackingMode
                if (trackingMode == "Per Putt") {
                    // Individual putt tracking
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                viewModel.recordTrainingPutt(false)
                            },
                            enabled = !currentTrainingRun!!.isCompleted,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Text("Missed")
                        }
                        Button(
                            onClick = {
                                viewModel.recordTrainingPutt(true)
                            },
                            enabled = !currentTrainingRun!!.isCompleted,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        ) {
                            Text("Made")
                        }
                    }
                } else {
                    // Per Distance Total tracking - Only Made Putts selection
                    var madePutts by remember { mutableStateOf<Int?>(0) }
                    val totalPuttsForStep = currentStep!!.numPutts
                    val missedPutts = (madePutts ?: 0).let { made -> totalPuttsForStep - made }
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Per Distance Total Mode",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Text(
                            text = "Enter made putts for ${currentStep!!.distance}m (${totalPuttsForStep} total putts)",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Made Putts Section
                        Text("Made Putts", style = MaterialTheme.typography.bodyMedium)
                        NumberPickerRow(
                            range = 0..totalPuttsForStep,
                            selected = madePutts,
                            onSelected = { madePutts = it }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Auto-calculated Missed Putts
                        Text(
                            text = "Missed Putts: $missedPutts (auto-calculated)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Summary
                        Text(
                            text = "Summary: ${madePutts ?: 0} made + $missedPutts missed = ${totalPuttsForStep} total",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Complete Step button
                        Button(
                            onClick = {
                                val made = madePutts ?: 0
                                android.util.Log.d("TrainingSession", "Completing step with $made made, $missedPutts missed")
                                repeat(made) { viewModel.recordTrainingPutt(true) }
                                repeat(missedPutts) { viewModel.recordTrainingPutt(false) }
                                madePutts = 0
                            },
                            enabled = !currentTrainingRun!!.isCompleted,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Text(
                                text = "Complete Step",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (currentTrainingRun!!.isCompleted) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Training Run Complete!",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    val finalHitRate = if (currentTrainingRun!!.completedPutts > 0) {
                        (currentTrainingRun!!.madePutts * 100 / currentTrainingRun!!.completedPutts)
                    } else 0
                    Text(
                        text = "Final hit rate: $finalHitRate%",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            // Save the training run and close
                            viewModel.saveTrainingRun(currentTrainingRun!!)
                            onComplete()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Text("Save Session & Close")
                    }
                }
            }
        }
    }
}

@Composable
fun NumberPickerRow(range: IntRange, selected: Int?, onSelected: (Int) -> Unit) {
    val itemSize = 56.dp
    val selectedItemSize = 60.dp
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
                targetValue = if (isSelected) Color(0xffee632c) else Color.LightGray,
                animationSpec = tween(durationMillis = 300), label = ""
            )
            val animatedElevation by animateDpAsState(
                targetValue = if (isSelected) 12.dp else 2.dp,
                animationSpec = tween(durationMillis = 300), label = ""
            )
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = animatedColor),
                elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation),
                modifier = Modifier
                    .size(if (isSelected) selectedItemSize else itemSize)
                    .shadow(if (isSelected) 16.dp else 2.dp, CircleShape)
                    .clickable { onSelected(value) }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = value.toString(),
                        style = if (isSelected) MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyLarge,
                        color = Color(0xff022f33)
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