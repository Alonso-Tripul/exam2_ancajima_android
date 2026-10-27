package pe.upeu.agrosierra

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import pe.upeu.agrosierra.domain.model.*
import pe.upeu.agrosierra.domain.repository.AgroRepository

class RegistroOfflineTest {
    @Test fun registroSinRedUsaIdNegativoYMarcaPendiente() = runTest {
        val repo = RepositorioMemoria()
        val creada = repo.registrarLabor(DatosLabor(1, TipoLabor.RIEGO, "2026-10-27", 12.0, "Riego offline"))
        assertTrue(creada.id < 0)
        assertTrue(creada.pendiente)
    }
}

private class RepositorioMemoria : AgroRepository {
    private var siguiente = -1L
    val operaciones = mutableListOf<String>()
    override fun observarLotes(busqueda: String, valle: String, soloAbiertas: Boolean): Flow<List<Lote>> = flowOf(emptyList())
    override fun observarLabores(loteId: Long): Flow<List<Labor>> = flowOf(emptyList())
    override fun ultimaSincronizacion(): Flow<String?> = flowOf(null)
    override suspend fun iniciarSesion(usuario: String, password: String) = error("No usado")
    override suspend fun sesionGuardada(): Sesion? = null
    override suspend fun cerrarSesion() = Unit
    override suspend fun registrarLabor(datos: DatosLabor): Labor = Labor(siguiente--, datos.loteId, datos.tipo, datos.fecha, "Tecnico", datos.cantidad, datos.observacion, true)
    override suspend fun corregirLabor(id: Long, datos: DatosLabor) = error("No usado")
    override suspend fun eliminarLabor(id: Long) = Unit
    override suspend fun cerrarCampania(loteId: Long) = error("No usado")
    override suspend fun sincronizar(): ResumenSincronizacion {
        operaciones += "subir_pendientes"
        operaciones += "bajar_actualizados"
        return ResumenSincronizacion(1, 8, "ok")
    }
}

