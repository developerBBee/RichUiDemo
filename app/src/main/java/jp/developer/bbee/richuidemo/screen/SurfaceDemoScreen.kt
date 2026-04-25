package jp.developer.bbee.richuidemo.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jp.developer.bbee.richuidemo.component.AnimatedSurfaceCard
import jp.developer.bbee.richuidemo.component.DemoSectionLabel
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurfaceDemoScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Animated Surface") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                DemoSectionLabel(
                    title = "Primary — Standard",
                    description = "Default speed · 500 ms sweep · 500 ms pause",
                )
                AnimatedSurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    SurfaceCardContent("Standard shimmer sweep", color = MaterialTheme.colorScheme.onPrimary)
                }
            }

            item {
                DemoSectionLabel(
                    title = "Tertiary — Warm Tones",
                    description = "Warm accent color with orange shimmer",
                )
                AnimatedSurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    backGroundColor = MaterialTheme.colorScheme.tertiary,
                    effectColor = Color(0xFFFFAA00),
                    gradient = Brush.horizontalGradient(
                        listOf(Color.Transparent, Color(0xFFFFAA00), Color.Transparent),
                    ),
                    blendMode = BlendMode.Plus,
                ) {
                    SurfaceCardContent("Warm accent shimmer", color = MaterialTheme.colorScheme.onTertiary)
                }
            }

            item {
                DemoSectionLabel(
                    title = "Secondary — Rapid Pulse",
                    description = "Fast: 200 ms sweep · 200 ms pause",
                )
                AnimatedSurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    backGroundColor = MaterialTheme.colorScheme.secondary,
                    effectColor = Color.White,
                    gradient = Brush.horizontalGradient(
                        listOf(Color.Transparent, Color.White, Color.Transparent),
                    ),
                    blendMode = BlendMode.Plus,
                    animationDuration = 200.milliseconds.toInt(DurationUnit.MILLISECONDS),
                    animationInterval = 200.milliseconds.toInt(DurationUnit.MILLISECONDS),
                ) {
                    SurfaceCardContent("Rapid pulse shimmer", color = MaterialTheme.colorScheme.onSecondary)
                }
            }

            item {
                DemoSectionLabel(
                    title = "Error — Long Pause",
                    description = "Slow: 800 ms sweep · 2000 ms pause",
                )
                AnimatedSurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    backGroundColor = MaterialTheme.colorScheme.error,
                    effectColor = Color(0xFFFF8080),
                    gradient = Brush.horizontalGradient(
                        listOf(Color.Transparent, Color(0xFFFF8080), Color.Transparent),
                    ),
                    blendMode = BlendMode.Plus,
                    animationDuration = 800.milliseconds.toInt(DurationUnit.MILLISECONDS),
                    animationInterval = 2000.milliseconds.toInt(DurationUnit.MILLISECONDS),
                ) {
                    SurfaceCardContent("Slow pulse shimmer", color = MaterialTheme.colorScheme.onError)
                }
            }

            item {
                DemoSectionLabel(
                    title = "Surface — Subtle Dark",
                    description = "Surface background with dark shimmer for depth",
                )
                AnimatedSurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backGroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    effectColor = Color.DarkGray,
                    animationDuration = 700.milliseconds.toInt(DurationUnit.MILLISECONDS),
                    animationInterval = 1200.milliseconds.toInt(DurationUnit.MILLISECONDS),
                ) {
                    SurfaceCardContent(
                        "Subtle surface depth",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SurfaceCardContent(label: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = color,
        )
    }
}
