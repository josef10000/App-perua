package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryContainerLavender,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryMint,
    onSecondary = OnPrimaryWhite,
    secondaryContainer = SecondaryMintContainer,
    onSecondaryContainer = SecondaryMint,
    tertiary = PrimaryPurple,
    background = BackgroundLight,
    onBackground = TextPrimaryDark,
    surface = SurfaceLight,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryMuted,
    outline = OutlineLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryContainerLavender,
    onPrimary = OnPrimaryContainerDark,
    primaryContainer = PrimaryPurple,
    onPrimaryContainer = OnPrimaryWhite,
    secondary = SecondaryMintContainer,
    onSecondary = SecondaryMint,
    background = TextPrimaryDark,
    onBackground = BackgroundLight,
    surface = TextPrimaryDark,
    onSurface = BackgroundLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OutlineLight
)

@Composable
fun RotaEscolarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

