package pe.upeu.agrosierra.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.upeu.agrosierra.data.local.AlmacenSeguro
import pe.upeu.agrosierra.data.local.MonitorConexion
import pe.upeu.agrosierra.data.remote.AgroApi
import pe.upeu.agrosierra.data.remote.aErrorApi
import pe.upeu.agrosierra.data.remote.dto.toDomain
import pe.upeu.agrosierra.data.remote.dto.toDto
import pe.upeu.agrosierra.db.AgroDb
import pe.upeu.agrosierra.domain.model.*
import pe.upeu.agrosierra.domain.repository.AgroRepository

class AgroRepositoryImpl(
    private val db: AgroDb,
    private val api: AgroApi,
    private val almacen: AlmacenSeguro,
    private val monitor: MonitorConexion,
) : AgroRepository {
    private val q get() = db.agroDbQueries

    override fun observarLotes(busqueda: String, valle: String, soloAbiertas: Boolean): Flow<List<Lote>> =
        q.observarLotes(busqueda, valle, if (soloAbiertas) 1L else 0L).asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { row ->
                Lote(row.id, row.codigo, row.nombre, row.hectareas, row.cultivo, row.valle, row.campaniaCerrada == 1L, row.dosisMaxima)
            }
        }

    override fun observarLabores(loteId: Long): Flow<List<Labor>> =
        q.observarLaboresPorLote(loteId).asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { row -> row.toDomain() }
        }

    override fun ultimaSincronizacion(): Flow<String?> =
        q.ultimaSincronizacion().asFlow().map { query -> query.executeAsOneOrNull() }

    override suspend fun iniciarSesion(usuario: String, password: String): Sesion = ejecutar {
        api.login(usuario, password).toDomain().also { almacen.guardarSesion(it) }
    }

    override suspend fun sesionGuardada(): Sesion? = almacen.leerSesion()

    override suspend fun cerrarSesion() {
        almacen.borrarSesion()
    }

    override suspend fun registrarLabor(datos: DatosLabor): Labor {
        val sesion = almacen.leerSesion() ?: throw ErrorApi.NoAutorizado()
        val minimo = q.siguienteIdLocal().executeAsOne()
        val localId = minOf(-1L, minimo - 1L)
        q.insertarLabor(localId, datos.loteId, datos.tipo.name, datos.fecha, sesion.nombre, datos.cantidad, datos.observacion, 1L)
        val local = Labor(localId, datos.loteId, datos.tipo, datos.fecha, sesion.nombre, datos.cantidad, datos.observacion, true)
        if (monitor.conectado.value) runCatching { sincronizar() }
        return local
    }

    override suspend fun corregirLabor(id: Long, datos: DatosLabor): Labor {
        val anterior = q.obtenerLabor(id).executeAsOneOrNull() ?: throw ErrorApi.NoEncontrado()
        q.actualizarLaborLocal(datos.tipo.name, datos.fecha, datos.cantidad, datos.observacion, 1L, id)
        val local = Labor(id, anterior.loteId, datos.tipo, datos.fecha, anterior.responsable, datos.cantidad, datos.observacion, true)
        if (monitor.conectado.value) runCatching { sincronizar() }
        return local
    }

    override suspend fun eliminarLabor(id: Long) = ejecutar {
        if (id < 0) q.eliminarLaborLocal(id)
        else {
            if (!monitor.conectado.value) throw ErrorApi.SinConexion("Conectate para eliminar una labor ya sincronizada")
            api.eliminarLabor(id)
            q.eliminarLaborLocal(id)
        }
    }

    override suspend fun cerrarCampania(loteId: Long): Lote = ejecutar {
        if (!monitor.conectado.value) throw ErrorApi.SinConexion("Conectate para cerrar la campania")
        api.cerrarCampania(loteId).toDomain().also { remoto ->
            q.upsertLote(remoto.id, remoto.codigo, remoto.nombre, remoto.hectareas, remoto.cultivo, remoto.valle, 1L, remoto.dosisMaximaPorHectarea)
        }
    }

    override suspend fun sincronizar(): ResumenSincronizacion = ejecutar {
        if (!monitor.conectado.value) throw ErrorApi.SinConexion()
        var enviados = 0
        q.laboresPendientes().executeAsList().forEach { local ->
            val datos = DatosLabor(local.loteId, TipoLabor.valueOf(local.tipo), local.fecha, local.cantidad, local.observacion)
            val remoto = if (local.id < 0) api.crearLabor(datos.toDto()) else api.corregirLabor(local.id, datos.toDto())
            db.transaction {
                q.eliminarLaborLocal(local.id)
                q.insertarLabor(remoto.id, remoto.loteId, remoto.tipo, remoto.fecha, remoto.responsable, remoto.cantidad, remoto.observacion, 0L)
            }
            enviados++
        }

        val lotes = api.lotes().contenido
        lotes.forEach { dto ->
            val lote = dto.toDomain()
            q.upsertLote(lote.id, lote.codigo, lote.nombre, lote.hectareas, lote.cultivo, lote.valle, if (lote.campaniaCerrada) 1L else 0L, lote.dosisMaximaPorHectarea)
            api.labores(lote.id).contenido.forEach { labor ->
                q.insertarLabor(labor.id, labor.loteId, labor.tipo, labor.fecha, labor.responsable, labor.cantidad, labor.observacion, 0L)
            }
        }
        q.guardarMeta("ultima_sync", "Sincronizado hace un momento")
        ResumenSincronizacion(enviados, lotes.size, "$enviados enviados, ${lotes.size} lotes actualizados")
    }

    private suspend fun <T> ejecutar(block: suspend () -> T): T = try {
        block()
    } catch (t: Throwable) {
        val error = t.aErrorApi()
        if (error is ErrorApi.NoAutorizado) almacen.borrarSesion()
        throw error
    }

    private fun pe.upeu.agrosierra.db.Labor.toDomain() = Labor(
        id, loteId, TipoLabor.valueOf(tipo), fecha, responsable, cantidad, observacion, pendiente == 1L,
    )
}

