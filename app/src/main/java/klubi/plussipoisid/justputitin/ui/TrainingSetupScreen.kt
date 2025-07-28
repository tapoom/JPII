package klubi.plussipoisid.justputitin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import klubi.plussipoisid.justputitin.SessionViewModel
import klubi.plussipoisid.justputitin.data.TrainingRun
import klubi.plussipoisid.justputitin.ui.NumberPickerRow

@Composable
fun TrainingSetupScreen(
    viewModel: SessionViewModel,
    onStartTraining: (TrainingRun) -> Unit,
    navController: NavController
) {
    val minDistance by viewModel.minDistance.collectAsState()
    val maxDistance by viewModel.maxDistance.collectAsState()
    val maxPuttsPerDistance by viewModel.maxPuttsPerDistance.collectAsState()
    val selectedStyle by viewModel.selectedStyle.collectAsState()
    val trackingMode by viewModel.trackingMode.collectAsState()
    
    var expandedStyle by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color.Blue.copy(alpha = 0.08f),
                        Color.White
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    style = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 24.dp),
                    textAlign = TextAlign.Center
                )

                //Distance Range Section
                Text("Distance Range", style = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Select the range of distances for your training session",
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Min Distance", style = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold))
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
                        Text("Max Distance", style = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold))
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
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Max Putts per Distance", style = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                NumberPickerRow(
                    range = 3..15,
                    selected = maxPuttsPerDistance,
                    onSelected = { viewModel.setMaxPuttsPerDistance(it) }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Tracking Mode", style = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        RadioButton(
                            selected = trackingMode == "Per Putt",
                            onClick = { viewModel.setTrackingMode("Per Putt") }
                        )
                        Text("Per Putt")
                        Text(
                            "Mark each putt individually",
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        RadioButton(
                            selected = trackingMode == "Per Distance Total",
                            onClick = { viewModel.setTrackingMode("Per Distance Total") }
                        )
                        Text("Per Distance Total")
                        Text(
                            "Enter total made/missed",
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Putting Style", style = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold))
                Box {
                    Button(
                        onClick = { expandedStyle = true },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Text(selectedStyle)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Pick Style")
                    }
                    DropdownMenu(
                        expanded = expandedStyle,
                        onDismissRequest = { expandedStyle = false },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        viewModel.styles.forEach { style ->
                            DropdownMenuItem(
                                text = { Text(style) },
                                onClick = {
                                    viewModel.setStyle(style)
                                    expandedStyle = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "The app will create a randomized training session with distances between ${minDistance}-${maxDistance}m and up to ${maxPuttsPerDistance} putts per distance. You'll track your putts ${if (trackingMode == "Per Putt") "individually" else "by entering totals for each distance"}.",
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