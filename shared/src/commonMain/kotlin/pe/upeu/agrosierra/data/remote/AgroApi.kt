package pe.upeu.agrosierra.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import pe.upeu.agrosierra.data.remote.dto.*

class AgroApi(private val client: HttpClient) {
    suspend fun login(usuario: String, password: String): LoginResponseDto =
        client.post("auth/login") { contentType(ContentType.Application.Json); setBody(LoginRequestDto(usuario, password)) }
            .exigirExito().body()

    suspend fun lotes(pagina: Int = 0, tamanio: Int = 100): PaginaDto<LoteDto> =
        client.get("lotes") { parameter("pagina", pagina); parameter("tamanio", tamanio) }.exigirExito().body()

    suspend fun labores(loteId: Long, pagina: Int = 0, tamanio: Int = 100): PaginaDto<LaborDto> =
        client.get("lotes/$loteId/labores") { parameter("pagina", pagina); parameter("tamanio", tamanio) }.exigirExito().body()

    suspend fun crearLabor(request: LaborRequestDto): LaborDto =
        client.post("labores") { contentType(ContentType.Application.Json); setBody(request) }.exigirExito().body()

    suspend fun corregirLabor(id: Long, request: LaborRequestDto): LaborDto =
        client.put("labores/$id") { contentType(ContentType.Application.Json); setBody(request) }.exigirExito().body()

    suspend fun eliminarLabor(id: Long) {
        client.delete("labores/$id").exigirExito()
    }

    suspend fun cerrarCampania(loteId: Long): LoteDto =
        client.post("lotes/$loteId/cerrar-campania").exigirExito().body()
}
