package com.hello_dev0ps.gbcemus22u.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint
import androidx.compose.ui.graphics.asAndroidColor

@Composable
fun GbcCanvas(
    frameBuffer: IntArray,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = 160
        val height = 144

        // Calculer le ratio pour maintenir l'aspect ratio
        val scale = minOf(
            size.width / width,
            size.height / height
        )

        // Centrer le canvas
        val offsetX = (size.width - width * scale) / 2
        val offsetY = (size.height - height * scale) / 2

        // Créer un seul Paint pour toutes les opérations de dessin
        val paint = Paint().apply {
            isAntiAlias = false // Désactiver l'anti-aliasing pour un rendu pixelisé
        }

        scale(scale) {
            drawIntoCanvas { canvas ->
                // Dessiner le fond noir
                canvas.nativeCanvas.drawColor(android.graphics.Color.BLACK)

                // Dessiner les pixels
                for (y in 0 until height) {
                    for (x in 0 until width) {
                        val index = y * width + x
                        val colorInt = frameBuffer.getOrNull(index) ?: 0x000000

                        // Convertir la couleur en format Android
                        paint.color = Color(colorInt or 0xFF000000.toInt()).asAndroidColor()

                        // Dessiner le pixel
                        canvas.nativeCanvas.drawRect(
                            x.toFloat(),
                            y.toFloat(),
                            (x + 1).toFloat(),
                            (y + 1).toFloat(),
                            paint
                        )
                    }
                }
            }
        }
    }
}