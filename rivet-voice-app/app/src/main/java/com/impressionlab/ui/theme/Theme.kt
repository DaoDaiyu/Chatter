package com.impressionlab.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

data class AppPalette(
    val name: String,
    val swatch: List<Color>,
    val light: ColorScheme,
    val dark: ColorScheme,
)

private fun lightScheme(p: Color, s: Color, t: Color) = lightColorScheme(
    primary = p,
    onPrimary = Color.White,
    primaryContainer = lerp(p, Color.White, 0.85f),
    onPrimaryContainer = lerp(p, Color.Black, 0.55f),
    secondary = s,
    onSecondary = Color.White,
    secondaryContainer = lerp(s, Color.White, 0.82f),
    onSecondaryContainer = lerp(s, Color.Black, 0.55f),
    tertiary = t,
    onTertiary = Color.White,
    tertiaryContainer = lerp(t, Color.White, 0.82f),
    onTertiaryContainer = lerp(t, Color.Black, 0.55f),
    surfaceTint = p,
)

private fun darkScheme(p: Color, s: Color, t: Color) = darkColorScheme(
    primary = lerp(p, Color.White, 0.28f),
    onPrimary = lerp(p, Color.Black, 0.65f),
    primaryContainer = lerp(p, Color.Black, 0.5f),
    onPrimaryContainer = lerp(p, Color.White, 0.82f),
    secondary = lerp(s, Color.White, 0.28f),
    onSecondary = lerp(s, Color.Black, 0.65f),
    secondaryContainer = lerp(s, Color.Black, 0.5f),
    onSecondaryContainer = lerp(s, Color.White, 0.82f),
    tertiary = lerp(t, Color.White, 0.28f),
    onTertiary = lerp(t, Color.Black, 0.65f),
    tertiaryContainer = lerp(t, Color.Black, 0.5f),
    onTertiaryContainer = lerp(t, Color.White, 0.82f),
    surfaceTint = lerp(p, Color.White, 0.28f),
)

private fun palette(name: String, p: Color, s: Color, t: Color) =
    AppPalette(name, listOf(p, s, t), lightScheme(p, s, t), darkScheme(p, s, t))

/** User-selectable color schemes. The first one is the default. */
val Palettes = listOf(
    palette("Rivet Rust", Color(0xFFE8722A), Color(0xFF8A6BF2), Color(0xFF2FA98C)),
    palette("Lombax Gold", Color(0xFFD9A511), Color(0xFFB4552D), Color(0xFF3E7CB1)),
    palette("Nebula", Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF22D3EE)),
    palette("Ocean", Color(0xFF0E7DB8), Color(0xFF14B8A6), Color(0xFF6366F1)),
    palette("Emerald", Color(0xFF169E62), Color(0xFF65A30D), Color(0xFF0891B2)),
    palette("Crimson", Color(0xFFDC3545), Color(0xFFF97316), Color(0xFF9F1239)),
)

fun paletteFor(name: String): AppPalette =
    Palettes.firstOrNull { it.name == name } ?: Palettes.first()

@Composable
fun AppTheme(paletteName: String, darkTheme: Boolean, content: @Composable () -> Unit) {
    val palette = paletteFor(paletteName)
    MaterialTheme(
        colorScheme = if (darkTheme) palette.dark else palette.light,
        content = content,
    )
}
