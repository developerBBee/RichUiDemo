@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package jp.developer.bbee.richuidemo.screen

import android.app.Activity
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import jp.developer.bbee.richuidemo.component.BackNavigationIcon
import kotlinx.coroutines.delay

private const val DEMO_URL =
    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

private enum class VideoSource { URL, LOCAL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val view = LocalView.current
    val window = (context as Activity).window

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    var source by remember { mutableStateOf(VideoSource.URL) }
    var localUri by remember { mutableStateOf<Uri?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var isMuted by remember { mutableStateOf(false) }
    var playerError by remember { mutableStateOf<String?>(null) }
    var controlsKey by remember { mutableIntStateOf(0) }
    var controlsVisible by remember { mutableStateOf(true) }

    LaunchedEffect(source, localUri) {
        playerError = null
        when {
            source == VideoSource.URL -> {
                exoPlayer.setMediaItem(MediaItem.fromUri(DEMO_URL))
                exoPlayer.prepare()
            }
            source == VideoSource.LOCAL && localUri != null -> {
                exoPlayer.setMediaItem(MediaItem.fromUri(localUri!!))
                exoPlayer.prepare()
            }
            else -> exoPlayer.clearMediaItems()
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
            override fun onPlayerError(error: PlaybackException) {
                playerError = error.message ?: "Playback error"
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration.coerceAtLeast(0L)
            delay(500)
        }
    }

    // Show/hide system bars based on orientation
    DisposableEffect(isLandscape) {
        val controller = WindowCompat.getInsetsController(window, view)
        if (isLandscape) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // Auto-hide controls in landscape after 3 seconds
    LaunchedEffect(controlsKey) {
        if (isLandscape) {
            delay(3000)
            controlsVisible = false
        }
    }

    val pickVideo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri -> uri?.let { localUri = it } }

    val onPlayPause = {
        if (isPlaying) exoPlayer.pause() else exoPlayer.play()
    }
    val onSeek = { pos: Long -> exoPlayer.seekTo(pos) }
    val onReplay10 = {
        exoPlayer.seekTo((exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L))
    }
    val onForward10 = {
        exoPlayer.seekTo(exoPlayer.currentPosition + 10_000L)
    }
    val onToggleMute = {
        isMuted = !isMuted
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    if (isLandscape) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    controlsVisible = !controlsVisible
                    if (controlsVisible) controlsKey++
                },
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )

            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopStart),
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                    )
                }
            }

            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    VideoControls(
                        isPlaying = isPlaying,
                        currentPosition = currentPosition,
                        duration = duration,
                        isMuted = isMuted,
                        onPlayPause = onPlayPause,
                        onSeek = onSeek,
                        onReplay10 = onReplay10,
                        onForward10 = onForward10,
                        onToggleMute = onToggleMute,
                        controlsColor = Color.White,
                    )
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Video Player") },
                    navigationIcon = { BackNavigationIcon(onClick = onBack) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Player surface
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center,
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = false
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (source == VideoSource.LOCAL && localUri == null) {
                        Text(
                            text = "動画を選択してください",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    playerError?.let {
                        Text(
                            text = "エラー: $it",
                            color = Color(0xFFFF6B6B),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(8.dp),
                        )
                    }
                }

                // Player controls
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    VideoControls(
                        isPlaying = isPlaying,
                        currentPosition = currentPosition,
                        duration = duration,
                        isMuted = isMuted,
                        onPlayPause = onPlayPause,
                        onSeek = onSeek,
                        onReplay10 = onReplay10,
                        onForward10 = onForward10,
                        onToggleMute = onToggleMute,
                        controlsColor = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Source selector
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "ソース選択",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = source == VideoSource.URL,
                            onClick = { source = VideoSource.URL },
                            label = { Text("URL") },
                        )
                        FilterChip(
                            selected = source == VideoSource.LOCAL,
                            onClick = { source = VideoSource.LOCAL },
                            label = { Text("ローカル") },
                        )
                    }
                    if (source == VideoSource.URL) {
                        Text(
                            text = "サンプル: Big Buck Bunny (Google CDN)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Button(
                            onClick = { pickVideo.launch("video/*") },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = if (localUri != null) "動画を変更" else "動画を選択")
                        }
                        localUri?.let { uri ->
                            Text(
                                text = uri.lastPathSegment ?: uri.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                            )
                        }
                    }
                }

                // Usage tips
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "操作方法",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = "• 横向きにすると全画面再生になります",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = "• 全画面中はタップでコントロールを表示/非表示",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = "• URLまたはローカルファイルを選んで再生",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun VideoControls(
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    isMuted: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onReplay10: () -> Unit,
    onForward10: () -> Unit,
    onToggleMute: () -> Unit,
    controlsColor: Color,
) {
    var seekingPosition by remember { mutableStateOf<Float?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatTime(
                    seekingPosition?.let { (it * duration).toLong() } ?: currentPosition,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = controlsColor,
            )
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.labelSmall,
                color = controlsColor,
            )
        }

        Slider(
            value = seekingPosition ?: (if (duration > 0) currentPosition.toFloat() / duration else 0f),
            onValueChange = { seekingPosition = it },
            onValueChangeFinished = {
                seekingPosition?.let { onSeek((it * duration).toLong()) }
                seekingPosition = null
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = controlsColor,
                activeTrackColor = controlsColor,
                inactiveTrackColor = controlsColor.copy(alpha = 0.3f),
            ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onToggleMute) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = if (isMuted) "Unmute" else "Mute",
                    tint = controlsColor,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onReplay10) {
                Icon(
                    imageVector = Icons.Default.Replay10,
                    contentDescription = "Replay 10s",
                    tint = controlsColor,
                    modifier = Modifier.size(28.dp),
                )
            }
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(56.dp),
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = controlsColor,
                    modifier = Modifier.size(40.dp),
                )
            }
            IconButton(onClick = onForward10) {
                Icon(
                    imageVector = Icons.Default.Forward10,
                    contentDescription = "Forward 10s",
                    tint = controlsColor,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
