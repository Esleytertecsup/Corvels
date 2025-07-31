package com.example.corvels

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory // Importar BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.corvels.ui.theme.CorvelsTheme
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Rect as AndroidRect

class ActaConformidadActivity : ComponentActivity() {
    private var actaCounter = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var darkTheme by remember { mutableStateOf(false) }
            val actaNumber = remember { mutableStateOf(actaCounter) }

            CorvelsTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Este es el logo de la aplicación, no el del PDF
                            Image(
                                painter = painterResource(id = R.drawable.corvels),
                                contentDescription = "Logo Corvels",
                                modifier = Modifier.size(24.dp)
                            )

                            IconButton(
                                onClick = { darkTheme = !darkTheme },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Image(
                                    painter = painterResource(
                                        id = if (darkTheme) R.drawable.luna
                                        else R.drawable.sol
                                    ),
                                    contentDescription = "Toggle Theme"
                                )
                            }
                        }

                        ActaConformidadScreen(
                            actaNumber = actaNumber.value,
                            onActaNumberUpdate = { actaNumber.value = it }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun ActaConformidadScreen(
        actaNumber: Int,
        onActaNumberUpdate: (Int) -> Unit
    ) {
        var cliente by remember { mutableStateOf("") }
        var direccion by remember { mutableStateOf("") }
        var fecha by remember { mutableStateOf(getCurrentDate()) }

        val context = LocalContext.current
        var horaInicio by remember { mutableStateOf("") }
        var horaFin by remember { mutableStateOf("") }

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerInicio = TimePickerDialog(
            context,
            { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                horaInicio = String.format("%02d:%02d %s",
                    if (selectedHour > 12) selectedHour - 12 else if (selectedHour == 0) 12 else selectedHour,
                    selectedMinute,
                    if (selectedHour < 12) "AM" else "PM")
            }, hour, minute, false
        )

        val timePickerFin = TimePickerDialog(
            context,
            { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                horaFin = String.format("%02d:%02d %s",
                    if (selectedHour > 12) selectedHour - 12 else if (selectedHour == 0) 12 else selectedHour,
                    selectedMinute,
                    if (selectedHour < 12) "AM" else "PM")
            }, hour, minute, false
        )

        var servicios by remember { mutableStateOf(listOf(
            Servicio("Fumigación integral", false),
            Servicio("Sostenimiento", false),
            Servicio("Otros", false)
        ))}
        var otrosServicios by remember { mutableStateOf(listOf("")) }
        var showOtrosServiciosDialog by remember { mutableStateOf(false) }

        var trabajosRealizados by remember { mutableStateOf(listOf(
            Trabajo("Desinfección", false),
            Trabajo("Limpieza y desinfección de cisternas o reservorios de agua", false),
            Trabajo("Desratización", false),
            Trabajo("Limpieza de tanques sépticos", false),
            Trabajo("Desinsectación", false),
            Trabajo("Otros", false)
        ))}
        var otrosTrabajos by remember { mutableStateOf(listOf("")) }
        var showOtrosTrabajosDialog by remember { mutableStateOf(false) }

        var areasTratadas by remember { mutableStateOf("") }
        var productos by remember { mutableStateOf("") }
        var tipoInsectos by remember { mutableStateOf("") }
        var cantidadInsectos by remember { mutableStateOf("") }
        var insectosList by remember { mutableStateOf(listOf<Insecto>()) }
        var cebaderos by remember { mutableStateOf("") }
        var consumoTotal by remember { mutableStateOf("") }
        var consumoParcial by remember { mutableStateOf("") }
        var observaciones by remember { mutableStateOf("") }
        var personal by remember { mutableStateOf(listOf("")) }

        val strokesCliente = remember { mutableStateListOf<List<Offset>>() }
        val currentStrokeCliente = remember { mutableStateListOf<Offset>() }

        val strokesTecnico = remember { mutableStateListOf<List<Offset>>() }
        val currentStrokeTecnico = remember { mutableStateListOf<Offset>() }

        var pdfName by remember { mutableStateOf(TextFieldValue("Acta_${actaNumber}")) }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Se elimina el Image Composable de Compose aquí, ya que el logo se dibujará directamente en el PDF
                // El logo se agregará directamente en la función de generación del PDF
                Spacer(modifier = Modifier.width(80.dp)) // Espaciador para mantener el diseño si era necesario antes

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "ACTA DE CONFORMIDAD Y FIN DE SERVICIO",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "N°$actaNumber",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            FormField(
                label = "CLIENTE",
                value = cliente,
                onValueChange = { cliente = it }
            )
            FormField(
                label = "DIRECCIÓN",
                value = direccion,
                onValueChange = { direccion = it }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FormField(
                    label = "FECHA",
                    value = fecha,
                    onValueChange = { fecha = it },
                    modifier = Modifier.weight(1f)
                )

                Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                    Text(
                        text = "HORA INICIO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    OutlinedButton(
                        onClick = { timePickerInicio.show() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (horaInicio.isEmpty()) "Seleccionar hora" else horaInicio,
                            color = if (horaInicio.isEmpty()) Color.Gray else Color.Unspecified
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                    Text(
                        text = "HORA FIN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    OutlinedButton(
                        onClick = { timePickerFin.show() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (horaFin.isEmpty()) "Seleccionar hora" else horaFin,
                            color = if (horaFin.isEmpty()) Color.Gray else Color.Unspecified
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tipo de Servicio:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            servicios.forEach { servicio ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ServiceCheckbox(
                        text = servicio.nombre,
                        checked = servicio.seleccionado,
                        onCheckedChange = { checked ->
                            val updated = servicios.toMutableList()
                            val index = updated.indexOf(servicio)
                            if (index != -1) {
                                updated[index] = servicio.copy(seleccionado = checked)
                                servicios = updated

                                if (servicio.nombre == "Otros" && checked) {
                                    showOtrosServiciosDialog = true
                                }
                            }
                        }
                    )

                    if (servicio.nombre == "Otros" && servicio.seleccionado && otrosServicios.any { it.isNotEmpty() }) {
                        Text(
                            text = ": ${otrosServicios.joinToString(", ")}",
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "La empresa CORVEL'S S.R.L ha realizado los siguientes trabajos:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            trabajosRealizados.forEach { trabajo ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ServiceCheckbox(
                        text = trabajo.nombre,
                        checked = trabajo.realizado,
                        onCheckedChange = { checked ->
                            val updated = trabajosRealizados.toMutableList()
                            val index = updated.indexOf(trabajo)
                            if (index != -1) {
                                updated[index] = trabajo.copy(realizado = checked)
                                trabajosRealizados = updated

                                if (trabajo.nombre == "Otros" && checked) {
                                    showOtrosTrabajosDialog = true
                                }
                            }
                        }
                    )

                    if (trabajo.nombre == "Otros" && trabajo.realizado && otrosTrabajos.any { it.isNotEmpty() }) {
                        Text(
                            text = ": ${otrosTrabajos.joinToString(", ")}",
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Área(s) tratada(s):",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = areasTratadas,
                onValueChange = { areasTratadas = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder = { Text("Describa las áreas tratadas") }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Productos:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = productos,
                onValueChange = { productos = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder = { Text("Liste los productos utilizados") }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Control de insectos:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FormField(
                    label = "TIPO DE INSECTOS",
                    value = tipoInsectos,
                    onValueChange = { tipoInsectos = it },
                    modifier = Modifier.weight(1f)
                )
                FormField(
                    label = "CANTIDAD",
                    value = cantidadInsectos,
                    onValueChange = { cantidadInsectos = it },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                IconButton(
                    onClick = {
                        if (tipoInsectos.isNotEmpty() && cantidadInsectos.isNotEmpty()) {
                            insectosList = insectosList + Insecto(tipoInsectos, cantidadInsectos)
                            tipoInsectos = ""
                            cantidadInsectos = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar insecto")
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                items(insectosList) { insecto ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${insecto.tipo} - Cantidad: ${insecto.cantidad}")
                        IconButton(
                            onClick = {
                                insectosList = insectosList.filter { it != insecto }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Control de roedores:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FormField(
                    label = "CEBADEROS O TRAMPAS",
                    value = cebaderos,
                    onValueChange = { cebaderos = it },
                    modifier = Modifier.weight(1f)
                )
                FormField(
                    label = "CONSUMO TOTAL",
                    value = consumoTotal,
                    onValueChange = { consumoTotal = it },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                FormField(
                    label = "CONSUMO PARCIAL",
                    value = consumoParcial,
                    onValueChange = { consumoParcial = it },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Observaciones:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder = { Text("Ingrese observaciones adicionales") }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Personal que intervino en el servicio:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            personal.forEach { nombre ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { newValue ->
                            val updated = personal.toMutableList()
                            val index = updated.indexOf(nombre)
                            if (index != -1) {
                                updated[index] = newValue
                                personal = updated
                            }
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Nombre del personal") }
                    )

                    IconButton(
                        onClick = {
                            personal = personal.filter { it != nombre }
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                }
            }

            Button(
                onClick = { personal = personal + "" },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
                Text("Agregar personal")
            }

            Spacer(modifier = Modifier.height(24.dp))
            // Signature Sections
            Text("FIRMAS", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Firma del Cliente", fontSize = 16.sp)
                PizarraFirma(strokes = strokesCliente, currentStroke = currentStrokeCliente, height = 180.dp, width = 300.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    strokesCliente.clear()
                    currentStrokeCliente.clear()
                }) {
                    Text("Limpiar Firma Cliente")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Firma del Técnico", fontSize = 16.sp)
                PizarraFirma(strokes = strokesTecnico, currentStroke = currentStrokeTecnico, height = 180.dp, width = 300.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    strokesTecnico.clear()
                    currentStrokeTecnico.clear()
                }) {
                    Text("Limpiar Firma Técnico")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Nombre del PDF:", fontSize = 18.sp)
            OutlinedTextField(
                value = pdfName,
                onValueChange = { pdfName = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { finish() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Volver")
                }

                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        exportActaToPdf( // Call the new export function
                            context = context,
                            actaNumber = actaNumber,
                            cliente = cliente,
                            direccion = direccion,
                            fecha = fecha,
                            horaInicio = horaInicio,
                            horaFin = horaFin,
                            servicios = servicios,
                            otrosServicios = otrosServicios,
                            trabajosRealizados = trabajosRealizados,
                            otrosTrabajos = otrosTrabajos,
                            areasTratadas = areasTratadas,
                            productos = productos,
                            insectosList = insectosList,
                            cebaderos = cebaderos,
                            consumoTotal = consumoTotal,
                            consumoParcial = consumoParcial,
                            observaciones = observaciones,
                            personal = personal,
                            strokesCliente = strokesCliente,
                            strokesTecnico = strokesTecnico,
                            fileName = pdfName.text.trim()
                        )
                        onActaNumberUpdate(actaNumber + 1)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF463EDC),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar Acta y PDF")
                }
            }
        }

        if (showOtrosServiciosDialog) {
            AlertDialog(
                onDismissRequest = { showOtrosServiciosDialog = false },
                title = { Text("Especificar otros servicios") },
                text = {
                    Column {
                        otrosServicios.forEach { servicio ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = servicio,
                                    onValueChange = { newValue ->
                                        val updated = otrosServicios.toMutableList()
                                        val index = updated.indexOf(servicio)
                                        if (index != -1) {
                                            updated[index] = newValue
                                            otrosServicios = updated
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Descripción del servicio") }
                                )
                                IconButton(
                                    onClick = {
                                        otrosServicios = otrosServicios.filter { it != servicio }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                }
                            }
                        }
                        Button(
                            onClick = { otrosServicios = otrosServicios + "" },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar")
                            Text("Agregar otro servicio")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showOtrosServiciosDialog = false
                        }
                    ) {
                        Text("Aceptar")
                    }
                }
            )
        }

        if (showOtrosTrabajosDialog) {
            AlertDialog(
                onDismissRequest = { showOtrosTrabajosDialog = false },
                title = { Text("Agregar otros trabajos realizados") },
                text = {
                    Column {
                        otrosTrabajos.forEach { trabajo ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = trabajo,
                                    onValueChange = { newValue ->
                                        val updated = otrosTrabajos.toMutableList()
                                        val index = updated.indexOf(trabajo)
                                        if (index != -1) {
                                            updated[index] = newValue
                                            otrosTrabajos = updated
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Descripción del trabajo") }
                                )
                                IconButton(
                                    onClick = {
                                        otrosTrabajos = otrosTrabajos.filter { it != trabajo }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                }
                            }
                        }
                        Button(
                            onClick = { otrosTrabajos = otrosTrabajos + "" },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar")
                            Text("Agregar otro trabajo")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showOtrosTrabajosDialog = false
                        }
                    ) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }

    @Composable
    fun FormField(
        label: String,
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default
    ) {
        Column(modifier = modifier.padding(horizontal = 4.dp)) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = keyboardOptions
            )
        }
    }

    @Composable
    fun ServiceCheckbox(
        text: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
            Text(text = text)
        }
    }

    @Composable
    fun PizarraFirma(
        strokes: MutableList<List<Offset>>,
        currentStroke: MutableList<Offset>,
        height: Dp,
        width: Dp
    ) {
        Box(
            modifier = Modifier
                .height(height)
                .width(width)
                .border(2.dp, Color.Black)
                .background(Color.White)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        while (true) {
                            awaitPointerEventScope {
                                val down: PointerInputChange = awaitPointerEvent().changes.firstOrNull() ?: return@awaitPointerEventScope
                                val area = Rect(0f, 0f, size.width.toFloat(), size.height.toFloat())

                                if (down.pressed && area.contains(down.position)) {
                                    currentStroke.clear()
                                    currentStroke.add(down.position)

                                    do {
                                        val event = awaitPointerEvent()
                                        val pointer = event.changes.firstOrNull() ?: break
                                        if (pointer.pressed && area.contains(pointer.position)) {
                                            currentStroke.add(pointer.position)
                                            pointer.consumeAllChanges()
                                        } else break
                                    } while (pointer.pressed)

                                    if (currentStroke.isNotEmpty()) {
                                        strokes.add(currentStroke.toList())
                                        currentStroke.clear()
                                    }
                                }
                            }
                        }
                    }
            ) {
                strokes.forEach { stroke ->
                    for (i in 1 until stroke.size) {
                        drawLine(
                            color = Color.Black,
                            start = stroke[i - 1],
                            end = stroke[i],
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )
                    }
                }
                for (i in 1 until currentStroke.size) {
                    drawLine(
                        color = Color.Black,
                        start = currentStroke[i - 1],
                        end = currentStroke[i],
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }

    /**
     * Helper function to draw wrapped text onto a PDF canvas.
     * Returns the new Y position after drawing the text.
     */
    private fun drawWrappedText(canvas: AndroidCanvas, text: String, x: Float, y: Float, maxWidth: Float, lineHeight: Float, textPaint: Paint): Float {
        val words = text.split(" ")
        var currentY = y
        var line = ""

        // Calculate text bounds to get exact height for vertical centering/alignment
        val bounds = AndroidRect()

        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            textPaint.getTextBounds(testLine, 0, testLine.length, bounds)
            if (textPaint.measureText(testLine) < maxWidth) {
                line = testLine
            } else {
                canvas.drawText(line.trim(), x, currentY, textPaint)
                currentY += lineHeight
                line = word
            }
        }
        // Draw the last line
        if (line.isNotEmpty()) {
            canvas.drawText(line.trim(), x, currentY, textPaint)
        }
        return currentY + lineHeight // Return the next available Y position
    }

    /**
     * Exports the Acta de Conformidad data, including signatures, to a PDF file
     * with a layout designed to mimic the provided Word document template.
     */
    fun exportActaToPdf(
        context: Context,
        actaNumber: Int,
        cliente: String,
        direccion: String,
        fecha: String,
        horaInicio: String,
        horaFin: String,
        servicios: List<ActaConformidadActivity.Servicio>,
        otrosServicios: List<String>,
        trabajosRealizados: List<ActaConformidadActivity.Trabajo>,
        otrosTrabajos: List<String>,
        areasTratadas: String,
        productos: String,
        insectosList: List<ActaConformidadActivity.Insecto>,
        cebaderos: String,
        consumoTotal: String,
        consumoParcial: String,
        observaciones: String,
        personal: List<String>,
        strokesCliente: List<List<Offset>>,
        strokesTecnico: List<List<Offset>>,
        fileName: String
    ) {
        val pdfDocument = PdfDocument()
        val pageHeight = 1120 // A4 portrait ~ 1120 points (approx 792 x 1120 for A4 at 72dpi)
        val pageWidth = 792
        val margin = 40f
        val lineHeight = 18f // Standard line height for text fields
        val labelToValuePadding = 10f // Nuevo padding para la distancia entre etiqueta y valor

        // Dimensiones originales de la PizarraFirma en Compose (en dp)
        val composePizarraWidthDp = 300f
        val composePizarraHeightDp = 180f

        // Obtener la densidad de la pantalla para convertir Dp a píxeles
        val density = context.resources.displayMetrics.density

        // Calcular las dimensiones de la pizarra en píxeles
        val composePizarraWidthPx = composePizarraWidthDp * density
        val composePizarraHeightPx = composePizarraHeightDp * density


        // Dimensiones del Bitmap de la firma interna (para alta resolución de la firma)
        // Se crean más grandes que las Compose DPs para mejorar la calidad al escalar
        val signatureBitmapWidthInternal = 400
        val signatureBitmapHeightInternal = 240

        // Dimensiones del Bitmap de la firma en el PDF (tamaño al que se escalará la firma)
        val targetSignaturePdfWidth = 300f // Ejemplo: 300 puntos de ancho en el PDF
        val targetSignaturePdfHeight = 180f // Ejemplo: 180 puntos de alto en el PDF (manteniendo la proporción ~1.66)


        // --- Paint Objects for Styling ---
        val titlePaint = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 24f
            isFakeBoldText = true
            textAlign = Paint.Align.LEFT
        }
        val headingPaint = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 12f
            isFakeBoldText = true
        }
        val normalTextPaint = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 12f
        }
        val smallTextPaint = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 10f
        }
        val linePaint = Paint().apply {
            color = AndroidColor.BLACK
            strokeWidth = 1f
        }
        val thinLinePaint = Paint().apply {
            color = AndroidColor.BLACK
            strokeWidth = 0.5f
        }
        val checkboxPaint = Paint().apply {
            color = AndroidColor.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        val checkmarkPaint = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 10f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        val corvelsSignaturePaint = Paint().apply { // Nuevo Paint para "CORVEL'S S.R.L"
            color = AndroidColor.BLUE
            textSize = 12f
            isFakeBoldText = true
            isUnderlineText = true // Subrayado
        }


        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var currentY = margin

        // --- Agregar Logo Corvels en la esquina superior izquierda ---
        // Cargar el bitmap desde los recursos
        val logoBitmap: Bitmap? = BitmapFactory.decodeResource(context.resources, R.drawable.corvels)

        if (logoBitmap != null) {
            val logoWidth = 60f // Ancho deseado para el logo en el PDF
            val logoHeight = 60f // Alto deseado para el logo en el PDF
            val logoRect = AndroidRect(margin.toInt(), margin.toInt(), (margin + logoWidth).toInt(), (margin + logoHeight).toInt())
            canvas.drawBitmap(logoBitmap, null, logoRect, null)
            logoBitmap.recycle() // Liberar la memoria del bitmap
        } else {
            Log.e("PDFCreation", "No se pudo cargar el logo 'corvels.webp' desde los recursos.")
        }
        // Ajustar currentY para que el contenido comience después del logo y el encabezado
        currentY = margin + 80f // Dejamos espacio para el logo y el título

        // --- Header (ajustado para que no se superponga con el logo) ---
        val headerText = "ACTA DE CONFORMIDAD Y FIN DE SERVICIO"
        val headerTextWidth = titlePaint.measureText(headerText)
        val headerX = (pageWidth - headerTextWidth) / 2
        canvas.drawText(headerText, headerX, margin + 30, titlePaint) // Ajustado para centrar y no superponer

        val actaNumText = "N°$actaNumber"
        val actaNumTextWidth = headingPaint.measureText(actaNumText)
        val actaNumX = (pageWidth - actaNumTextWidth) / 2
        canvas.drawText(actaNumText, actaNumX, margin + 55, headingPaint.apply { textAlign = Paint.Align.CENTER }) // Ajustado

        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, linePaint)
        currentY += 20f

        // --- CLIENTE ---
        val clientLabelX = margin + 5f
        canvas.drawText("CLIENTE:", clientLabelX, currentY, headingPaint)
        val clientValueXStart = clientLabelX + headingPaint.measureText("CLIENTE:") + labelToValuePadding
        canvas.drawText(cliente, clientValueXStart, currentY, normalTextPaint)
        canvas.drawLine(clientValueXStart, currentY + 5, pageWidth - margin, currentY + 5, linePaint)
        currentY += 30f

        // --- DIRECCIÓN ---
        val directionLabelX = margin + 5f
        canvas.drawText("DIRECCIÓN:", directionLabelX, currentY, headingPaint)
        val directionValueXStart = directionLabelX + headingPaint.measureText("DIRECCIÓN:") + labelToValuePadding
        canvas.drawText(direccion, directionValueXStart, currentY, normalTextPaint)
        canvas.drawLine(directionValueXStart, currentY + 5, pageWidth - margin, currentY + 5, linePaint)
        currentY += 30f

        // --- FECHA, HORA INICIO, HORA FIN ---
        val textGap = 10f
        val fieldWidth = (pageWidth - 2 * margin - 2 * textGap) / 3

        // FECHA
        val fechaLabelX = margin + 5f
        canvas.drawText("FECHA:", fechaLabelX, currentY, headingPaint)
        val fechaValueX = fechaLabelX + headingPaint.measureText("FECHA:") + labelToValuePadding
        canvas.drawText(fecha, fechaValueX, currentY, normalTextPaint)
        canvas.drawLine(fechaValueX, currentY + 5, fechaValueX + fieldWidth - headingPaint.measureText("FECHA:") - labelToValuePadding - 20, currentY + 5, linePaint)


        // HORA INICIO
        val horaInicioLabelX = margin + fieldWidth + textGap + 5f
        canvas.drawText("HORA INICIO:", horaInicioLabelX, currentY, headingPaint)
        val horaInicioValueX = horaInicioLabelX + headingPaint.measureText("HORA INICIO:") + labelToValuePadding
        canvas.drawText(horaInicio, horaInicioValueX, currentY, normalTextPaint)
        canvas.drawLine(horaInicioValueX, currentY + 5, horaInicioValueX + fieldWidth - headingPaint.measureText("HORA INICIO:") - labelToValuePadding + 20, currentY + 5, linePaint)

        // HORA FIN
        val horaFinLabelX = margin + 2 * fieldWidth + 2 * textGap + 10f
        canvas.drawText("HORA FIN:", horaFinLabelX, currentY, headingPaint)
        val horaFinValueX = horaFinLabelX + headingPaint.measureText("HORA FIN:") + labelToValuePadding
        canvas.drawText(horaFin, horaFinValueX, currentY, normalTextPaint)
        canvas.drawLine(horaFinValueX, currentY + 5, pageWidth - margin, currentY + 5, linePaint)
        currentY += 40f

        // --- Tipo de Servicio ---
        canvas.drawText("Tipo de Servicio:", margin, currentY, headingPaint)
        currentY += 20f
        servicios.forEach { servicio ->
            val checkboxSize = 12f
            val checkboxX = margin + 5f
            val checkboxY = currentY - (checkboxSize / 2) - 2

            canvas.drawRect(checkboxX, checkboxY, checkboxX + checkboxSize, checkboxY + checkboxSize, checkboxPaint)
            if (servicio.seleccionado) {
                canvas.drawText("X", checkboxX + checkboxSize / 2, checkboxY + checkboxSize * 0.8f, checkmarkPaint)
            }
            canvas.drawText(servicio.nombre, margin + 25f, currentY, normalTextPaint)
            if (servicio.nombre == "Otros" && servicio.seleccionado && otrosServicios.any { it.isNotEmpty() }) {
                canvas.drawText(": ${otrosServicios.joinToString(", ")}", margin + 100f, currentY, normalTextPaint)
            }
            currentY += lineHeight
        }
        currentY += 20f

        // --- Trabajos Realizados ---
        canvas.drawText("La empresa CORVEL'S S.R.L ha realizado los siguientes trabajos:", margin, currentY, headingPaint)
        currentY += 20f
        trabajosRealizados.forEach { trabajo ->
            val checkboxSize = 12f
            val checkboxX = margin + 5f
            val checkboxY = currentY - (checkboxSize / 2) - 2

            canvas.drawRect(checkboxX, checkboxY, checkboxX + checkboxSize, checkboxY + checkboxSize, checkboxPaint)
            if (trabajo.realizado) {
                canvas.drawText("X", checkboxX + checkboxSize / 2, checkboxY + checkboxSize * 0.8f, checkmarkPaint)
            }
            canvas.drawText(trabajo.nombre, margin + 25f, currentY, normalTextPaint)
            if (trabajo.nombre == "Otros" && trabajo.realizado && otrosTrabajos.any { it.isNotEmpty() }) {
                canvas.drawText(": ${otrosTrabajos.joinToString(", ")}", margin + 100f, currentY, normalTextPaint)
            }
            currentY += lineHeight
        }
        currentY += 20f

        // --- Área(s) tratada(s) ---
        canvas.drawText("Área(s) tratada(s):", margin, currentY, headingPaint)
        currentY += 15f
        currentY = drawWrappedText(canvas, areasTratadas, margin, currentY, pageWidth - 2 * margin, lineHeight, normalTextPaint)
        currentY += 20f

        // --- Productos ---
        canvas.drawText("Productos:", margin, currentY, headingPaint)
        currentY += 15f
        currentY = drawWrappedText(canvas, productos, margin, currentY, pageWidth - 2 * margin, lineHeight, normalTextPaint)
        currentY += 20f

        // --- Control de insectos ---
        canvas.drawText("Control de insectos:", margin, currentY, headingPaint)
        currentY += 20f
        // Dibujar encabezados de tabla para insectos
        val insectHeaderY = currentY
        canvas.drawText("TIPO DE INSECTOS:", margin + 5, insectHeaderY, normalTextPaint)
        canvas.drawText("CANTIDAD:", margin + 350, insectHeaderY, normalTextPaint)
        canvas.drawLine(margin, insectHeaderY + 5, pageWidth - margin, insectHeaderY + 5, thinLinePaint)

        currentY += 20f
        insectosList.forEach { insecto ->
            canvas.drawText(insecto.tipo, margin + 5, currentY, normalTextPaint)
            canvas.drawText(insecto.cantidad, margin + 350, currentY, normalTextPaint)
            canvas.drawLine(margin, currentY + 5, pageWidth - margin, currentY + 5, thinLinePaint)
            currentY += 20f
        }
        currentY += 20f

        // --- Control de roedores ---
        canvas.drawText("Control de roedores:", margin, currentY, headingPaint)
        currentY += 20f
        // Dibujar encabezados de tabla para roedores
        val roedorHeaderY = currentY
        canvas.drawText("CEBADEROS O TRAMPAS:", margin + 5, roedorHeaderY, normalTextPaint)
        canvas.drawText("CONSUMO TOTAL:", margin + 280, roedorHeaderY, normalTextPaint)
        canvas.drawText("CONSUMO PARCIAL:", margin + 530, roedorHeaderY, normalTextPaint)
        canvas.drawLine(margin, roedorHeaderY + 5, pageWidth - margin, roedorHeaderY + 5, thinLinePaint)

        currentY += 20f
        canvas.drawText(cebaderos, margin + 5, currentY, normalTextPaint)
        canvas.drawText(consumoTotal, margin + 280, currentY, normalTextPaint)
        canvas.drawText(consumoParcial, margin + 530, currentY, normalTextPaint)
        canvas.drawLine(margin, currentY + 5, pageWidth - margin, currentY + 5, thinLinePaint)
        currentY += 40f

        // --- Observaciones ---
        canvas.drawText("Observaciones:", margin, currentY, headingPaint)
        currentY += 15f
        currentY = drawWrappedText(canvas, observaciones, margin, currentY, pageWidth - 2 * margin, lineHeight, normalTextPaint)
        currentY += 20f

        // --- Personal que intervino en el servicio ---
        canvas.drawText("Personal que intervino en el servicio:", margin, currentY, headingPaint)
        currentY += 20f
        personal.forEach { name ->
            canvas.drawText(name, margin + 5, currentY, normalTextPaint)
            currentY += lineHeight
        }
        currentY += 50f

        // --- Signatures ---

        fun drawStrokesToBitmap(strokes: List<List<Offset>>): Bitmap {
            // Create a higher-resolution bitmap to draw strokes onto
            val bitmap = Bitmap.createBitmap(signatureBitmapWidthInternal, signatureBitmapHeightInternal, Bitmap.Config.ARGB_8888)
            val canvas = AndroidCanvas(bitmap)
            canvas.drawColor(AndroidColor.WHITE) // White background for the signature area

            val paint = Paint().apply {
                color = AndroidColor.BLACK
                strokeWidth = 6f // Line thickness for the signature
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                isAntiAlias = true
            }

            // Scale factor from actual Compose Pizarra PX to internal bitmap resolution
            val scaleX = signatureBitmapWidthInternal / composePizarraWidthPx
            val scaleY = signatureBitmapHeightInternal / composePizarraHeightPx

            for (stroke in strokes) {
                for (i in 1 until stroke.size) {
                    canvas.drawLine(
                        stroke[i - 1].x * scaleX,
                        stroke[i - 1].y * scaleY,
                        stroke[i].x * scaleX,
                        stroke[i].y * scaleY,
                        paint
                    )
                }
            }
            return bitmap
        }

        val bitmapCliente = drawStrokesToBitmap(strokesCliente)
        val bitmapTecnico = drawStrokesToBitmap(strokesTecnico)

        val signatureAreaY = currentY
        val signatureBlockPadding = 60f
        val signatureBlockWidth = (pageWidth - 2 * margin - signatureBlockPadding) / 2

        val clientSignatureBlockX = margin + 10f
        val tecnicoSignatureBlockX = clientSignatureBlockX + signatureBlockWidth + signatureBlockPadding

        // Calculate positions for scaled bitmaps
        val clientBitmapLeft = clientSignatureBlockX + (signatureBlockWidth - targetSignaturePdfWidth) / 2
        val clientBitmapTop = signatureAreaY
        val clientBitmapRight = clientBitmapLeft + targetSignaturePdfWidth
        val clientBitmapBottom = clientBitmapTop + targetSignaturePdfHeight
        val clientDstRect = AndroidRect(clientBitmapLeft.toInt(), clientBitmapTop.toInt(), clientBitmapRight.toInt(), clientBitmapBottom.toInt())

        val tecnicoBitmapLeft = tecnicoSignatureBlockX + (signatureBlockWidth - targetSignaturePdfWidth) / 2
        val tecnicoBitmapTop = signatureAreaY
        val tecnicoBitmapRight = tecnicoBitmapLeft + targetSignaturePdfWidth
        val tecnicoBitmapBottom = tecnicoBitmapTop + targetSignaturePdfHeight
        val tecnicoDstRect = AndroidRect(tecnicoBitmapLeft.toInt(), tecnicoBitmapTop.toInt(), tecnicoBitmapRight.toInt(), tecnicoBitmapBottom.toInt())


        // --- Firma del CLIENTE ---
        val clientLineY = signatureAreaY + targetSignaturePdfHeight + 10f
        canvas.drawLine(clientSignatureBlockX, clientLineY, clientSignatureBlockX + signatureBlockWidth, clientLineY, linePaint)

        // Draw the client's signature bitmap, scaled to target size
        canvas.drawBitmap(bitmapCliente, null, clientDstRect, null)

        val clienteText = "CLIENTE"
        val clientTextWidth = normalTextPaint.measureText(clienteText)
        canvas.drawText(clienteText, clientSignatureBlockX + (signatureBlockWidth / 2) - (clientTextWidth / 2), clientLineY + 20f, normalTextPaint)

        canvas.drawText("DNI:", clientSignatureBlockX, clientLineY + 40f, normalTextPaint)
        canvas.drawLine(clientSignatureBlockX + 40, clientLineY + 45f, clientSignatureBlockX + signatureBlockWidth, clientLineY + 45f, linePaint)


        // --- Firma de CORVEL’S S.R.L (Técnico) ---
        val tecnicoLineY = signatureAreaY + targetSignaturePdfHeight + 10f
        canvas.drawLine(tecnicoSignatureBlockX, tecnicoLineY, tecnicoSignatureBlockX + signatureBlockWidth, tecnicoLineY, linePaint)

        // Draw the technician's signature bitmap, scaled to target size
        canvas.drawBitmap(bitmapTecnico, null, tecnicoDstRect, null)

        val corvelsText = "CORVEL’S S.R.L"
        val corvelsTextWidth = corvelsSignaturePaint.measureText(corvelsText)
        canvas.drawText(corvelsText, tecnicoSignatureBlockX + (signatureBlockWidth / 2) - (corvelsTextWidth / 2), tecnicoLineY + 20f, corvelsSignaturePaint)

        canvas.drawText("TECNICO:", tecnicoSignatureBlockX, tecnicoLineY + 40f, normalTextPaint)
        canvas.drawLine(tecnicoSignatureBlockX + 70, tecnicoLineY + 45f, tecnicoSignatureBlockX + signatureBlockWidth, tecnicoLineY + 45f, linePaint)


        currentY = signatureAreaY + targetSignaturePdfHeight + 80f

        pdfDocument.finishPage(page)

        val finalFileName = if (fileName.isBlank()) {
            "ActaConformidad_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date()) + ".pdf"
        } else {
            fileName.replace(".pdf", "") + ".pdf"
        }

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), finalFileName)

        try {
            FileOutputStream(file).use {
                pdfDocument.writeTo(it)
            }
            pdfDocument.close()
            bitmapCliente.recycle()
            bitmapTecnico.recycle()

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Abrir PDF con..."))
            Toast.makeText(context, "PDF guardado en: ${file.absolutePath}", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Log.e("PDFSaveError", "Error al guardar PDF: ${e.message}", e)
            Toast.makeText(context, "Error al guardar PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    data class Servicio(val nombre: String, val seleccionado: Boolean)
    data class Trabajo(val nombre: String, val realizado: Boolean)
    data class Insecto(val tipo: String, val cantidad: String)
}