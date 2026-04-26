package jp.developer.bbee.richuidemo.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun MusicPlayerWidget(
    modifier: Modifier = Modifier,
    trackTitle: String = "Cosmic Journey",
    artistName: String = "Stellar Waves",
    totalDurationSeconds: Int = 213,
    accentColor: Color = Color(0xFF6C63FF),
) {
    var isPlaying by remember { mutableStateOf(false) }
    var progressFraction by remember { mutableFloatStateOf(0.35f) }

    val demoTracks = remember(trackTitle, artistName) {
        listOf(
            Pair(trackTitle, artistName),
            Pair("Midnight Drive", "Neon Pulse"),
            Pair("Ocean Breeze", "Wave Riders"),
        )
    }
    var trackIndex by remember { mutableIntStateOf(0) }
    val (currentTitle, currentArtist) = demoTracks[trackIndex]

    // Disc rotation stops when paused, avoiding continuous recompositions at 60fps
    val discAngle = remember { Animatable(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                val start = discAngle.value % 360f
                discAngle.snapTo(start)
                discAngle.animateTo(
                    start + 360f,
                    animationSpec = tween(4000, easing = LinearEasing),
                )
            }
        }
    }

    // Waveform phase stops when paused, avoiding continuous recompositions at 60fps
    val wavePhase = remember { Animatable(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                wavePhase.snapTo(0f)
                wavePhase.animateTo(
                    (2 * PI).toFloat(),
                    animationSpec = tween(800, easing = LinearEasing),
                )
            }
        }
    }

    val discBrush = remember(accentColor) {
        Brush.sweepGradient(
            listOf(
                accentColor,
                Color(0xFFFF6B6B),
                Color(0xFF4ECDC4),
                accentColor,
            ),
        )
    }
    val barBrush = remember(accentColor) {
        Brush.verticalGradient(listOf(accentColor, accentColor.copy(alpha = 0.35f)))
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(discBrush)
                        .rotate(discAngle.value),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = currentTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = currentArtist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            val barCount = 20
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(barCount) { i ->
                    val normalized = if (isPlaying) {
                        (sin(wavePhase.value + i * PI / barCount * 4f).toFloat() * 0.5f + 0.5f)
                    } else {
                        0.15f + (i % 5) * 0.12f
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height((4 + normalized * 28).dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(barBrush),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Slider(
                    value = progressFraction,
                    onValueChange = { progressFraction = it },
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor,
                        inactiveTrackColor = accentColor.copy(alpha = 0.2f),
                    ),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    val elapsed = (progressFraction * totalDurationSeconds).toInt()
                    Text(
                        text = "%d:%02d".format(elapsed / 60, elapsed % 60),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "%d:%02d".format(totalDurationSeconds / 60, totalDurationSeconds % 60),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = {
                        isPlaying = false
                        progressFraction = 0f
                        trackIndex = (trackIndex - 1 + demoTracks.size) % demoTracks.size
                    },
                    modifier = Modifier.semantics { contentDescription = "Previous track" },
                ) {
                    Text("⏮", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier.size(52.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = accentColor),
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(28.dp),
                        tint = Color.White,
                    )
                }
                Spacer(Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        isPlaying = false
                        progressFraction = 0f
                        trackIndex = (trackIndex + 1) % demoTracks.size
                    },
                    modifier = Modifier.semantics { contentDescription = "Next track" },
                ) {
                    Text("⏭", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
