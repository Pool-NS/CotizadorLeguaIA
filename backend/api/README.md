# Backend de interpretación generativa

Este servicio recibe texto de una solicitud, lo envía al modelo configurado mediante la API de OpenAI y devuelve campos JSON estructurados para que el cotizador los revise. El modelo no fija tarifas, descuentos, stock ni resultados comerciales.

## Ejecución local para demostración

1. Instala Python 3.10 o posterior.
2. Desde `backend/api`, crea y activa un entorno virtual e instala dependencias:

   ```powershell
   py -m venv .venv
   .\.venv\Scripts\Activate.ps1
   pip install -r requirements.txt
   ```

3. Copia `.env.example` a `.env` y coloca una clave de API propia. No compartas ese archivo ni lo subas a Git.
4. Inicia el servicio en el equipo de desarrollo:

   ```powershell
   uvicorn app:app --host 127.0.0.1 --port 8000
   ```

La versión Debug del emulador Android apunta a `http://10.0.2.2:8000`. Si la app está en un teléfono físico, define `aiBackendUrl` en el `local.properties` local con la dirección accesible del backend. El manifiesto Debug permite HTTP para desarrollo; Release no. No expongas este backend local a Internet ni lo uses con datos identificables.

La API no registra deliberadamente el contenido de solicitudes. Envía `store: false` al proveedor. Aun así, el uso de la API está sujeto a los controles y retención del proveedor. Antes de pruebas con solicitudes reales, elimina nombres y datos personales y obtiene las autorizaciones requeridas. La cuenta de la API se factura por uso; no está incluida en ChatGPT.

## Configuración de producción

Este backend está preparado para pruebas locales, no para despliegue multiusuario. Antes de usarlo con trabajadores se requiere hosting HTTPS, autenticación por usuario, límites de uso, rotación y protección de secretos, registro de auditoría sin contenido sensible y política de retención. No se debe guardar una clave del proveedor dentro del APK.

## Verificación

- `GET /health`: confirma que el proceso está activo y si detecta una clave configurada, sin revelar la clave.
- `POST /v1/interpret`: requiere JSON `{"requirement":"..."}` y devuelve servicio, medidas, material, atributos de carpa/moto, campos faltantes y ambigüedades.
- La respuesta siempre requiere revisión humana. Ante falta de clave, error de red, salida inválida o ambigüedad, la app permite completar el formulario manualmente.
