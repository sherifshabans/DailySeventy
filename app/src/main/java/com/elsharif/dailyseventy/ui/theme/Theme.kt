package com.elsharif.dailyseventy.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


@Composable
fun DailySeventyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    userPrimary: Color = DefaultUserColor, // default Blue
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = userPrimary,
            secondary = DarkGray, // added gray here
            background = Black,
            surface = Black,
            onPrimary = White,
            onSecondary = White,
            onBackground = White,
            onSurface = White
        )
        else -> lightColorScheme(
            primary = userPrimary,
            secondary = LightGray, // added gray here
            background = White,
            surface = White,
            onPrimary = Black,
            onSecondary = Black,
            onBackground = Black,
            onSurface = Black
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
