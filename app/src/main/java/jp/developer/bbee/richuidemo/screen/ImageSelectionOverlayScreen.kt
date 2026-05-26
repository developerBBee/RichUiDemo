package jp.developer.bbee.richuidemo.screen

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.ui.input.pointer.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import jp.developer.bbee.richuidemo.component.BackNavigationIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class SelectionCorner { TopLeft, TopRight, BottomLeft, BottomRight }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSelectionOverlayScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selection by remember { mutableStateOf<Rect?>(null) }
    var canvasSizePx by remember { mutableStateOf(IntSize.Zero) }
    val imageBitmap = remember { createSampleBitmap().asImageBitmap() }

    // Canvas pixel coords become stale on size changes (e.g. rotation); clear selection.
    LaunchedEffect(canvasSizePx) {
        selection = null
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val sel = selection ?: return@rememberLauncherForActivityResult
            scope.launch(Dispatchers.IO) {
                val msg = saveToStorage(context, imageBitmap, sel, canvasSizePx)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "保存には権限が必要です", Toast.LENGTH_SHORT).show()
        }
    }

    fun onSave() {
        val sel = selection ?: return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            scope.launch(Dispatchers.IO) {
                val msg = saveToStorage(context, imageBitmap, sel, canvasSizePx)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Image Selection Overlay") },
                navigationIcon = { BackNavigationIcon(onClick = onBack) },
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selection != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                ExtendedFloatingActionButton(
                    onClick = ::onSave,
                    icon = { Icon(Icons.Default.Save, contentDescription = null) },
                    text = { Text("選択範囲を保存") },
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Text(
                text = if (selection == null) {
                    "画像上をドラッグして選択範囲を作成"
                } else {
                    "四隅のハンドルをドラッグして調整 · 枠外タップで解除"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .onSizeChanged { canvasSizePx = it },
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawImage(
                        image = imageBitmap,
                        dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                    )
                }

                val handleRadiusDp = 14.dp
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            val handleRadiusPx = handleRadiusDp.toPx()
                            val touchTarget = handleRadiusPx * 2f

                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val downPos = down.position
                                val sel = selection
                                val cornerHit = sel?.let {
                                    findNearCorner(it, downPos, touchTarget)
                                }

                                if (cornerHit != null) {
                                    down.consume()
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull {
                                            it.id == down.id
                                        } ?: break
                                        if (!change.pressed) break
                                        val current = selection ?: break
                                        selection = current.resizeCorner(
                                            cornerHit,
                                            change.position.coerceInSize(size),
                                        )
                                        change.consume()
                                    }
                                } else {
                                    var dragging = false
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull {
                                            it.id == down.id
                                        } ?: break
                                        if (!change.pressed) break
                                        val dist = (change.position - downPos).getDistance()
                                        if (!dragging && dist > viewConfiguration.touchSlop) {
                                            dragging = true
                                        }
                                        if (dragging) {
                                            selection = normalizeRect(
                                                downPos.coerceInSize(size),
                                                change.position.coerceInSize(size),
                                            )
                                            change.consume()
                                        }
                                    }
                                    if (!dragging) selection = null
                                }
                            }
                        },
                ) {
                    val sel = selection
                    if (sel != null) {
                        drawSelectionOverlay(sel, handleRadiusDp.toPx())
                    }
                }
            }

            val sel = selection
            if (sel != null && canvasSizePx.width > 0) {
                val scaleX = imageBitmap.width.toFloat() / canvasSizePx.width
                val scaleY = imageBitmap.height.toFloat() / canvasSizePx.height
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "選択サイズ: ${(sel.width * scaleX).toInt()} × ${(sel.height * scaleY).toInt()} px",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

private fun DrawScope.drawSelectionOverlay(sel: Rect, handleRadius: Float) {
    val dim = Color.Black.copy(alpha = 0.5f)
    drawRect(dim, topLeft = Offset.Zero, size = Size(size.width, sel.top))
    drawRect(dim, topLeft = Offset(0f, sel.bottom), size = Size(size.width, size.height - sel.bottom))
    drawRect(dim, topLeft = Offset(0f, sel.top), size = Size(sel.left, sel.height))
    drawRect(dim, topLeft = Offset(sel.right, sel.top), size = Size(size.width - sel.right, sel.height))

    drawRect(
        color = Color.White,
        topLeft = sel.topLeft,
        size = sel.size,
        style = Stroke(width = 2.dp.toPx()),
    )

    val thirdW = sel.width / 3f
    val thirdH = sel.height / 3f
    for (i in 1..2) {
        drawLine(
            color = Color.White.copy(alpha = 0.35f),
            start = Offset(sel.left + thirdW * i, sel.top),
            end = Offset(sel.left + thirdW * i, sel.bottom),
            strokeWidth = 1f,
        )
        drawLine(
            color = Color.White.copy(alpha = 0.35f),
            start = Offset(sel.left, sel.top + thirdH * i),
            end = Offset(sel.right, sel.top + thirdH * i),
            strokeWidth = 1f,
        )
    }

    for (corner in listOf(sel.topLeft, sel.topRight, sel.bottomLeft, sel.bottomRight)) {
        drawCircle(color = Color.White, radius = handleRadius, center = corner)
        drawCircle(color = Color(0xFF1A1A1A), radius = handleRadius * 0.45f, center = corner)
    }
}

private fun findNearCorner(rect: Rect, pos: Offset, radius: Float): SelectionCorner? =
    listOf(
        SelectionCorner.TopLeft to rect.topLeft,
        SelectionCorner.TopRight to rect.topRight,
        SelectionCorner.BottomLeft to rect.bottomLeft,
        SelectionCorner.BottomRight to rect.bottomRight,
    ).firstOrNull { (_, c) -> (pos - c).getDistance() <= radius }?.first

private fun Rect.resizeCorner(corner: SelectionCorner, pos: Offset): Rect {
    val min = 40f
    return when (corner) {
        SelectionCorner.TopLeft -> Rect(
            left = pos.x.coerceAtMost(right - min),
            top = pos.y.coerceAtMost(bottom - min),
            right = right,
            bottom = bottom,
        )
        SelectionCorner.TopRight -> Rect(
            left = left,
            top = pos.y.coerceAtMost(bottom - min),
            right = pos.x.coerceAtLeast(left + min),
            bottom = bottom,
        )
        SelectionCorner.BottomLeft -> Rect(
            left = pos.x.coerceAtMost(right - min),
            top = top,
            right = right,
            bottom = pos.y.coerceAtLeast(top + min),
        )
        SelectionCorner.BottomRight -> Rect(
            left = left,
            top = top,
            right = pos.x.coerceAtLeast(left + min),
            bottom = pos.y.coerceAtLeast(top + min),
        )
    }
}

private fun normalizeRect(a: Offset, b: Offset) = Rect(
    left = minOf(a.x, b.x),
    top = minOf(a.y, b.y),
    right = maxOf(a.x, b.x),
    bottom = maxOf(a.y, b.y),
)

private fun Offset.coerceInSize(size: IntSize) = Offset(
    x.coerceIn(0f, size.width.toFloat()),
    y.coerceIn(0f, size.height.toFloat()),
)

private fun saveToStorage(
    context: Context,
    imageBitmap: ImageBitmap,
    selection: Rect,
    canvasSize: IntSize,
): String {
    if (canvasSize.width <= 0 || canvasSize.height <= 0) return "エラー: 画像サイズが不明です"

    val scaleX = imageBitmap.width.toFloat() / canvasSize.width
    val scaleY = imageBitmap.height.toFloat() / canvasSize.height

    val srcX = (selection.left * scaleX).toInt().coerceIn(0, imageBitmap.width - 1)
    val srcY = (selection.top * scaleY).toInt().coerceIn(0, imageBitmap.height - 1)
    val srcW = (selection.width * scaleX).toInt()
        .coerceAtLeast(1)
        .coerceAtMost(imageBitmap.width - srcX)
    val srcH = (selection.height * scaleY).toInt()
        .coerceAtLeast(1)
        .coerceAtMost(imageBitmap.height - srcY)

    val cropped = Bitmap.createBitmap(imageBitmap.asAndroidBitmap(), srcX, srcY, srcW, srcH)
    val filename = "selection_${System.currentTimeMillis()}.png"

    return try {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/RichUiDemo",
                )
            } else {
                // On pre-Q, use DATA to pin the save location to Pictures/RichUiDemo/
                @Suppress("DEPRECATION")
                val dir = java.io.File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "RichUiDemo",
                )
                if (!dir.exists() && !dir.mkdirs()) {
                    return "エラー: ディレクトリの作成に失敗しました"
                }
                @Suppress("DEPRECATION")
                put(MediaStore.Images.Media.DATA, java.io.File(dir, filename).absolutePath)
            }
        }
        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values,
        ) ?: return "エラー: URI の取得に失敗しました"

        val stream = context.contentResolver.openOutputStream(uri)
        if (stream == null) {
            context.contentResolver.delete(uri, null, null)
            return "エラー: OutputStream を開けませんでした"
        }
        val ok = stream.use { out -> cropped.compress(Bitmap.CompressFormat.PNG, 100, out) }
        if (!ok) {
            context.contentResolver.delete(uri, null, null)
            return "エラー: 画像の書き込みに失敗しました"
        }

        "保存しました: Pictures/RichUiDemo/$filename"
    } catch (e: Exception) {
        "保存に失敗しました: ${e.message}"
    }
}

private fun createSampleBitmap(): Bitmap {
    val w = 1200
    val h = 900
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Sky gradient
    paint.shader = LinearGradient(
        0f, 0f, 0f, h * 0.58f,
        intArrayOf(0xFF4FC3F7.toInt(), 0xFFB3E5FC.toInt(), 0xFFE1F5FE.toInt()),
        null,
        Shader.TileMode.CLAMP,
    )
    canvas.drawRect(0f, 0f, w.toFloat(), h * 0.58f, paint)
    paint.shader = null

    // Ground gradient
    paint.shader = LinearGradient(
        0f, h * 0.58f, 0f, h.toFloat(),
        intArrayOf(0xFF66BB6A.toInt(), 0xFF388E3C.toInt()),
        null,
        Shader.TileMode.CLAMP,
    )
    canvas.drawRect(0f, h * 0.58f, w.toFloat(), h.toFloat(), paint)
    paint.shader = null

    // Sun glow
    paint.color = 0x33FFD700.toInt()
    canvas.drawCircle(w * 0.82f, h * 0.18f, 110f, paint)
    // Sun core
    paint.color = 0xFFFFD600.toInt()
    canvas.drawCircle(w * 0.82f, h * 0.18f, 72f, paint)

    // Mountains (back layer — lighter)
    paint.color = 0xFF90A4AE.toInt()
    val backMtn = Path().apply {
        moveTo(0f, h * 0.58f)
        lineTo(w * 0.12f, h * 0.32f)
        lineTo(w * 0.28f, h * 0.50f)
        lineTo(w * 0.42f, h * 0.28f)
        lineTo(w * 0.58f, h * 0.46f)
        lineTo(w * 0.70f, h * 0.30f)
        lineTo(w * 0.85f, h * 0.50f)
        lineTo(w.toFloat(), h * 0.38f)
        lineTo(w.toFloat(), h * 0.58f)
        close()
    }
    canvas.drawPath(backMtn, paint)

    // Mountains (front layer — darker)
    paint.color = 0xFF546E7A.toInt()
    val frontMtn = Path().apply {
        moveTo(0f, h * 0.58f)
        lineTo(w * 0.08f, h * 0.42f)
        lineTo(w * 0.22f, h * 0.55f)
        lineTo(w * 0.35f, h * 0.36f)
        lineTo(w * 0.50f, h * 0.52f)
        lineTo(w * 0.63f, h * 0.40f)
        lineTo(w * 0.78f, h * 0.56f)
        lineTo(w * 0.92f, h * 0.44f)
        lineTo(w.toFloat(), h * 0.58f)
        close()
    }
    canvas.drawPath(frontMtn, paint)

    // Clouds
    paint.color = 0xCCFFFFFF.toInt()
    for ((cx, cy, r) in listOf(
        Triple(180f, h * 0.12f, 55f),
        Triple(280f, h * 0.10f, 70f),
        Triple(380f, h * 0.12f, 50f),
        Triple(560f, h * 0.09f, 48f),
        Triple(660f, h * 0.07f, 65f),
        Triple(760f, h * 0.09f, 52f),
        Triple(950f, h * 0.14f, 44f),
        Triple(1040f, h * 0.12f, 58f),
        Triple(1130f, h * 0.14f, 40f),
    )) {
        canvas.drawCircle(cx, cy, r, paint)
    }

    // Trees
    for (i in 0..6) {
        val tx = w * (0.05f + i * 0.135f)
        val ty = h * 0.58f
        val th = h * 0.14f + (i % 3) * h * 0.03f

        // Trunk
        paint.color = 0xFF5D4037.toInt()
        canvas.drawRect(tx - 9f, ty, tx + 9f, ty + 32f, paint)

        // Foliage (2 layers)
        paint.color = 0xFF2E7D32.toInt()
        val tri1 = Path().apply {
            moveTo(tx, ty - th)
            lineTo(tx - th * 0.38f, ty - th * 0.28f)
            lineTo(tx + th * 0.38f, ty - th * 0.28f)
            close()
        }
        canvas.drawPath(tri1, paint)

        paint.color = 0xFF388E3C.toInt()
        val tri2 = Path().apply {
            moveTo(tx, ty - th * 0.55f)
            lineTo(tx - th * 0.48f, ty)
            lineTo(tx + th * 0.48f, ty)
            close()
        }
        canvas.drawPath(tri2, paint)
    }

    // Wildflowers in foreground
    val flowerColors = listOf(
        0xFFFF8F00.toInt(), 0xFFE91E63.toInt(),
        0xFFFFEB3B.toInt(), 0xFFAB47BC.toInt(), 0xFFFF5722.toInt(),
    )
    val rng = java.util.Random(42)
    for (i in 0..80) {
        val fx = rng.nextFloat() * w
        val fy = h * 0.62f + rng.nextFloat() * h * 0.35f
        paint.color = flowerColors[i % flowerColors.size]
        canvas.drawCircle(fx, fy, 6f + rng.nextFloat() * 6f, paint)
        paint.color = 0xFF33691E.toInt()
        canvas.drawLine(fx, fy, fx, fy + 18f + rng.nextFloat() * 14f, paint)
    }

    // Water reflection strip
    paint.color = 0x554FC3F7.toInt()
    canvas.drawRect(w * 0.3f, h * 0.60f, w * 0.65f, h * 0.72f, paint)
    paint.color = 0x33FFFFFF.toInt()
    for (j in 0..4) {
        val ry = h * 0.615f + j * 16f
        canvas.drawLine(w * 0.32f, ry, w * 0.63f, ry, paint)
    }

    return bmp
}
