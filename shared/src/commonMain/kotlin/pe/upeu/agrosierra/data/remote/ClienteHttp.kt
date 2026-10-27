package pe.upeu.agrosierra.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pe.upeu.agrosierra.data.local.AlmacenSeguro

object ApiConfig {
    const val BASE_URL = "http://10.0.2.2:8080/api/v1/"
}

fun crearClienteHttp(engine: HttpClientEngine, almacen: AlmacenSeguro): HttpClient {
    val client = HttpClient(engine) {
        expectSuccess = false
        defaultRequest { url(ApiConfig.BASE_URL) }
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; explicitNulls = false })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 8_000
            socketTimeoutMillis = 15_000
        }
        install(Logging) {
            logger = object : Logger { override fun log(message: String) = Unit }
            level = LogLevel.INFO
            sanitizeHeader { it == HttpHeaders.Authorization }
        }
    }
    client.plugin(HttpSend).intercept { request ->
        almacen.leerSesion()?.token?.let { token ->
            request.headers.remove(HttpHeaders.Authorization)
            request.headers.append(HttpHeaders.Authorization, "Bearer $token")
        }
        execute(request)
    }
    return client
}

