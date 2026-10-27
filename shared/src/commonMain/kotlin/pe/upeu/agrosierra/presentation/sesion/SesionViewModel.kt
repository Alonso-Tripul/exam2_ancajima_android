package pe.upeu.agrosierra.presentation.sesion

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.upeu.agrosierra.domain.model.ErrorApi
import pe.upeu.agrosierra.domain.model.Sesion
import pe.upeu.agrosierra.domain.repository.AgroRepository

sealed interface SesionUiState {
    data object Cargando : SesionUiState
    data object SinSesion : SesionUiState
    data class Autenticado(val sesion: Sesion) : SesionUiState
    data class Error(val mensaje: String, val sinConexion: Boolean = false) : SesionUiState
}

class SesionViewModel(
    private val repository: AgroRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    private val _state = MutableStateFlow<SesionUiState>(SesionUiState.Cargando)
    val state: StateFlow<SesionUiState> = _state.asStateFlow()

    init {
        scope.launch {
            _state.value = repository.sesionGuardada()?.let { SesionUiState.Autenticado(it) } ?: SesionUiState.SinSesion
        }
    }

    fun login(usuario: String, password: String) {
        if (usuario.isBlank() || password.isBlank()) {
            _state.value = SesionUiState.Error("Completa usuario y contrasena")
            return
        }
        scope.launch {
            _state.value = SesionUiState.Cargando
            _state.value = try {
                SesionUiState.Autenticado(repository.iniciarSesion(usuario, password))
            } catch (e: ErrorApi) {
                SesionUiState.Error(e.message ?: "No se pudo iniciar sesion", e is ErrorApi.SinConexion)
            }
        }
    }

    fun limpiarError() { _state.value = SesionUiState.SinSesion }

    fun logout() = scope.launch {
        repository.cerrarSesion()
        _state.value = SesionUiState.SinSesion
    }
}

