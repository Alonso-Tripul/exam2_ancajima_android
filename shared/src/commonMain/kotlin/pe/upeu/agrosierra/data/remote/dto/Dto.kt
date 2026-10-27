package pe.upeu.agrosierra.data.remote.dto

import kotlinx.serialization.Serializable
import pe.upeu.agrosierra.domain.model.*

@Serializable data class LoginRequestDto(val usuario: String, val password: String)
@Serializable data class LoginResponseDto(
    val token: String, val usuario: String, val nombre: String, val rol: String,
    val privilegios: Set<String>, val expiraEn: Long,
)
@Serializable data class LoteDto(
    val id: Long, val codigo: String, val nombre: String, val hectareas: Double,
    val cultivo: String, val valle: String, val campaniaCerrada: Boolean, val dosisMaximaPorHectarea: Double,
)
@Serializable data class LaborDto(
    val id: Long, val loteId: Long, val tipo: String, val fecha: String,
    val responsable: String, val cantidad: Double, val observacion: String,
)
@Serializable data class LaborRequestDto(
    val loteId: Long, val tipo: String, val fecha: String, val cantidad: Double, val observacion: String,
)
@Serializable data class PaginaDto<T>(
    val contenido: List<T>, val pagina: Int, val tamanio: Int,
    val totalElementos: Long, val totalPaginas: Int, val ultima: Boolean,
)
@Serializable data class ErrorDto(
    val status: Int = 500, val message: String = "Error del servidor",
    val validationErrors: Map<String, String> = emptyMap(),
)

fun LoginResponseDto.toDomain() = Sesion(token, usuario, nombre, rol, privilegios, expiraEn)
fun LoteDto.toDomain() = Lote(id, codigo, nombre, hectareas, cultivo, valle, campaniaCerrada, dosisMaximaPorHectarea)
fun LaborDto.toDomain(pendiente: Boolean = false) = Labor(
    id, loteId, TipoLabor.valueOf(tipo), fecha, responsable, cantidad, observacion, pendiente,
)
fun DatosLabor.toDto() = LaborRequestDto(loteId, tipo.name, fecha, cantidad, observacion)

