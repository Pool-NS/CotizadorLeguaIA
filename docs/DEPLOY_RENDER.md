# Despliegue del backend FastAPI en Render

## Estado y alcance

El servicio de `backend/api` solo interpreta solicitudes mediante Gemini. No es la base de datos de la aplicación: las cotizaciones, tarifas e inventario continúan guardándose en Room, localmente en cada instalación Android. El CSV de 200 filas es sintético y sirve para análisis en jamovi/SPSS; no se carga al backend ni representa el historial real de la empresa.

La configuración incluida permite desplegar un **entorno de demostración** público en Render. No se debe presentar como producción segura para uso multiusuario: actualmente el endpoint `/v1/interpret` no implementa autenticación de usuarios, autorización ni límites propios por usuario. Una clave Gemini guardada en Render protege la credencial del proveedor, pero no impide que terceros consuman la cuota llamando al endpoint público. Antes de uso operativo se requiere, como mínimo, autenticación y autorización en servidor, limitación de solicitudes, observabilidad sin datos sensibles, política de retención y una base de datos central con migraciones y respaldos si se compartirán cotizaciones, tarifas o stock.

Render Free suspende los servicios web tras 15 minutos sin tráfico y el primer acceso posterior puede tardar alrededor de un minuto. Es adecuado para una presentación o prueba, no para garantizar disponibilidad inmediata. Consulta las limitaciones vigentes en la documentación de Render.

## Archivos incluidos

- `render.yaml`: Blueprint para crear el Web Service desde la raíz del repositorio. Define `rootDir: backend/api`, Python, comandos, chequeo de salud y solicita la clave como secreto sin escribir su valor en Git.
- `backend/api/requirements.txt`: dependencias Python fijadas. `httpx` es el cliente HTTP que utiliza el backend para llamar a Gemini directamente; no se necesita el SDK `google-genai` para esta implementación.
- `backend/api/.gitignore` y el `.gitignore` raíz: excluyen `.env`, `.venv`, bytecode y archivos locales.

## 1. Preparar y verificar dependencias en Windows PowerShell

Desde la raíz del proyecto:

```powershell
cd C:\Users\ns62\Documents\ChatGPT\CotizadorLeguaIA.git\backend\api
```

Si el entorno virtual aún no existe, créalo y actívalo:

```powershell
py -m venv .venv
.\.venv\Scripts\Activate.ps1
```

Instala las dependencias fijadas del proyecto:

```powershell
python -m pip install --upgrade pip
python -m pip install -r requirements.txt
```

Para regenerar automáticamente el inventario desde el entorno activado:

```powershell
python -m pip freeze > requirements.txt
```

`pip freeze` guarda también dependencias transitivas y cualquier paquete instalado manualmente. Revisa el resultado antes de confirmarlo; para este repositorio ya existe un `requirements.txt` pequeño y fijado, por lo que no lo reemplaces con un entorno contaminado. Las dependencias directas actuales son FastAPI, Pydantic, HTTPX y Uvicorn.

Comprueba que no existan secretos versionados:

```powershell
git status --short --ignored backend\api
git check-ignore backend\api\.env backend\api\.venv
```

Si alguna clave real se llegó a subir a Git o compartir públicamente, revócala en el proveedor y genera otra; borrar el archivo en un commit posterior no invalida la clave filtrada.

## 2. Publicar el repositorio en GitHub

Este proyecto ya es un repositorio Git con Android, backend y documentación. No inicialices otro repositorio dentro de `backend/api`: un repositorio anidado complica Android Studio y Render. El archivo `render.yaml` hace que Render construya únicamente desde `backend/api`, aunque el repositorio contenga el resto del proyecto. La documentación queda junto al código en GitHub.

Revisa primero la rama y el remoto configurados:

```powershell
cd C:\Users\ns62\Documents\ChatGPT\CotizadorLeguaIA.git
git status
git remote -v
git branch --show-current
```

Agrega la configuración y documentación de despliegue junto con la guía de backend:

```powershell
git add render.yaml docs/DEPLOY_RENDER.md README.md backend/api/README.md backend/api/requirements.txt backend/api/.gitignore
git diff --cached --check
git diff --cached --stat
git commit -m "docs: prepara despliegue del backend en Render"
```

Si trabajas en una rama de cambios, súbela y crea un Pull Request hacia `main`:

```powershell
git push -u origin HEAD
```

Si los cambios ya están en la rama `main` y tu flujo permite actualizarla directamente:

```powershell
git push origin main
```

No uses `git init`, no cambies `origin` y no subas `.env`, `.venv`, `local.properties`, claves, archivos APK o información real de clientes. Si `git status` muestra una rama local distinta, no cambies de rama ni fuerces el push: primero confirma que el commit base corresponda a `origin/main`.

## 3. Crear el Web Service en Render

1. Inicia sesión en [Render](https://dashboard.render.com/) y vincula GitHub desde la configuración de cuenta si aún no aparece conectado.
2. En Render selecciona **New → Blueprint** para que Render lea `render.yaml` desde la raíz del repositorio. Autoriza el repositorio `Pool-NS/CotizadorLeguaIA`.
3. Revisa que el servicio se llame `cotizador-leguia-api` y que el plan seleccionado sea `Free`.
4. En la creación inicial, Render solicitará el valor de `GEMINI_API_KEY` porque en el Blueprint figura `sync: false`. Pega allí la clave vigente de Google AI Studio. Nunca la pegues en `render.yaml`, en el código, en Android ni en GitHub.
5. Confirma la creación y espera el deploy. La configuración equivalente en un Web Service manual es:

   | Campo de Render | Valor |
   |---|---|
   | Root Directory | `backend/api` |
   | Build Command | `pip install -r requirements.txt` |
   | Start Command | `uvicorn app:app --host 0.0.0.0 --port $PORT` |
   | Health Check Path | `/health` |
   | `GEMINI_API_KEY` | valor secreto de Google AI Studio |
   | `GEMINI_MODEL` | `gemini-3.5-flash-lite` |

6. En el panel del servicio abre **Environment** para añadir o actualizar variables. Guarda el cambio y realiza un redeploy si Render lo solicita. Render inyecta las variables en el proceso: no copies `.env` al repositorio ni uses `--env-file .env` en el comando de producción.
7. En **Events/Logs**, verifica que el deploy termine correctamente. Abre `https://<nombre-del-servicio>.onrender.com/health`. Debe devolver JSON con `ok: true` y `ai_configured: true`. `GET /` muestra información del servicio; la interpretación de IA requiere `POST /v1/interpret`.
8. Prueba una interpretación con la app. Si `/health` funciona y la IA falla, revisa logs y cuota de Gemini. HTTP 429 indica límite/cuota; 502 indica que el backend no obtuvo una respuesta utilizable del proveedor; 503 indica configuración de clave no disponible/autorizada. El primer pedido tras un período inactivo puede tardar por el arranque en frío del plan Free.

Google puede cambiar modelos, disponibilidad, límites y precios. `GEMINI_MODEL` puede actualizarse desde Environment sin alterar la clave; consulta el catálogo oficial antes de cambiarlo.

## 4. Configurar Android para HTTPS de Render

El módulo Android obtiene ambas direcciones desde `local.properties` en la raíz del proyecto. El archivo está ignorado por Git. Conserva `sdk.dir` y agrega o actualiza estas propiedades:

```properties
aiBackendUrl=https://<URL-HTTPS-ACTUAL-DE-NGROK>

aiBackendUrlRelease=https://<nombre-del-servicio>.onrender.com
```

La primera propiedad solo es para la compilación Debug local; si pruebas con emulador Android, su valor habitual es `http://10.0.2.2:8000`. Para la compilación Release se utiliza exclusivamente `aiBackendUrlRelease`, que debe comenzar por `https://`. No pegues la URL con `/v1/interpret`; el cliente agrega esa ruta automáticamente.

En Android Studio:

1. Abre la raíz del proyecto y selecciona **File → Sync Project with Gradle Files**.
2. Usa **Build → Clean Project** y luego **Build → Generate Signed Bundle / APK**.
3. Genera una variante **release** firmada para distribución. No distribuyas el APK Debug.
4. Instala la versión Release en un dispositivo y prueba conectividad, arranque en frío, interpretación y flujo manual cuando Gemini no esté disponible.

La URL pública solo habilita el acceso al servicio de IA. No sincroniza Room ni cotizaciones, tarifas o inventario entre teléfonos.

## Criterios de calidad y evidencia

La documentación existente en `qa/` y `docs/QUALITY.md` relaciona las prácticas con ISO/IEC 25010, ISO/IEC/IEEE 29119, gestión de calidad y revisión de código. Es una referencia de evaluación y trazabilidad; no declara certificación ISO ni garantiza un Quality Gate de SonarQube. Para presentar evidencia del despliegue, registra en `qa/` fecha, commit desplegado, URL pública sin secretos, respuestas de `/health`, pruebas funcionales, resultado de SonarQube y riesgos pendientes. Nunca guardes claves, tokens, datos personales ni cuerpos sensibles de peticiones en esas evidencias.

## Fuentes oficiales consultadas

- [Render: Web Services](https://render.com/docs/web-services)
- [Render: Free instances](https://render.com/docs/free)
- [Render: Blueprint specification](https://render.com/docs/blueprint-spec)
- [Render: environment variables and secrets](https://render.com/docs/configure-environment-variables)
- [Google AI: Gemini API keys](https://ai.google.dev/gemini-api/docs/api-key)
- [Google AI: models](https://ai.google.dev/gemini-api/docs/models)
