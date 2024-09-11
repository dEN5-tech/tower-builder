package com.den5.towerbuilder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.den5.towerbuilder.ui.theme.TowerBuilderGameTheme
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue
import androidx.compose.ui.geometry.Size as ComposeSize
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TowerBuilderGameTheme {
                var gameState by remember { mutableStateOf<GameState>(GameState.MainMenu) }
                var highScore by remember { mutableStateOf(0) }
                var soundEffectsEnabled by remember { mutableStateOf(true) }
                var musicEnabled by remember { mutableStateOf(true) }

                when (gameState) {
                    is GameState.MainMenu -> MainScreen(
                        onStartGame = { gameState = GameState.Playing },
                        onOpenSettings = { gameState = GameState.Settings },
                        highScore = highScore
                    )
                    is GameState.Playing -> GameScreen(
                        onGameOver = { score ->
                            if (score > highScore) highScore = score
                            gameState = GameState.GameOver(score)
                        },
                        soundEffectsEnabled = soundEffectsEnabled,
                        musicEnabled = musicEnabled
                    )
                    is GameState.GameOver -> GameOverScreen(
                        score = (gameState as GameState.GameOver).score,
                        onPlayAgain = { gameState = GameState.Playing },
                        highScore = highScore
                    )
                    is GameState.Settings -> SettingsScreen(
                        soundEffectsEnabled = soundEffectsEnabled,
                        musicEnabled = musicEnabled,
                        onSoundEffectsToggle = { soundEffectsEnabled = it },
                        onMusicToggle = { musicEnabled = it },
                        onBack = { gameState = GameState.MainMenu }
                    )
                }
            }
        }
    }
}

sealed class GameState {
    object MainMenu : GameState()
    object Playing : GameState()
    data class GameOver(val score: Int) : GameState()
    object Settings : GameState()
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun GameScreen(onGameOver: (Int) -> Unit, soundEffectsEnabled: Boolean, musicEnabled: Boolean) {
    var score by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val screenWidth = with(density) { 360.dp.toPx() }
    val screenHeight = with(density) { 640.dp.toPx() }
    val blockWidth = with(density) { 60.dp.toPx() }
    val blockHeight = with(density) { 20.dp.toPx() }
    var tower by remember { mutableStateOf(listOf(Block(blockWidth, blockHeight, 0f))) }
    var currentBlock by remember { mutableStateOf(Block(blockWidth, blockHeight, tower.last().y + blockHeight)) }
    var gameSpeed by remember { mutableStateOf(2f) }
    val textMeasurer = rememberTextMeasurer()

    var particles by remember { mutableStateOf(emptyList<Particle>()) }

    val infiniteTransition = rememberInfiniteTransition()
    val blockPosition by infiniteTransition.animateFloat(
        initialValue = -blockWidth / 2,
        targetValue = screenWidth - blockWidth / 2,
        animationSpec = infiniteRepeatable(
            animation = tween(2000 / gameSpeed.toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            currentBlock = currentBlock.copy(x = blockPosition)
            if (currentBlock.x > screenWidth - blockWidth / 2 || currentBlock.x < -blockWidth / 2) {
                gameSpeed = -gameSpeed
            }
            particles = particles.filter { it.lifetime > 0 }.map { it.update() }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBackground(screenWidth, screenHeight)
            drawTower(tower, screenWidth, screenHeight)
            drawCurrentBlock(currentBlock, screenWidth, screenHeight)
            drawScore(score, textMeasurer)
            drawSpeedIndicator(gameSpeed)
            drawParticles(particles)
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .clickable {
                val result = handleBlockPlacement(
                    tower,
                    currentBlock,
                    score,
                    gameSpeed,
                    soundEffectsEnabled,
                    screenWidth,
                    screenHeight
                )
                tower = result.tower
                currentBlock = result.currentBlock
                score = result.score
                gameSpeed = result.gameSpeed
                if (result.gameOver) {
                    onGameOver(score)
                } else {
                    particles = generateParticles(currentBlock)
                }
            }
        )
    }

    LaunchedEffect(musicEnabled) {
        if (musicEnabled) {
            // Начать проигрывание фоновой музыки
        } else {
            // Остановить проигрывание фоновой музыки
        }
    }
}

@Composable
fun MainScreen(onStartGame: () -> Unit, onOpenSettings: () -> Unit, highScore: Int) {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBackground(size.width, size.height)
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Постройка башни", style = MaterialTheme.typography.headlineLarge, color = Color.White)
            Spacer(modifier = Modifier.height(20.dp))
            Text("Лучший результат: $highScore", style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Spacer(modifier = Modifier.height(40.dp))
            Button(onClick = onStartGame) {
                Text("Начать игру")
            }
        }
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = "Настройки", tint = Color.White)
        }
    }
}

fun DrawScope.drawBackground(screenWidth: Float, screenHeight: Float) {
    // Градиентный фон
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1A237E), Color(0xFF3949AB), Color(0xFF5C6BC0))
        ),
        size = ComposeSize(screenWidth, screenHeight)
    )

    // Звезды
    repeat(100) {
        drawCircle(
            color = Color.White.copy(alpha = Random.nextFloat()),
            radius = Random.nextFloat() * 2f,
            center = Offset(Random.nextFloat() * screenWidth, Random.nextFloat() * screenHeight)
        )
    }

    // Луна
    drawCircle(
        color = Color(0xFFFFF176),
        radius = 30f,
        center = Offset(screenWidth - 50f, 50f)
    )
}

fun DrawScope.drawTower(tower: List<Block>, screenWidth: Float, screenHeight: Float) {
    tower.forEachIndexed { index, block ->
        val color = when {
            index % 3 == 0 -> Color(0xFFFF6B6B)
            index % 3 == 1 -> Color(0xFF4ECDC4)
            else -> Color(0xFFFFD93D)
        }
        val centerX = screenWidth / 2
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(color, color.copy(alpha = 0.7f))
            ),
            topLeft = Offset(centerX - block.width / 2 + block.x, screenHeight - block.y - block.height),
            size = ComposeSize(block.width, block.height),
            cornerRadius = CornerRadius(5f, 5f)
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(centerX - block.width / 2 + block.x, screenHeight - block.y - block.height),
            size = ComposeSize(block.width, block.height),
            cornerRadius = CornerRadius(5f, 5f),
            style = Stroke(width = 2f)
        )
    }
}

fun DrawScope.drawCurrentBlock(currentBlock: Block, screenWidth: Float, screenHeight: Float) {
    val centerX = screenWidth / 2
    drawRoundRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF6C5CE7), Color(0xFF6C5CE7).copy(alpha = 0.7f)),
            center = Offset(currentBlock.width / 2, currentBlock.height / 2),
            radius = currentBlock.width / 2
        ),
        topLeft = Offset(centerX - currentBlock.width / 2 + currentBlock.x, screenHeight - currentBlock.y - currentBlock.height),
        size = ComposeSize(currentBlock.width, currentBlock.height),
        cornerRadius = CornerRadius(5f, 5f)
    )
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(centerX - currentBlock.width / 2 + currentBlock.x, screenHeight - currentBlock.y - currentBlock.height),
        size = ComposeSize(currentBlock.width, currentBlock.height),
        cornerRadius = CornerRadius(5f, 5f),
        style = Stroke(width = 2f)
    )
}

@OptIn(ExperimentalTextApi::class)
fun DrawScope.drawScore(score: Int, textMeasurer: TextMeasurer) {
    val textLayoutResult = textMeasurer.measure(
        text = AnnotatedString("Очки: $score"),
        style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    )
    drawText(
        textLayoutResult = textLayoutResult,
        color = Color.White,
        topLeft = Offset(16f, 32f),
        shadow = Shadow(
            color = Color.Black,
            offset = Offset(2f, 2f),
            blurRadius = 2f
        )
    )
}

fun DrawScope.drawSpeedIndicator(gameSpeed: Float) {
    val maxSpeed = 10f
    val normalizedSpeed = (gameSpeed.absoluteValue / maxSpeed).coerceIn(0f, 1f)
    val indicatorColor = lerp(Color.Green, Color.Red, normalizedSpeed)
    
    drawArc(
        color = indicatorColor,
        startAngle = 180f,
        sweepAngle = 180f * normalizedSpeed,
        useCenter = false,
        topLeft = Offset(size.width - 70f, 10f),
        size = ComposeSize(60f, 60f),
        style = Stroke(width = 5f)
    )
}

data class Particle(
    var position: Offset,
    var velocity: Offset,
    var color: Color,
    var size: Float,
    var lifetime: Int
) {
    fun update(): Particle {
        position += velocity
        lifetime--
        return this
    }
}

fun generateParticles(block: Block): List<Particle> {
    return List(20) {
        Particle(
            position = Offset(block.x + block.width / 2, block.y + block.height / 2),
            velocity = Offset(Random.nextFloat() * 4 - 2, Random.nextFloat() * -4),
            color = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 1f),
            size = Random.nextFloat() * 5 + 2,
            lifetime = Random.nextInt(20, 40)
        )
    }
}

fun DrawScope.drawParticles(particles: List<Particle>) {
    particles.forEach { particle ->
        drawCircle(
            color = particle.color,
            radius = particle.size,
            center = particle.position
        )
    }
}

data class BlockPlacementResult(
    val tower: List<Block>,
    val currentBlock: Block,
    val score: Int,
    val gameSpeed: Float,
    val gameOver: Boolean
)

fun handleBlockPlacement(
    tower: List<Block>,
    currentBlock: Block,
    score: Int,
    gameSpeed: Float,
    soundEffectsEnabled: Boolean,
    screenWidth: Float,
    screenHeight: Float
): BlockPlacementResult {
    val lastBlock = tower.last()
    val overlap = minOf(
        currentBlock.x + currentBlock.width,
        lastBlock.x + lastBlock.width
    ) - maxOf(currentBlock.x, lastBlock.x)

    if (overlap <= 0) {
        return BlockPlacementResult(tower, currentBlock, score, gameSpeed, true)
    } else {
        val newBlock = Block(
            width = overlap,
            height = currentBlock.height,
            y = lastBlock.y + lastBlock.height,
            x = maxOf(currentBlock.x, lastBlock.x)
        )
        val newTower = tower + newBlock
        val newCurrentBlock = Block(
            width = overlap,
            height = currentBlock.height,
            y = newBlock.y + newBlock.height,
            x = if (Random.nextBoolean()) -currentBlock.width / 2 else screenWidth - currentBlock.width / 2
        )
        val newScore = score + (newTower.size * 10)
        val newGameSpeed = gameSpeed * 1.05f
        if (soundEffectsEnabled) {
            // Проиграть звук размещения блока
        }
        return BlockPlacementResult(newTower, newCurrentBlock, newScore, newGameSpeed, false)
    }
}

@Composable
fun GameOverScreen(score: Int, onPlayAgain: () -> Unit, highScore: Int) {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBackground(size.width, size.height)
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Игра окончена", style = MaterialTheme.typography.headlineLarge, color = Color.White)
            Spacer(modifier = Modifier.height(20.dp))
            Text("Ваш результат: $score", style = MaterialTheme.typography.bodyLarge, color = Color.White)
            if (score > highScore) {
                Text("Новый рекорд!", style = MaterialTheme.typography.bodyLarge, color = Color.Green)
            }
            Spacer(modifier = Modifier.height(40.dp))
            Button(onClick = onPlayAgain) {
                Text("Начать заново")
            }
        }
    }
}

@Composable
fun SettingsScreen(
    soundEffectsEnabled: Boolean,
    musicEnabled: Boolean,
    onSoundEffectsToggle: (Boolean) -> Unit,
    onMusicToggle: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBackground(size.width, size.height)
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Звуковые эффекты", color = Color.White)
                Switch(checked = soundEffectsEnabled, onCheckedChange = onSoundEffectsToggle)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Музыка", color = Color.White)
                Switch(checked = musicEnabled, onCheckedChange = onMusicToggle)
            }
        }
    }
}

data class Block(val width: Float, val height: Float, val y: Float, val x: Float = 0f)

