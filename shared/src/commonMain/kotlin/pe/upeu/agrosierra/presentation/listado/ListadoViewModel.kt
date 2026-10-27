package pe.upeu.agrosierra.presentation.listado

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.upeu.agrosierra.data.local.MonitorConexion
import pe.upeu.agrosierra.domain.model.ErrorApi
import pe.upeu.agrosierra.domain.model.Lote
import pe.upeu.agrosierra.domain.repository.AgroRepository

data class FiltrosLote(val busqueda: String = "", val valle: String = "", val soloAbiertas: Boolean = false)
data class ListadoUiState(
    val cargando: Boolean = true,
    val lotes: List<Lote> = emptyList(),
    val filtros: FiltrosLote = FiltrosLote(),
    val ultimaSync: String? = null,
    val mensaje: String? = null,
    val error: String? = null,
)

class ListadoViewModel(
    private val repository: AgroRepository,
    private val monitor: MonitorConexion,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    private val filtros = MutableStateFlow(FiltrosLote())
    private val transitorio = MutableStateFlow(ListadoUiState())
    val conectado: StateFlow<Boolean> = monitor.conectado

    val state: StateFlow<ListadoUiState> = combine(
        filtros.flatMapLatest { repository.observarLotes(it.busqueda, it.valle, it.soloAbiertas) },
        filtros,
        repository.ultimaSincronizacion(),
        transitorio,
    ) { lotes, filtro, ultima, temp ->
        temp.copy(cargando = false, lotes = lotes, filtros = filtro, ultimaSync = ultima)
    }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), ListadoUiState())

    init {
        scope.launch {
            monitor.conectado.drop(1).filter { it }.collect { sincronizar(automatico = true) }
        }
    }

    fun buscar(texto: String) { filtros.value = filtros.value.copy(busqueda = texto) }
    fun filtrarValle(valle: String) { filtros.value = filtros.value.copy(valle = valle) }
    fun soloAbiertas(valor: Boolean) { filtros.value = filtros.value.copy(soloAbiertas = valor) }

    fun sincronizar(automatico: Boolean = false) = scope.launch {
        transitorio.value = transitorio.value.copy(cargando = true, mensaje = if (automatico) "Conexion recuperada: sincronizando" else null, error = null)
        try {
            val resumen = repository.sincronizar()
            transitorio.value = transitorio.value.copy(cargando = false, mensaje = resumen.mensaje)
        } catch (e: ErrorApi) {
            transitorio.value = transitorio.value.copy(cargando = false, error = e.message)
        }
    }

    fun limpiarAviso() { transitorio.value = transitorio.value.copy(mensaje = null, error = null) }
}

