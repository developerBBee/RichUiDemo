package jp.developer.bbee.richuidemo.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

@Composable
fun AnimatedSurfaceCard(
    modifier: Modifier = Modifier,
    backGroundColor: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = RoundedCornerShape(size = 0.dp),
    effectColor: Color = Color.DarkGray,
    gradient: Brush = Brush.horizontalGradient(
        listOf(Color.Transparent, effectColor, Color.Transparent),
    ),
    blendMode: BlendMode = if (isSystemInDarkTheme()) BlendMode.Plus else BlendMode.Multiply,
    animationDuration: Int = 500.milliseconds.toInt(DurationUnit.MILLISECONDS),
    animationInterval: Int = 500.milliseconds.toInt(DurationUnit.MILLISECONDS),
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite Color Animation")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = animationDuration + animationInterval
                0f at animationInterval with LinearEasing
                2f at durationMillis with LinearEasing
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "Gradient Offset",
    )

    Surface(
        modifier = modifier,
        shape = shape,
        color = backGroundColor,
    ) {
        Surface(
            modifier = Modifier
                .drawWithContent {
                    drawContent()
                    translate(
                        left = (gradientOffset - 1.0f) * 2.0f * size.width,
                    ) {
                        drawRect(
                            brush = gradient,
                            blendMode = blendMode,
                        )
                    }
                },
            color = Color.Transparent
        ) {
            content()
        }
    }
}

@Preview
@Composable
fun AnimatedSurfaceCardPreview() {
    AnimatedSurfaceCard(
        modifier = Modifier
            .height(100.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Animated Surface Card Preview",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}