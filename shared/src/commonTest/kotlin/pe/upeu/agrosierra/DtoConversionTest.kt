package pe.upeu.agrosierra

import kotlin.test.Test
import kotlin.test.assertEquals
import pe.upeu.agrosierra.data.remote.dto.LoteDto
import pe.upeu.agrosierra.data.remote.dto.toDomain

class DtoConversionTest {
    @Test fun convierteDtoSinFiltrarElDominio() {
        val dto = LoteDto(1, "L-001", "Pampa", 12.5, "Papa", "Mantaro", false, 2.5)
        val domain = dto.toDomain()
        assertEquals("L-001", domain.codigo)
        assertEquals(2.5, domain.dosisMaximaPorHectarea)
    }
}

