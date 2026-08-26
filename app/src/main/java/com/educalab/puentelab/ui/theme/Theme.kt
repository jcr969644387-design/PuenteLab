package com.educalab.puentelab.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Blueprint700,
    onPrimary = White,
    secondary = SiteOrange,
    onSecondary = Blueprint900,
    tertiary = RiverTeal,
    background = PaperBg,
    onBackground = Ink900,
    surface = White,
    onSurface = Ink900,
    surfaceVariant = Blueprint100,
    onSurfaceVariant = Ink600,
    error = WarningRed
)

private val DarkColors = darkColorScheme(
    primary = Blueprint300,
    onPrimary = Blueprint900,
    secondary = SiteAmber,
    onSecondary = Blueprint900,
    tertiary = RiverTeal,
    background = Ink900,
    onBackground = PaperBg,
    surface = Blueprint900,
    onSurface = PaperBg,
    error = WarningRed
)

/**
 * PuenteLab siempre usa el esquema de colores claro: los textos y componentes de toda la app
 * usan colores fijos (Ink900, Ink600, etc.) pensados para fondos claros, y nunca se diseñó una
 * paleta oscura coherente. Seguir el tema oscuro del sistema volvía ilegibles muchas pantallas
 * (tarjetas oscuras con texto oscuro encima), así que se ignora isSystemInDarkTheme().
 */
@Composable
fun PuenteLabTheme(useDark: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (useDark) DarkColors else LightColors,
        typography = PuenteLabTypography,
        content = content
    )
}
