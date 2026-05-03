package jp.developer.bbee.richuidemo

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.app.PictureInPictureParams
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import jp.developer.bbee.richuidemo.navigation.AppNavigation
import jp.developer.bbee.richuidemo.ui.theme.RichUiDemoTheme

class MainActivity : ComponentActivity() {

    // Observed by AppNavigation to switch to full-screen PiP UI
    val isInPipMode = mutableStateOf(false)

    // Set by AppNavigation so this activity knows when PiP is active
    var shouldEnterPip: () -> Boolean = { false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

    // Called from AppNavigation via SideEffect whenever pipState.isVisible changes.
    // On API 31+, setPictureInPictureParams must be called *before* backgrounding so
    // setAutoEnterEnabled takes effect (passing it only in enterPictureInPictureMode is a no-op).
    fun updatePipParams() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val builder = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(4, 3))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                builder.setAutoEnterEnabled(shouldEnterPip())
            }
            setPictureInPictureParams(builder.build())
        }
    }

    // Fallback for API 26-30 (gesture nav does not call onUserLeaveHint on Android 10-11,
    // but button nav does — keep this as a safety net for older devices / button navigation).
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && shouldEnterPip()) {
            enterPictureInPictureMode(
                PictureInPictureParams.Builder().setAspectRatio(Rational(4, 3)).build()
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode.value = isInPictureInPictureMode
    }
}
