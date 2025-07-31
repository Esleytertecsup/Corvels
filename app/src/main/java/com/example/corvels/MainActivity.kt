package com.example.corvels

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.corvels.archivos.PdfActivity
import com.example.corvels.archivos.XlsxActivity
import com.example.corvels.ui.theme.CorvelsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var darkTheme by remember { mutableStateOf(false) }
            var showFileDialog by remember { mutableStateOf(false) }

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

                        // Contenido principal
                        MainScreen(
                            onFilesClick = { showFileDialog = true }
                        )

                        // Diálogo para seleccionar tipo de archivo
                        if (showFileDialog) {
                            FileTypeDialog(
                                onDismiss = { showFileDialog = false },
                                onPdfSelected = {
                                    startActivity(Intent(this@MainActivity, PdfActivity::class.java))
                                    showFileDialog = false
                                },
                                onXlsxSelected = {
                                    startActivity(Intent(this@MainActivity, XlsxActivity::class.java))
                                    showFileDialog = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun MainScreen(
        onFilesClick: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.corvels),
                contentDescription = "Logo Corvels",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    startActivity(Intent(this@MainActivity, ActaConformidadActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF463EDC),
                    contentColor = Color.White
                )
            ) {
                Text("ACTA DE CONFORMIDAD Y FIN DE SERVICIO")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    startActivity(Intent(this@MainActivity, FichaTecnicaActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF463EDC),
                    contentColor = Color.White
                )
            ) {
                Text("FICHA TÉCNICA DE SERVICIOS")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onFilesClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF463EDC),
                    contentColor = Color.White
                )
            ) {
                Text("ARCHIVOS PDF Y XLSX")
            }
        }
    }

    @Composable
    fun FileTypeDialog(
        onDismiss: () -> Unit,
        onPdfSelected: () -> Unit,
        onXlsxSelected: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Seleccionar tipo de archivo") },
            text = { Text("¿Qué tipo de archivo deseas ver?") },
            confirmButton = {
                Button(
                    onClick = onPdfSelected,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF463EDC),
                        contentColor = Color.White
                    )
                ) {
                    Text("PDF")
                }
            },
            dismissButton = {
                Button(
                    onClick = onXlsxSelected,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF463EDC),
                        contentColor = Color.White
                    )
                ) {
                    Text("XLSX")
                }
            }
        )
    }
}