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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jp.developer.bbee.richuidemo.component.AnimatedBorderCard
import jp.developer.bbee.richuidemo.component.BackNavigationIcon
import jp.developer.bbee.richuidemo.component.DemoSectionLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorderDemoScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Animated Border") },
                navigationIcon = { BackNavigationIcon(onClick = onBack) },
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
                    title = "Two-Color Fast",
                    description = "Magenta ↔ Cyan · 3 seconds · 8 dp border",
                )
                AnimatedBorderCard(
                    modifier = Modifier.fillMaxWidth(),
                    gradient = Brush.sweepGradient(listOf(Color.Magenta, Color.Cyan, Color.Magenta)),
                    borderWidth = 8.dp,
                    shape = RoundedCornerShape(12.dp),
                    animationDuration = 3_000,
                ) {
                    DemoCardContent("Fast rotation gradient")
                }
            }

            item {
                DemoSectionLabel(
                    title = "Rainbow Gradient",
                    description = "Full spectrum · 10 seconds · 4 dp border",
                )
                AnimatedBorderCard(
                    modifier = Modifier.fillMaxWidth(),
                    gradient = Brush.sweepGradient(
                        listOf(
                            Color.Red,
                            Color(0xFFFF6600),
                            Color.Yellow,
                            Color.Green,
                            Color.Cyan,
                            Color.Blue,
                            Color(0xFF8000FF),
                            Color.Red,
                        ),
                    ),
                    borderWidth = 4.dp,
                    shape = RoundedCornerShape(12.dp),
                    animationDuration = 10_000,
                ) {
                    DemoCardContent("Rainbow spectrum rotation")
                }
            }

            item {
                DemoSectionLabel(
                    title = "Gold & Silver",
                    description = "Metallic tones · 8 seconds · 12 dp border",
                )
                AnimatedBorderCard(
                    modifier = Modifier.fillMaxWidth(),
                    gradient = Brush.sweepGradient(
                        listOf(
                            Color(0xFFFFD700),
                            Color(0xFFF5F5F5),
                            Color(0xFFFFA500),
                            Color(0xFFF5F5F5),
                            Color(0xFFFFD700),
                        ),
                    ),
                    borderWidth = 12.dp,
                    shape = RoundedCornerShape(20.dp),
                    animationDuration = 8_000,
                ) {
                    DemoCardContent("Metallic shimmer border")
                }
            }

            item {
                DemoSectionLabel(
                    title = "Neon Pulse",
                    description = "Green → Blue → Pink · 5 seconds · 3 dp border",
                )
                AnimatedBorderCard(
                    modifier = Modifier.fillMaxWidth(),
                    gradient = Brush.sweepGradient(
                        listOf(
                            Color(0xFF00FF88),
                            Color(0xFF00AAFF),
                            Color(0xFFFF00AA),
                            Color(0xFF00FF88),
                        ),
                    ),
                    borderWidth = 3.dp,
                    shape = RoundedCornerShape(8.dp),
                    animationDuration = 5_000,
                ) {
                    DemoCardContent("Neon color cycling")
                }
            }

            item {
                DemoSectionLabel(
                    title = "Slow Rotation",
                    description = "Pastel tones · 20 seconds · 6 dp border",
                )
                AnimatedBorderCard(
                    modifier = Modifier.fillMaxWidth(),
                    gradient = Brush.sweepGradient(
                        listOf(
                            Color(0xFFFFB3C1),
                            Color(0xFFB3D4FF),
                            Color(0xFFB3FFD4),
                            Color(0xFFFFEEB3),
                            Color(0xFFFFB3C1),
                        ),
                    ),
                    borderWidth = 6.dp,
                    shape = RoundedCornerShape(24.dp),
                    animationDuration = 20_000,
                ) {
                    DemoCardContent("Slow dreamy rotation")
                }
            }
        }
    }
}

@Composable
private fun DemoCardContent(label: String) {
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
        )
    }
}
