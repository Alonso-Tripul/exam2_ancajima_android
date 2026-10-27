package pe.upeu.agrosierra

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import pe.upeu.agrosierra.data.local.AlmacenSeguro
import pe.upeu.agrosierra.domain.model.Sesion

class AlmacenSeguroAndroid(context: Context) : AlmacenSeguro {
    private val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "sesion_segura",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override suspend fun guardarSesion(sesion: Sesion) {
        prefs.edit()
            .putString("token", sesion.token)
            .putString("usuario", sesion.usuario)
            .putString("nombre", sesion.nombre)
            .putString("rol", sesion.rol)
            .putString("privilegios", sesion.privilegios.joinToString("|"))
            .putLong("expira", sesion.expiraEn)
            .apply()
    }

    override suspend fun leerSesion(): Sesion? {
        val token = prefs.getString("token", null) ?: return null
        val expira = prefs.getLong("expira", 0)
        if (expira <= System.currentTimeMillis() / 1000) {
            borrarSesion()
            return null
        }
        return Sesion(
            token = token,
            usuario = prefs.getString("usuario", "") ?: "",
            nombre = prefs.getString("nombre", "") ?: "",
            rol = prefs.getString("rol", "") ?: "",
            privilegios = prefs.getString("privilegios", "")?.split('|')?.filter { it.isNotBlank() }?.toSet().orEmpty(),
            expiraEn = expira,
        )
    }

    override suspend fun borrarSesion() { prefs.edit().clear().apply() }
}

