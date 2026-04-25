package jp.developer.bbee.richuidemo.component

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import jp.developer.bbee.richuidemo.R

@Composable
fun BackNavigationIcon(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_back),
            contentDescription = "Back",
        )
    }
}
