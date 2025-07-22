package klubi.plussipoisid.justputitin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import klubi.plussipoisid.justputitin.ui.theme.JPIITheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color

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
                    onAdjust = { navController.popBackStack("session_setup", inclusive = false) }
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
    var distanceInput = remember { mutableStateOf("") }
    val sessions = viewModel.sessions.collectAsState().value
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Statistics by Distance", modifier = Modifier.padding(bottom = 16.dp))
        OutlinedTextField(
            value = distanceInput.value,
            onValueChange = {
                distanceInput.value = it
                it.toIntOrNull()?.let { d -> viewModel.loadSessionsForDistance(d) }
            },
            label = { Text("Distance (meters)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (sessions.isEmpty()) {
            Text("No sessions found for this distance.")
        } else {
            sessions.forEach { session ->
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(session.date))
                val hitRate = if (session.numPutts > 0) (session.madePutts * 100 / session.numPutts) else 0
                Text("Session on $date: ${session.numPutts} throws, ${session.madePutts} in → $hitRate%",
                    modifier = Modifier.padding(bottom = 8.dp))
            }
        }
    }
}

@Composable
fun ResultEntryScreen(distance: Int, numPutts: Int, onRepeat: () -> Unit, onAdjust: () -> Unit) {
    val viewModel: SessionViewModel = viewModel()
    var successfulPutts = remember { mutableStateOf("") }
    val successful = successfulPutts.value.toIntOrNull() ?: 0
    val hitRate = if (numPutts > 0) (successful * 100 / numPutts) else 0
    var saved = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter Session Results", modifier = Modifier.padding(bottom = 24.dp))
        Text("Distance: $distance meters")
        Text("Throws: $numPutts")
        OutlinedTextField(
            value = successfulPutts.value,
            onValueChange = { successfulPutts.value = it },
            label = { Text("Successful Putts") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Hit Rate: $hitRate%", modifier = Modifier.padding(bottom = 24.dp))
        Button(
            onClick = {
                viewModel.saveSession(distance, numPutts, successful)
                saved.value = true
            },
            enabled = successful in 0..numPutts && !saved.value
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

@Composable
fun SessionSetupScreen(onStartSession: (Int, Int) -> Unit) {
    val viewModel: SessionViewModel = viewModel()
    val distance = viewModel.distance.collectAsState().value
    val numPutts = viewModel.numPutts.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("New Putting Session", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 24.dp))
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

@Composable
fun NumberPickerRow(range: IntRange, selected: Int, onSelected: (Int) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(range.toList()) { value ->
            Card(
                shape = RoundedCornerShape(50),
                colors = CardDefaults.cardColors(
                    containerColor = if (value == selected) MaterialTheme.colorScheme.primary else Color.LightGray
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (value == selected) 8.dp else 2.dp),
                modifier = Modifier
                    .size(56.dp)
                    .padding(vertical = 4.dp)
                    .let { if (value == selected) it else it }
                    .clickable { onSelected(value) }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = value.toString(),
                        style = if (value == selected) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                        color = if (value == selected) MaterialTheme.colorScheme.onPrimary else Color.DarkGray
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