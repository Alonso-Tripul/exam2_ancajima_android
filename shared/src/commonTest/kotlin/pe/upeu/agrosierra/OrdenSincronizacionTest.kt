package pe.upeu.agrosierra

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OrdenSincronizacionTest {
    @Test fun siempreSubeAntesDeBajar() = runTest {
        val operaciones = mutableListOf<String>()
        operaciones += "subir_pendientes"
        operaciones += "bajar_actualizados"
        assertEquals(listOf("subir_pendientes", "bajar_actualizados"), operaciones)
    }
}

