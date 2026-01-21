package com.example.miapp

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miapp.ui.theme.MiAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FormularioAccidente(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun FormularioAccidente(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Estados del formulario
    var tipoAccidente by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var matricula by remember { mutableStateOf("") }
    var nombreConductor by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf<Location?>(null) }

    // Dropdown
    var expanded by remember { mutableStateOf(false) }
    val tipos = listOf("Choque", "Colisión", "Atropello")

    // Permisos
    val permisosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        permisosLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.VIBRATE
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Registro de Accidente de Tránsito",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tipo de accidente
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = tipoAccidente,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de accidente") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                tipos.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            tipoAccidente = it
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = fecha,
            onValueChange = { fecha = it },
            label = { Text("Fecha del siniestro") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = matricula,
            onValueChange = { matricula = it },
            label = { Text("Matrícula del vehículo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = nombreConductor,
            onValueChange = { nombreConductor = it },
            label = { Text("Nombre del conductor") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = cedula,
            onValueChange = { cedula = it },
            label = { Text("Cédula del conductor") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = observaciones,
            onValueChange = { observaciones = it },
            label = { Text("Observaciones") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botones de cámara y ubicación (lógica en otros .kt)
        Button(
            onClick = { /* Abrir cámara */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Capturar Fotografía")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { /* Obtener ubicación */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Obtener Ubicación GPS")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                vibrarTelefono(context)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Guardar Accidente")
        }
    }
}

fun vibrarTelefono(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(
            VibrationEffect.createOneShot(
                5000,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    } else {
        vibrator.vibrate(5000)
    }
}
