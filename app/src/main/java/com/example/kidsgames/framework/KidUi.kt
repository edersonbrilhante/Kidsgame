package com.example.kidsgames.framework

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kidsgames.ui.theme.SkyBottom
import com.example.kidsgames.ui.theme.SkyTop

/** Soft top-to-bottom gradient that fills the screen, used as a page background. */
@Composable
fun KidBackground(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(SkyTop, SkyBottom),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors)),
    ) {
        content()
    }
}

/**
 * Big, rounded, chunky button that springs when pressed — sized for small fingers.
 * Replaces the tiny default Material buttons across the app.
 */
@Composable
fun KidButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.90f else 1f, label = "kidButtonPress")

    Surface(
        onClick = onClick,
        modifier = modifier.scale(scale),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 6.dp,
        interactionSource = interaction,
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

/** Round icon button (emoji or glyph) with a press spring — used for back / photo actions. */
@Composable
fun KidCircleButton(
    onClick: () -> Unit,
    glyph: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    size: Int = 56,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.88f else 1f, label = "kidCirclePress")

    Surface(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .size(size.dp),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 6.dp,
        interactionSource = interaction,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(glyph, fontSize = (size * 0.42f).sp)
        }
    }
}

/**
 * Standard page scaffold for a minigame: gradient background + a round back button
 * in the top-left. The [content] lays out below the back button in a column.
 */
@Composable
fun KidScreen(
    onExit: () -> Unit,
    colors: List<Color> = listOf(SkyTop, SkyBottom),
    content: @Composable ColumnScope.() -> Unit,
) {
    KidBackground(colors = colors) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                KidCircleButton(onClick = onExit, glyph = "◀")
            }
            content()
        }
    }
}
