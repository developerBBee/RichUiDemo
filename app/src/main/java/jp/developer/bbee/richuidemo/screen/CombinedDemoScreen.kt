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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jp.developer.bbee.richuidemo.component.AnimatedBorderCard
import jp.developer.bbee.richuidemo.component.BackNavigationIcon
import jp.developer.bbee.richuidemo.component.AnimatedSurfaceCard
import jp.developer.bbee.richuidemo.component.DemoSectionLabel
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombinedDemoScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Combined Effect") },
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
                    title = "Classic",
                    description = "Magenta/Cyan border · Surface shimmer · 10 s",
                )
                AnimatedBorderCard(
                    modifier = Modifier.fillMaxWidth(),
                    gradient = Brush.sweepGradient(
                        listOf(Color.Magenta, Color.White, Color.Cyan, Color.White, Color.Magenta),
                    ),
                    borderWidth = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    animationDuration = 10_000,
                ) {
                    AnimatedSurfaceCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        backGroundColor = MaterialTheme.colorScheme.surface,
                        effectColor = MaterialTheme.colorScheme.secondary,
                        animationDuration = 500.milliseconds.toInt(DurationUnit.MILLISECONDS),
                        animationInterval = 500.milliseconds.toInt(DurationUnit.MILLISECONDS),
                    ) {
                        CombinedCardContent("Classic combo effect")
                    }
                }
            }

            item {
                DemoSectionLabel(
                    title = "Neon Vibes",
                    description = "Neon green/pink border · White shimmer · 4 s",
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
                    borderWidth = 6.dp,
                    shape = RoundedCornerShape(12.dp),
                    animationDuration = 4_000,
                ) {
                    AnimatedSurfaceCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        backGroundColor = Color(0xFF1A1A2E),
                        effectColor = Color.White,
                        gradient = Brush.horizontalGradient(
                            listOf(Color.Transparent, Color.White, Color.Transparent),
                        ),
                        blendMode = BlendMode.Plus,
                        animationDuration = 300.milliseconds.toInt(DurationUnit.MILLISECONDS),
                        animationInterval = 700.milliseconds.toInt(DurationUnit.MILLISECONDS),
                    ) {
                        CombinedCardContent("Neon on dark", contentColor = Color.White)
                    }
                }
            }

            item {
                DemoSectionLabel(
                    title = "Golden Hour",
                    description = "Gold border · Warm shimmer · 8 s slow rotation",
                )
                AnimatedBorderCard(
                    modifier = Modifier.fillMaxWidth(),
                    gradient = Brush.sweepGradient(
                        listOf(
                            Color(0xFFFFD700),
                            Color(0xFFFFF8DC),
                            Color(0xFFFFA500),
                            Color(0xFFFFF8DC),
                            Color(0xFFFFD700),
                        ),
                    ),
                    borderWidth = 10.dp,
                    shape = RoundedCornerShape(20.dp),
                    animationDuration = 8_000,
                ) {
                    AnimatedSurfaceCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        backGroundColor = Color(0xFFFFF3E0),
                        effectColor = Color(0xFFFFD700),
                        gradient = Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0xFFFFD700), Color.Transparent),
                        ),
                        blendMode = BlendMode.Multiply,
                        animationDuration = 800.milliseconds.toInt(DurationUnit.MILLISECONDS),
                        animationInterval = 1500.milliseconds.toInt(DurationUnit.MILLISECONDS),
                    ) {
                        CombinedCardContent("Golden shimmer", contentColor = Color(0xFF5D4037))
                    }
                }
            }

            item {
                DemoSectionLabel(
                    title = "Galaxy",
                    description = "Deep space colors · Star shimmer · 6 s",
                )
                AnimatedBorderCard(
                    modifier = Modifier.fillMaxWidth(),
                    gradient = Brush.sweepGradient(
                        listOf(
                            Color(0xFF6600CC),
                            Color(0xFF0033FF),
                            Color(0xFF00CCFF),
                            Color(0xFF9900FF),
                            Color(0xFF6600CC),
                        ),
                    ),
                    borderWidth = 5.dp,
                    shape = RoundedCornerShape(16.dp),
                    animationDuration = 6_000,
                ) {
                    AnimatedSurfaceCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        backGroundColor = Color(0xFF0D0D2B),
                        effectColor = Color(0xFFCCAAFF),
                        gradient = Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0xFFCCAAFF), Color.Transparent),
                        ),
                        blendMode = BlendMode.Plus,
                        animationDuration = 600.milliseconds.toInt(DurationUnit.MILLISECONDS),
                        animationInterval = 1000.milliseconds.toInt(DurationUnit.MILLISECONDS),
                    ) {
                        CombinedCardContent("Deep space effect", contentColor = Color(0xFFE0CCFF))
                    }
                }
            }
        }
    }
}

@Composable
private fun CombinedCardContent(
    label: String,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = contentColor,
            fontWeight = FontWeight.Medium,
        )
    }
}
