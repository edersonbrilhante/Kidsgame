package com.example.kidsgames.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

// Big, round, friendly corners everywhere — kids-friendly and thumb-friendly.
private val KidsShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(40.dp),
)

// Bolder, larger type scale so text reads big and playful.
private val KidsTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Black, fontSize = 48.sp, lineHeight = 54.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 40.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 24.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 22.sp),
)

@Composable
fun KidsGameTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KidsColors,
        typography = KidsTypography,
        shapes = KidsShapes,
        content = content,
    )
}
