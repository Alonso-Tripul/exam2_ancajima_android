package pe.upeu.agrosierra.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.upeu.agrosierra.domain.model.*

interface AgroRepository {
    fun observarLotes(busqueda: String, valle: String, soloAbiertas: Boolean): Flow<List<Lote>>
    fun observarLabores(loteId: Long): Flow<List<Labor>>
    fun ultimaSincronizacion(): Flow<String?>
    suspend fun iniciarSesion(usuario: String, password: String): Sesion
    suspend fun sesionGuardada(): Sesion?
    suspend fun cerrarSesion()
    suspend fun registrarLabor(datos: DatosLabor): Labor
    suspend fun corregirLabor(id: Long, datos: DatosLabor): Labor
    suspend fun eliminarLabor(id: Long)
    suspend fun cerrarCampania(loteId: Long): Lote
    suspend fun sincronizar(): ResumenSincronizacion
}

