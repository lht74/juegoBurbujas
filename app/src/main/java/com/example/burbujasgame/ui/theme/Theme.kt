// ruta: app/src/main/java/com/example/burbujasgame/ui/theme/Theme.kt
package com.example.burbujasgame.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.core.view.WindowCompat
import com.example.burbujasgame.R

@Composable
fun BurbujasGameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = colorResource(R.color.primary_indigo),
        secondary = colorResource(R.color.secondary_pink),
        background = colorResource(R.color.dark_blue_background),
        surface = colorResource(R.color.medium_blue_surface),
        onPrimary = colorResource(R.color.text_white),
        onSecondary = colorResource(R.color.text_white),
        onBackground = colorResource(R.color.text_white),
        onSurface = colorResource(R.color.text_white),
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}