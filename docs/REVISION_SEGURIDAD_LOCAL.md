# Revisión de seguridad local (alcance limitado)

Fecha: 2026-10-15. Revisión estática del código disponible en este repositorio. No equivale a pentest, auditoría independiente, certificación, revisión de configuración real de Render ni análisis de vulnerabilidades completo de dependencias.

## Hallazgos y estado

| Severidad | Hallazgo | Estado / acción |
|---|---|---|
| Alta para exposición pública | `POST /v1/interpret` no autentica clientes ni limita uso; una URL pública podría consumir la cuota Gemini del titular. | Pendiente. No usar el endpoint Render con trabajadores ni publicarlo como servicio de producción antes de definir identidad, cuotas/rate limit y protección antiabuso del lado servidor. |
| Media | El PIN del jefe era SHA-256 directo sin sal y sin límite de intentos, guardado por instalación. | Corregido en el cliente local: PBKDF2 (HMAC-SHA256 cuando el dispositivo lo soporta, HMAC-SHA1 como compatibilidad para API 24–25) con sal aleatoria por PIN, comparación constante, migración transparente del hash anterior luego de un login válido y espera de 60 s después de cinco fallos. |
| Media | Android permitía respaldo automático de los datos privados de la app. | Corregido con `android:allowBackup="false"`; validar el comportamiento en las versiones/dispositivos objetivo. |
| Media para operación | PIN local no es una identidad de trabajador ni control de acceso multiusuario. | Sigue pendiente. La base Room permanece separada en cada teléfono; se requiere autenticación/autorización de servidor para roles y datos compartidos. |
| Media | Solicitud libre de cotización puede contener información personal que sería enviada a Gemini. | Pendiente de control técnico de minimización/consentimiento. Instruir no incluir nombre, teléfono, dirección u otros identificadores; la revisión de código no puede garantizar detección de todos los datos personales. |
| Baja/entorno | Android Debug permite HTTP para conexión a localhost/LAN; Release debe usar HTTPS. | Solo desarrollo. No distribuir APK Debug ni configurar URL Release en HTTP. |
| Pendiente | SonarQube está configurado, pero no se ejecutó contra instancia/credenciales disponibles. | No hay Quality Gate ni hallazgos de Sonar que reportar. |
| Informativo (Python) | `pip-audit` contra las dependencias fijadas en `backend/api/requirements.txt`. | Ejecutado el 2026-10-15: sin vulnerabilidades conocidas en los paquetes auditados; el resultado se conserva en `qa/security/python_dependencies_pip_audit.json`. Esto no cubre dependencias Android, imagen de Render, configuración ni vulnerabilidades desconocidas. |

## Cambios locales de protección del PIN

- Los PIN nuevos usan 210 000 iteraciones PBKDF2 y sal aleatoria de 128 bits; usa HMAC-SHA256 cuando está disponible y HMAC-SHA1 en Android API 24–25 para mantener el `minSdk` actual. Se conserva el identificador del KDF para poder verificar el PIN tras una actualización de Android; la comparación usa `MessageDigest.isEqual`.
- La clave heredada SHA-256 se valida solo para permitir la migración al nuevo formato después de un acceso correcto; luego se elimina.
- Tras cinco intentos incorrectos, el acceso queda bloqueado un minuto. Es una defensa local de demostración; un atacante con control del dispositivo/almacenamiento puede eludir controles cliente.
- La función no protege cotizaciones frente a extracción de un teléfono comprometido, root o ingeniería inversa y no sustituye login empresarial.

## Verificaciones que faltan antes de distribución

1. Instalar la app en dispositivos objetivo y verificar migración de credenciales, bloqueo temporal y política de backup/restauración.
2. Pruebas locales ejecutadas el 2026-10-15: Android `test`, `lintDebug`, `assembleDebug`; backend 5 pruebas unitarias. Repetirlas en CI y dispositivos objetivo.
3. Pendiente ejecutar CodeQL/SAST de GitHub, escaneo de secretos e inventario actualizado de dependencias Android; revisar falsos positivos y remediar hallazgos aplicables.
4. Ejecutar SonarQube con host/token, guardar versión de servidor, fecha y Quality Gate.
5. Revisión de seguridad del backend público: autenticación, autorización, rate limit, cuota, CORS si aplica, tamaño de solicitud, timeout, errores y retención.
6. Pentest/DAST autorizado del artefacto de producción y configuración real antes de guardar datos reales o distribuir a trabajadores.

No se deben subir `.env`, API keys, PINes, datos de clientes ni registros locales a GitHub. Si una clave ya fue expuesta en chat/captura o commit, revocarla y crear una nueva; no basta con borrar el texto del repositorio.
