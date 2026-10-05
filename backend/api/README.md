# Backend de interpretación generativa

Este servicio FastAPI recibe texto de una solicitud y lo envía a Gemini Developer API. Devuelve campos JSON estructurados para que el cotizador los revise. El modelo no fija tarifas, descuentos, stock ni resultados comerciales. Room es la base de datos local del Android; este backend actúa como API y puente a Gemini, y no es una base de datos compartida.

## Ejecución local para demostración

1. Instala Python 3.10 o posterior.
2. Desde `backend/api`, crea y activa un entorno virtual e instala dependencias:

   ```powershell
   py -m venv .venv
   .\.venv\Scripts\Activate.ps1
   pip install -r requirements.txt
   ```

3. Crea una clave Gemini en Google AI Studio. Si aún no existe `.env`, copia `.env.example` a `.env`; luego reemplaza `GEMINI_API_KEY` con la clave y conserva `GEMINI_MODEL`. No compartas ese archivo ni lo subas a Git. La cuenta universitaria no garantiza acceso API gratuito.
4. Inicia el servicio en el equipo de desarrollo:

   ```powershell
   uvicorn app:app --env-file .env --host 127.0.0.1 --port 8000
   ```

La versión Debug del emulador Android apunta a `http://10.0.2.2:8000`. Si la app está en un teléfono físico, obtén la IPv4 Wi-Fi del PC con `ipconfig` y agrega a `local.properties` (en la raíz del proyecto, no en `backend/api`) una propiedad como `aiBackendUrl=http://192.168.1.25:8000`. Conserva las demás propiedades del archivo. El teléfono y el PC deben estar en la misma Wi-Fi. En Android Studio sincroniza Gradle y reinstala Debug después de cambiar la URL. El archivo `local.properties` está ignorado por Git; no se debe subir la IP local.

Para celular físico, inicia Uvicorn con `uvicorn app:app --env-file .env --host 0.0.0.0 --port 8000`; para el emulador, `127.0.0.1` basta. `--env-file .env` carga la clave desde el archivo local. Comprueba desde el propio teléfono `http://<IPv4-del-PC>:8000/health`. Si no abre, revisa el perfil de red privada y el firewall de Windows. Mantén la ejecución solo durante la prueba en una red privada de confianza: esta demostración no implementa autenticación de usuario y no debe exponerse a Internet. El manifiesto Debug permite HTTP para desarrollo; Release exige HTTPS.

La API no registra deliberadamente el contenido de solicitudes. Gemini recibe un esquema JSON para estructurar los campos. La disponibilidad de nivel gratuito y sus límites dependen del proyecto, modelo y cuota vigentes; al agotarse, la interpretación falla y debe completarse manualmente. Según Google, los datos del nivel gratuito pueden usarse para mejorar sus productos. No envíes nombres, teléfonos, direcciones ni información comercial confidencial con una clave gratuita. Revisa las condiciones y el panel de cuota antes de usar solicitudes reales.

Referencias oficiales: [crear clave y configurar API](https://ai.google.dev/gemini-api/docs/get-started), [límites y niveles de uso](https://ai.google.dev/gemini-api/docs/billing), [modelos disponibles](https://ai.google.dev/gemini-api/docs/models) y [salidas JSON estructuradas](https://ai.google.dev/gemini-api/docs/structured-output).

## Configuración de producción

Este backend está preparado para pruebas locales, no para despliegue multiusuario. Antes de usarlo con trabajadores se requiere hosting HTTPS, autenticación por usuario, límites de uso, rotación y protección de secretos, registro de auditoría sin contenido sensible y política de retención. No se debe guardar una clave del proveedor dentro del APK.

## Verificación

- `GET /health`: confirma que el proceso está activo y si detecta una clave Gemini configurada, sin revelar la clave. La respuesta debe mostrar `ok: true` y `ai_configured: true` para poder interpretar solicitudes.
- `POST /v1/interpret`: requiere JSON `{"requirement":"..."}` y devuelve servicio, medidas, material, atributos de carpa/moto, campos faltantes y ambigüedades.
- La respuesta siempre requiere revisión humana. Ante falta de clave, error de red, salida inválida o ambigüedad, la app permite completar el formulario manualmente.
