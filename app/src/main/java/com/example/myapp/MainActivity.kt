package com.example.myapp

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapp.ui.theme.MyAppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.text.SimpleDateFormat
import java.util.*

// Estructura de datos renombrada y con nuevos campos
data class SiniestroVial(
    val categoria: String,
    val fecha: String,
    val placa: String,
    val nombreChofer: String,
    val cedulaChofer: String,
    val notas: String,
    val coordenadas: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyAppTheme {
                PantallaPrincipalAgente()
            }
        }
    }
}

@Composable
fun PantallaPrincipalAgente() {
    var seccionSeleccionada by remember { mutableStateOf("registro") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = seccionSeleccionada == "registro",
                    onClick = { seccionSeleccionada = "registro" },
                    label = { Text("Registrar") },
                    icon = { }
                )
                NavigationBarItem(
                    selected = seccionSeleccionada == "lista",
                    onClick = { seccionSeleccionada = "lista" },
                    label = { Text("Historial") },
                    icon = { }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (seccionSeleccionada == "registro") {
                VistaFormulario()
            } else {
                VistaHistorial()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VistaFormulario() {
    val contexto = LocalContext.current
    val scrollForm = rememberScrollState()

    // Estados de los campos
    var tipoSiniestro by remember { mutableStateOf("") }
    var placaAuto by remember { mutableStateOf("") }
    var choferNombre by remember { mutableStateOf("") }
    var choferCedula by remember { mutableStateOf("") }
    var notasAdicionales by remember { mutableStateOf("") }

    // Fecha automática
    val fechaHoy = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()) }

    var menuAbierto by remember { mutableStateOf(false) }
    val opcionesCategorias = listOf("Choque", "Colisión", "Atropello")

    var imagenCapturada by remember { mutableStateOf<Bitmap?>(null) }
    var textoGps by remember { mutableStateOf("GPS: Pendiente") }
    var buscandoGps by remember { mutableStateOf(false) }

    val permisossistema = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val lanzadorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { foto -> if (foto != null) imagenCapturada = foto }

    val clienteGps = remember { LocationServices.getFusedLocationProviderClient(contexto) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollForm)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Reporte de Tránsito", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)

        // Selector de tipo (Exposed Dropdown)
        ExposedDropdownMenuBox(
            expanded = menuAbierto,
            onExpandedChange = { menuAbierto = !menuAbierto }
        ) {
            OutlinedTextField(
                value = tipoSiniestro,
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoría del Accidente") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuAbierto) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                opcionesCategorias.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = { tipoSiniestro = item; menuAbierto = false }
                    )
                }
            }
        }

        OutlinedTextField(
            value = fechaHoy,
            onValueChange = {},
            label = { Text("Fecha y Hora") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = placaAuto,
            onValueChange = { placaAuto = it },
            label = { Text("Matrícula/Placa") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = choferNombre,
            onValueChange = { choferNombre = it },
            label = { Text("Nombre del Conductor") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = choferCedula,
            onValueChange = { choferCedula = it },
            label = { Text("Cédula del Conductor") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = notasAdicionales,
            onValueChange = { notasAdicionales = it },
            label = { Text("Observaciones del Evento") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        // Visualización de Foto
        imagenCapturada?.let {
            Card(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize())
            }
        }

        // Estado del GPS
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(textoGps, style = MaterialTheme.typography.bodySmall)
                if (buscandoGps) {
                    Spacer(Modifier.width(10.dp))
                    CircularProgressIndicator(modifier = Modifier.size(14.dp))
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    if (permisossistema.allPermissionsGranted) lanzadorCamara.launch(null)
                    else permisossistema.launchMultiplePermissionRequest()
                },
                modifier = Modifier.weight(1f)
            ) { Text("Foto") }

            Button(
                onClick = {
                    if (permisossistema.allPermissionsGranted) {
                        buscandoGps = true
                        try {
                            clienteGps.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                                .addOnSuccessListener { loc ->
                                    buscandoGps = false
                                    textoGps = loc?.let { "Lat: ${it.latitude}, Lon: ${it.longitude}" } ?: "Gps no disponible"
                                }
                        } catch (e: SecurityException) { buscandoGps = false }
                    } else {
                        permisossistema.launchMultiplePermissionRequest()
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text("Ubicación") }
        }

        Button(
            onClick = {
                if (tipoSiniestro.isNotBlank() && placaAuto.isNotBlank() && choferCedula.isNotBlank()) {
                    val dataRecord = SiniestroVial(
                        categoria = tipoSiniestro,
                        fecha = fechaHoy,
                        placa = placaAuto,
                        nombreChofer = choferNombre,
                        cedulaChofer = choferCedula,
                        notas = notasAdicionales,
                        coordenadas = textoGps
                    )

                    // Guardado en SharedPreferences
                    val prefs = contexto.getSharedPreferences("RegistroAgente", Context.MODE_PRIVATE)
                    with(prefs.edit()) {
                        val key = "siniestro_${System.currentTimeMillis()}"
                        val value = "${dataRecord.categoria}|${dataRecord.fecha}|${dataRecord.placa}|${dataRecord.nombreChofer}|${dataRecord.cedulaChofer}|${dataRecord.notas}|${dataRecord.coordenadas}"
                        putString(key, value)
                        apply()
                    }

                    ejecutarVibracionLarga(contexto) // Vibración de 5 segundos
                    Toast.makeText(contexto, "Siniestro Almacenado", Toast.LENGTH_LONG).show()

                    // Resetear formulario
                    tipoSiniestro = ""; placaAuto = ""; choferNombre = ""; choferCedula = ""; notasAdicionales = ""; imagenCapturada = null; textoGps = "GPS: Pendiente"
                } else {
                    Toast.makeText(contexto, "Faltan datos obligatorios", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("FINALIZAR REGISTRO")
        }
    }
}

@Composable
fun VistaHistorial() {
    val ctx = LocalContext.current
    val dataStore = ctx.getSharedPreferences("RegistroAgente", Context.MODE_PRIVATE)

    val listaSiniestros = remember {
        dataStore.all.filter { it.key.startsWith("siniestro_") }
            .map { item ->
                val partes = item.value.toString().split("|")
                SiniestroVial(
                    categoria = partes.getOrElse(0) { "" },
                    fecha = partes.getOrElse(1) { "" },
                    placa = partes.getOrElse(2) { "" },
                    nombreChofer = partes.getOrElse(3) { "" },
                    cedulaChofer = partes.getOrElse(4) { "" },
                    notas = partes.getOrElse(5) { "" },
                    coordenadas = partes.getOrElse(6) { "" }
                )
            }.reversed()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Registros Guardados", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(listaSiniestros) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("${item.categoria} - ${item.fecha}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Vehículo: ${item.placa}")
                    Text("Chofer: ${item.nombreChofer} (ID: ${item.cedulaChofer})")
                    Text("Lugar: ${item.coordenadas}", style = MaterialTheme.typography.labelSmall)
                    if (item.notas.isNotBlank()) {
                        Text("Obs: ${item.notas}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

// Función de vibración ajustada a 5 segundos (5000 ms)
fun ejecutarVibracionLarga(context: Context) {
    val vibrador = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vm.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    val duracionMilisecons = 5000L // 5 segundos exactos

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrador.vibrate(VibrationEffect.createOneShot(duracionMilisecons, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrador.vibrate(duracionMilisecons)
    }
}