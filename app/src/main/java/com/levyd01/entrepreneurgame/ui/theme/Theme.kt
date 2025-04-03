package com.levyd01.entrepreneurgame.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.levyd01.entrepreneurgame.ui.theme.DarkBackground
import com.levyd01.entrepreneurgame.ui.theme.LightBackground

val MyTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp
    )
    // Add or modify more styles as needed
)

// Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = CartoonRed,
    onPrimary = Color.White,
    secondary = CartoonYellow,
    onSecondary = Color.Black,
    background = LightBackground,
    onBackground = CartoonTextPrimary,
    surface = CartoonBlue,
    onSurface = Color.White
)

// Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = CartoonBlue,
    onPrimary = Color.Black,
    secondary = CartoonPurple,
    onSecondary = Color.White,
    background = DarkBackground,
    onBackground = Color.White,
    surface = CartoonGreen,
    onSurface = CartoonTextPrimary
)

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)
@Composable
fun EntrepreneurGameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {

    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = MyTypography,
        shapes = Shapes,
        content = content
    )
}