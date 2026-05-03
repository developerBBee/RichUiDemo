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

    // Called when the user presses Home / switches apps — ideal moment to enter PiP
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && shouldEnterPip()) {
            enterPip()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun enterPip() {
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(4, 3))
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setAutoEnterEnabled(true)
                }
            }
            .build()
        enterPictureInPictureMode(params)
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
