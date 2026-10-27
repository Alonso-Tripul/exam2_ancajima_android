package pe.upeu.agrosierra

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pe.upeu.agrosierra.data.local.MonitorConexion

class MonitorConexionAndroid(context: Context) : MonitorConexion {
    private val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _conectado = MutableStateFlow(hayInternet())
    override val conectado: StateFlow<Boolean> = _conectado.asStateFlow()

    init {
        manager.registerNetworkCallback(
            NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) { _conectado.value = true }
                override fun onLost(network: Network) { _conectado.value = hayInternet() }
            },
        )
    }

    private fun hayInternet(): Boolean {
        val network = manager.activeNetwork ?: return false
        return manager.getNetworkCapabilities(network)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}

