package com.impressionlab

import android.net.Uri
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.impressionlab.data.SettingsState
import com.impressionlab.ui.components.decodeSampledImage
import com.impressionlab.ui.screens.CharacterScreen
import com.impressionlab.ui.screens.HomeScreen
import com.impressionlab.ui.screens.ReferenceScreen
import com.impressionlab.ui.screens.SettingsScreen
import com.impressionlab.ui.screens.SplashScreen
import com.impressionlab.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SettingsState.isDarkTheme(): Boolean = when (darkMode) {
    SettingsState.DARK_LIGHT -> false
    SettingsState.DARK_DARK -> true
    else -> isSystemInDarkTheme()
}

@Composable
fun App(settings: SettingsState) {
    AppTheme(paletteName = settings.paletteName, darkTheme = settings.isDarkTheme()) {
        Box(Modifier.fillMaxSize()) {
            BackgroundLayer(settings)
            AppNavHost(settings)
        }
    }
}

@Composable
private fun AppNavHost(settings: SettingsState) {
    val nav = rememberNavController()
    NavHost(
        navController = nav,
        startDestination = "splash",
        enterTransition = {
            slideInHorizontally(tween(320), initialOffsetX = { it / 4 }) + fadeIn(tween(320))
        },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(280)) },
        popExitTransition = {
            slideOutHorizontally(tween(280), targetOffsetX = { it / 4 }) + fadeOut(tween(280))
        },
    ) {
        composable("splash") {
            SplashScreen(onFinished = {
                nav.navigate("home") { popUpTo("splash") { inclusive = true } }
            })
        }
        composable("home") {
            HomeScreen(
                onOpenCharacter = { name -> nav.navigate("character/${Uri.encode(name)}") },
                onOpenSettings = { nav.navigate("settings") },
            )
        }
        composable("character/{char}") { entry ->
            val char = entry.arguments?.getString("char") ?: return@composable
            CharacterScreen(
                characterName = char,
                onBack = { nav.popBackStack() },
                onOpenReference = { ref ->
                    nav.navigate("reference/${Uri.encode(char)}/${Uri.encode(ref)}")
                },
            )
        }
        composable("reference/{char}/{ref}") { entry ->
            val char = entry.arguments?.getString("char") ?: return@composable
            val ref = entry.arguments?.getString("ref") ?: return@composable
            ReferenceScreen(
                characterName = char,
                referenceName = ref,
                onBack = { nav.popBackStack() },
            )
        }
        composable("settings") {
            SettingsScreen(settings = settings, onBack = { nav.popBackStack() })
        }
    }
}

@Composable
private fun BackgroundLayer(settings: SettingsState) {
    val colors = MaterialTheme.colorScheme
    val dark = settings.isDarkTheme()

    val background by produceState<ImageBitmap?>(initialValue = null, settings.backgroundVersion) {
        value = if (settings.hasBackground) {
            withContext(Dispatchers.IO) { decodeSampledImage(settings.backgroundFile, 1600) }
        } else {
            null
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        colors.surface,
                        colors.surfaceContainerHighest,
                        colors.primaryContainer,
                    )
                )
            )
    ) {
        background?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            // Scrim keeps text readable over any photo.
            Box(
                Modifier
                    .fillMaxSize()
                    .background(colors.surface.copy(alpha = if (dark) 0.72f else 0.6f))
            )
        }
    }
}
