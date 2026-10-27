package pe.upeu.agrosierra.domain.usecase

import pe.upeu.agrosierra.domain.model.DatosLabor
import pe.upeu.agrosierra.domain.repository.AgroRepository

class SincronizarUseCase(private val repository: AgroRepository) {
    suspend operator fun invoke() = repository.sincronizar()
}

class RegistrarLaborUseCase(private val repository: AgroRepository) {
    suspend operator fun invoke(datos: DatosLabor) = repository.registrarLabor(datos)
}
