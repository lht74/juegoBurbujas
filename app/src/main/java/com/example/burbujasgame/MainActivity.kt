// ruta: app/src/main/java/com/example/burbujasgame/MainActivity.kt
package com.example.burbujasgame


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColor
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.burbujasgame.ui.theme.BurbujasGameTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// --- MainActivity: Punto de entrada y configuración del tema y navegación ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BurbujasGameTheme {
                BurbujasGameApp()
            }
        }
    }
}

// --- Lógica de Navegación ---
@Composable
fun BurbujasGameApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "menu") {
        composable("menu") {
            MenuScreen(navController = navController)
        }
        composable(
            route = "juego/{tabla}",
            arguments = listOf(navArgument("tabla") { type = NavType.IntType })
        ) { backStackEntry ->
            val tabla = backStackEntry.arguments?.getInt("tabla") ?: 2
            GameScreen(navController = navController, tabla = tabla)
        }
        composable(
            route = "resultados/{puntaje}/{estrellas}/{tabla}",
            arguments = listOf(
                navArgument("puntaje") { type = NavType.IntType },
                navArgument("estrellas") { type = NavType.IntType },
                navArgument("tabla") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val puntaje = backStackEntry.arguments?.getInt("puntaje") ?: 0
            val estrellas = backStackEntry.arguments?.getInt("estrellas") ?: 0
            val tabla = maxOf(2, backStackEntry.arguments?.getInt("tabla") ?: 2)
            ResultsScreen(
                navController = navController,
                puntaje = puntaje,
                estrellas = estrellas,
                tabla = tabla
            )
        }
    }
}


// --- Modelos de Datos y ViewModel ---
data class Burbuja(
    val id: Int,
    val numero: Int,
    val x: Float,
    val y: Float,
    val velocidad: Float,
    val color: Color,
    val esMultiplo: Boolean,
    val mostrarError: Boolean = false // ← nuevo campo para efecto visual
)

data class EstadoJuego(
    val puntaje: Int = 0,
    val vidas: Int = 3,
    val racha: Int = 0,
    val tiempoRestante: Int = 60,
    val burbujas: List<Burbuja> = emptyList()
)

class GameViewModel : ViewModel() {
    private var _estado = mutableStateOf(EstadoJuego())
    val estado: State<EstadoJuego> = _estado
    private var contadorBurbujas = 0
    private var timerJob: Job? = null
    private var bubbleJob: Job? = null
    private var gameLoopJob: Job? = null
    private var juegoActivo = false
    companion object {
        private val colorBurbuja = Color(0xFF4A90E2) // Azul bonito
    }
    fun iniciarJuego(tabla: Int, onJuegoTerminado: (puntaje: Int, estrellas: Int) -> Unit) {
        if (juegoActivo) return // Evitar múltiples inicios
        juegoActivo = true
        _estado.value = EstadoJuego()
        iniciarTimer(tabla, onJuegoTerminado)
        iniciarGeneracionBurbujas(tabla)
        iniciarBucleJuego()
    }

    private fun iniciarTimer(tabla: Int, onJuegoTerminado: (puntaje: Int, estrellas: Int) -> Unit) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (juegoActivo && _estado.value.tiempoRestante > 0) {
                delay(1000)
                if (juegoActivo) {
                    _estado.value = _estado.value.copy(tiempoRestante = _estado.value.tiempoRestante - 1)
                }
            }
            if (juegoActivo) terminarJuego(tabla, onJuegoTerminado)
        }
    }

    private fun iniciarGeneracionBurbujas(tabla: Int) {
        bubbleJob?.cancel()
        bubbleJob = viewModelScope.launch {
            while (juegoActivo) {
                delay(2000)
                if (juegoActivo) {
                    generarBurbuja(tabla)
                }
            }
        }
    }

    private fun iniciarBucleJuego() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (juegoActivo) {
                delay(50)
                if (juegoActivo) actualizarBurbujas()
            }
        }
    }

    private fun generarBurbuja(tabla: Int) {
        // Decidimos si será correcto o no (80% correcto, 20% incorrecto)
        val esCorrecto = Random.nextFloat() < 0.8f

        val numero = if (esCorrecto) {
            // Múltiplo correcto de la tabla
            tabla * (Random.nextInt(1, 11)) // Número entre tabla*1 y tabla*10
        } else {
            // Número aleatorio que NO es múltiplo de la tabla
            var num = Random.nextInt(1, tabla) + tabla * Random.nextInt(0, 11)

            num
        }

        val nuevaBurbuja = Burbuja(
            id = contadorBurbujas++,
            numero = numero,
            x = Random.nextFloat() * 0.8f + 0.1f,
            y = 1.1f,
            velocidad = Random.nextFloat() * 0.5f + 0.5f,
            color = colorBurbuja, // Color único, ver abajo
            esMultiplo = numero % tabla == 0
        )

        _estado.value = _estado.value.copy(burbujas = _estado.value.burbujas + nuevaBurbuja)
    }

    private fun actualizarBurbujas() {
        val burbujasActualizadas = _estado.value.burbujas.mapNotNull { burbuja ->
            val nuevaY = burbuja.y - burbuja.velocidad * 0.01f
            if (nuevaY > -0.2f) burbuja.copy(y = nuevaY) else null
        }
        _estado.value = _estado.value.copy(burbujas = burbujasActualizadas)
    }

    fun tocarBurbuja(burbuja: Burbuja, tabla: Int, onJuegoTerminado: (puntaje: Int, estrellas: Int) -> Unit) {
        if (!juegoActivo) return
        if (burbuja.esMultiplo) {
            val nuevaRacha = _estado.value.racha + 1
            val puntosExtra = if (nuevaRacha >= 5) 10 else 0
            _estado.value = _estado.value.copy(
                puntaje = _estado.value.puntaje + 10 + puntosExtra,
                racha = nuevaRacha,
                burbujas = _estado.value.burbujas.filter { it.id != burbuja.id }
            )
        } else {
            val nuevasVidas = _estado.value.vidas - 1
            val nuevasBurbujas = _estado.value.burbujas.map {
                if (it.id == burbuja.id) it.copy(mostrarError = true) else it
            }
            _estado.value = _estado.value.copy(
                puntaje = maxOf(0, _estado.value.puntaje - 5),
                vidas = nuevasVidas,
                racha = 0,
                burbujas = nuevasBurbujas
            )

            viewModelScope.launch {
                delay(500) // Mostrar animación de error durante 500ms
                _estado.value = _estado.value.copy(
                    burbujas = _estado.value.burbujas.filter { it.id != burbuja.id }
                )
                if (nuevasVidas <= 0) terminarJuego(tabla, onJuegoTerminado)
            }
        }
    }


    private fun terminarJuego(tabla: Int, onJuegoTerminado: (puntaje: Int, estrellas: Int) -> Unit) {
        if (!juegoActivo) return
        juegoActivo = false
        timerJob?.cancel()
        bubbleJob?.cancel()
        gameLoopJob?.cancel()
        val estrellas = when {
            _estado.value.puntaje >= 200 -> 3
            _estado.value.puntaje >= 100 -> 2
            _estado.value.puntaje >= 50 -> 1
            else -> 0
        }
        onJuegoTerminado(_estado.value.puntaje, estrellas)
    }

    override fun onCleared() {
        super.onCleared()
        juegoActivo = false
        timerJob?.cancel()
        bubbleJob?.cancel()
        gameLoopJob?.cancel()
    }
}


// --- Pantallas (Composables) ---

@Composable
fun MenuScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorResource(id = R.color.dark_blue_background),
                        colorResource(id = R.color.medium_blue_surface)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.game_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.text_white),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.game_subtitle),
                fontSize = 18.sp,
                color = colorResource(id = R.color.text_gray),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = stringResource(id = R.string.table_selection_prompt),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = colorResource(id = R.color.text_white)
            )
            Spacer(modifier = Modifier.height(24.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(11) { index ->
                    val tabla = index + 2
                    BotonTabla(
                        tabla = tabla,
                        onClick = { navController.navigate("juego/$tabla") }
                    )
                }
            }
        }
    }
}

@Composable
fun BotonTabla(tabla: Int, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(80.dp)
            .shadow(8.dp, CircleShape),
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.primary_indigo)),
        shape = CircleShape
    ) {
        Text(
            text = stringResource(id = R.string.table_button_prefix) + tabla,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.text_white)
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun GameScreen(
    navController: NavController,
    tabla: Int,
    viewModel: GameViewModel = viewModel()
) {
    val estado by viewModel.estado

    LaunchedEffect(key1 = tabla) {
        viewModel.iniciarJuego(tabla) { puntaje, estrellas ->
            // Navegar a resultados sin volver a agregar a la pila
            navController.navigate("resultados/$puntaje/$estrellas/$tabla") {
                popUpTo("menu") // Limpia la pila hasta el menú
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorResource(id = R.color.dark_blue_background),
                        colorResource(id = R.color.medium_blue_surface)
                    )
                )
            )
    ) {
        // UI superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(id = R.string.table_of_label, tabla),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.text_white)
                )
                Text(
                    text = stringResource(id = R.string.score_label, estado.puntaje),
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.text_gray)
                )
                if (estado.racha > 0) {
                    Text(
                        text = stringResource(id = R.string.streak_label, estado.racha),
                        fontSize = 14.sp,
                        color = colorResource(id = R.color.correct_green)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(id = R.string.time_label, estado.tiempoRestante),
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.text_white)
                )
                Text(
                    text = "❤️".repeat(estado.vidas),
                    fontSize = 16.sp
                )
            }
        }

        // Área de burbujas
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            estado.burbujas.forEach { burbuja ->
                ComponenteBurbuja(
                    burbuja = burbuja,
                    x = maxWidth * burbuja.x,
                    y = maxHeight * burbuja.y,
                    onClick = {
                        viewModel.tocarBurbuja(burbuja, tabla) { puntaje, estrellas ->
                            navController.navigate("resultados/$puntaje/$estrellas/$tabla") {
                                popUpTo("menu")
                            }
                        }
                    }
                )
            }
        }

        // Botón de salir
        FloatingActionButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = colorResource(id = R.color.secondary_pink)
        ) {
            Text(stringResource(id = R.string.exit_button_emoji), fontSize = 20.sp)
        }
    }
}

@Composable
fun ComponenteBurbuja(
    burbuja: Burbuja,
    x: Dp,
    y: Dp,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")

    // Rotación continua
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing)
        ),
        label = ""
    )

    // Gradiente dinámico de fondo
    val animatedColor by infiniteTransition.animateColor(
        initialValue = burbuja.color.copy(alpha = 0.6f),
        targetValue = burbuja.color.copy(alpha = 0.9f),
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    // Brillo animado del reflejo
    val brilloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val baseSize = 60.dp
    val scaleFactor = remember { Random.nextDouble(1.0, 1.5).toFloat() }
    val bubbleSize = baseSize * scaleFactor

    var clicked by remember { mutableStateOf(false) }
    val animatedClickScale by animateFloatAsState(
        targetValue = if (clicked) 0.7f else 1f,
        animationSpec = tween(150),
        label = ""
    )

    // Halo rojo animado cuando hay error
    val haloAlpha = remember { Animatable(0f) }
    if (burbuja.mostrarError) {
        LaunchedEffect(burbuja.id) {
            haloAlpha.animateTo(0.8f, tween(200))
            haloAlpha.animateTo(0f, tween(300))
        }
    }

    LaunchedEffect(clicked) {
        if (clicked) {
            delay(150)
            clicked = false
        }
    }

    Box(
        modifier = Modifier
            .offset(x = x - bubbleSize / 2, y = y - bubbleSize / 2)
            .size(bubbleSize)
            .scale(animatedClickScale),
        contentAlignment = Alignment.Center
    ) {
        // Halo rojo (solo si mostrarError)
        if (burbuja.mostrarError) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = haloAlpha.value))
                    .blur(16.dp)
            )
        }

        // Fondo rotante con gradiente
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    rotationZ = rotation
                    transformOrigin = TransformOrigin.Center
                }
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            animatedColor,
                            burbuja.color.copy(alpha = 0.4f)
                        ),
                        radius = 100f
                    )
                )
                .border(width = 2.dp, color = Color.White.copy(alpha = 0.3f), shape = CircleShape)
                .clickable {
                    onClick()
                    clicked = true
                }
        )

        // Número
        Text(
            text = burbuja.numero.toString(),
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.White,
                    offset = Offset(0f, 0f),
                    blurRadius = 10f
                )
            ),
            color = Color.White
        )

        // Reflejo
        Box(
            modifier = Modifier
                .offset(x = 15.dp, y = (-15).dp)
                .size(30.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = brilloAlpha), Color.Transparent)
                    )
                )
        )
    }
}


@Composable
fun ResultsScreen(
    navController: NavController,
    puntaje: Int,
    estrellas: Int,
    tabla: Int
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorResource(id = R.color.dark_blue_background),
                        colorResource(id = R.color.medium_blue_surface)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.game_over_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.text_white)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.table_of_label, tabla),
                fontSize = 20.sp,
                color = colorResource(id = R.color.text_gray)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$puntaje",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.text_white)
            )
            Text(
                text = stringResource(id = R.string.final_score_label),
                fontSize = 16.sp,
                color = colorResource(id = R.color.text_gray)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.stars_emoji).repeat(estrellas),
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(48.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { navController.navigate("juego/$tabla") { popUpTo("menu") } },
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.correct_green))
                ) {
                    Text(stringResource(id = R.string.retry_button))
                }
                Button(
                    onClick = { navController.navigate("menu") { popUpTo("menu") { inclusive = true } } },
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.primary_indigo))
                ) {
                    Text(stringResource(id = R.string.menu_button))
                }
            }
        }
    }
}