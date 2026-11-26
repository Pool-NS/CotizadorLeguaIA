# Limitaciones y evidencia visual

## Limitaciones conocidas

1. Room/SQLite vive dentro de cada instalación. Firestore y sus reglas están configurados, pero no se verificó una sincronización real desde el teléfono; el dashboard sigue siendo local.
2. FastAPI interpreta solicitudes y conecta Gemini, pero no almacena datos, no autentica trabajadores ni aplica cuotas/rate limit. Render se configura en el repositorio, pero esta revisión no verificó un host desplegado.
3. Room incluye entidades de producción, requerimientos y predicción, aunque no hay flujo completo de todas ellas ni predictor conectado. No inferir que su presencia en el esquema sea una funcionalidad terminada.
4. No hay receta aprobada de consumo; la app no promete suficiencia, no reserva material y no descuenta stock automáticamente al cotizar o vender.
5. El dashboard no mide exactitud independiente. La marca de comunicación del precio es declarada por operador. Las métricas del dashboard no son aún el análisis pretest/posttest de la tesis.
6. Pretest sin app, fase experimental, modalidad física de atención, errores atribuibles y exactitud necesitan definición y observación con el instrumento aprobado.
7. `exportSchema=false` y la falta de migración v1→2 dificultan certificar actualización desde esquemas muy antiguos.
8. No se ejecutó pentest, DAST, escaneo Sonar contra servidor, Quality Gate remoto ni análisis Qodana en esta revisión. Los marcos ISO/IEC son referencias de calidad/seguridad, no certificación lograda.
9. No se realizó una prueba visual de escalado, teclado, TalkBack o contraste en Android físico/emulador.
10. Firestore `(default)` está creado en `nam5` (EE. UU.) y las reglas están publicadas. Falta habilitar Email/Password, autenticar un usuario de prueba con membresía activa y demostrar lectura/escritura desde Android.
11. Hay instrumentación Firebase Performance para la interpretación, sincronización y guardado local Room; no se han observado trazas desde dispositivo. La traza `quote_local_save` mide solo la escritura local, no la duración completa de atención.
12. No se midieron CPU, memoria RAM ni tráfico de red en un dispositivo real. La plantilla `qa/data/technical_efficiency_runs_template.csv` está vacía y los criterios no funcionales esperan aprobación.
13. El flujo actual de App Distribution entrega una APK Debug de ensayo; no hay certificado de publicación ni variante release distribuida. No se presenta como evidencia de producción.
14. App Distribution contiene el release de prueba `1.5.0 (8)`, pero ningún tester/grupo fue añadido. La instalación requiere asignar un tester autorizado.
15. Las pruebas de reglas en emulador pasaron (5/5). El teléfono físico `2409BRN2CY` (Android 16 / API 36) detectó e inició la app, pero la pantalla de sincronización indicó fallo de inicio de sesión; aún falta cuenta/membresía activa para la escritura real.
16. `npm audit` reporta cinco vulnerabilidades altas transitivas del SDK de Firebase en las dependencias de test; deben revisarse al actualizar el SDK/compatibilidad de Node.

Android documenta que `allowBackup=false` puede no impedir transferencias directas entre dispositivos en ciertos fabricantes si la app apunta a Android 12 o posterior; por eso el proyecto declara también exclusiones explícitas en reglas de respaldo en nube y transferencia. Debe confirmarse en los dispositivos objetivo. Referencia: [Android Developers — Auto Backup](https://developer.android.com/identity/data/autobackup).

## Capturas de interfaces verificadas

**Estado:** comprobación parcial en teléfono físico `2409BRN2CY` (Android 16 / API 36): la app de ensayo se instaló y abrió. No se conserva la captura de la pantalla de sincronización porque exponía campos de autenticación. Las capturas seguras de Inicio, Formulario, Resumen, Panel, Tarifas y Stock siguen pendientes; no se usan previews como evidencia ejecutada.

Para completar la evidencia visual, compilar e instalar el APK Debug en un emulador configurado o un dispositivo de prueba autorizado y capturar, como mínimo: Inicio, Formulario por servicio, Resumen, Panel, Tarifas y Stock. Anotar dispositivo, Android, `versionName`, estado de backend y si los datos son de prueba. No incluir datos reales de clientes.

## Uso en sustentación

Presentar por separado: (A) verificación técnica del software; (B) evaluación estadística de solicitudes reales pre/post, cuando se hayan recolectado conforme al protocolo; (C) limitaciones del prototipo. El éxito de compilación y las pruebas unitarias no equivalen a un efecto causal ni a mejora empresarial observada.
