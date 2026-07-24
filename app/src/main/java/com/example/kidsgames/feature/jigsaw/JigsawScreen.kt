package com.example.kidsgames.feature.jigsaw

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.example.kidsgames.core.ParentalGate
import com.example.kidsgames.core.SpeechService
import com.example.kidsgames.framework.EmojiIcon
import com.example.kidsgames.framework.GameServices
import com.example.kidsgames.framework.KidBackground
import com.example.kidsgames.framework.KidButton
import com.example.kidsgames.framework.KidCircleButton
import com.example.kidsgames.ui.theme.Sunshine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.random.Random

/** One draggable puzzle piece with Compose-observable position + placed state. */
private class Piece(val row: Int, val col: Int, val image: ImageBitmap) {
    var pos by mutableStateOf(Offset.Zero)
    var placed by mutableStateOf(false)
    var initialized = false
}

@Composable
fun JigsawScreen(services: GameServices, onExit: () -> Unit) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val savedGrid by services.settings.gridSize.collectAsState(initial = 2)
    var grid by remember { mutableIntStateOf(2) }
    LaunchedEffect(savedGrid) { grid = savedGrid }

    // null when a custom photo is loaded (no built-in word to show/say).
    var pictureId by remember { mutableStateOf<String?>(PuzzleLogic.samples.first().id) }

    var source by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(Unit) {
        val latest = services.imageStore.latest()
        if (latest != null) {
            source = services.imageStore.loadBitmap(latest)
            pictureId = null
        } else {
            source = PuzzleLogic.sample(context)
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val file = services.imageStore.importFromUri(uri)
            if (file != null) {
                source = services.imageStore.loadBitmap(file)
                pictureId = null // custom photo: don't announce a built-in word
            }
        }
    }
    var showGate by remember { mutableStateOf(false) }
    var showGallery by remember { mutableStateOf(false) }
    val completed = remember { mutableStateListOf<String>() }
    val currentPic = remember(pictureId) { PuzzleLogic.samples.firstOrNull { it.id == pictureId } }

    // Say the picture's name in all three languages whenever it changes (child can't read).
    LaunchedEffect(pictureId) {
        currentPic?.let { pic ->
            services.speech.speakSequence(
                listOf(
                    SpeechService.Utterance(pic.en, Locale.ENGLISH),
                    SpeechService.Utterance(pic.pl, Locale("pl")),
                    SpeechService.Utterance(pic.pt, Locale("pt", "BR")),
                )
            )
        }
    }

    val tiles = remember(source, grid) {
        source?.let { PuzzleLogic.slice(it, grid, grid) } ?: emptyList()
    }
    val pieces = remember(tiles) { tiles.map { Piece(it.row, it.col, it.bitmap.asImageBitmap()) } }
    var resetKey by remember(pieces) { mutableIntStateOf(0) }
    var won by remember(pieces) { mutableStateOf(false) }

    // On winning: mark this picture done, say its word, then walk to the next picture.
    LaunchedEffect(won) {
        if (won) {
            currentPic?.let { pic ->
                if (pic.id !in completed) completed.add(pic.id)
                services.speech.speakSequence(
                    listOf(
                        SpeechService.Utterance(pic.en, Locale.ENGLISH),
                        SpeechService.Utterance(pic.pl, Locale("pl")),
                        SpeechService.Utterance(pic.pt, Locale("pt", "BR")),
                    )
                )
            }
            delay(1800)
            val idx = PuzzleLogic.samples.indexOfFirst { it.id == pictureId }
            val nextPic = PuzzleLogic.samples[if (idx < 0) 0 else (idx + 1) % PuzzleLogic.samples.size]
            pictureId = nextPic.id
            source = nextPic.draw(context, 900)
        }
    }

    KidBackground {

        Column(modifier = Modifier.fillMaxSize()) {
            // --- Top bar: back, difficulty, add photo ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KidCircleButton(onClick = onExit, glyph = "\u25C0")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(2, 3, 4).forEach { n ->
                        val selected = grid == n
                        KidButton(
                            onClick = {
                                grid = n
                                scope.launch { services.settings.setGridSize(n) }
                            },
                            containerColor = if (selected) Sunshine else Color.White,
                            contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        ) { Text("${n}\u00D7$n", style = MaterialTheme.typography.labelLarge) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Open the picture gallery (shows the current picture).
                    KidCircleButton(
                        onClick = { showGallery = true },
                        glyph = currentPic?.emoji ?: "\uD83D\uDDBC\uFE0F",
                    )
                    // Import a photo (behind the parental gate).
                    KidCircleButton(onClick = { showGate = true }, glyph = "\uD83D\uDCF7")
                }
            }

            // --- Trilingual word card: tap a flag to hear that language again ---
            // Stacked vertically so long words (e.g. "guarda-chuva") never break the layout.
            currentPic?.let { pic ->
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    WordChip("🇬🇧", pic.en) { services.speech.speak(pic.en, Locale.ENGLISH) }
                    WordChip("🇵🇱", pic.pl) { services.speech.speak(pic.pl, Locale("pl")) }
                    WordChip("🇧🇷", pic.pt) { services.speech.speak(pic.pt, Locale("pt", "BR")) }
                }
            }

            if (pieces.isNotEmpty()) {
                BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                val wpx = with(density) { maxWidth.toPx() }
                val hpx = with(density) { maxHeight.toPx() }
                val boardSize = minOf(wpx * 0.92f, hpx * 0.55f)
                val cell = boardSize / grid
                val boardLeft = (wpx - boardSize) / 2f
                val boardTop = hpx * 0.02f
                val cellDp = with(density) { cell.toDp() }
                val boardDp = with(density) { boardSize.toDp() }

                // Scatter unplaced pieces into the tray below the board.
                LaunchedEffect(pieces, wpx, hpx, resetKey) {
                    if (wpx > 0f && hpx > 0f) {
                        val rnd = Random(42)
                        val trayTop = boardTop + boardSize + cell * 0.3f
                        val trayBottom = (hpx - cell * 1.1f).coerceAtLeast(trayTop)
                        pieces.forEach { pc ->
                            if (!pc.initialized) {
                                val px = (rnd.nextFloat() * (wpx - cell)).coerceIn(0f, (wpx - cell).coerceAtLeast(0f))
                                val py = (trayTop + rnd.nextFloat() * (trayBottom - trayTop))
                                    .coerceIn(trayTop, trayBottom)
                                pc.pos = Offset(px, py)
                                pc.initialized = true
                            }
                        }
                    }
                }

                // Board background + grid lines.
                Canvas(Modifier.fillMaxSize()) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.7f),
                        topLeft = Offset(boardLeft, boardTop),
                        size = Size(boardSize, boardSize),
                        cornerRadius = CornerRadius(20f, 20f),
                    )
                    val line = Color(0x33000000)
                    for (i in 0..grid) {
                        val gx = boardLeft + i * cell
                        val gy = boardTop + i * cell
                        drawLine(line, Offset(gx, boardTop), Offset(gx, boardTop + boardSize), 2f)
                        drawLine(line, Offset(boardLeft, gy), Offset(boardLeft + boardSize, gy), 2f)
                    }
                }

                // Faint full-image hint on the board.
                source?.let { src ->
                    val hint = remember(src) { PuzzleLogic.square(src).asImageBitmap() }
                    Image(
                        bitmap = hint,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .offset { IntOffset(boardLeft.roundToInt(), boardTop.roundToInt()) }
                            .size(boardDp)
                            .alpha(0.18f),
                    )
                }

                // Pieces.
                pieces.forEach { pc ->
                    key(pc.row, pc.col, resetKey) {
                        val dragMod = if (pc.placed) Modifier else Modifier.pointerInput(pc, cell, boardLeft, boardTop) {
                            detectDragGestures(
                                onDrag = { change, drag ->
                                    change.consume()
                                    pc.pos = pc.pos + drag
                                },
                                onDragEnd = {
                                    val cx = pc.pos.x + cell / 2f
                                    val cy = pc.pos.y + cell / 2f
                                    val slotX = boardLeft + pc.col * cell + cell / 2f
                                    val slotY = boardTop + pc.row * cell + cell / 2f
                                    if (hypot(cx - slotX, cy - slotY) < cell * 0.55f) {
                                        pc.pos = Offset(boardLeft + pc.col * cell, boardTop + pc.row * cell)
                                        pc.placed = true
                                        services.audio.playCorrect()
                                        if (pieces.all { it.placed }) {
                                            won = true
                                            services.celebrate()
                                        }
                                    }
                                },
                            )
                        }
                        Image(
                            bitmap = pc.image,
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .offset { IntOffset(pc.pos.x.roundToInt(), pc.pos.y.roundToInt()) }
                                .size(cellDp)
                                .then(dragMod),
                        )
                    }
                }
            }
            }
        }

        if (won) {
            WinOverlay()
        }

        if (showGallery) {
            PictureGallery(
                completedIds = completed,
                selectedId = pictureId,
                onPick = { pic ->
                    pictureId = pic.id
                    source = pic.draw(context, 900)
                    showGallery = false
                },
                onClose = { showGallery = false },
            )
        }
    }

    if (showGate) {
        ParentalGate(
            onPass = {
                showGate = false
                photoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onDismiss = { showGate = false },
        )
    }
}

/** A tappable flag + word chip that re-speaks the word in that language. */
@Composable
private fun WordChip(flag: String, word: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = Color.White,
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(flag, fontSize = 20.sp)
            Text(
                word,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun WinOverlay() {
    val bounce = rememberInfiniteTransition(label = "winBounce")
    val emojiScale by bounce.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "emojiScale",
    )
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xE6FFFFFF)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("\uD83C\uDF89", fontSize = 110.sp, modifier = Modifier.scale(emojiScale))
            Spacer(Modifier.height(8.dp))
            Text(
                "You did it!",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

/** Full-screen grid of every picture; completed ones are grayed with a check. */
@Composable
private fun PictureGallery(
    completedIds: List<String>,
    selectedId: String?,
    onPick: (SamplePicture) -> Unit,
    onClose: () -> Unit,
) {
    KidBackground {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Pick a picture",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                KidCircleButton(onClick = onClose, glyph = "✕")
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 84.dp),
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(PuzzleLogic.samples, key = { it.id }) { pic ->
                    val done = pic.id in completedIds
                    val selected = pic.id == selectedId
                    Surface(
                        onClick = { onPick(pic) },
                        shape = MaterialTheme.shapes.large,
                        color = when {
                            selected -> Sunshine
                            done -> Color(0xFFE4E4E8)
                            else -> Color.White
                        },
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(84.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            EmojiIcon(
                                pic.emoji,
                                48.dp,
                                modifier = Modifier.alpha(if (done && !selected) 0.4f else 1f),
                            )
                            if (done) {
                                Text(
                                    "✓",
                                    fontSize = 18.sp,
                                    color = Color(0xFF2E7D46),
                                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
