package pe.upeu.agrosierra.data.remote

import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException
import pe.upeu.agrosierra.data.remote.dto.ErrorDto
import pe.upeu.agrosierra.domain.model.ErrorApi

suspend fun HttpResponse.exigirExito(): HttpResponse {
    if (status.value in 200..299) return this
    val error = runCatching { body<ErrorDto>() }.getOrElse { ErrorDto(status.value, "Error HTTP ${status.value}") }
    throw when (status) {
        HttpStatusCode.BadRequest -> ErrorApi.Validacion(error.validationErrors, error.message)
        HttpStatusCode.Unauthorized -> ErrorApi.NoAutorizado(error.message)
        HttpStatusCode.Forbidden -> ErrorApi.SinPrivilegio(error.message)
        HttpStatusCode.NotFound -> ErrorApi.NoEncontrado(error.message)
        HttpStatusCode.Conflict -> ErrorApi.Conflicto(error.message)
        else -> ErrorApi.Servidor(error.message)
    }
}

fun Throwable.aErrorApi(): ErrorApi = when (this) {
    is ErrorApi -> this
    is HttpRequestTimeoutException -> ErrorApi.TiempoAgotado()
    is IOException -> ErrorApi.SinConexion()
    else -> ErrorApi.Desconocido(message ?: "Error inesperado")
}

