package com.example.kidsgames.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KidsColors = lightColorScheme(
    primary = SkyBlue,
    secondary = Bubblegum,
    tertiary = Grass,
    background = Cream,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Ink,
    onSurface = Ink,
)

@Composable
fun KidsGameTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KidsColors,
        typography = Typography(),
        content = content,
    )
}
