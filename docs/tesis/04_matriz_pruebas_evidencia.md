# Matriz de requisitos, pruebas y evidencia

## Requisitos y trazabilidad

| Requisito / indicador | Módulo o archivo | Prueba disponible | Evidencia ejecutada en esta revisión | Estado |
|---|---|---|---|---|
| Interpretar requerimiento y devolver campos estructurados con revisión humana | `BackendGenerativeAiClient.kt`, `backend/api/app.py`, `PantallaFormularioServicio.kt` | `AiErrorMessageTest`; `backend/api/tests/test_app.py` | Android unit test ejecutado; llamada real Gemini pendiente | Parcial; depende de conectividad/cuota; humano revisa |
| Medidas y reglas de precio existentes | `domain/Pricing.kt`, `PantallaFormularioServicio.kt`, `ParametroPrecioDao.kt` | `PricingWorkflowTest`, `SpokenRequirementParserTest` | `testDebugUnitTest` | Cubierto en lógica aislada; falta prueba UI/dispositivo y validación comercial |
| Guardar solicitud, precio y marcas temporales | `CotizacionEntity.kt`, `CotizacionDao.kt`, `PantallaResumenPrecio.kt` | Falta prueba de Room instrumentada | Compilación y unit tests; persistencia real no instrumentada | Parcial |
| Exactitud auditada y errores atribuibles | `RegistroErrorEntity.kt`, `RegistroErrorDao.kt`, protocolo de tesis | Falta flujo funcional conectado y prueba | No se marcó como medido | Pendiente de definir/capturar con instrumento aprobado |
| Cumplimiento declarado por operador | `PantallaResumenPrecio.kt`, `CotizacionEntity.kt`, `PantallaPanelAdmin.kt` | Cobertura de UI pendiente | Lógica revisada; fórmula y denominador visibles | Parcial; no reemplaza observación independiente |
| Inventario: saldo, presentación, ingreso, salida y ajuste | `PantallaInventario.kt`, `StockMovementRules.kt`, `WorkflowDaos.kt` | `StockMovementRulesTest` | `testDebugUnitTest` | Reglas unitarias; persistencia Room/UI manual pendiente |
| Exportar datos anonimizados y versionados | `QuoteCsvExport.kt`, `MainActivity.kt` | `QuoteCsvExportTest` | `testDebugUnitTest` | CSV probado; debe validar instrumento y privacidad antes de compartir |
| Registrar trazas de interpretación, guardado Room y sincronización Firestore | `FirebasePerformanceTrace.kt`, `BackendGenerativeAiClient.kt`, `CotizadorViewModel.kt`, `FirebaseCloudRepository.kt` | Pruebas unitarias del cliente y compilación | Compilación `FIREBASE_ENABLED=true`; app instalada en teléfono y arranque revisado; aún sin sesión/membresía Firebase ni traza nube visible | Implementado; persistencia remota desde Android pendiente |
| Restringir acceso y esquema de datos en Firestore | `firestore.rules`, `firebase-tests/rules.test.js` | Emulador Firestore: 5 casos | 5 aprobados; reglas e índices publicados en `cotizador-leguia-poolns-2026` | Reglas vigentes; la prueba de emulador no sustituye lectura/escritura desde Android |
| Capturar CPU, RSS/memoria y bytes de red | `qa/data/technical_efficiency_runs_template.csv`, `qa/firebase/plan-medicion-eficiencia.md` | Protocolo con Profiler/Perfetto | No ejecutado en dispositivo físico | Pendiente de medición real y aprobación de umbrales |
| Promedio y mediana de tiempo | `QuoteDurationMetrics.kt`, `PantallaPanelAdmin.kt` | `QuoteDurationMetricsTest` (muestra par, tiempos no válidos, vacío) | `testDebugUnitTest` | Función pura comprobada; marca temporal sigue siendo declaración operativa |
| Navegación y accesibilidad | `MainActivity.kt`, pantallas Compose | Test instrumentado Compose aún no implementado | App 1.5.0 (8) abrió en teléfono físico `2409BRN2CY`, Android 16 / API 36; no se vieron cierres en el muestreo de logs | Prueba de arranque parcial; revisión completa de pantallas y TalkBack pendiente |
| Seguridad local del panel | `AdminPinHasher.kt`, `PantallaLoginAdmin.kt` | `AdminPinHasherTest` | `testDebugUnitTest` | Lógica local probada; no es identidad de red |
| Actualización Room | `AppDatabase.kt` | Sin test de migración instrumentado | Compilación; no se ejecutó migración con BD antigua | Pendiente |
| Calidad Android | workflow `quality-security.yml` | Unit tests, lint, build | Run local indicado abajo | Ejecutado localmente; Qodana/Sonar remoto no se ejecutó aquí |

`qa/iso29119/test_cases_200.csv` contiene casos documentados; su nombre no significa que 200 ejecuciones se hayan realizado. TDD previo no se afirma sin evidencia histórica.

## Resultado de ejecución local (25-11-2026)

Resultados sobre la rama `codex/firebase-integration`, con proyecto Firebase Android configurado localmente. El archivo `google-services.json` está excluido de Git:

- Android `testDebugUnitTest`: **34 pruebas, 0 fallos y 0 errores**, según XML de Gradle.
- Android `lintDebug`: **BUILD SUCCESSFUL**.
- Android `assembleDebug`: **BUILD SUCCESSFUL**; APK generado en `app/build/outputs/apk/debug/app-debug.apk` (19 299 169 bytes). Es un APK de depuración para ensayo, no una publicación de producción.
- Backend: el comando local `python -m unittest discover -s tests -v` no produjo salida ni finalizó en el tiempo disponible y se interrumpió. Por ello **no se reporta como aprobado**. La ejecución de tests backend del workflow remoto no forma parte de esta comprobación local.
- Dispositivo físico: modelo `2409BRN2CY`, Android 16 / API 36. APK `1.5.0 (8)` instalada y actividad principal abierta. La pantalla de sincronización mostró un error genérico de inicio de sesión; no se confirmó lectura/escritura remota. La captura temporal no se conservó porque contenía campos de autenticación. No se ejecutó Android Profiler/Perfetto.

La prueba del proveedor Gemini es simulada en los tests backend; no prueba disponibilidad de red, clave, facturación o modelo en el proyecto del usuario. No se realizó una llamada real a Gemini en esta verificación.

## Estado de Firebase

El proyecto `cotizador-leguia-poolns-2026`, la app Android y Firestore `(default)` están creados. Firebase Console confirma región multirregional `nam5` (EE. UU.). Reglas e índices se publicaron correctamente y cinco pruebas de reglas pasaron en el emulador. Email/Password está habilitado; existe una cuenta de prueba con membresía activa en la organización y fue agregada como tester a App Distribution. **Lectura/escritura remota verificada:** el 25-11-2026 se observó en Firebase Console el documento `organizations/cotizador-leguia-poolns-2026/quotes/a236c6c4-dcc8-46c4-9f87-77215a1629c8`, generado por Android (`source=android_room`, versión `1.5.0`) con timestamp `uploadedAt` del servidor. Es una prueba funcional individual y no una medición de campo ni resultado estadístico. La traza local de guardado sigue midiendo Room local, no debe confundirse con el tiempo de sincronización remota.

El APK `1.5.0 (8)` de ensayo se distribuyó mediante Firebase App Distribution (release `48gqqn14hcu58`) al tester `pieropoolc+cotizador-test@gmail.com`; Firebase CLI confirmó la asignación. `npm audit` detectó cinco vulnerabilidades altas transitivas en el SDK de Firebase empleado por las pruebas del emulador; no se aplicó una actualización forzada que pudiera cambiar versiones mayores. La suite de reglas sí completó con Node 22.18 pese al aviso de motor; el workflow especifica Node 24.12. La contraseña de prueba se entregó al propietario por separado y no se versiona.
