package jp.developer.bbee.richuidemo.screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.developer.bbee.richuidemo.component.AnimatedBorderCard
import jp.developer.bbee.richuidemo.component.AnimatedSurfaceCard
import jp.developer.bbee.richuidemo.component.DemoSectionLabel

private data class HeroCard(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val tag: String,
    val gradientColors: List<Color>,
    val borderColors: List<Color>,
)

private data class TrendingCard(
    val emoji: String,
    val title: String,
    val label: String,
    val category: String,
    val backgroundColor: Color,
    val effectColor: Color,
)

private val heroCards = listOf(
    HeroCard(
        emoji = "🎨",
        title = "Creative Design",
        subtitle = "Shapes, colors & composition",
        tag = "Design",
        gradientColors = listOf(Color(0xFFE040FB), Color(0xFF7C4DFF), Color(0xFF40C4FF)),
        borderColors = listOf(Color(0xFFE040FB), Color(0xFF40C4FF), Color(0xFFE040FB)),
    ),
    HeroCard(
        emoji = "🚀",
        title = "Technology",
        subtitle = "The future is now",
        tag = "Tech",
        gradientColors = listOf(Color(0xFF29B6F6), Color(0xFF00E5FF), Color(0xFF69F0AE)),
        borderColors = listOf(Color(0xFF29B6F6), Color(0xFF69F0AE), Color(0xFF29B6F6)),
    ),
    HeroCard(
        emoji = "🌿",
        title = "Nature & Life",
        subtitle = "Green earth, healthy planet",
        tag = "Nature",
        gradientColors = listOf(Color(0xFF66BB6A), Color(0xFF26C6DA), Color(0xFFD4E157)),
        borderColors = listOf(Color(0xFF66BB6A), Color(0xFFD4E157), Color(0xFF66BB6A)),
    ),
    HeroCard(
        emoji = "🍜",
        title = "Food Culture",
        subtitle = "Taste the world",
        tag = "Food",
        gradientColors = listOf(Color(0xFFFF7043), Color(0xFFFFCA28), Color(0xFFFF5252)),
        borderColors = listOf(Color(0xFFFF7043), Color(0xFFFFCA28), Color(0xFFFF7043)),
    ),
    HeroCard(
        emoji = "✈️",
        title = "Travel & Explore",
        subtitle = "Journey beyond horizons",
        tag = "Travel",
        gradientColors = listOf(Color(0xFFFFEB3B), Color(0xFFFF9800), Color(0xFFFF5722)),
        borderColors = listOf(Color(0xFFFFEB3B), Color(0xFFFF5722), Color(0xFFFFEB3B)),
    ),
)

// Each card carries its category so the chip row can filter this list.
private val trendingCards = listOf(
    TrendingCard("🎨", "Illustration", "Creative", "Design", Color(0xFFAB47BC), Color(0xFFCE93D8)),
    TrendingCard("🎮", "Gaming", "Fun", "Tech", Color(0xFF5C6BC0), Color(0xFF9FA8DA)),
    TrendingCard("🌿", "Botany", "Green", "Nature", Color(0xFF66BB6A), Color(0xFF81C784)),
    TrendingCard("🍕", "Cuisine", "Taste", "Food", Color(0xFFFF7043), Color(0xFFFF8A65)),
    TrendingCard("✈️", "Journey", "Explore", "Travel", Color(0xFF26C6DA), Color(0xFF80DEEA)),
    TrendingCard("🎵", "Music", "Audio", "Music", Color(0xFF7E57C2), Color(0xFFB39DDB)),
    TrendingCard("📸", "Photography", "Visual", "Art", Color(0xFF26A69A), Color(0xFF80CBC4)),
    TrendingCard("🏋️", "Fitness", "Health", "Sport", Color(0xFF42A5F5), Color(0xFF90CAF9)),
)

private val categories = listOf(
    "All", "Design", "Tech", "Nature", "Food", "Travel", "Music", "Art", "Sport",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizontalScrollCardsScreen(onBack: () -> Unit) {
    val pagerState = rememberPagerState { heroCards.size }
    var selectedCategory by remember { mutableStateOf("All") }
    val filteredTrending = remember(selectedCategory) {
        if (selectedCategory == "All") trendingCards
        else trendingCards.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Horizontal Scroll Cards") },
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
            contentPadding = PaddingValues(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    DemoSectionLabel(
                        title = "Featured",
                        description = "Swipe cards · snap pager · animated border",
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 32.dp),
                    pageSpacing = 12.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) { page ->
                    HeroCardItem(card = heroCards[page])
                }
                Spacer(modifier = Modifier.height(12.dp))
                PagerIndicator(
                    count = heroCards.size,
                    currentPage = pagerState.currentPage,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    DemoSectionLabel(
                        title = "Categories",
                        description = "Filters the Trending cards below",
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                        )
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    DemoSectionLabel(
                        title = "Trending Now",
                        description = "Shimmer sweep · animated surface cards",
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (filteredTrending.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No cards for this category",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(filteredTrending) { card ->
                            TrendingCardItem(card = card)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCardItem(card: HeroCard) {
    // Pre-compute alpha-adjusted background colors once per card reference.
    val backgroundColors = remember(card) {
        card.gradientColors.map { it.copy(alpha = 0.25f) }
    }

    AnimatedBorderCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        gradient = Brush.sweepGradient(card.borderColors),
        borderWidth = 4.dp,
        shape = RoundedCornerShape(20.dp),
        animationDuration = 6_000,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.linearGradient(colors = backgroundColors)),
        ) {
            // Dark scrim ensures the white tag label is readable on any gradient color.
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp),
                shape = RoundedCornerShape(50.dp),
                color = Color.Black.copy(alpha = 0.45f),
            ) {
                Text(
                    text = card.tag,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(18.dp),
            ) {
                Text(text = card.emoji, fontSize = 40.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = card.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TrendingCardItem(card: TrendingCard) {
    AnimatedSurfaceCard(
        modifier = Modifier
            .width(160.dp)
            .height(140.dp),
        backGroundColor = card.backgroundColor,
        effectColor = card.effectColor,
        shape = RoundedCornerShape(16.dp),
        animationDuration = 600,
        animationInterval = 1200,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = card.emoji, fontSize = 36.sp)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = Color.White.copy(alpha = 0.25f),
                ) {
                    Text(
                        text = card.label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                    )
                }
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun PagerIndicator(
    count: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            val isSelected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 6.dp,
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                label = "indicator_width",
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .height(6.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                    ),
            )
        }
    }
}
