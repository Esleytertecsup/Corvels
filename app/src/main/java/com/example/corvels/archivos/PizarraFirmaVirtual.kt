package com.example.corvels.archivos

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.corvels.ui.theme.CorvelsTheme
import kotlin.math.roundToInt

class PizarraFirmaVirtual : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CorvelsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PizarraFirmaScreen { strokes, widthPx, heightPx ->
                        try {
                            val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
                            val canvas = AndroidCanvas(bitmap)
                            canvas.drawColor(AndroidColor.WHITE)

                            val paint = Paint().apply {
                                color = AndroidColor.BLACK
                                style = Paint.Style.STROKE
                                strokeWidth = 8f
                                strokeCap = Paint.Cap.ROUND
                                strokeJoin = Paint.Join.ROUND
                            }

                            strokes.forEach { stroke ->
                                for (i in 1 until stroke.size) {
                                    canvas.drawLine(
                                        stroke[i - 1].x,
                                        stroke[i - 1].y,
                                        stroke[i].x,
                                        stroke[i].y,
                                        paint
                                    )
                                }
                            }

                            val resultIntent = Intent().apply {
                                putExtra("firma_bitmap", bitmap)
                            }
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        } catch (e: Exception) {
                            Log.e("FirmaError", "Error al crear bitmap", e)
                            setResult(RESULT_CANCELED)
                            finish()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PizarraFirmaScreen(
    onSave: (List<List<Offset>>, Int, Int) -> Unit
) {
    val strokes = remember { mutableStateListOf<MutableList<Offset>>() }
    val currentStroke = remember { mutableStateListOf<Offset>() }
    var canvasWidth by remember { mutableStateOf(0f) }
    var canvasHeight by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Firma Virtual",
            fontSize = 24.sp,
            modifier = Modifier.padding(16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White)
                .padding(8.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        while (true) {
                            awaitPointerEventScope {
                                val down = awaitPointerEvent().changes.firstOrNull() ?: return@awaitPointerEventScope
                                if (down.pressed) {
                                    currentStroke.clear()
                                    currentStroke.add(down.position)

                                    do {
                                        val event = awaitPointerEvent()
                                        val pointer = event.changes.firstOrNull() ?: break
                                        if (pointer.pressed) {
                                            currentStroke.add(pointer.position)
                                            pointer.consume()
                                        }
                                    } while (pointer.pressed)

                                    if (currentStroke.isNotEmpty()) {
                                        strokes.add(currentStroke.toMutableList())
                                        currentStroke.clear()
                                    }
                                }
                            }
                        }
                    }
            ) {
                canvasWidth = size.width
                canvasHeight = size.height

                strokes.forEach { stroke ->
                    for (i in 1 until stroke.size) {
                        drawLine(Color.Black, stroke[i - 1], stroke[i], strokeWidth = 8f)
                    }
                }

                for (i in 1 until currentStroke.size) {
                    drawLine(Color.Black, currentStroke[i - 1], currentStroke[i], strokeWidth = 8f)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    strokes.clear()
                    currentStroke.clear()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Borrar")
            }

            Button(
                onClick = {
                    if (strokes.isNotEmpty()) {
                        val widthPx = canvasWidth.roundToInt()
                        val heightPx = canvasHeight.roundToInt()
                        onSave(strokes.map { it.toList() }, widthPx, heightPx)
                    }
                }
            ) {
                Text("Guardar")
            }
        }
    }
}
