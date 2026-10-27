package pe.upeu.agrosierra.domain.model

sealed class ErrorApi(message: String) : Exception(message) {
    class Validacion(val campos: Map<String, String>, message: String = "Revisa los datos") : ErrorApi(message)
    class NoAutorizado(message: String = "Tu sesion vencio") : ErrorApi(message)
    class SinPrivilegio(message: String = "No tienes permiso para esta operacion") : ErrorApi(message)
    class NoEncontrado(message: String = "El recurso ya no existe") : ErrorApi(message)
    class Conflicto(message: String) : ErrorApi(message)
    class Servidor(message: String = "El servidor no esta disponible") : ErrorApi(message)
    class SinConexion(message: String = "Sin conexion. El cambio quedara pendiente") : ErrorApi(message)
    class TiempoAgotado(message: String = "La solicitud tardo demasiado") : ErrorApi(message)
    class Desconocido(message: String = "Ocurrio un error inesperado") : ErrorApi(message)
}

