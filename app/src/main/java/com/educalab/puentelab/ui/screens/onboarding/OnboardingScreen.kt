package com.educalab.puentelab.ui.screens.onboarding

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.educalab.puentelab.ui.components.ExampleBridgeIllustration
import com.educalab.puentelab.ui.components.PivotCharacter
import com.educalab.puentelab.ui.components.PivotMood
import com.educalab.puentelab.ui.components.SecurityIllustration
import com.educalab.puentelab.ui.components.WorkshopIllustration
import com.educalab.puentelab.ui.theme.*
import kotlinx.coroutines.launch

private data class OnboardingPage(val title: String, val body: String)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pages = listOf(
        OnboardingPage("Bienvenido a PuenteLab", "Un estudio de ingeniería te necesita: hay ríos, cañones y valles que separan a la gente, y tú vas a unirlos."),
        OnboardingPage("Conoce a PIVOT", "PIVOT es el robot topógrafo del estudio. Te acompañará con pistas cuando un diseño no aguante."),
        OnboardingPage("Construye de verdad", "Coloca nodos, tiende barras y elige materiales. Cada puente se prueba con un vehículo real antes de aprobarlo."),
        OnboardingPage("Tus datos se quedan aquí", "PuenteLab funciona sin conexión. No pedimos tu nombre real ni ningún dato personal: solo un apodo y un avatar.")
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Surface(color = PaperBg, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (page) {
                        0 -> WorkshopIllustration(modifier = Modifier.size(180.dp))
                        1 -> PivotCharacter(mood = PivotMood.HAPPY, modifier = Modifier.size(140.dp))
                        2 -> ExampleBridgeIllustration(modifier = Modifier.size(width = 220.dp, height = 150.dp))
                        else -> SecurityIllustration(modifier = Modifier.size(160.dp))
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(pages[page].title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, color = Blueprint900)
                    Spacer(Modifier.height(12.dp))
                    Text(pages[page].body, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = Ink600)
                }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center) {
                repeat(pages.size) { i ->
                    val active = pagerState.currentPage == i
                    Box(
                        Modifier
                            .padding(4.dp)
                            .size(if (active) 10.dp else 8.dp)
                            .background(if (active) SiteOrange else Blueprint100, shape = CircleShape)
                    )
                }
            }
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.lastIndex) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1, animationSpec = tween(300)) }
                    } else {
                        onFinished()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SiteOrange)
            ) {
                Text(if (pagerState.currentPage < pages.lastIndex) "Siguiente" else "Entrar al estudio")
            }
        }
    }
}
