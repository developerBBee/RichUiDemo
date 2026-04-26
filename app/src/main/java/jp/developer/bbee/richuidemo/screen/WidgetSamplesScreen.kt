package jp.developer.bbee.richuidemo.screen

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import jp.developer.bbee.richuidemo.component.BackNavigationIcon
import jp.developer.bbee.richuidemo.component.CountdownTimerWidget
import jp.developer.bbee.richuidemo.component.DemoSectionLabel
import jp.developer.bbee.richuidemo.component.MusicPlayerWidget
import jp.developer.bbee.richuidemo.component.StatsRingWidget
import jp.developer.bbee.richuidemo.component.WeatherWidget
import jp.developer.bbee.richuidemo.widget.CountdownTimerGlanceWidget
import jp.developer.bbee.richuidemo.widget.CountdownTimerGlanceWidgetReceiver
import jp.developer.bbee.richuidemo.widget.WeatherGlanceWidget
import jp.developer.bbee.richuidemo.widget.WeatherGlanceWidgetReceiver
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetSamplesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    AddToHomeScreenButton(
                        label = "Weather をホーム画面に追加",
                        onClick = {
                            scope.launch {
                                GlanceAppWidgetManager(context).requestPinGlanceAppWidget(
                                    receiver = WeatherGlanceWidgetReceiver::class.java,
                                    preview = WeatherGlanceWidget(),
                                )
                            }
                        },
                    )
                }
            }

            item { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }

            item {
                DemoSectionLabel(
                    title = "Countdown Timer",
                    description = "Arc progress · green→orange→red shift · pulse when low",
                )
                CountdownTimerWidget(modifier = Modifier.fillMaxWidth())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    AddToHomeScreenButton(
                        label = "Timer をホーム画面に追加",
                        onClick = {
                            scope.launch {
                                GlanceAppWidgetManager(context).requestPinGlanceAppWidget(
                                    receiver = CountdownTimerGlanceWidgetReceiver::class.java,
                                    preview = CountdownTimerGlanceWidget(),
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AddToHomeScreenButton(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.AddHome,
                contentDescription = null,
                modifier = Modifier.padding(end = 6.dp),
            )
            Text(label)
        }
    }
}
