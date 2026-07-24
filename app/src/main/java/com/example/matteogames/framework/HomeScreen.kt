package com.example.matteogames.framework

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(onSelect: (String) -> Unit) {
    val context = LocalContext.current
    val version = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "?"
    }
    KidBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))
            Text(text = "🌈", fontSize = 56.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Matteo Games",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Pick a game",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(28.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                state = rememberLazyListState(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 20.dp),
            ) {
                items(MiniGameRegistry.games) { game ->
                    GameCard(game.info) { onSelect(game.info.id) }
                }
            }

            Spacer(Modifier.weight(1f))
            Text(
                text = "Matteo Games  v$version  ·  Emoji: Twemoji CC-BY 4.0",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
    }
}

@Composable
private fun GameCard(info: MiniGameInfo, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, label = "cardPress")

    Surface(
        onClick = onClick,
        modifier = Modifier
            .scale(scale)
            .size(width = 220.dp, height = 280.dp),
        shape = MaterialTheme.shapes.extraLarge,
        shadowElevation = 10.dp,
        interactionSource = interaction,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(info.gradient)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // White circle "badge" holding the game icon.
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (info.emoji != null) {
                        EmojiIcon(info.emoji, 72.dp)
                    } else if (info.iconRes != 0) {
                        Image(
                            painter = painterResource(info.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(84.dp),
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    text = stringResource(info.titleRes),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
