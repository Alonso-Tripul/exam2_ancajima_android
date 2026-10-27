package pe.upeu.agrosierra

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import pe.upeu.agrosierra.data.remote.AgroApi
import pe.upeu.agrosierra.domain.model.ErrorApi

class MapeoErrorTest {
    @Test fun traduce400EnValidacionPorCampo() = runTest {
        val engine = MockEngine {
            respond(
                """{"status":400,"message":"Datos invalidos","validationErrors":{"cantidad":"Supera la dosis"}}""",
                HttpStatusCode.BadRequest,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val client = HttpClient(engine) { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } }
        val error = assertFailsWith<ErrorApi.Validacion> { AgroApi(client).login("x", "y") }
        assertEquals("Supera la dosis", error.campos["cantidad"])
    }
}

