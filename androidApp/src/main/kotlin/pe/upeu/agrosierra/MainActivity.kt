package pe.upeu.agrosierra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pe.upeu.agrosierra.domain.model.*
import pe.upeu.agrosierra.domain.repository.AgroRepository
import pe.upeu.agrosierra.presentation.detalle.DetalleViewModel
import pe.upeu.agrosierra.presentation.listado.ListadoViewModel
import pe.upeu.agrosierra.presentation.sesion.SesionUiState
import pe.upeu.agrosierra.presentation.sesion.SesionViewModel

class MainActivity : ComponentActivity(), KoinComponent {
    private val sesionViewModel: SesionViewModel by inject()
    private val listadoViewModel: ListadoViewModel by inject()
    private val repository: AgroRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgroTheme {
                val sesionState by sesionViewModel.state.collectAsStateWithLifecycle()
                when (val state = sesionState) {
                    SesionUiState.Cargando -> PantallaCarga()
                    SesionUiState.SinSesion -> LoginScreen(onLogin = sesionViewModel::login)
                    is SesionUiState.Error -> LoginScreen(
                        mensaje = state.mensaje,
                        sinConexion = state.sinConexion,
                        onLogin = sesionViewModel::login,
                    )
                    is SesionUiState.Autenticado -> AplicacionProtegida(
                        sesion = state.sesion,
                        listadoViewModel = listadoViewModel,
                        repository = repository,
                        onLogout = sesionViewModel::logout,
                    )
                }
            }
        }
    }
}

private val Verde = Color(0xFF1F6B46)
private val VerdeClaro = Color(0xFFE4F3E8)
private val Tierra = Color(0xFF9B5D2E)
private val Fondo = Color(0xFFF6F8F3)
private val Tinta = Color(0xFF173126)

@Composable
private fun AgroTheme(content: @Composable () -> Unit) {
    val scheme = lightColorScheme(
        primary = Verde, onPrimary = Color.White, primaryContainer = VerdeClaro,
        secondary = Tierra, background = Fondo, surface = Color.White,
        onBackground = Tinta, onSurface = Tinta, error = Color(0xFFB3261E),
    )
    MaterialTheme(colorScheme = scheme, typography = Typography(), content = content)
}

@Composable
private fun PantallaCarga() {
    Box(Modifier.fillMaxSize().background(Fondo), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Rounded.Home, null, tint = Verde, modifier = Modifier.size(52.dp))
            CircularProgressIndicator(color = Verde)
            Text("Preparando AgroCampo", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun LoginScreen(mensaje: String? = null, sinConexion: Boolean = false, onLogin: (String, String) -> Unit) {
    var usuario by remember { mutableStateOf("tecnico1") }
    var password by remember { mutableStateOf("Agro2026*") }
    Box(Modifier.fillMaxSize().background(Fondo).padding(24.dp), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp),
        ) {
            Column(Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(shape = RoundedCornerShape(20.dp), color = VerdeClaro) {
                    Icon(Icons.Rounded.Home, null, tint = Verde, modifier = Modifier.padding(16.dp).size(38.dp))
                }
                Text("AgroCampo", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Tinta)
                Text("Labores de campo, incluso sin señal", color = Color(0xFF557064))
                if (mensaje != null) {
                    Surface(color = if (sinConexion) Color(0xFFFFF3D6) else MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(if (sinConexion) Icons.Rounded.CloudOff else Icons.Rounded.Lock, null)
                            Text(mensaje)
                        }
                    }
                }
                OutlinedTextField(
                    value = usuario, onValueChange = { usuario = it }, label = { Text("Usuario") },
                    leadingIcon = { Icon(Icons.Rounded.Person, null) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = password, onValueChange = { password = it }, label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Rounded.Lock, null) }, visualTransformation = PasswordVisualTransformation(),
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
                Button(onClick = { onLogin(usuario, password) }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
                    Text("Ingresar", fontWeight = FontWeight.Bold)
                }
                Text("Proyecto exam2_ancajima · UPeU 2026", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AplicacionProtegida(
    sesion: Sesion,
    listadoViewModel: ListadoViewModel,
    repository: AgroRepository,
    onLogout: () -> Unit,
) {
    val state by listadoViewModel.state.collectAsStateWithLifecycle()
    val conectado by listadoViewModel.conectado.collectAsStateWithLifecycle()
    var loteSeleccionado by remember { mutableStateOf<Lote?>(null) }
    var perfil by remember { mutableStateOf(false) }

    if (perfil) PerfilDialog(sesion, onDismiss = { perfil = false }, onLogout = { perfil = false; onLogout() })

    loteSeleccionado?.let { lote ->
        val detalle = remember(lote.id) { DetalleViewModel(lote, repository) }
        DetalleScreen(
            sesion = sesion,
            viewModel = detalle,
            onBack = { loteSeleccionado = null },
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("AgroCampo", fontWeight = FontWeight.Bold); Text("Hola, ${sesion.nombre}", fontSize = 12.sp) } },
                navigationIcon = { Icon(Icons.Rounded.Home, null, tint = Verde, modifier = Modifier.padding(start = 16.dp)) },
                actions = {
                    IconButton(onClick = { listadoViewModel.sincronizar() }) { Icon(Icons.Rounded.Sync, "Sincronizar") }
                    IconButton(onClick = { perfil = true }) { Icon(Icons.Rounded.Person, "Perfil") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Fondo),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (!conectado) {
                Row(Modifier.fillMaxWidth().background(Color(0xFFFFE9B5)).padding(10.dp), horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Rounded.CloudOff, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Modo sin conexión · puedes seguir registrando", fontWeight = FontWeight.Medium)
                }
            }
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.filtros.busqueda,
                    onValueChange = listadoViewModel::buscar,
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    placeholder = { Text("Buscar por código o nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("" to "Todos", "Mantaro" to "Mantaro", "Urubamba" to "Urubamba", "Colca" to "Colca").forEach { (value, label) ->
                        FilterChip(selected = state.filtros.valle == value, onClick = { listadoViewModel.filtrarValle(value) }, label = { Text(label) })
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = state.filtros.soloAbiertas, onCheckedChange = listadoViewModel::soloAbiertas)
                    Spacer(Modifier.width(8.dp))
                    Text("Solo campañas abiertas")
                    Spacer(Modifier.weight(1f))
                    Text(state.ultimaSync ?: "Aún no sincronizado", fontSize = 11.sp, color = Color.Gray)
                }
            }
            state.mensaje?.let { Aviso(it, false) { listadoViewModel.limpiarAviso() } }
            state.error?.let { Aviso(it, true) { listadoViewModel.limpiarAviso() } }
            when {
                state.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.lotes.isEmpty() -> EstadoVacio("No hay lotes para estos filtros")
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.lotes, key = { it.id }) { lote -> LoteCard(lote) { loteSeleccionado = lote } }
                }
            }
        }
    }
}

@Composable
private fun LoteCard(lote: Lote, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(lote.codigo, color = Verde, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                SuggestionChip(onClick = {}, label = { Text(if (lote.campaniaCerrada) "Cerrada" else "Abierta") })
            }
            Text(lote.nombre, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Text("${lote.cultivo} · ${lote.hectareas} ha", color = Color(0xFF557064))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.LocationOn, null, tint = Tierra, modifier = Modifier.size(18.dp))
                Text(lote.valle, fontSize = 13.sp)
                Spacer(Modifier.weight(1f))
                Text("Ver labores →", color = Verde, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetalleScreen(sesion: Sesion, viewModel: DetalleViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var editor by remember { mutableStateOf<Labor?>(null) }
    var nuevo by remember { mutableStateOf(false) }
    val puedeCrear = "LABOR_CREAR" in sesion.privilegios && !state.lote.campaniaCerrada
    val puedeEditar = "LABOR_EDITAR" in sesion.privilegios && !state.lote.campaniaCerrada
    val puedeEliminar = "LABOR_ELIMINAR" in sesion.privilegios

    if (nuevo || editor != null) {
        LaborDialog(
            loteId = state.lote.id,
            labor = editor,
            errores = state.erroresCampo,
            procesando = state.procesando,
            onDismiss = { nuevo = false; editor = null },
            onGuardar = { datos -> viewModel.guardar(datos, editor?.id) { nuevo = false; editor = null } },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.lote.nombre, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Volver") } },
            )
        },
        floatingActionButton = {
            if (puedeCrear) ExtendedFloatingActionButton(onClick = { nuevo = true }, icon = { Icon(Icons.Rounded.Add, null) }, text = { Text("Registrar labor") })
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = VerdeClaro), shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${state.lote.codigo} · ${state.lote.valle}", color = Verde, fontWeight = FontWeight.Bold)
                        Text("${state.lote.cultivo} · ${state.lote.hectareas} hectáreas", fontSize = 18.sp)
                        Text(if (state.lote.campaniaCerrada) "Campaña cerrada" else "Campaña activa")
                        if ("CAMPANIA_CERRAR" in sesion.privilegios && !state.lote.campaniaCerrada) {
                            OutlinedButton(onClick = viewModel::cerrarCampania) { Icon(Icons.Rounded.Lock, null); Spacer(Modifier.width(6.dp)); Text("Cerrar campaña") }
                        }
                    }
                }
            }
            state.mensaje?.let { item { Aviso(it, false, viewModel::limpiarAviso) } }
            state.error?.let { item { Aviso(it, true, viewModel::limpiarAviso) } }
            item { Text("Labores (${state.labores.size})", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
            if (state.labores.isEmpty()) item { EstadoVacio("Todavía no hay labores registradas") }
            items(state.labores, key = { it.id }) { labor ->
                LaborCard(labor, puedeEditar, puedeEliminar, onEdit = { editor = labor }, onDelete = { viewModel.eliminar(labor.id) })
            }
        }
    }
}

@Composable
private fun LaborCard(labor: Labor, editar: Boolean, eliminar: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(labor.tipo.name, color = Verde, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                if (labor.pendiente) AssistChip(onClick = {}, label = { Text("Pendiente") }, leadingIcon = { Icon(Icons.Rounded.CloudOff, null, Modifier.size(16.dp)) })
            }
            Text("${labor.fecha} · ${labor.cantidad}", fontWeight = FontWeight.SemiBold)
            Text(labor.observacion)
            Text("Responsable: ${labor.responsable}", fontSize = 12.sp, color = Color.Gray)
            if (editar || eliminar) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (editar) TextButton(onClick = onEdit) { Icon(Icons.Rounded.Edit, null); Text("Editar") }
                if (eliminar) TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Icon(Icons.Rounded.Delete, null); Text("Eliminar") }
            }
        }
    }
}

@Composable
private fun LaborDialog(
    loteId: Long,
    labor: Labor?,
    errores: Map<String, String>,
    procesando: Boolean,
    onDismiss: () -> Unit,
    onGuardar: (DatosLabor) -> Unit,
) {
    var tipo by remember(labor) { mutableStateOf(labor?.tipo ?: TipoLabor.RIEGO) }
    var fecha by remember(labor) { mutableStateOf(labor?.fecha ?: "2026-10-27") }
    var cantidad by remember(labor) { mutableStateOf(labor?.cantidad?.toString() ?: "") }
    var observacion by remember(labor) { mutableStateOf(labor?.observacion ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (labor == null) "Registrar labor" else "Corregir labor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TipoLabor.entries.forEach { opcion -> FilterChip(selected = tipo == opcion, onClick = { tipo = opcion }, label = { Text(opcion.name.take(4)) }) }
                }
                OutlinedTextField(value = fecha, onValueChange = { fecha = it }, label = { Text("Fecha (AAAA-MM-DD)") }, supportingText = errores["fecha"]?.let { { Text(it) } }, isError = "fecha" in errores)
                OutlinedTextField(value = cantidad, onValueChange = { cantidad = it }, label = { Text("Cantidad aplicada") }, supportingText = errores["cantidad"]?.let { { Text(it) } }, isError = "cantidad" in errores)
                OutlinedTextField(value = observacion, onValueChange = { observacion = it }, label = { Text("Observación") }, minLines = 2, supportingText = errores["observacion"]?.let { { Text(it) } }, isError = "observacion" in errores)
            }
        },
        confirmButton = {
            Button(
                enabled = !procesando,
                onClick = { onGuardar(DatosLabor(loteId, tipo, fecha, cantidad.toDoubleOrNull() ?: 0.0, observacion)) },
            ) { if (procesando) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun PerfilDialog(sesion: Sesion, onDismiss: () -> Unit, onLogout: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Person, null, tint = Verde) },
        title = { Text(sesion.nombre) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("@${sesion.usuario} · ${sesion.rol}", fontWeight = FontWeight.Bold)
                Text("Privilegios", color = Verde)
                sesion.privilegios.sorted().forEach { Text("• $it", fontSize = 13.sp) }
                Text("El token está protegido por Android Keystore.", fontSize = 12.sp, color = Color.Gray)
            }
        },
        confirmButton = { Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Icon(Icons.Rounded.Logout, null); Text("Cerrar sesión") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Volver") } },
    )
}

@Composable
private fun Aviso(texto: String, error: Boolean, onCerrar: () -> Unit) {
    Surface(
        color = if (error) MaterialTheme.colorScheme.errorContainer else VerdeClaro,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(texto, modifier = Modifier.weight(1f))
            IconButton(onClick = onCerrar, modifier = Modifier.size(28.dp)) { Icon(Icons.Rounded.Close, "Cerrar") }
        }
    }
}

@Composable
private fun EstadoVacio(texto: String) {
    Column(Modifier.fillMaxWidth().padding(36.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(Icons.Rounded.Inbox, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
        Text(texto, color = Color.Gray)
    }
}
