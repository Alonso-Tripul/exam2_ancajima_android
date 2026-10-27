# exam2_ancajima - AgroCampo

Aplicación Android/KMP del caso AgroSierra.

## Ejecutar

1. Levantar el backend en el puerto 8080.
2. Abrir esta carpeta en Android Studio.
3. Seleccionar `androidApp` y un emulador API 24 o superior.
4. Ejecutar.

La dirección base está centralizada en `ApiConfig.BASE_URL` y usa `10.0.2.2`, alias del equipo anfitrión desde el emulador Android.

## Arquitectura

- `shared/domain`: modelos, interfaces de repositorio y casos de uso.
- `shared/data/remote`: cliente Ktor único, DTO, conversiones y errores tipados.
- `shared/data/local`: contrato de almacenamiento seguro y monitor de red.
- `shared/data/repository`: repositorio offline-first.
- `shared/presentation`: StateFlow, UiState y ViewModels sin dependencia Android.
- `androidApp`: SQLDelight Android driver, Android Keystore, conectividad y Compose.

## Offline-first

La interfaz observa SQLDelight, no la respuesta HTTP. Un registro sin conexión recibe un ID negativo y `pendiente=true`. La sincronización primero envía pendientes (POST para ID negativo, PUT para ID definitivo) y después descarga lotes y labores dentro de transacciones.

El buscador, el valle seleccionado y la opción **Solo campañas abiertas** se combinan en una única consulta local. Por eso el filtro continúa respondiendo sin Internet y no depende de una nueva petición al backend.

## Seguridad

La sesión se almacena con preferencias cifradas respaldadas por Android Keystore. La cabecera Bearer se agrega en un único interceptor `HttpSend`. Un 401 borra la sesión; un 403 conserva la sesión y muestra el motivo.

## Pruebas

```powershell
.\gradlew.bat :shared:desktopTest
```

Incluye conversión DTO, traducción de 400 por campo, alta offline, orden de sincronización y SQLDelight en memoria.

