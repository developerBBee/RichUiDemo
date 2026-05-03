package jp.developer.bbee.richuidemo

import android.app.PictureInPictureParams
import android.app.PictureInPictureUiState
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.app.PictureInPictureParamsCompat
import androidx.core.app.PictureInPictureProvider
import androidx.core.app.PictureInPictureUiStateCompat
import androidx.core.content.ContextCompat
import androidx.core.pip.BasicPictureInPicture
import androidx.core.pip.PictureInPictureDelegate
import androidx.core.util.Consumer
import jp.developer.bbee.richuidemo.navigation.AppNavigation
import jp.developer.bbee.richuidemo.ui.theme.RichUiDemoTheme

class MainActivity : ComponentActivity(), PictureInPictureProvider {

    val isInPipMode = mutableStateOf(false)

    private val basicPip by lazy { BasicPictureInPicture(this) }

    private val uiStateListeners = mutableListOf<Consumer<PictureInPictureUiStateCompat>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        basicPip.setAspectRatio(Rational(4, 3))
        basicPip.addOnPictureInPictureEventListener(
            ContextCompat.getMainExecutor(this),
            object : PictureInPictureDelegate.OnPictureInPictureEventListener {
                override fun onPictureInPictureEvent(event: PictureInPictureDelegate.Event, config: Configuration?) {
                    when (event) {
                        PictureInPictureDelegate.Event.ENTERED -> isInPipMode.value = true
                        PictureInPictureDelegate.Event.EXITED -> isInPipMode.value = false
                    }
                }
            },
        )

        setContent {
            RichUiDemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNavigation()
                }
            }
        }
    }

    fun setPipEnabled(enabled: Boolean) {
        basicPip.setEnabled(enabled)
    }

    // PictureInPictureProvider

    override fun enterPictureInPictureMode(params: PictureInPictureParamsCompat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            enterPictureInPictureMode(params.toFrameworkParams())
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            @Suppress("DEPRECATION")
            enterPictureInPictureMode()
        }
    }

    override fun setPictureInPictureParams(params: PictureInPictureParamsCompat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setPictureInPictureParams(params.toFrameworkParams())
        }
    }

    override fun addOnPictureInPictureUiStateChangedListener(listener: Consumer<PictureInPictureUiStateCompat>) {
        uiStateListeners.add(listener)
    }

    override fun removeOnPictureInPictureUiStateChangedListener(listener: Consumer<PictureInPictureUiStateCompat>) {
        uiStateListeners.remove(listener)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onPictureInPictureUiStateChanged(state: PictureInPictureUiState) {
        super.onPictureInPictureUiStateChanged(state)
        val compat = PictureInPictureUiStateCompat.fromPictureInPictureUiState(state)
        uiStateListeners.forEach { it.accept(compat) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun PictureInPictureParamsCompat.toFrameworkParams(): PictureInPictureParams {
        val builder = PictureInPictureParams.Builder()
        aspectRatio?.let { builder.setAspectRatio(it) }
        if (actions.isNotEmpty()) builder.setActions(actions)
        sourceRectHint?.let { builder.setSourceRectHint(it) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setAutoEnterEnabled(isEnabled)
            builder.setSeamlessResizeEnabled(isSeamlessResizeEnabled)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            expandedAspectRatio?.let { builder.setExpandedAspectRatio(it) }
            closeAction?.let { builder.setCloseAction(it) }
            title?.let { builder.setTitle(it) }
            subTitle?.let { builder.setSubtitle(it) }
        }
        return builder.build()
    }
}
