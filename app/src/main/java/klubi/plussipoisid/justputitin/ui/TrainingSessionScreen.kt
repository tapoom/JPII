package klubi.plussipoisid.justputitin.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import klubi.plussipoisid.justputitin.SessionViewModel

@Composable
fun TrainingSessionScreen(
    viewModel: SessionViewModel, 
    onComplete: () -> Unit, 
    navController: NavHostController
) {
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