package pe.upeu.agrosierra

import android.app.Application
import io.ktor.client.engine.android.Android
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import pe.upeu.agrosierra.di.dataModule
import pe.upeu.agrosierra.di.domainModule
import pe.upeu.agrosierra.di.presentationModule

class AgroApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val almacen = AlmacenSeguroAndroid(this)
        val monitor = MonitorConexionAndroid(this)
        val db = crearBaseLocal(this)
        startKoin {
            androidContext(this@AgroApplication)
            modules(dataModule(db, almacen, monitor, Android.create()), domainModule, presentationModule)
        }
    }
}
