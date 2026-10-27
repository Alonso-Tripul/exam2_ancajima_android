package pe.upeu.agrosierra.di

import io.ktor.client.engine.HttpClientEngine
import org.koin.core.module.Module
import org.koin.dsl.module
import pe.upeu.agrosierra.data.local.AlmacenSeguro
import pe.upeu.agrosierra.data.local.MonitorConexion
import pe.upeu.agrosierra.data.remote.AgroApi
import pe.upeu.agrosierra.data.remote.crearClienteHttp
import pe.upeu.agrosierra.data.repository.AgroRepositoryImpl
import pe.upeu.agrosierra.db.AgroDb
import pe.upeu.agrosierra.domain.repository.AgroRepository
import pe.upeu.agrosierra.domain.usecase.RegistrarLaborUseCase
import pe.upeu.agrosierra.domain.usecase.SincronizarUseCase
import pe.upeu.agrosierra.presentation.listado.ListadoViewModel
import pe.upeu.agrosierra.presentation.sesion.SesionViewModel

fun dataModule(
    db: AgroDb,
    almacen: AlmacenSeguro,
    monitor: MonitorConexion,
    engine: HttpClientEngine,
): Module = module {
    single { db }
    single<AlmacenSeguro> { almacen }
    single<MonitorConexion> { monitor }
    single { crearClienteHttp(engine, get()) }
    single { AgroApi(get()) }
    single<AgroRepository> { AgroRepositoryImpl(get(), get(), get(), get()) }
}

val domainModule = module {
    factory { SincronizarUseCase(get()) }
    factory { RegistrarLaborUseCase(get()) }
}

val presentationModule = module {
    factory { SesionViewModel(get()) }
    factory { ListadoViewModel(get(), get()) }
}
