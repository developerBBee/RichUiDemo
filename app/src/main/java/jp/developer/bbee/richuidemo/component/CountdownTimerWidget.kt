package jp.developer.bbee.richuidemo.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun CountdownTimerWidget(
    modifier: Modifier = Modifier,
    totalSeconds: Int = 60,
) {
    var remainingSeconds by remember { mutableIntStateOf(totalSeconds) }
    var isRunning by remember { mutableStateOf(false) }

    // Sync state when totalSeconds changes from outside
    LaunchedEffect(totalSeconds) {
        remainingSeconds = totalSeconds
        isRunning = false
    }

    // Cancels and restarts whenever isRunning changes — naturally pauses when false
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
            }
            isRunning = false
        }
    }

    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "timer arc",
    )
    val timerColor by animateColorAsState(
        targetValue = when {
            progress > 0.5f -> Color(0xFF4CAF50)
            progress > 0.25f -> Color(0xFFFF9800)
            else -> Color(0xFFF44336)
        },
        animationSpec = tween(400),
        label = "timer color",
    )

    // Pulse only runs when the condition is met; Animatable stops when LaunchedEffect cancels
    val pulseScale = remember { Animatable(1f) }
    val shouldPulse = isRunning && progress < 0.2f && remainingSeconds > 0
    LaunchedEffect(shouldPulse) {
        if (shouldPulse) {
            while (true) {
                pulseScale.animateTo(1.05f, tween(500, easing = FastOutSlowInEasing))
                pulseScale.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
            }
        } else {
            pulseScale.animateTo(1f, tween(200))
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = "Countdown Timer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start),
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.scale(pulseScale.value),
            ) {
                Canvas(modifier = Modifier.size(140.dp)) {
                    val stroke = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(
                        color = timerColor.copy(alpha = 0.15f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = stroke,
                    )
                    drawArc(
                        color = timerColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = stroke,
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%d:%02d".format(remainingSeconds / 60, remainingSeconds % 60),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = timerColor,
                    )
                    Text(
                        text = when {
                            remainingSeconds == 0 -> "Done! 🎉"
                            isRunning -> "running"
                            else -> "paused"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { if (remainingSeconds > 0) isRunning = !isRunning },
                    enabled = remainingSeconds > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = timerColor),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (isRunning) "Pause" else "Start")
                }
                OutlinedButton(
                    onClick = {
                        isRunning = false
                        remainingSeconds = totalSeconds
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Reset")
                }
            }
        }
    }
}
