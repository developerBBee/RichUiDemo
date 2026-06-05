package jp.developer.bbee.richuidemo.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// ============================================================
// Game data model
// ============================================================

private data class V2(val x: Float, val y: Float) {
    operator fun plus(o: V2) = V2(x + o.x, y + o.y)
    operator fun minus(o: V2) = V2(x - o.x, y - o.y)
    operator fun times(s: Float) = V2(x * s, y * s)
    fun dist(o: V2) = sqrt((x - o.x).pow(2) + (y - o.y).pow(2))
    fun len() = sqrt(x * x + y * y)
    fun norm() = len().let { l -> if (l < 1e-6f) V2(0f, 0f) else V2(x / l, y / l) }
    fun toOffset() = Offset(x, y)
}

private enum class GamePhase { PLAYING, GAME_OVER, CLEARED }

private data class Bullet(
    val pos: V2, val vel: V2, val radius: Float, val color: Color, val isPlayer: Boolean,
)

private data class Player(
    val pos: V2,
    val radius: Float = 10f,
    val lives: Int = 3,
    val invincible: Int = 0,
    val shootCooldown: Int = 0,
)

private data class Enemy(
    val pos: V2,
    val hp: Float,
    val maxHp: Float = 3000f,
    val spiralAngle: Float = 0f,
)

private data class BulletHellState(
    val w: Float,
    val h: Float,
    val player: Player,
    val enemy: Enemy,
    val bullets: List<Bullet>,
    val score: Int,
    val phase: GamePhase,
    val frame: Long,
    val stars: List<V2>,
)

// ============================================================
// Game initialization
// ============================================================

private fun initGame(w: Float, h: Float): BulletHellState {
    val rng = Random(42)
    return BulletHellState(
        w = w, h = h,
        player = Player(pos = V2(w / 2f, h * 0.82f)),
        enemy = Enemy(pos = V2(w / 2f, h * 0.15f), hp = 3000f),
        bullets = emptyList(),
        score = 0,
        phase = GamePhase.PLAYING,
        frame = 0L,
        stars = List(100) { V2(rng.nextFloat() * w, rng.nextFloat() * h) },
    )
}

// ============================================================
// Game logic
// ============================================================

private fun enemyPhase(e: Enemy) = when {
    e.hp / e.maxHp > 0.75f -> 0
    e.hp / e.maxHp > 0.50f -> 1
    e.hp / e.maxHp > 0.25f -> 2
    else -> 3
}

private fun dirVec(angleDeg: Float, speed: Float): V2 {
    val rad = angleDeg * PI.toFloat() / 180f
    return V2(cos(rad) * speed, sin(rad) * speed)
}

private fun spawnEnemyBullets(g: BulletHellState, enemyPos: V2): List<Bullet> {
    val e = g.enemy
    val f = g.frame
    val sa = e.spiralAngle
    val phase = enemyPhase(e)
    val aimedAngle = atan2(g.player.pos.y - enemyPos.y, g.player.pos.x - enemyPos.x) * 180f / PI.toFloat()
    val list = mutableListOf<Bullet>()

    when (phase) {
        0 -> {
            if (f % 60 == 0L) {
                repeat(12) { i ->
                    list += Bullet(enemyPos, dirVec(sa + i * 30f, 2.5f), 7f, Color(0xFFFF3333), false)
                }
            }
            if (f % 90 == 45L) {
                list += Bullet(enemyPos, dirVec(aimedAngle, 4f), 9f, Color(0xFFFF8800), false)
            }
        }
        1 -> {
            if (f % 3 == 0L) {
                repeat(3) { arm ->
                    list += Bullet(enemyPos, dirVec(sa + arm * 120f, 3.5f), 6f, Color(0xFFFF44FF), false)
                }
            }
            if (f % 60 == 0L) {
                for (i in -2..2) {
                    list += Bullet(enemyPos, dirVec(aimedAngle + i * 12f, 5f), 7f, Color(0xFF44FFFF), false)
                }
            }
        }
        2 -> {
            if (f % 2 == 0L) {
                repeat(5) { arm ->
                    list += Bullet(enemyPos, dirVec(sa * 1.5f + arm * 72f, 3f), 5f, Color(0xFFFFFF44), false)
                }
            }
            if (f % 50 == 0L) {
                repeat(16) { i ->
                    list += Bullet(enemyPos, dirVec(i * 22.5f, 4.5f), 6f, Color(0xFF44FF88), false)
                }
            }
            if (f % 40 == 20L) {
                for (i in -3..3) {
                    list += Bullet(enemyPos, dirVec(aimedAngle + i * 10f, 5.5f), 5f, Color(0xFFFF6644), false)
                }
            }
        }
        else -> {
            // 6-arm clockwise spiral every frame
            repeat(6) { arm ->
                list += Bullet(enemyPos, dirVec(sa * 2f + arm * 60f, 3.5f), 5f, Color(0xFFFF3333), false)
            }
            if (f % 2 == 0L) {
                repeat(4) { arm ->
                    list += Bullet(enemyPos, dirVec(-sa * 3f + arm * 90f, 4.5f), 5f, Color(0xFF4444FF), false)
                }
            }
            if (f % 25 == 0L) {
                for (i in -4..4) {
                    list += Bullet(enemyPos, dirVec(aimedAngle + i * 8f, 6f), 5f, Color(0xFFFFAA33), false)
                }
            }
            if (f % 40 == 20L) {
                repeat(24) { i ->
                    list += Bullet(enemyPos, dirVec(i * 15f + sa, 5f), 5f, Color(0xFFFF44FF), false)
                }
            }
        }
    }
    return list
}

private fun updateGame(g: BulletHellState, dragDelta: V2): BulletHellState {
    if (g.phase != GamePhase.PLAYING) return g

    // Move player
    val newPos = V2(
        (g.player.pos.x + dragDelta.x).coerceIn(g.player.radius, g.w - g.player.radius),
        (g.player.pos.y + dragDelta.y).coerceIn(g.player.radius + 40f, g.h - g.player.radius),
    )
    var player = g.player.copy(
        pos = newPos,
        invincible = (g.player.invincible - 1).coerceAtLeast(0),
        shootCooldown = (g.player.shootCooldown - 1).coerceAtLeast(0),
    )

    // Player fires
    val newPlayerBullets = mutableListOf<Bullet>()
    if (player.shootCooldown <= 0) {
        newPlayerBullets += Bullet(
            pos = player.pos + V2(0f, -player.radius - 6f),
            vel = V2(0f, -14f),
            radius = 5f, color = Color.Cyan, isPlayer = true,
        )
        // Side shots when enemy is in phase 2+
        if (enemyPhase(g.enemy) >= 2) {
            newPlayerBullets += Bullet(player.pos + V2(-8f, -player.radius), V2(-2f, -13f), 4f, Color(0xFF00EEEE), true)
            newPlayerBullets += Bullet(player.pos + V2(8f, -player.radius), V2(2f, -13f), 4f, Color(0xFF00EEEE), true)
        }
        player = player.copy(shootCooldown = 6)
    }

    // Enemy oscillates horizontally
    val ex = g.w / 2f + sin(g.frame * 0.025f) * g.w * 0.28f
    val newEnemy = g.enemy.copy(
        pos = V2(ex, g.enemy.pos.y),
        spiralAngle = g.enemy.spiralAngle + 2.5f,
    )

    // Generate enemy bullets using the updated enemy position
    val newEnemyBullets = spawnEnemyBullets(g, newEnemy.pos)

    // Move all bullets and cull out-of-bounds
    val allBullets = (g.bullets + newPlayerBullets + newEnemyBullets).mapNotNull { b ->
        val np = b.pos + b.vel
        if (np.x < -40f || np.x > g.w + 40f || np.y < -40f || np.y > g.h + 40f) null
        else b.copy(pos = np)
    }

    // Cap total bullet count for performance
    val cappedBullets = if (allBullets.size > 700) allBullets.drop(allBullets.size - 700) else allBullets

    // Collision: player bullets hit enemy
    var enemyHp = newEnemy.hp
    var score = g.score
    val afterPlayerHits = cappedBullets.filter { b ->
        if (b.isPlayer && b.pos.dist(newEnemy.pos) < 42f + b.radius) {
            enemyHp -= 8f
            score += 8
            false
        } else true
    }

    // Collision: enemy bullets hit player (skip if invincible)
    var lives = player.lives
    var invincible = player.invincible
    val afterEnemyHits = afterPlayerHits.filter { b ->
        if (!b.isPlayer && invincible <= 0 && b.pos.dist(player.pos) < player.radius + b.radius - 2f) {
            lives--
            invincible = 120
            false
        } else true
    }

    val finalPhase = when {
        lives <= 0 -> GamePhase.GAME_OVER
        enemyHp <= 0f -> GamePhase.CLEARED
        else -> GamePhase.PLAYING
    }

    return g.copy(
        player = player.copy(lives = lives, invincible = invincible),
        enemy = newEnemy.copy(hp = enemyHp.coerceAtLeast(0f)),
        bullets = afterEnemyHits,
        score = score,
        phase = finalPhase,
        frame = g.frame + 1L,
    )
}

// ============================================================
// Rendering helpers
// ============================================================

private fun DrawScope.drawStars(stars: List<V2>, frame: Long) {
    stars.forEachIndexed { i, s ->
        val alpha = (0.3f + 0.35f * (sin(frame * 0.02f + i * 0.7f) * 0.5f + 0.5f)).toFloat()
        drawCircle(Color.White.copy(alpha = alpha), radius = 1.5f, center = s.toOffset())
    }
}

private fun DrawScope.drawEnemy(enemy: Enemy) {
    val hpRatio = (enemy.hp / enemy.maxHp).coerceIn(0f, 1f)
    val phase = enemyPhase(enemy)
    val coreColor = when (phase) {
        0 -> Color(0xFF4488FF)
        1 -> Color(0xFFAA44FF)
        2 -> Color(0xFFFF4488)
        else -> Color(0xFFFF2222)
    }
    val c = enemy.pos.toOffset()

    // Outer glow
    drawCircle(coreColor.copy(alpha = 0.15f), radius = 60f, center = c)
    drawCircle(coreColor.copy(alpha = 0.25f), radius = 48f, center = c)

    // Rotating hex arms
    repeat(6) { seg ->
        rotate(enemy.spiralAngle + seg * 60f, pivot = c) {
            drawLine(
                color = coreColor.copy(alpha = 0.8f),
                start = c, end = c + Offset(42f, 0f), strokeWidth = 3f,
            )
        }
    }
    // Counter-rotating arms for higher phases
    if (phase >= 2) {
        repeat(4) { seg ->
            rotate(-enemy.spiralAngle * 1.5f + seg * 90f, pivot = c) {
                drawLine(
                    color = Color.White.copy(alpha = 0.4f),
                    start = c + Offset(14f, 0f), end = c + Offset(38f, 0f), strokeWidth = 2f,
                )
            }
        }
    }

    // Core
    drawCircle(coreColor, radius = 30f, center = c)
    drawCircle(Color.White.copy(alpha = 0.5f), radius = 14f, center = c)
    drawCircle(coreColor.copy(alpha = 0.3f), radius = 30f, center = c, style = Stroke(2f))

    // HP bar near top of screen
    val barW = (size.width * 0.55f).coerceAtMost(260f)
    val barH = 10f
    val barX = size.width / 2f - barW / 2f
    val barY = 10f
    drawRoundRect(Color(0xFF222233), topLeft = Offset(barX, barY), size = Size(barW, barH), cornerRadius = CornerRadius(4f))
    drawRoundRect(coreColor, topLeft = Offset(barX, barY), size = Size(barW * hpRatio, barH), cornerRadius = CornerRadius(4f))
    drawRoundRect(Color.White.copy(alpha = 0.25f), topLeft = Offset(barX, barY), size = Size(barW, barH), cornerRadius = CornerRadius(4f), style = Stroke(1f))
}

private fun DrawScope.drawPlayer(player: Player) {
    val c = player.pos.toOffset()
    val r = player.radius

    // Engine exhaust glow
    drawCircle(Color(0xFF3366FF).copy(alpha = 0.5f), radius = r * 1.1f, center = c + Offset(0f, r * 0.9f))

    // Ship body: upward-pointing triangle
    val body = Path().apply {
        moveTo(c.x, c.y - r * 1.9f)
        lineTo(c.x - r * 1.1f, c.y + r * 0.6f)
        lineTo(c.x, c.y + r * 0.1f)
        lineTo(c.x + r * 1.1f, c.y + r * 0.6f)
        close()
    }
    drawPath(body, Color(0xFF33BBFF))
    drawPath(body, Color.White.copy(alpha = 0.35f), style = Stroke(1.5f))

    // Wing accents
    drawLine(Color(0xFF66DDFF), c + Offset(-r * 0.5f, -r * 0.2f), c + Offset(-r * 1.0f, r * 0.5f), 1.5f)
    drawLine(Color(0xFF66DDFF), c + Offset(r * 0.5f, -r * 0.2f), c + Offset(r * 1.0f, r * 0.5f), 1.5f)

    // Hitbox dot
    drawCircle(Color.White.copy(alpha = 0.9f), radius = 2.5f, center = c)
}

private fun DrawScope.drawBullet(b: Bullet) {
    val c = b.pos.toOffset()
    if (b.isPlayer) {
        drawLine(
            b.color.copy(alpha = 0.4f),
            start = c + Offset(0f, b.radius * 2.5f),
            end = c - Offset(0f, b.radius * 2.5f),
            strokeWidth = b.radius * 1.8f,
        )
        drawCircle(b.color, radius = b.radius, center = c)
    } else {
        drawCircle(b.color.copy(alpha = 0.35f), radius = b.radius + 3f, center = c)
        drawCircle(b.color, radius = b.radius, center = c)
        drawCircle(Color.White.copy(alpha = 0.65f), radius = b.radius * 0.38f, center = c)
    }
}

// ============================================================
// Screen composable
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulletHellScreen(onBack: () -> Unit) {
    var gameState by remember { mutableStateOf<BulletHellState?>(null) }
    var dragDelta by remember { mutableStateOf(V2(0f, 0f)) }
    val textMeasurer = rememberTextMeasurer()

    // Main game loop: runs every frame, updates state when initialized
    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameMillis { _ ->
                val delta = dragDelta
                dragDelta = V2(0f, 0f)
                gameState = gameState?.let { updateGame(it, delta) }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF05050F)),
    ) {
        TopAppBar(
            title = { Text("弾幕シューティング", color = Color.White) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0A1E)),
        )

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val w = constraints.maxWidth.toFloat()
            val h = constraints.maxHeight.toFloat()

            // Initialize game once dimensions are known
            LaunchedEffect(w, h) {
                if (w > 0f && h > 0f) {
                    gameState = initGame(w, h)
                }
            }

            val g = gameState

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { _, amount ->
                            dragDelta = V2(dragDelta.x + amount.x, dragDelta.y + amount.y)
                        }
                    },
            ) {
                if (g == null) {
                    drawRect(Color(0xFF05050F))
                    return@Canvas
                }

                // Background
                drawRect(Color(0xFF05050F))

                // Stars
                drawStars(g.stars, g.frame)

                // Enemy
                drawEnemy(g.enemy)

                // Enemy bullets (drawn first, below player bullets)
                g.bullets.forEach { if (!it.isPlayer) drawBullet(it) }

                // Player bullets
                g.bullets.forEach { if (it.isPlayer) drawBullet(it) }

                // Player — blinks during invincibility
                if (g.player.invincible <= 0 || (g.frame / 4L) % 2L == 0L) {
                    drawPlayer(g.player)
                }

                // HUD
                val hudStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                val redStyle = TextStyle(color = Color(0xFFFF4466), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                val phaseLabel = TextStyle(color = Color(0xFFAABBFF), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                drawText(textMeasurer, "SCORE  ${g.score}", topLeft = Offset(12f, 30f), style = hudStyle)

                val livesStr = "♥".repeat(g.player.lives) + "♡".repeat((3 - g.player.lives).coerceAtLeast(0))
                drawText(textMeasurer, livesStr, topLeft = Offset(size.width - 72f, 30f), style = redStyle)

                val phaseNum = when (enemyPhase(g.enemy)) { 0 -> "Ⅰ"; 1 -> "Ⅱ"; 2 -> "Ⅲ"; else -> "Ⅳ !!!" }
                drawText(textMeasurer, "Phase $phaseNum", topLeft = Offset(size.width / 2f - 32f, 26f), style = phaseLabel)
            }

            // Game Over overlay
            if (g?.phase == GamePhase.GAME_OVER) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("GAME OVER", color = Color(0xFFFF4444), fontSize = 40.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Score: ${g.score}", color = Color.White, fontSize = 22.sp)
                        Spacer(Modifier.height(28.dp))
                        Button(
                            onClick = { gameState = initGame(g.w, g.h) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333355)),
                        ) {
                            Text("リトライ", color = Color.White, fontSize = 16.sp)
                        }
                    }
                }
            }

            // Stage Clear overlay
            if (g?.phase == GamePhase.CLEARED) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("STAGE CLEAR!", color = Color(0xFF44FF88), fontSize = 38.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Score: ${g.score}", color = Color.White, fontSize = 22.sp)
                        Spacer(Modifier.height(28.dp))
                        Button(
                            onClick = { gameState = initGame(g.w, g.h) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF224433)),
                        ) {
                            Text("もう一度", color = Color.White, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
