package net.lemontree.push.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Teal200
)

private val LightColorPalette = lightColors(
    primary = Teal600,
    primaryVariant = Teal700,
    secondary = Teal200,
    background = Gray100,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Gray900,
    onBackground = Gray900,
    onSurface = Gray900,
    error = Red500
)

@Composable
fun MsglistenerTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {


    MaterialTheme(
        colors = LightColorPalette,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}