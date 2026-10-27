package pe.upeu.agrosierra.data.local

import pe.upeu.agrosierra.domain.model.Sesion

interface AlmacenSeguro {
    suspend fun guardarSesion(sesion: Sesion)
    suspend fun leerSesion(): Sesion?
    suspend fun borrarSesion()
}

interface MonitorConexion {
    val conectado: kotlinx.coroutines.flow.StateFlow<Boolean>
}
