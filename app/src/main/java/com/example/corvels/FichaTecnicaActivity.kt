package com.example.corvels

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.corvels.ui.theme.CorvelsTheme
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Rect as AndroidRect

// Data classes for dynamic lists
data class ProductEntry(
    var name: String = "",
    var quantity: String = "",
    var concentration: String = ""
)

data class PersonnelEntry(
    var name: String = "",
    var signature: String = "" // Placeholder for signature, could be a Bitmap/Path list
)

class FichaTecnicaActivity : ComponentActivity() {
    // Mantener un contador para el número de Ficha
    private var fichaCounter = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var darkTheme by remember { mutableStateOf(false) }
            val fichaNumber = remember { mutableStateOf(fichaCounter) }

            CorvelsTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        // Header con logo y botón de tema
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Logo de la empresa (24dp para igualar tamaño del icono de tema)
                            Image(
                                painter = painterResource(id = R.drawable.corvels),
                                contentDescription = "Logo Corvels",
                                modifier = Modifier.size(24.dp)
                            )

                            // Botón de cambio de tema
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

                        // Contenido principal
                        FichaTecnicaScreen(
                            fichaNumber = fichaNumber.value,
                            onFichaNumberUpdate = { fichaNumber.value = it }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun FichaTecnicaScreen(
        fichaNumber: Int,
        onFichaNumberUpdate: (Int) -> Unit
    ) {
        val context = LocalContext.current
        var clienteFicha by remember { mutableStateOf("") }
        var direccionLugar by remember { mutableStateOf("") }
        var giroEmpresa by remember { mutableStateOf("") }
        var fecha by remember { mutableStateOf(getCurrentDate()) }
        var horaInicio by remember { mutableStateOf("") }
        var horaFinal by remember { mutableStateOf("") }
        var certificadoNum by remember { mutableStateOf("") }

        // 1. DIAGNOSTICO - 1.1 DESINFECCION
        var antecedentesDesinfeccion by remember { mutableStateOf("") }
        var condicionesPreviasDesinfeccion by remember { mutableStateOf("") }

        // 1.2 DESRATIZACION
        var rastrosDesratizacion by remember { mutableStateOf("") }
        var tipoRoedores by remember { mutableStateOf("") }

        // 1.3 DESINSECTACION
        var rastrosDesinsectacion by remember { mutableStateOf("") }
        var tipoInsectos by remember { mutableStateOf("") }

        // 1.4 LIMPIEZA Y DESINFECCION DE CISTERNAS O RESERVORIOS AGUA
        var sarroAcumulado by remember { mutableStateOf("") }
        var mohoAcumulado by remember { mutableStateOf("") }
        var arenillaSedimentada by remember { mutableStateOf("") }
        var piedrillasRestos by remember { mutableStateOf("") }
        var otrosObjetos by remember { mutableStateOf("") }

        // 1.5 LIMPIEZA DE TANQUES SEPTICOS
        var limpiezaTanquesSepticosContent by remember { mutableStateOf("") }

        // 2. CONDICION SANITARIA DE LA ZONA CIRCUNDANTE (Checkbox + Observación)
        var acumResiduosSolidosChecked by remember { mutableStateOf(false) }
        var acumResiduosSolidosObs by remember { mutableStateOf("") }

        var espacioDeshabitadoChecked by remember { mutableStateOf(false) }
        var espacioDeshabitadoObs by remember { mutableStateOf("") }

        var descompResiduosOrganChecked by remember { mutableStateOf(false) }
        var descompResiduosOrganObs by remember { mutableStateOf("") }

        var buzoneDesagueDescubChecked by remember { mutableStateOf(false) }
        var buzoneDesagueDescubObs by remember { mutableStateOf("") }

        var desordenChecked by remember { mutableStateOf(false) }
        var desordenObs by remember { mutableStateOf("") }

        var sembriosPlantacionesChecked by remember { mutableStateOf(false) }
        var sembriosPlantacionesObs by remember { mutableStateOf("") }

        var charcosAguaChecked by remember { mutableStateOf(false) }
        var charcosAguaObs by remember { mutableStateOf("") }

        var malOlorChecked by remember { mutableStateOf(false) }
        var malOlorObs by remember { mutableStateOf("") }

        var recolectoresSinTapaChecked by remember { mutableStateOf(false) }
        var recolectoresSinTapaObs by remember { mutableStateOf("") }

        var hierbaCrecidaChecked by remember { mutableStateOf(false) }
        var hierbaCrecidaObs by remember { mutableStateOf("") }

        var areasVerdesExtensasChecked by remember { mutableStateOf(false) }
        var areasVerdesExtensasObs by remember { mutableStateOf("") }

        var excesoPolvoChecked by remember { mutableStateOf(false) }
        var excesoPolvoObs by remember { mutableStateOf("") }


        // 3. TRABAJOS REALIZADOS (usaremos checkboxes)
        var desinsectacionRealizado by remember { mutableStateOf(false) }
        var desratizacionRealizado by remember { mutableStateOf(false) }
        var desinfeccionRealizado by remember { mutableStateOf(false) }
        var limpiezaTanquesSepticoRealizado by remember { mutableStateOf(false) }
        var limpiezaCisternasRealizado by remember { mutableStateOf(false) }

        // 4. PRODUCTOS QUIMICOS O BIOLOGICOS UTILIZADOS (Lista dinámica)
        val productEntries = remember { mutableStateListOf(ProductEntry()) }

        // 5. ACCIONES CORRECTIVAS
        var accionesCorrectivasContent by remember { mutableStateOf("") }

        // 6. PERSONAL QUE EJECUTO EL TRABAJO (Lista dinámica)
        val personnelEntries = remember { mutableStateListOf(PersonnelEntry()) }


        var pdfName by remember { mutableStateOf("Ficha_${fichaNumber}") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Título principal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "FICHA TÉCNICA DE EVALUACIÓN Y DESCRIPCIÓN DE ACTIVIDADES",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // CLIENTE y FICHA N°
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = clienteFicha,
                    onValueChange = { clienteFicha = it },
                    label = { Text("CLIENTE") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "FICHA N°", fontWeight = FontWeight.Bold)
                    Text(text = fichaNumber.toString(), fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // DIRECCIÓN - LUGAR y CERTIFICADO N°
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = direccionLugar,
                    onValueChange = { direccionLugar = it },
                    label = { Text("DIRECCIÓN - LUGAR") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "CERTIFICADO N°", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = certificadoNum,
                        onValueChange = { certificadoNum = it },
                        modifier = Modifier.width(120.dp),
                        singleLine = true
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // GIRO DE LA EMPRESA
            OutlinedTextField(
                value = giroEmpresa,
                onValueChange = { giroEmpresa = it },
                label = { Text("GIRO DE LA EMPRESA") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // FECHA, HORA INICIO, HORA FINAL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    label = { Text("FECHA") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = horaInicio,
                    onValueChange = { horaInicio = it },
                    label = { Text("HORA INICIO") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = horaFinal,
                    onValueChange = { horaFinal = it },
                    label = { Text("HORA FINAL") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            // 1. DIAGNOSTICO Section
            Text(text = "1. DIAGNÓSTICO", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // 1.1 DESINFECCION, 1.2 DESRATIZACION, 1.3 DESINSECTACION (en 3 columnas lógicas)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                    Text(text = "1.1 DESINFECCIÓN", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = antecedentesDesinfeccion,
                        onValueChange = { antecedentesDesinfeccion = it },
                        label = { Text("ANTECEDENTES") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        singleLine = false
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = condicionesPreviasDesinfeccion,
                        onValueChange = { condicionesPreviasDesinfeccion = it },
                        label = { Text("CONDICIONES PREVIAS") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        singleLine = false
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f).padding(start = 4.dp, end = 4.dp)) {
                    Text(text = "1.2. DESRATIZACIÓN", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = rastrosDesratizacion,
                        onValueChange = { rastrosDesratizacion = it },
                        label = { Text("RASTROS") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        singleLine = false
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tipoRoedores,
                        onValueChange = { tipoRoedores = it },
                        label = { Text("TIPO DE ROEDORES") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        singleLine = false
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                    Text(text = "1.3. DESINSECTACIÓN", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = rastrosDesinsectacion,
                        onValueChange = { rastrosDesinsectacion = it },
                        label = { Text("RASTROS") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        singleLine = false
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tipoInsectos,
                        onValueChange = { tipoInsectos = it },
                        label = { Text("TIPO DE INSECTOS") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        singleLine = false
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 1.4 LIMPIEZA Y DESINFECCION DE CISTERNAS O RESERVORIOS AGUA
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                    Text(text = "1.4. LIMPIEZA Y DESINFECCIÓN DE CISTERNAS O RESERVORIOS AGUA", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    OutlinedTextField(value = sarroAcumulado, onValueChange = { sarroAcumulado = it }, label = { Text("SARRO ACUMULADO") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = mohoAcumulado, onValueChange = { mohoAcumulado = it }, label = { Text("MOHO ACUMULADO") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = arenillaSedimentada, onValueChange = { arenillaSedimentada = it }, label = { Text("ARENILLA SEDIMENTADA") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = piedrillasRestos, onValueChange = { piedrillasRestos = it }, label = { Text("PIEDRILLAS O RESTOS") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = otrosObjetos, onValueChange = { otrosObjetos = it }, label = { Text("OTROS OBJETOS") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                Spacer(modifier = Modifier.width(8.dp))
                // 1.5 LIMPIEZA DE TANQUES SEPTICOS
                Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                    Text(text = "1.5. LIMPIEZA DE TANQUES SÉPTICOS", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = limpiezaTanquesSepticosContent,
                        onValueChange = { limpiezaTanquesSepticosContent = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth().height(150.dp), // Ajusta altura
                        singleLine = false
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // 2. CONDICION SANITARIA DE LA ZONA CIRCUNDANTE
            Text(text = "2. CONDICIÓN SANITARIA DE LA ZONA CIRCUNDANTE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "CONDICIÓN", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f))
                Text(text = "OBSERVACIÓN", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.3f))
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Helper para las filas de condición sanitaria con Checkbox y Observación
            @Composable
            fun ConditionRowWithCheckbox(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit, observation: String, onObservationChange: (String) -> Unit) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Row(modifier = Modifier.weight(0.7f), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isChecked, onCheckedChange = onCheckedChange)
                        Text(text = label, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(
                        value = observation,
                        onValueChange = onObservationChange,
                        modifier = Modifier.weight(0.3f),
                        singleLine = true,
                        placeholder = { Text("Obs.") }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            ConditionRowWithCheckbox("ACUMUL. RESIDUOS SÓLIDOS:", acumResiduosSolidosChecked, { acumResiduosSolidosChecked = it }, acumResiduosSolidosObs) { acumResiduosSolidosObs = it }
            ConditionRowWithCheckbox("ESPACIO DESHABITADO:", espacioDeshabitadoChecked, { espacioDeshabitadoChecked = it }, espacioDeshabitadoObs) { espacioDeshabitadoObs = it }
            ConditionRowWithCheckbox("DESCOMP. RESIDUOS ORGÁN:", descompResiduosOrganChecked, { descompResiduosOrganChecked = it }, descompResiduosOrganObs) { descompResiduosOrganObs = it }
            ConditionRowWithCheckbox("BUZÓN DE DESAGÜE DESCUB.:", buzoneDesagueDescubChecked, { buzoneDesagueDescubChecked = it }, buzoneDesagueDescubObs) { buzoneDesagueDescubObs = it }
            ConditionRowWithCheckbox("DESORDEN:", desordenChecked, { desordenChecked = it }, desordenObs) { desordenObs = it }
            ConditionRowWithCheckbox("SEMBRÍOS PLANTACIONES:", sembriosPlantacionesChecked, { sembriosPlantacionesChecked = it }, sembriosPlantacionesObs) { sembriosPlantacionesObs = it }
            ConditionRowWithCheckbox("CHARCOS DE AGUA:", charcosAguaChecked, { charcosAguaChecked = it }, charcosAguaObs) { charcosAguaObs = it }
            ConditionRowWithCheckbox("MAL OLOR:", malOlorChecked, { malOlorChecked = it }, malOlorObs) { malOlorObs = it }
            ConditionRowWithCheckbox("RECOLECTORES DE BASURA SIN TAPA:", recolectoresSinTapaChecked, { recolectoresSinTapaChecked = it }, recolectoresSinTapaObs) { recolectoresSinTapaObs = it }
            ConditionRowWithCheckbox("HIERBA CRECIDA:", hierbaCrecidaChecked, { hierbaCrecidaChecked = it }, hierbaCrecidaObs) { hierbaCrecidaObs = it }
            ConditionRowWithCheckbox("ÁREAS VERDES EXTENSAS:", areasVerdesExtensasChecked, { areasVerdesExtensasChecked = it }, areasVerdesExtensasObs) { areasVerdesExtensasObs = it }
            ConditionRowWithCheckbox("EXCESO DE POLVO:", excesoPolvoChecked, { excesoPolvoChecked = it }, excesoPolvoObs) { excesoPolvoObs = it }
            Spacer(modifier = Modifier.height(24.dp))

            // 3. TRABAJOS REALIZADOS
            Text(text = "3. TRABAJOS REALIZADOS", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // Helper para trabajos realizados con Checkbox
            @Composable
            fun TrabajoRealizadoCheckbox(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isChecked, onCheckedChange = onCheckedChange)
                    Text(text = label)
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                TrabajoRealizadoCheckbox("DESINSECTACIÓN", desinsectacionRealizado) { desinsectacionRealizado = it }
                TrabajoRealizadoCheckbox("DESRATIZACIÓN", desratizacionRealizado) { desratizacionRealizado = it }
                TrabajoRealizadoCheckbox("DESINFECCIÓN", desinfeccionRealizado) { desinfeccionRealizado = it }
                TrabajoRealizadoCheckbox("LIMPIEZA DE TANQUES SÉPTICOS", limpiezaTanquesSepticoRealizado) { limpiezaTanquesSepticoRealizado = it }
                TrabajoRealizadoCheckbox("LIMPIEZA Y DESINFECCIÓN DE CISTERNAS O RESERVORIOS DE AGUA", limpiezaCisternasRealizado) { limpiezaCisternasRealizado = it }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // 4. PRODUCTOS QUIMICOS O BIOLOGICOS UTILIZADOS (LISTA DINÁMICA)
            Text(text = "4. PRODUCTOS QUÍMICOS O BIOLÓGICOS UTILIZADOS", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            productEntries.forEachIndexed { index, product ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedTextField(
                        value = product.name,
                        onValueChange = { productEntries[index] = product.copy(name = it) },
                        label = { Text("Producto ${index + 1}") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = product.quantity,
                        onValueChange = { productEntries[index] = product.copy(quantity = it) },
                        label = { Text("Cantidad") },
                        modifier = Modifier.weight(0.5f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = product.concentration,
                        onValueChange = { productEntries[index] = product.copy(concentration = it) },
                        label = { Text("Concentración") },
                        modifier = Modifier.weight(0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            // Botón para añadir más productos
            Button(
                onClick = { productEntries.add(ProductEntry()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir producto")
                    Spacer(Modifier.width(4.dp))
                    Text("Añadir Producto")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))


            // 5. ACCIONES CORRECTIVAS
            Text(text = "5. ACCIONES CORRECTIVAS:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            OutlinedTextField(
                value = accionesCorrectivasContent,
                onValueChange = { accionesCorrectivasContent = it },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                singleLine = false
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 6. PERSONAL QUE EJECUTO EL TRABAJO (LISTA DINÁMICA)
            Text(text = "6. PERSONAL QUE EJECUTÓ EL TRABAJO", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            personnelEntries.forEachIndexed { index, personnel ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedTextField(
                        value = personnel.name,
                        onValueChange = { personnelEntries[index] = personnel.copy(name = it) },
                        label = { Text("Nombre ${index + 1}") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = personnel.signature,
                        onValueChange = { personnelEntries[index] = personnel.copy(signature = it) },
                        label = { Text("Firma") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ej: Firma Aquí") }
                    )
                    // TODO: Aquí se integraría un componente de firma tipo pizarra
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            // Botón para añadir más personal
            Button(
                onClick = { personnelEntries.add(PersonnelEntry()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir personal")
                    Spacer(Modifier.width(4.dp))
                    Text("Añadir Personal")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))


            // Nombre del PDF
            OutlinedTextField(
                value = pdfName,
                onValueChange = { pdfName = it },
                label = { Text("Nombre del archivo PDF (opcional)") },
                placeholder = { Text("Ej: Ficha_Cliente_Nro${fichaNumber}") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botones
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
                        exportFichaTecnicaToPdf(
                            context = context,
                            fichaNumber = fichaNumber,
                            clienteFicha = clienteFicha,
                            direccionLugar = direccionLugar,
                            giroEmpresa = giroEmpresa,
                            fecha = fecha,
                            horaInicio = horaInicio,
                            horaFinal = horaFinal,
                            certificadoNum = certificadoNum,
                            antecedentesDesinfeccion = antecedentesDesinfeccion,
                            condicionesPreviasDesinfeccion = condicionesPreviasDesinfeccion,
                            rastrosDesratizacion = rastrosDesratizacion,
                            tipoRoedores = tipoRoedores,
                            rastrosDesinsectacion = rastrosDesinsectacion,
                            tipoInsectos = tipoInsectos,
                            sarroAcumulado = sarroAcumulado,
                            mohoAcumulado = mohoAcumulado,
                            arenillaSedimentada = arenillaSedimentada,
                            piedrillasRestos = piedrillasRestos,
                            otrosObjetos = otrosObjetos,
                            limpiezaTanquesSepticosContent = limpiezaTanquesSepticosContent,
                            acumResiduosSolidosChecked = acumResiduosSolidosChecked,
                            acumResiduosSolidosObs = acumResiduosSolidosObs,
                            espacioDeshabitadoChecked = espacioDeshabitadoChecked,
                            espacioDeshabitadoObs = espacioDeshabitadoObs,
                            descompResiduosOrganChecked = descompResiduosOrganChecked,
                            descompResiduosOrganObs = descompResiduosOrganObs,
                            buzoneDesagueDescubChecked = buzoneDesagueDescubChecked,
                            buzoneDesagueDescubObs = buzoneDesagueDescubObs,
                            desordenChecked = desordenChecked,
                            desordenObs = desordenObs,
                            sembriosPlantacionesChecked = sembriosPlantacionesChecked,
                            sembriosPlantacionesObs = sembriosPlantacionesObs,
                            charcosAguaChecked = charcosAguaChecked,
                            charcosAguaObs = charcosAguaObs,
                            malOlorChecked = malOlorChecked,
                            malOlorObs = malOlorObs,
                            recolectoresSinTapaChecked = recolectoresSinTapaChecked,
                            recolectoresSinTapaObs = recolectoresSinTapaObs,
                            hierbaCrecidaChecked = hierbaCrecidaChecked,
                            hierbaCrecidaObs = hierbaCrecidaObs,
                            areasVerdesExtensasChecked = areasVerdesExtensasChecked,
                            areasVerdesExtensasObs = areasVerdesExtensasObs,
                            excesoPolvoChecked = excesoPolvoChecked,
                            excesoPolvoObs = excesoPolvoObs,
                            desinsectacionRealizado = desinsectacionRealizado,
                            desratizacionRealizado = desratizacionRealizado,
                            desinfeccionRealizado = desinfeccionRealizado,
                            limpiezaTanquesSepticoRealizado = limpiezaTanquesSepticoRealizado,
                            limpiezaCisternasRealizado = limpiezaCisternasRealizado,
                            productEntries = productEntries, // Pasa la lista
                            accionesCorrectivasContent = accionesCorrectivasContent,
                            personnelEntries = personnelEntries, // Pasa la lista
                            fileName = pdfName.trim()
                        )
                        fichaCounter++ // Incrementa el contador global de la ficha
                        onFichaNumberUpdate(fichaCounter) // Actualiza el estado en Compose
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF463EDC),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar PDF")
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

        // Adjust line height based on text size if needed for better fit
        val adjustedLineHeight = lineHeight

        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (textPaint.measureText(testLine) < maxWidth) {
                line = testLine
            } else {
                canvas.drawText(line.trim(), x, currentY, textPaint)
                currentY += adjustedLineHeight
                line = word
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line.trim(), x, currentY, textPaint)
        }
        return currentY + adjustedLineHeight
    }


    /**
     * Exports the Ficha Técnica content to a PDF file.
     */
    fun exportFichaTecnicaToPdf(
        context: Context,
        fichaNumber: Int,
        clienteFicha: String,
        direccionLugar: String,
        giroEmpresa: String,
        fecha: String,
        horaInicio: String,
        horaFinal: String,
        certificadoNum: String,
        antecedentesDesinfeccion: String,
        condicionesPreviasDesinfeccion: String,
        rastrosDesratizacion: String,
        tipoRoedores: String,
        rastrosDesinsectacion: String,
        tipoInsectos: String,
        sarroAcumulado: String,
        mohoAcumulado: String,
        arenillaSedimentada: String,
        piedrillasRestos: String,
        otrosObjetos: String,
        limpiezaTanquesSepticosContent: String,
        acumResiduosSolidosChecked: Boolean,
        acumResiduosSolidosObs: String,
        espacioDeshabitadoChecked: Boolean,
        espacioDeshabitadoObs: String,
        descompResiduosOrganChecked: Boolean,
        descompResiduosOrganObs: String,
        buzoneDesagueDescubChecked: Boolean,
        buzoneDesagueDescubObs: String,
        desordenChecked: Boolean,
        desordenObs: String,
        sembriosPlantacionesChecked: Boolean,
        sembriosPlantacionesObs: String,
        charcosAguaChecked: Boolean,
        charcosAguaObs: String,
        malOlorChecked: Boolean,
        malOlorObs: String,
        recolectoresSinTapaChecked: Boolean,
        recolectoresSinTapaObs: String,
        hierbaCrecidaChecked: Boolean,
        hierbaCrecidaObs: String,
        areasVerdesExtensasChecked: Boolean,
        areasVerdesExtensasObs: String,
        excesoPolvoChecked: Boolean,
        excesoPolvoObs: String,
        desinsectacionRealizado: Boolean,
        desratizacionRealizado: Boolean,
        desinfeccionRealizado: Boolean,
        limpiezaTanquesSepticoRealizado: Boolean,
        limpiezaCisternasRealizado: Boolean,
        productEntries: List<ProductEntry>, // Recibe la lista
        accionesCorrectivasContent: String,
        personnelEntries: List<PersonnelEntry>, // Recibe la lista
        fileName: String
    ) {
        val pdfDocument = PdfDocument()
        val pageHeight = 1120 // A4 portrait ~ 1120 points
        val pageWidth = 792
        val margin = 40f
        val lineHeight = 18f
        val smallLineHeight = 14f // Para texto más pequeño en algunos campos
        val labelToValuePadding = 10f
        val textPadding = 5f // Padding inside a text box

        // --- Paint Objects for Styling ---
        val titlePaint = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 20f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER // Centrar el texto del título
        }
        val sectionTitlePaint = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 16f
            isFakeBoldText = true
        }
        val subSectionTitlePaint = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 13f
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
        val checkboxPaint = Paint().apply { // Paint para los símbolos de checkbox
            color = AndroidColor.BLACK
            textSize = 16f // Tamaño para ☑ o ☐
        }
        val linePaint = Paint().apply {
            color = AndroidColor.BLACK
            strokeWidth = 1f
        }
        val thinLinePaint = Paint().apply {
            color = AndroidColor.BLACK
            strokeWidth = 0.5f
        }
        val boxPaint = Paint().apply { // Para dibujar recuadros de texto
            color = AndroidColor.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }


        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var currentY = margin

        // --- Agregar Logo Corvels en la esquina superior izquierda ---
        val logoBitmap: Bitmap? = BitmapFactory.decodeResource(context.resources, R.drawable.corvels)

        if (logoBitmap != null) {
            val logoWidth = 60f
            val logoHeight = 60f
            val logoRect = AndroidRect(margin.toInt(), margin.toInt(), (margin + logoWidth).toInt(), (margin + logoHeight).toInt())
            canvas.drawBitmap(logoBitmap, null, logoRect, null)
            logoBitmap.recycle()
        } else {
            Log.e("PDFCreation", "No se pudo cargar el logo 'corvels.webp' desde los recursos.")
        }
        // Ajustar currentY para que el contenido comience después del logo y el título
        currentY = margin + 80f // Dejamos espacio para el logo y el título

        // --- Título del documento ---
        val headerText = "FICHA TÉCNICA DE EVALUACIÓN Y DE DESCRIPCIÓN DE ACTIVIDADES"
        val headerX = pageWidth / 2f
        canvas.drawText(headerText, headerX, margin + 30, titlePaint) // Centrado

        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, linePaint)
        currentY += 15f

        // --- CLIENTE y FICHA N° ---
        val field1Width = (pageWidth - 2 * margin) * 0.7f // Ancho para cliente, dirección, giro
        val field2Width = (pageWidth - 2 * margin) * 0.3f // Ancho para Ficha N°, Certificado N°

        // CLIENTE
        canvas.drawText("CLIENTE:", margin, currentY, sectionTitlePaint)
        canvas.drawText(clienteFicha, margin + sectionTitlePaint.measureText("CLIENTE:") + labelToValuePadding, currentY, normalTextPaint)
        canvas.drawLine(margin + sectionTitlePaint.measureText("CLIENTE:") + labelToValuePadding, currentY + 5, margin + field1Width, currentY + 5, linePaint)

        // FICHA N° (alineado a la derecha)
        val fichaNumLabel = "FICHA N°"
        val fichaNumValue = fichaNumber.toString()
        canvas.drawText(fichaNumLabel, pageWidth - margin - sectionTitlePaint.measureText(fichaNumLabel) - normalTextPaint.measureText(fichaNumValue) - labelToValuePadding, currentY, sectionTitlePaint)
        canvas.drawText(fichaNumValue, pageWidth - margin - normalTextPaint.measureText(fichaNumValue), currentY, normalTextPaint)
        canvas.drawLine(pageWidth - margin - sectionTitlePaint.measureText(fichaNumLabel) - normalTextPaint.measureText(fichaNumValue) - labelToValuePadding, currentY + 5, pageWidth - margin, currentY + 5, linePaint)
        currentY += 25f

        // DIRECCIÓN - LUGAR y CERTIFICADO N°
        // DIRECCIÓN
        canvas.drawText("DIRECCIÓN - LUGAR:", margin, currentY, sectionTitlePaint)
        canvas.drawText(direccionLugar, margin + sectionTitlePaint.measureText("DIRECCIÓN - LUGAR:") + labelToValuePadding, currentY, normalTextPaint)
        canvas.drawLine(margin + sectionTitlePaint.measureText("DIRECCIÓN - LUGAR:") + labelToValuePadding, currentY + 5, margin + field1Width, currentY + 5, linePaint)

        // CERTIFICADO N° (alineado a la derecha)
        val certificadoNumLabel = "CERTIFICADO N°"
        canvas.drawText(certificadoNumLabel, pageWidth - margin - sectionTitlePaint.measureText(certificadoNumLabel) - normalTextPaint.measureText(certificadoNum) - labelToValuePadding, currentY, sectionTitlePaint)
        canvas.drawText(certificadoNum, pageWidth - margin - normalTextPaint.measureText(certificadoNum), currentY, normalTextPaint)
        canvas.drawLine(pageWidth - margin - sectionTitlePaint.measureText(certificadoNumLabel) - normalTextPaint.measureText(certificadoNum) - labelToValuePadding, currentY + 5, pageWidth - margin, currentY + 5, linePaint)
        currentY += 25f

        // GIRO DE LA EMPRESA
        canvas.drawText("GIRO DE LA EMPRESA:", margin, currentY, sectionTitlePaint)
        canvas.drawText(giroEmpresa, margin + sectionTitlePaint.measureText("GIRO DE LA EMPRESA:") + labelToValuePadding, currentY, normalTextPaint)
        canvas.drawLine(margin + sectionTitlePaint.measureText("GIRO DE LA EMPRESA:") + labelToValuePadding, currentY + 5, pageWidth - margin, currentY + 5, linePaint)
        currentY += 25f

        // FECHA, HORA INICIO, HORA FINAL
        val timeFieldWidth = (pageWidth - 2 * margin - 4 * labelToValuePadding) / 3f
        currentY += 10f // Espacio antes de esta sección

        canvas.drawText("FECHA:", margin, currentY, sectionTitlePaint)
        canvas.drawText(fecha, margin + sectionTitlePaint.measureText("FECHA:") + labelToValuePadding, currentY, normalTextPaint)
        canvas.drawLine(margin + sectionTitlePaint.measureText("FECHA:") + labelToValuePadding, currentY + 5, margin + timeFieldWidth, currentY + 5, linePaint)

        canvas.drawText("HORA INICIO:", margin + timeFieldWidth + 2*labelToValuePadding, currentY, sectionTitlePaint)
        canvas.drawText(horaInicio, margin + timeFieldWidth + 2*labelToValuePadding + sectionTitlePaint.measureText("HORA INICIO:") + labelToValuePadding, currentY, normalTextPaint)
        canvas.drawLine(margin + timeFieldWidth + 2*labelToValuePadding + sectionTitlePaint.measureText("HORA INICIO:") + labelToValuePadding, currentY + 5, margin + 2 * timeFieldWidth + 2*labelToValuePadding, currentY + 5, linePaint)

        canvas.drawText("HORA FINAL:", margin + 2 * timeFieldWidth + 4*labelToValuePadding, currentY, sectionTitlePaint)
        canvas.drawText(horaFinal, margin + 2 * timeFieldWidth + 4*labelToValuePadding + sectionTitlePaint.measureText("HORA FINAL:") + labelToValuePadding, currentY, normalTextPaint)
        canvas.drawLine(margin + 2 * timeFieldWidth + 4*labelToValuePadding + sectionTitlePaint.measureText("HORA FINAL:") + labelToValuePadding, currentY + 5, pageWidth - margin, currentY + 5, linePaint)
        currentY += 40f

        // --- 1. DIAGNOSTICO ---
        canvas.drawText("1. DIAGNÓSTICO", margin, currentY, sectionTitlePaint)
        canvas.drawLine(margin, currentY + 5, pageWidth - margin, currentY + 5, thinLinePaint)
        currentY += 25f

        val col1X = margin
        val col2X = margin + (pageWidth - 2 * margin) / 3f
        val col3X = margin + 2 * (pageWidth - 2 * margin) / 3f
        val colWidth = (pageWidth - 2 * margin) / 3f

        // Headers de las 3 columnas de diagnóstico
        canvas.drawText("1.1 DESINFECCIÓN", col1X + (colWidth - subSectionTitlePaint.measureText("1.1 DESINFECCIÓN")) / 2, currentY, subSectionTitlePaint)
        canvas.drawText("1.2. DESRATIZACIÓN", col2X + (colWidth - subSectionTitlePaint.measureText("1.2. DESRATIZACIÓN")) / 2, currentY, subSectionTitlePaint)
        canvas.drawText("1.3. DESINSECTACIÓN", col3X + (colWidth - subSectionTitlePaint.measureText("1.3. DESINSECTACIÓN")) / 2, currentY, subSectionTitlePaint)

        canvas.drawLine(col1X, currentY + 5, col1X + colWidth, currentY + 5, thinLinePaint)
        canvas.drawLine(col2X, currentY + 5, col2X + colWidth, currentY + 5, thinLinePaint)
        canvas.drawLine(col3X, currentY + 5, col3X + colWidth, currentY + 5, thinLinePaint)
        currentY += 20f

        val boxHeight = 70f // Altura para las cajas de texto multilinea
        val boxInnerWidth = colWidth - 10f // Ancho de la caja de texto

        // ANTECEDENTES (1.1 DESINFECCIÓN)
        canvas.drawText("ANTECEDENTES:", col1X + textPadding, currentY, normalTextPaint)
        var tempY = currentY + 10f
        var boxRect = AndroidRect(
            (col1X + textPadding).toInt(),
            tempY.toInt(),
            (col1X + textPadding + boxInnerWidth).toInt(),
            (tempY + boxHeight).toInt()
        )
        canvas.drawRect(boxRect, boxPaint)
        drawWrappedText(canvas, antecedentesDesinfeccion, boxRect.left + textPadding, boxRect.top + smallLineHeight, boxRect.width() - 2 * textPadding, smallLineHeight, smallTextPaint)

        // RASTROS (1.2 DESRATIZACION)
        canvas.drawText("RASTROS:", col2X + textPadding, currentY, normalTextPaint)
        boxRect = AndroidRect(
            (col2X + textPadding).toInt(),
            tempY.toInt(),
            (col2X + textPadding + boxInnerWidth).toInt(),
            (tempY + boxHeight).toInt()
        )
        canvas.drawRect(boxRect, boxPaint)
        drawWrappedText(canvas, rastrosDesratizacion, boxRect.left + textPadding, boxRect.top + smallLineHeight, boxRect.width() - 2 * textPadding, smallLineHeight, smallTextPaint)

        // RASTROS (1.3 DESINSECTACION)
        canvas.drawText("RASTROS:", col3X + textPadding, currentY, normalTextPaint)
        boxRect = AndroidRect(
            (col3X + textPadding).toInt(),
            tempY.toInt(),
            (col3X + textPadding + boxInnerWidth).toInt(),
            (tempY + boxHeight).toInt()
        )
        canvas.drawRect(boxRect, boxPaint)
        drawWrappedText(canvas, rastrosDesinsectacion, boxRect.left + textPadding, boxRect.top + smallLineHeight, boxRect.width() - 2 * textPadding, smallLineHeight, smallTextPaint)
        currentY = tempY + boxHeight + 20f

        // CONDICIONES PREVIAS (1.1 DESINFECCIÓN)
        canvas.drawText("CONDICIONES PREVIAS:", col1X + textPadding, currentY, normalTextPaint)
        tempY = currentY + 10f
        boxRect = AndroidRect(
            (col1X + textPadding).toInt(),
            tempY.toInt(),
            (col1X + textPadding + boxInnerWidth).toInt(),
            (tempY + boxHeight).toInt()
        )
        canvas.drawRect(boxRect, boxPaint)
        drawWrappedText(canvas, condicionesPreviasDesinfeccion, boxRect.left + textPadding, boxRect.top + smallLineHeight, boxRect.width() - 2 * textPadding, smallLineHeight, smallTextPaint)

        // TIPO DE ROEDORES (1.2 DESRATIZACION)
        canvas.drawText("TIPO DE ROEDORES:", col2X + textPadding, currentY, normalTextPaint)
        boxRect = AndroidRect(
            (col2X + textPadding).toInt(),
            tempY.toInt(),
            (col2X + textPadding + boxInnerWidth).toInt(),
            (tempY + boxHeight).toInt()
        )
        canvas.drawRect(boxRect, boxPaint)
        drawWrappedText(canvas, tipoRoedores, boxRect.left + textPadding, boxRect.top + smallLineHeight, boxRect.width() - 2 * textPadding, smallLineHeight, smallTextPaint)

        // TIPO DE INSECTOS (1.3 DESINSECTACION)
        canvas.drawText("TIPO DE INSECTOS:", col3X + textPadding, currentY, normalTextPaint)
        boxRect = AndroidRect(
            (col3X + textPadding).toInt(),
            tempY.toInt(),
            (col3X + textPadding + boxInnerWidth).toInt(),
            (tempY + boxHeight).toInt()
        )
        canvas.drawRect(boxRect, boxPaint)
        drawWrappedText(canvas, tipoInsectos, boxRect.left + textPadding, boxRect.top + smallLineHeight, boxRect.width() - 2 * textPadding, smallLineHeight, smallTextPaint)
        currentY = tempY + boxHeight + 20f

        // 1.4 LIMPIEZA Y DESINFECCION DE CISTERNAS O RESERVORIOS AGUA
        val cisternaColWidth = (pageWidth - 2 * margin) * 0.6f
        val tanquesSepticosColWidth = (pageWidth - 2 * margin) * 0.4f

        canvas.drawText("1.4. LIMPIEZA Y DESINFECCIÓN DE CISTERNAS O RESERVORIOS AGUA", margin, currentY, subSectionTitlePaint)
        canvas.drawText("1.5. LIMPIEZA DE TANQUES SÉPTICOS", margin + cisternaColWidth + 10, currentY, subSectionTitlePaint)
        currentY += 20f

        val itemLabelWidth = 150f // Ancho para las etiquetas de los items de cisterna
        fun drawLabeledLine(canvas: AndroidCanvas, label: String, value: String, xStart: Float, y: Float, labelPaint: Paint, valuePaint: Paint, lineWidth: Float, lineP: Paint) {
            canvas.drawText(label, xStart, y, labelPaint)
            canvas.drawText(value, xStart + labelPaint.measureText(label) + labelToValuePadding, y, valuePaint)
            canvas.drawLine(xStart + labelPaint.measureText(label) + labelToValuePadding, y + 5, xStart + lineWidth, y + 5, lineP)
        }

        val tempCisternaY = currentY
        val tempTanquesSepticosY = currentY

        // 1.4 items
        drawLabeledLine(canvas, "SARRO ACUMULADO:", sarroAcumulado, margin, tempCisternaY, normalTextPaint, normalTextPaint, margin + cisternaColWidth, linePaint)
        currentY += 20f
        drawLabeledLine(canvas, "MOHO ACUMULADO:", mohoAcumulado, margin, currentY, normalTextPaint, normalTextPaint, margin + cisternaColWidth, linePaint)
        currentY += 20f
        drawLabeledLine(canvas, "ARENILLA SEDIMENTADA:", arenillaSedimentada, margin, currentY, normalTextPaint, normalTextPaint, margin + cisternaColWidth, linePaint)
        currentY += 20f
        drawLabeledLine(canvas, "PIEDRILLAS O RESTOS:", piedrillasRestos, margin, currentY, normalTextPaint, normalTextPaint, margin + cisternaColWidth, linePaint)
        currentY += 20f
        drawLabeledLine(canvas, "OTROS OBJETOS:", otrosObjetos, margin, currentY, normalTextPaint, normalTextPaint, margin + cisternaColWidth, linePaint)
        currentY += 20f

        // 1.5 Limpieza de Tanques Sépticos
        val limpiezaSepticosBoxRect = AndroidRect(
            (margin + cisternaColWidth + 10).toInt(),
            (tempTanquesSepticosY + 10).toInt(),
            (margin + cisternaColWidth + 10 + tanquesSepticosColWidth - 10).toInt(),
            (tempTanquesSepticosY + 10 + (currentY - tempTanquesSepticosY - 10) ).toInt() // Ajustar altura
        )
        canvas.drawRect(limpiezaSepticosBoxRect, boxPaint)
        drawWrappedText(canvas, limpiezaTanquesSepticosContent, limpiezaSepticosBoxRect.left + textPadding, limpiezaSepticosBoxRect.top + smallLineHeight, limpiezaSepticosBoxRect.width() - 2 * textPadding, smallLineHeight, smallTextPaint)
        currentY = maxOf(currentY, tempTanquesSepticosY + limpiezaSepticosBoxRect.height() + 20f)
        currentY += 20f

        // --- 2. CONDICION SANITARIA DE LA ZONA CIRCUNDANTE ---
        canvas.drawText("2. CONDICIÓN SANITARIA DE LA ZONA CIRCUNDANTE", margin, currentY, sectionTitlePaint)
        canvas.drawLine(margin, currentY + 5, pageWidth - margin, currentY + 5, thinLinePaint)
        currentY += 25f

        val conditionLabelX = margin + 20 // Espacio para el checkbox
        val conditionCheckboxX = margin
        val conditionValueX = margin + (pageWidth - 2 * margin) * 0.7f
        val conditionLineEndX = pageWidth - margin

        // Headers de columna
        canvas.drawText("CONDICIÓN", conditionLabelX, currentY, subSectionTitlePaint)
        canvas.drawText("OBSERVACIÓN", conditionValueX, currentY, subSectionTitlePaint)
        currentY += 15f
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, thinLinePaint)
        currentY += 5f

        fun drawConditionRow(canvas: AndroidCanvas, label: String, isChecked: Boolean, observation: String, currentY: Float): Float {
            canvas.drawText(if (isChecked) "☑" else "☐", conditionCheckboxX, currentY, checkboxPaint) // Checkbox
            canvas.drawText(label, conditionLabelX, currentY, normalTextPaint)
            canvas.drawText(observation, conditionValueX, currentY, normalTextPaint)
            canvas.drawLine(conditionValueX, currentY + 5, conditionLineEndX, currentY + 5, thinLinePaint)
            return currentY + 20f
        }

        currentY = drawConditionRow(canvas, "ACUMUL. RESIDUOS SÓLIDOS:", acumResiduosSolidosChecked, acumResiduosSolidosObs, currentY)
        currentY = drawConditionRow(canvas, "ESPACIO DESHABITADO:", espacioDeshabitadoChecked, espacioDeshabitadoObs, currentY)
        currentY = drawConditionRow(canvas, "DESCOMP. RESIDUOS ORGÁN:", descompResiduosOrganChecked, descompResiduosOrganObs, currentY)
        currentY = drawConditionRow(canvas, "BUZÓN DE DESAGÜE DESCUB.:", buzoneDesagueDescubChecked, buzoneDesagueDescubObs, currentY)
        currentY = drawConditionRow(canvas, "DESORDEN:", desordenChecked, desordenObs, currentY)
        currentY = drawConditionRow(canvas, "SEMBRÍOS PLANTACIONES:", sembriosPlantacionesChecked, sembriosPlantacionesObs, currentY)
        currentY = drawConditionRow(canvas, "CHARCOS DE AGUA:", charcosAguaChecked, charcosAguaObs, currentY)
        currentY = drawConditionRow(canvas, "MAL OLOR:", malOlorChecked, malOlorObs, currentY)
        currentY = drawConditionRow(canvas, "RECOLECTORES DE BASURA SIN TAPA:", recolectoresSinTapaChecked, recolectoresSinTapaObs, currentY)
        currentY = drawConditionRow(canvas, "HIERBA CRECIDA:", hierbaCrecidaChecked, hierbaCrecidaObs, currentY)
        currentY = drawConditionRow(canvas, "ÁREAS VERDES EXTENSAS:", areasVerdesExtensasChecked, areasVerdesExtensasObs, currentY)
        currentY = drawConditionRow(canvas, "EXCESO DE POLVO:", excesoPolvoChecked, excesoPolvoObs, currentY)
        currentY += 20f

        // --- 3. TRABAJOS REALIZADOS ---
        canvas.drawText("3. TRABAJOS REALIZADOS", margin, currentY, sectionTitlePaint)
        canvas.drawLine(margin, currentY + 5, pageWidth - margin, currentY + 5, thinLinePaint)
        currentY += 25f

        val trabajoCheckboxX = margin
        val trabajoLabelX = margin + 20 // Espacio para el checkbox

        fun drawTrabajoRealizadoRow(canvas: AndroidCanvas, label: String, isChecked: Boolean, y: Float): Float {
            canvas.drawText(if (isChecked) "☑" else "☐", trabajoCheckboxX, y, checkboxPaint)
            canvas.drawText(label, trabajoLabelX, y, normalTextPaint)
            return y + 20f
        }

        currentY = drawTrabajoRealizadoRow(canvas, "DESINSECTACIÓN", desinsectacionRealizado, currentY)
        currentY = drawTrabajoRealizadoRow(canvas, "DESRATIZACIÓN", desratizacionRealizado, currentY)
        currentY = drawTrabajoRealizadoRow(canvas, "DESINFECCIÓN", desinfeccionRealizado, currentY)
        currentY = drawTrabajoRealizadoRow(canvas, "LIMPIEZA DE TANQUES SÉPTICOS", limpiezaTanquesSepticoRealizado, currentY)
        currentY = drawTrabajoRealizadoRow(canvas, "LIMPIEZA Y DESINFECCIÓN DE CISTERNAS O RESERVORIOS DE AGUA", limpiezaCisternasRealizado, currentY)
        currentY += 20f // Espacio extra

        // --- 4. PRODUCTOS QUIMICOS O BIOLOGICOS UTILIZADOS (LISTA DINÁMICA) ---
        canvas.drawText("4. PRODUCTOS QUÍMICOS O BIOLÓGICOS UTILIZADOS", margin, currentY, sectionTitlePaint)
        canvas.drawLine(margin, currentY + 5, pageWidth - margin, currentY + 5, thinLinePaint)
        currentY += 25f

        val prodCol1X = margin
        val prodCol2X = margin + (pageWidth - 2 * margin) * 0.5f // Cantidad
        val prodCol3X = margin + (pageWidth - 2 * margin) * 0.75f // Concentracion
        val prodColWidth = (pageWidth - 2 * margin) * 0.5f

        // Headers de columna
        canvas.drawText("PRODUCTO", prodCol1X, currentY, subSectionTitlePaint)
        canvas.drawText("CANTIDAD", prodCol2X, currentY, subSectionTitlePaint)
        canvas.drawText("CONCENTRACIÓN", prodCol3X, currentY, subSectionTitlePaint)
        currentY += 15f
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, thinLinePaint)
        currentY += 5f

        // Dibuja cada producto de la lista
        productEntries.forEach { product ->
            if (currentY + lineHeight > pageHeight - margin) {
                pdfDocument.finishPage(page)
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdfDocument.pages.size + 1).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = margin // Reinicia Y para nueva página
                canvas.drawText("4. PRODUCTOS QUÍMICOS O BIOLÓGICOS UTILIZADOS (cont.)", margin, currentY, sectionTitlePaint)
                currentY += 25f
                canvas.drawText("PRODUCTO", prodCol1X, currentY, subSectionTitlePaint)
                canvas.drawText("CANTIDAD", prodCol2X, currentY, subSectionTitlePaint)
                canvas.drawText("CONCENTRACIÓN", prodCol3X, currentY, subSectionTitlePaint)
                currentY += 15f
                canvas.drawLine(margin, currentY, pageWidth - margin, currentY, thinLinePaint)
                currentY += 5f
            }

            canvas.drawText(product.name, prodCol1X + textPadding, currentY, normalTextPaint)
            canvas.drawText(product.quantity, prodCol2X + textPadding, currentY, normalTextPaint)
            canvas.drawText(product.concentration, prodCol3X + textPadding, currentY, normalTextPaint)

            canvas.drawLine(prodCol1X, currentY + 5, prodCol1X + prodColWidth, currentY + 5, thinLinePaint) // Línea para producto
            canvas.drawLine(prodCol2X, currentY + 5, prodCol2X + (pageWidth - prodCol2X - margin)/2f, currentY + 5, thinLinePaint) // Línea para cantidad
            canvas.drawLine(prodCol3X, currentY + 5, prodCol3X + (pageWidth - prodCol3X - margin), currentY + 5, thinLinePaint) // Línea para concentración
            currentY += lineHeight
        }
        currentY += 20f

        // --- 5. ACCIONES CORRECTIVAS ---
        if (currentY + 150 > pageHeight - margin) { // Estimar espacio para la sección completa
            pdfDocument.finishPage(page)
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdfDocument.pages.size + 1).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            currentY = margin // Reinicia Y para nueva página
        }
        canvas.drawText("5. ACCIONES CORRECTIVAS:", margin, currentY, sectionTitlePaint)
        canvas.drawLine(margin, currentY + 5, pageWidth - margin, currentY + 5, thinLinePaint)
        currentY += 25f
        val accionesCorrectivasBoxHeight = 100f
        val accionesCorrectivasBoxRect = AndroidRect(
            margin.toInt(),
            currentY.toInt(),
            (pageWidth - margin).toInt(),
            (currentY + accionesCorrectivasBoxHeight).toInt()
        )
        canvas.drawRect(accionesCorrectivasBoxRect, boxPaint)
        drawWrappedText(canvas, accionesCorrectivasContent, accionesCorrectivasBoxRect.left + textPadding, accionesCorrectivasBoxRect.top + smallLineHeight, accionesCorrectivasBoxRect.width() - 2 * textPadding, smallLineHeight, smallTextPaint)
        currentY += accionesCorrectivasBoxHeight + 20f

        // --- 6. PERSONAL QUE EJECUTÓ EL TRABAJO (LISTA DINÁMICA) ---
        if (currentY + 100 > pageHeight - margin) { // Estimar espacio para la sección completa
            pdfDocument.finishPage(page)
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdfDocument.pages.size + 1).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            currentY = margin // Reinicia Y para nueva página
        }
        canvas.drawText("6. PERSONAL QUE EJECUTÓ EL TRABAJO", margin, currentY, sectionTitlePaint)
        canvas.drawLine(margin, currentY + 5, pageWidth - margin, currentY + 5, thinLinePaint)
        currentY += 25f

        val personalColWidth = (pageWidth - 2 * margin) / 2f
        canvas.drawText("NOMBRE", margin + personalColWidth * 0.25f - normalTextPaint.measureText("NOMBRE")/2, currentY, subSectionTitlePaint)
        canvas.drawText("FIRMA", margin + personalColWidth + personalColWidth * 0.5f - normalTextPaint.measureText("FIRMA")/2, currentY, subSectionTitlePaint)
        currentY += 15f
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, thinLinePaint)
        currentY += 5f

        // Dibuja cada personal de la lista
        personnelEntries.forEach { personnel ->
            if (currentY + lineHeight > pageHeight - margin) {
                pdfDocument.finishPage(page)
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdfDocument.pages.size + 1).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = margin // Reinicia Y para nueva página
                canvas.drawText("6. PERSONAL QUE EJECUTÓ EL TRABAJO (cont.)", margin, currentY, sectionTitlePaint)
                currentY += 25f
                canvas.drawText("NOMBRE", margin + personalColWidth * 0.25f - normalTextPaint.measureText("NOMBRE")/2, currentY, subSectionTitlePaint)
                canvas.drawText("FIRMA", margin + personalColWidth + personalColWidth * 0.5f - normalTextPaint.measureText("FIRMA")/2, currentY, subSectionTitlePaint)
                currentY += 15f
                canvas.drawLine(margin, currentY, pageWidth - margin, currentY, thinLinePaint)
                currentY += 5f
            }
            canvas.drawText(personnel.name, margin + textPadding, currentY, normalTextPaint)
            canvas.drawLine(margin, currentY + 5, margin + personalColWidth - 10, currentY + 5, linePaint) // Línea para el nombre
            canvas.drawText(personnel.signature, margin + personalColWidth + textPadding, currentY, normalTextPaint)
            canvas.drawLine(margin + personalColWidth, currentY + 5, pageWidth - margin, currentY + 5, linePaint) // Línea para la firma
            currentY += lineHeight // Espacio por cada entrada de personal
        }
        currentY += 20f // Espacio final

        pdfDocument.finishPage(page)

        val finalFileName = if (fileName.isBlank()) {
            "FichaTecnica_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date()) + ".pdf"
        } else {
            fileName.replace(".pdf", "") + ".pdf"
        }

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), finalFileName)

        try {
            FileOutputStream(file).use {
                pdfDocument.writeTo(it)
            }
            pdfDocument.close()

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
}