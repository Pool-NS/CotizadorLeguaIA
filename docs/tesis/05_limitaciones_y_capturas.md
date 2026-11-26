# Limitaciones y evidencia visual

## Limitaciones conocidas

1. Room/SQLite vive dentro de cada instalación y conserva el flujo offline. Firestore y sus reglas están configurados; se verificó una escritura real de ensayo desde Android en 1.5.0 (8). La consulta de lectura agregada se añadió en 1.5.1 (9), pero aún falta ejecutarla en el teléfono.
2. FastAPI interpreta solicitudes y conecta Gemini, pero no almacena datos, no autentica trabajadores ni aplica cuotas/rate limit. Render se configura en el repositorio, pero esta revisión no verificó un host desplegado.
3. Room incluye entidades de producción, requerimientos y predicción, aunque no hay flujo completo de todas ellas ni predictor conectado. No inferir que su presencia en el esquema sea una funcionalidad terminada.
4. No hay receta aprobada de consumo; la app no promete suficiencia, no reserva material y no descuenta stock automáticamente al cotizar o vender.
5. El dashboard no mide exactitud independiente. La marca de comunicación del precio es declarada por operador. Las métricas del dashboard no son aún el análisis pretest/posttest de la tesis.
6. Pretest sin app, fase experimental, modalidad física de atención, errores atribuibles y exactitud necesitan definición y observación con el instrumento aprobado.
7. `exportSchema=false` y la falta de migración v1→2 dificultan certificar actualización desde esquemas muy antiguos.
8. No se ejecutó pentest, DAST, escaneo Sonar contra servidor, Quality Gate remoto ni análisis Qodana en esta revisión. Los marcos ISO/IEC son referencias de calidad/seguridad, no certificación lograda.
9. No se realizó una prueba visual de escalado, teclado, TalkBack o contraste en Android físico/emulador.
10. Firestore `(default)` está creado en `nam5` (EE. UU.) y las reglas están publicadas. Email/Password, el usuario de prueba y su membresía están configurados. Se observó un documento creado por Android en Firestore; falta verificar desde la versión nueva la lectura por sesión autorizada y el comportamiento con usuario no miembro.
11. La app ahora incluye trazas Firebase Performance `ai_interpretation`, `quote_calculation`, `quote_local_save`, `cloud_quote_sync` y `cloud_quote_query`. La lectura de cotizaciones y sus trazas nuevas compilan, pero no se han observado desde el teléfono. `durationMillis` mide con reloj monotónico el intervalo desde que se inicia el flujo de cotización hasta que el operador confirma que comunicó el precio. Es nulo cuando no se comunica o falta una duración válida; no representa el guardado en Firestore.
12. No se midieron CPU, memoria RAM ni tráfico de red en un dispositivo real. La plantilla `qa/data/technical_efficiency_runs_template.csv` está vacía y los criterios no funcionales esperan aprobación.
13. App Distribution entrega APK Debug de ensayo; no hay certificado de publicación ni variante release distribuida. No se presenta como evidencia de producción.
14. App Distribution contiene el release `1.5.0 (8)` y un tester autorizado. La distribución de `1.5.1 (9)` está pendiente de publicarse.
15. Las pruebas de reglas en emulador pasaron (5/5). En 1.5.0 (8), el teléfono físico `2409BRN2CY` (Android 16 / API 36) generó una cotización observada en Firestore. La consulta de lectura desde la nueva interfaz y la consulta entre sesiones aún deben comprobarse con 1.5.1.
16. `npm audit` reporta cinco vulnerabilidades altas transitivas del SDK de Firebase en las dependencias de test; deben revisarse al actualizar el SDK/compatibilidad de Node.

Android documenta que `allowBackup=false` puede no impedir transferencias directas entre dispositivos en ciertos fabricantes si la app apunta a Android 12 o posterior; por eso el proyecto declara también exclusiones explícitas en reglas de respaldo en nube y transferencia. Debe confirmarse en los dispositivos objetivo. Referencia: [Android Developers — Auto Backup](https://developer.android.com/identity/data/autobackup).

## Capturas de interfaces verificadas

**Estado:** comprobación parcial en teléfono físico `2409BRN2CY` (Android 16 / API 36): la versión 1.5.0 se instaló, abrió y produjo un registro observado en Firestore. La prueba de interfaz de mostrar/ocultar contraseña de 1.5.1 compila, pero Android canceló la instalación instrumentada (`INSTALL_FAILED_USER_RESTRICTED`); por ello no se reporta como ejecutada. Las capturas seguras de Inicio, Formulario, Resumen, Panel, Tarifas y Stock siguen pendientes; no se usan previews como evidencia ejecutada.

Para completar la evidencia visual, compilar e instalar el APK Debug en un emulador configurado o un dispositivo de prueba autorizado y capturar, como mínimo: Inicio, Formulario por servicio, Resumen, Panel, Tarifas y Stock. Anotar dispositivo, Android, `versionName`, estado de backend y si los datos son de prueba. No incluir datos reales de clientes.

## Uso en sustentación

Presentar por separado: (A) verificación técnica del software; (B) evaluación estadística de solicitudes reales pre/post, cuando se hayan recolectado conforme al protocolo; (C) limitaciones del prototipo. El éxito de compilación y las pruebas unitarias no equivalen a un efecto causal ni a mejora empresarial observada.
