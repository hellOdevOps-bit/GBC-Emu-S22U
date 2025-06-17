package com.hello_dev0ps.gbcemus22u

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hello_dev0ps.gbcemus22u.core.Emulator
import com.hello_dev0ps.gbcemus22u.ui.GbcCanvas
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var emulator: Emulator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialisation de l'émulateur
        emulator = Emulator()

        // Chargement de la ROM de test
        try {
            assets.open("cpu_instrs.gb").use { inputStream ->
                val romData = inputStream.readBytes()
                emulator.loadROM(romData)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            GBCEmuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EmulatorScreen(emulator)
                }
            }
        }
    }
}

@Composable
fun EmulatorScreen(emulator: Emulator) {
    var isRunning by remember { mutableStateOf(false) }
    val frameBuffer = remember { mutableStateOf(IntArray(160 * 144) { 0xFF000000.toInt() }) }
    val scope = rememberCoroutineScope()

    // Boucle d'émulation (rafraîchit le framebuffer)
    LaunchedEffect(isRunning) {
        while (isRunning) {
            emulator.stepFrame() // À implémenter : exécute une frame complète
            frameBuffer.value = emulator.getFrameBuffer()
            delay(16L) // ~60 FPS
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Affichage Game Boy
        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(160f / 144f)
                .background(Color.Black)
        ) {
            GbcCanvas(
                frameBuffer = frameBuffer.value,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Contrôles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { isRunning = !isRunning }
            ) {
                Text(if (isRunning) "Stop" else "Start")
            }
            Button(
                onClick = { emulator.reset() }
            ) {
                Text("Reset")
            }
        }

        // Zone de debug
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Debug Info:")
                Text("FPS: ${emulator.getFPS()}")
                Text("CPU Cycles: ${emulator.getCPUCycles()}")
            }
        }
    }
}

@Composable
fun GBCEmuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(),
        content = content
    )
}