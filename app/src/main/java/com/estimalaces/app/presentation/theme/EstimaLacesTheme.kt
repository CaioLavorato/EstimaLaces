package com.estimalaces.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Rose = Color(0xFFC85C73)
val Ink = Color(0xFF1F1E24)
val Blush = Color(0xFFFFF1F3)
val Cream = Color(0xFFFFF8F5)
val Sage = Color(0xFF7A9B8F)
val Gold = Color(0xFFB88A44)

private val LightColors: ColorScheme = lightColorScheme(
    primary = Rose,
    onPrimary = Color.White,
    secondary = Sage,
    onSecondary = Color.White,
    tertiary = Gold,
    background = Cream,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Blush,
    onSurfaceVariant = Ink
)

@Composable
fun EstimaLacesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
