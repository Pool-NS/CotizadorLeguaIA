# Revisión de seguridad local (alcance limitado)

Fecha: 2026-10-15. Revisión estática del código disponible en este repositorio. No equivale a pentest, auditoría independiente, certificación, revisión de configuración real de Render ni análisis de vulnerabilidades completo de dependencias.

## Hallazgos y estado

| Severidad | Hallazgo | Estado / acción |
|---|---|---|
| Alta para exposición pública | `POST /v1/interpret` no autentica clientes ni limita uso; una URL pública podría consumir la cuota Gemini del titular. | Pendiente. No usar el endpoint Render con trabajadores ni publicarlo como servicio de producción antes de definir identidad, cuotas/rate limit y protección antiabuso del lado servidor. |
| Media | La contraseña local del jefe era SHA-256 directo sin sal y sin límite de intentos, guardada por instalación. | Corregido en el cliente local: PBKDF2 (HMAC-SHA256 cuando el dispositivo lo soporta, HMAC-SHA1 como compatibilidad para API 24–25) con sal aleatoria, comparación constante, migración transparente del hash anterior luego de un acceso válido y espera de 60 s después de cinco fallos. Las nuevas credenciales aplican la política interna indicada abajo. |
| Media | Android permitía respaldo automático de los datos privados de la app. | Corregido con `android:allowBackup="false"`; validar el comportamiento en las versiones/dispositivos objetivo. |
| Media para operación | PIN local no es una identidad de trabajador ni control de acceso multiusuario. | Sigue pendiente. La base Room permanece separada en cada teléfono; se requiere autenticación/autorización de servidor para roles y datos compartidos. |
| Media | Solicitud libre de cotización puede contener información personal que sería enviada a Gemini. | Pendiente de control técnico de minimización/consentimiento. Instruir no incluir nombre, teléfono, dirección u otros identificadores; la revisión de código no puede garantizar detección de todos los datos personales. |
| Baja/entorno | Android Debug permite HTTP para conexión a localhost/LAN; Release debe usar HTTPS. | Solo desarrollo. No distribuir APK Debug ni configurar URL Release en HTTP. |
| Pendiente | SonarQube está configurado, pero no se ejecutó contra instancia/credenciales disponibles. | No hay Quality Gate ni hallazgos de Sonar que reportar. |
| Informativo | Qodana Community for Android se ejecuta en contenedor en GitHub Actions. | Ejecución #4 del 2026-10-15 para `c6de50d`: completada correctamente. Revisar el artefacto `qodana-report` para valorar los hallazgos; esto no es certificación ni Quality Gate de SonarQube. |
| Pendiente de corregir en CI | GitHub Actions falla al invocar `./gradlew` porque el bit ejecutable no estaba versionado en Git. | Corregido en esta revisión al marcar `gradlew` como ejecutable; validar en la siguiente ejecución de CI. |
| Configuración del repositorio | El envío del grafo de dependencias Gradle falla porque Dependency graph está desactivado en GitHub. | Se retiró ese paso del workflow para no fallar por una opción externa del repositorio. Si se desea el grafo en GitHub, habilitarlo en Settings → Code security and analysis y volver a añadir el envío. Dependabot continúa configurado aparte. |
| Configuración de CodeQL | CodeQL rechazó `build-mode: manual` al incluir Python, cuyo modo admitido es `none`. | Se separó el análisis en una matriz: Java/Kotlin con compilación manual y Python con modo `none`; validar en la siguiente ejecución de CI. |
| Informativo (Python) | `pip-audit` contra las dependencias fijadas en `backend/api/requirements.txt`. | Ejecutado el 2026-10-15: sin vulnerabilidades conocidas en los paquetes auditados; el resultado se conserva en `qa/security/python_dependencies_pip_audit.json`. Esto no cubre dependencias Android, imagen de Render, configuración ni vulnerabilidades desconocidas. |

## Cambios locales de protección del PIN

- Los PIN nuevos usan 210 000 iteraciones PBKDF2 y sal aleatoria de 128 bits; usa HMAC-SHA256 cuando está disponible y HMAC-SHA1 en Android API 24–25 para mantener el `minSdk` actual. Se conserva el identificador del KDF para poder verificar el PIN tras una actualización de Android; la comparación usa `MessageDigest.isEqual`.
- La clave heredada SHA-256 se valida solo para permitir la migración al nuevo formato después de un acceso correcto; luego se elimina.
- Tras cinco intentos incorrectos, el acceso queda bloqueado un minuto. Es una defensa local de demostración; un atacante con control del dispositivo/almacenamiento puede eludir controles cliente.
- La política interna del prototipo requiere para contraseñas nuevas entre 12 y 64 caracteres, con al menos una letra mayúscula, una minúscula, un número y un símbolo. Es una decisión de producto solicitada para este proyecto, no una regla textual de ISO/IEC 27001 A.5.17 ni evidencia de conformidad/certificación. Los espacios se permiten, pero no cuentan como símbolo. Una credencial anterior puede usarse para verificar identidad; si no cumple la política actual, antes de entrar al panel la app solicita crear la nueva contraseña.
- Esta credencial solo protege la pantalla en un teléfono. No sustituye cuentas individuales, autenticación en backend ni MFA para administración multiusuario.
- La función no protege cotizaciones frente a extracción de un teléfono comprometido, root o ingeniería inversa y no sustituye login empresarial.

## Verificaciones que faltan antes de distribución

1. Instalar la app en dispositivos objetivo y verificar migración de credenciales, bloqueo temporal y política de backup/restauración.
2. Verificación local de Android el 2026-10-15: `testDebugUnitTest`, `lintDebug` y `assembleDebug` completaron correctamente. 21 pruebas unitarias pasaron; lint informó 0 errores y 2 avisos (`targetSdk` 35 y Room con Kapt). APK Debug 1.3.0 generado. Esta compilación no prueba comportamiento visual en teléfonos ni firma de distribución.
3. Qodana CI pasó el 2026-10-15 para la revisión anterior. Esta ejecución local no repite Qodana, CodeQL ni el backend; confirmar los análisis en CI al publicar los cambios.
4. Ejecutar escaneo de secretos e inventario actualizado de dependencias Android; revisar falsos positivos y remediar hallazgos aplicables.
5. Ejecutar SonarQube con host/token, guardar versión de servidor, fecha y Quality Gate.
6. Revisión de seguridad del backend público: autenticación, autorización, rate limit, cuota, CORS si aplica, tamaño de solicitud, timeout, errores y retención.
7. Pentest/DAST autorizado del artefacto de producción y configuración real antes de guardar datos reales o distribuir a trabajadores.

No se deben subir `.env`, API keys, PINes, datos de clientes ni registros locales a GitHub. Si una clave ya fue expuesta en chat/captura o commit, revocarla y crear una nueva; no basta con borrar el texto del repositorio.

## Referencias de autenticación

- ISO/IEC 27001:2022 define un sistema de gestión de seguridad. A.5.17 aborda la gestión de información de autenticación; la política de composición aplicada aquí es una regla interna del producto y no debe atribuirse a un mandato explícito de ese control: https://www.iso.org/standard/27001
- NIST SP 800-63B (Digital Identity Guidelines) desaconseja reglas de composición para contraseñas verificadas centralmente y define longitud según el contexto de autenticación. No debe aplicarse mecánicamente a este PIN de desbloqueo local: https://pages.nist.gov/800-63-4/sp800-63b.html
