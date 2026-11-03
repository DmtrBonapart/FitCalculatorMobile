package com.example.ru.yourname.fitcalculatormobile.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    secondary = PrimaryGreen,
    tertiary = PrimaryGreen,

    background = AppBackground,
    surface = AppWhite,

    onPrimary = AppWhite,
    onSecondary = AppWhite,
    onTertiary = AppWhite,

    onBackground = TextDark,
    onSurface = TextDark
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    secondary = PrimaryGreen,
    tertiary = PrimaryGreen,

    onPrimary = AppWhite,
    onSecondary = AppWhite,
    onTertiary = AppWhite
)

@Composable
fun FitCalculatorMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}