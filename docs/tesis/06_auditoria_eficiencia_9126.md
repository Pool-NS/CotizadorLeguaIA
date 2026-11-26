# Auditoría de eficiencia para el Anexo 7

**Rama revisada:** `codex/firebase-integration`  
**Versión local:** Android `1.5.1` (`versionCode 9`)  
**Fecha:** 26-11-2026

## Alcance verificado

La app combina Android/Kotlin con Jetpack Compose, Room/SQLite para operación local, Firebase Authentication y Firestore para registros minimizados, Firebase Performance para trazas agregadas, Crashlytics para fallos saneados y FastAPI/Gemini para interpretación opcional. La ubicación de Firestore es `nam5` (EE. UU.). La integración conserva cotizaciones locales si Firebase no está disponible.

Una cotización de prueba guardada por Android en la versión 1.5.0 (8) fue observada directamente en Firebase Console. El documento tiene `uploadedAt` del servidor y datos minimizados. Esto comprueba una escritura individual de ensayo, no el funcionamiento completo de lectura entre sesiones, el despliegue productivo ni resultados de tesis.

La versión 1.5.1 (9) incorpora consulta autenticada de las últimas 50 cotizaciones y una traza de lectura, además del registro de duración monotónica del proceso. El código compila, pero no se instaló/probó en el móvil en esta revisión. No se afirmará que la lectura nueva haya ocurrido hasta observarla desde Android.

## Modelo de calidad y cautela metodológica

La matriz del curso solicita ISO/IEC 9126. La edición 9126-1:2001 está retirada; este documento registra el modelo solicitado en clase y no declara certificación. En el esquema publicado de 9126, eficiencia comprende comportamiento temporal y utilización de recursos; la idea de “cumplimiento de la eficiencia” puede tratarse como criterio de evaluación del conjunto de requisitos no funcionales que el docente apruebe. Confirma con el docente si sus materiales de clase la presentan como una tercera subcaracterística. Ninguna meta numérica propuesta aquí proviene de la norma.

## Matriz de medición

| Eje de evaluación | Qué puede medirse | Implementación/evidencia actual | Fuente y pendiente |
|---|---|---|---|
| Comportamiento temporal | Inicio de app, cálculo local, interpretación Gemini, consulta y sincronización Firestore, y tiempo total de atención | Firebase Performance automático/`ai_interpretation`, `quote_calculation`, `cloud_quote_query`, `cloud_quote_sync`; Room guarda `durationMillis` | La telemetría nueva requiere observación en teléfono y Firebase Console. Inicio requiere medición repetible con Macrobenchmark/Profiler. |
| Utilización de recursos | CPU, RSS/memoria y red TX/RX durante un escenario fijo | No hay valores de CPU/RAM/red medidos ni guardados por la app | Obtener con Android Studio Profiler/Perfetto y Network Profiler; registrar modelo, API, build, escenario, red, batería, versión y commit. |
| Cumplimiento de objetivos | Porcentaje de operaciones válidas dentro de umbrales acordados | No calculado | Requiere que el asesor apruebe los umbrales, método, repeticiones, exclusiones y denominador. |

### Definición de `durationMillis`

`durationMillis` mide desde el inicio del flujo de cotización en Android hasta el instante en que el operador marca que comunicó el precio. El reloj usado para la diferencia es `SystemClock.elapsedRealtime()`, que es monotónico. Las marcas de fecha de calendario permanecen para trazabilidad, pero no se restan para calcular la duración. El valor queda `null` si el operador no confirma la comunicación o si el intervalo guardado no es válido. Por ello no mide cálculo aislado ni escritura en Firestore. La comunicación es una declaración operativa, no una verificación independiente.

## Propuestas para aprobación previa

Los siguientes objetivos son candidatos para discusión con el docente. **No están aprobados, no se ejecutaron y no deben presentarse como resultados.** Antes de usarlos, fija dispositivo objetivo, red, tamaño de muestra, repeticiones, percentil, preparación/caché, tratamiento de errores y regla de exclusión.

| Medida candidata | Objetivo inicial a revisar | Evidencia prevista |
|---|---:|---|
| Inicio en frío hasta UI utilizable | p95 ≤ 3 s | Macrobenchmark o Android Profiler |
| Cálculo local con tarifa disponible | p95 ≤ 250 ms | traza `quote_calculation` |
| Consulta de hasta 50 cotizaciones por Wi-Fi estable | p95 ≤ 3 s | traza `cloud_quote_query` |
| Interpretación IA con solicitud fija | p95 ≤ 15 s | traza `ai_interpretation`; errores informados por separado |
| Sincronización de una cotización | p95 ≤ 5 s | traza `cloud_quote_sync` |
| Pico RSS durante 10 cotizaciones consecutivas | ≤ 250 MiB | Profiler/Perfetto |
| CPU de proceso durante escenario activo repetible | promedio ≤ 35% de un núcleo en 60 s | Perfetto, identificando condiciones exactas |
| Tráfico de consulta de hasta 50 registros | RX ≤ 256 KiB; TX/RX reportados por separado | Network Profiler, condición de caché declarada |
| Cumplimiento de metas | ≥ 90% de operaciones válidas | `100 × válidas que cumplen / válidas evaluadas` |

El resultado se calcula por escenario y se muestra junto al numerador y denominador. No contar reintentos como observaciones independientes sin aprobación metodológica. Las métricas Firebase agregadas no sustituyen mediciones de CPU, memoria ni la duración del proceso comercial.

## Registros, privacidad y límites

- Las cotizaciones cloud no almacenan texto libre del cliente, nombres, precios ni contraseñas. UID de Firebase y medidas son seudonimizados, no completamente anónimos.
- La exportación CSV local incluye ID de cotización aleatorio, versión, marcas, duración y estado; no contiene credenciales. Antes de usarla en la tesis, confirmar consentimiento, anonimización y protocolo con el asesor.
- Firebase Performance presenta agregados del SDK y muestreo del servicio; no constituye un CSV fila por fila para SmartPLS ni instrumento de CPU/RAM.
- Las reglas de Firestore rechazan cambios y borrados de cotizaciones. Los cambios de estado posteriores a la creación se conservan localmente y no se replican a Firestore. Sincronizar estados es trabajo pendiente y requeriría diseño seguro de permisos y pruebas.
- FastAPI/Gemini es un servicio distinto de Firestore. La disponibilidad de interpretación depende del despliegue, conexión, cuota y clave del servidor; ninguna clave Gemini se distribuye en Android.
- Una versión Debug en App Distribution es para ensayo, no evidencia de publicación de producción. La subida de la versión 1.5.1 requiere confirmación de Firebase; no se asume publicada.

## Resultados verificables de esta revisión

| Verificación | Resultado | Límite |
|---|---|---|
| Unit tests Android | `testDebugUnitTest` terminó correctamente | No prueba comportamiento de red o UX en teléfono. |
| Lint | `lintDebug` terminó correctamente | No equivale a análisis Sonar/Qodana ni certificación ISO. |
| Ensamblado | `assembleDebug` terminó correctamente para 1.5.1 (9) | APK Debug, no release de producción. |
| Compilación de UI instrumentada | `compileDebugAndroidTestKotlin` terminó correctamente | El test no corrió: Android canceló la instalación con `INSTALL_FAILED_USER_RESTRICTED` (cero casos ejecutados). |
| Firestore Rules | 5 pruebas del emulador aprobaron en la verificación previa | No sustituye prueba del proyecto cloud. |
| Escritura Firestore desde Android | Un documento 1.5.0 (8) visible en consola | Una sola operación de prueba, no muestra ni resultado estadístico. |
| Lectura Firestore desde Android 1.5.1 | Pendiente | Debe instalarse y consultar con una cuenta autorizada. |
| CPU, RSS, TX/RX y cumplimiento | No medido | Requiere Profiler/Perfetto y protocolo aprobado. |
| Distribución Firebase 1.5.1 | Pendiente de confirmación | La distribución previa 1.5.0 (8) sí quedó asignada al tester. |

## Procedimiento reproducible pendiente

1. Obtener aprobación del asesor para el diseño, metas, tamaño de muestra y condiciones.
2. Instalar 1.5.1 por App Distribution o en dispositivo de prueba autorizado; iniciar sesión con cuenta de prueba.
3. Hacer una cotización de ensayo sin información personal, marcar comunicación y esperar sincronización.
4. Consultar desde la pantalla de nube; verificar que aparezca el conteo y la cotización. Repetir con sesión autorizada prevista por el protocolo y confirmar acceso denegado a usuario no miembro.
5. Guardar captura saneada de Firestore/Performance y registrar hora, versión, commit y dispositivo. No incluir correo, UID, contraseña o texto de cliente en evidencia pública.
6. Ejecutar escenarios de rendimiento aprobados en variante profileable/release cercana y capturar CPU, memoria y red con herramientas Android. Mantener archivos crudos.
7. Exportar CSV local, auditar valores ausentes y calcular descriptivos e intervalos solo sobre datos observados. Separar fases pre y post de acuerdo con el protocolo, sin mezclarlas con pruebas técnicas.

