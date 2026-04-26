package jp.developer.bbee.richuidemo.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jp.developer.bbee.richuidemo.component.BackNavigationIcon
import jp.developer.bbee.richuidemo.component.CountdownTimerWidget
import jp.developer.bbee.richuidemo.component.DemoSectionLabel
import jp.developer.bbee.richuidemo.component.MusicPlayerWidget
import jp.developer.bbee.richuidemo.component.StatsRingWidget
import jp.developer.bbee.richuidemo.component.WeatherWidget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetSamplesScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Widget Samples") },
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
                    title = "Music Player",
                    description = "Rotating disc · sine-wave waveform · playback controls",
                )
                MusicPlayerWidget(modifier = Modifier.fillMaxWidth())
            }

            item { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }

            item {
                DemoSectionLabel(
                    title = "System Stats",
                    description = "Animated arc rings · count-up from zero on enter",
                )
                StatsRingWidget(modifier = Modifier.fillMaxWidth())
            }

            item { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }

            item {
                DemoSectionLabel(
                    title = "Weather Card",
                    description = "Animated gradient sky · pulsing sun · frosted details row",
                )
                WeatherWidget(modifier = Modifier.fillMaxWidth())
            }

            item { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }

            item {
                DemoSectionLabel(
                    title = "Countdown Timer",
                    description = "Arc progress · green→orange→red shift · pulse when low",
                )
                CountdownTimerWidget(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
