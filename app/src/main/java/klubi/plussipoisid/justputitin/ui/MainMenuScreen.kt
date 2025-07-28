package klubi.plussipoisid.justputitin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import klubi.plussipoisid.justputitin.SessionViewModel
import klubi.plussipoisid.justputitin.data.PuttSession
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainMenuScreen(
    viewModel: SessionViewModel,
    onStartSession: (distance: Int, numPutts: Int) -> Unit,
    onStartTraining: (trainingRun: klubi.plussipoisid.justputitin.data.TrainingRun) -> Unit,
    navController: NavController
) {
    val sessions by viewModel.sessions.collectAsState()
    val distances by viewModel.distances.collectAsState()
    val stylesForDistance by viewModel.stylesForDistance.collectAsState()
    val averageHitRate by viewModel.averageHitRate.collectAsState()
    val puttingRating by viewModel.puttingRating.collectAsState()
    val soundOn by viewModel.soundOn.collectAsState()

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
                    text = "JPII",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Just Put It In",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Main action buttons
                Button(
                    onClick = {
                        navController.navigate("session_setup")
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 16.dp)
                ) {
                    Text("Practice Session")
                }

                Button(
                    onClick = {
                        navController.navigate("training_setup")
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 16.dp)
                ) {
                    Text("Training Run")
                }

                Button(
                    onClick = {
                        navController.navigate("trends")
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 16.dp)
                ) {
                    Text("View Trends")
                }

                // Sound toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sound Effects", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = soundOn,
                        onCheckedChange = { viewModel.setSoundOn(it) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Recent sessions
                if (sessions.isNotEmpty()) {
                    Text(
                        text = "Recent Sessions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sessions.take(5)) { session ->
                            SessionCard(
                                session = session,
                                onDelete = { viewModel.deleteSession(session) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionCard(session: PuttSession, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val date = Date(session.date)
    val hitRate = if (session.numPutts > 0) {
        (session.madePutts * 100 / session.numPutts)
    } else 0

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${session.distance}m - ${session.numPutts} putts",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${session.madePutts}/${session.numPutts} made ($hitRate%)",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = dateFormat.format(date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (session.style.isNotEmpty()) {
                    Text(
                        text = session.style,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete session",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
} 