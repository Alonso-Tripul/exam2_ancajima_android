package pe.upeu.agrosierra.presentation.detalle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.upeu.agrosierra.domain.model.*
import pe.upeu.agrosierra.domain.repository.AgroRepository

data class DetalleUiState(
    val lote: Lote,
    val labores: List<Labor> = emptyList(),
    val procesando: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null,
    val erroresCampo: Map<String, String> = emptyMap(),
)

class DetalleViewModel(
    lote: Lote,
    private val repository: AgroRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    private val temp = MutableStateFlow(DetalleUiState(lote))
    val state: StateFlow<DetalleUiState> = combine(repository.observarLabores(lote.id), temp) { labores, estado ->
        estado.copy(labores = labores)
    }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), DetalleUiState(lote))

    fun guardar(datos: DatosLabor, idEditar: Long? = null, onOk: () -> Unit = {}) = scope.launch {
        temp.value = temp.value.copy(procesando = true, error = null, erroresCampo = emptyMap())
        try {
            if (idEditar == null) repository.registrarLabor(datos) else repository.corregirLabor(idEditar, datos)
            temp.value = temp.value.copy(procesando = false, mensaje = if (idEditar == null) "Labor guardada" else "Correccion guardada")
            onOk()
        } catch (e: ErrorApi.Validacion) {
            temp.value = temp.value.copy(procesando = false, error = e.message, erroresCampo = e.campos)
        } catch (e: ErrorApi) {
            temp.value = temp.value.copy(procesando = false, error = e.message)
        }
    }

    fun eliminar(id: Long) = scope.launch {
        try { repository.eliminarLabor(id); temp.value = temp.value.copy(mensaje = "Labor eliminada") }
        catch (e: ErrorApi) { temp.value = temp.value.copy(error = e.message) }
    }

    fun cerrarCampania() = scope.launch {
        try {
            val actualizado = repository.cerrarCampania(temp.value.lote.id)
            temp.value = temp.value.copy(lote = actualizado, mensaje = "Campania cerrada")
        } catch (e: ErrorApi) { temp.value = temp.value.copy(error = e.message) }
    }

    fun limpiarAviso() { temp.value = temp.value.copy(mensaje = null, error = null, erroresCampo = emptyMap()) }
}
