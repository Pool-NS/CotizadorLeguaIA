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
| Promedio y mediana de tiempo | `QuoteDurationMetrics.kt`, `PantallaPanelAdmin.kt` | `QuoteDurationMetricsTest` (muestra par, tiempos no válidos, vacío) | `testDebugUnitTest` | Función pura comprobada; marca temporal sigue siendo declaración operativa |
| Navegación y accesibilidad | `MainActivity.kt`, pantallas Compose | Test instrumentado Compose aún no implementado | No hay dispositivo/AVD conectado | Pendiente de prueba visual y TalkBack |
| Seguridad local del panel | `AdminPinHasher.kt`, `PantallaLoginAdmin.kt` | `AdminPinHasherTest` | `testDebugUnitTest` | Lógica local probada; no es identidad de red |
| Actualización Room | `AppDatabase.kt` | Sin test de migración instrumentado | Compilación; no se ejecutó migración con BD antigua | Pendiente |
| Calidad Android | workflow `quality-security.yml` | Unit tests, lint, build | Run local indicado abajo | Ejecutado localmente; Qodana/Sonar remoto no se ejecutó aquí |

`qa/iso29119/test_cases_200.csv` contiene casos documentados; su nombre no significa que 200 ejecuciones se hayan realizado. TDD previo no se afirma sin evidencia histórica.

## Resultado de ejecución local

Resultados de la ejecución local del 19-11-2026, sobre la rama `codex/tesis-audit`, implementación `cc07eab`:

- Android `testDebugUnitTest`: **34 pruebas, 0 fallos y 0 errores**, según los XML de Gradle bajo `app/build/test-results/testDebugUnitTest/`.
- Android `lintDebug`: **BUILD SUCCESSFUL, 0 errores y 32 advertencias**. Las advertencias incluyen nivel de `targetSdk`, dependencias con versiones más recientes disponibles y uso de KAPT donde Room también soporta KSP. No se ocultaron mediante exclusiones.
- Android `assembleDebug`: **BUILD SUCCESSFUL**; APK generado en `app/build/outputs/apk/debug/app-debug.apk` (10 267 928 bytes). Es un APK de depuración, no un paquete firmado de publicación.
- Backend: el comando local `python -m unittest discover -s tests -v` no produjo salida ni finalizó en el tiempo disponible y se interrumpió. Por ello **no se reporta como aprobado**. La ejecución de tests backend del workflow remoto no forma parte de esta comprobación local.
- Capturas: no disponibles; no se detectó ningún teléfono conectado ni AVD configurado.

La prueba del proveedor Gemini es simulada en los tests backend; no prueba disponibilidad de red, clave, facturación o modelo en el proyecto del usuario. No se realizó una llamada real a Gemini en esta verificación.

## Estado de la rama Firebase

La rama `codex/firebase-integration` agrega autenticación y sincronización opcional con Firestore, pruebas de reglas en emulador y un flujo manual de distribución. Al registrar esta nota no se ha compilado esa rama: el equipo carece de dependencias Gradle completas y el acceso de red a repositorios Maven no terminó. La prueba local de reglas también está pendiente; las dependencias actuales requieren Node.js 24.12 o superior y el equipo tiene Node 22.18. El workflow CI se configuró con Node 24.12 para ejecutarla en GitHub Actions.

Firebase CLI no tiene autenticación local y el repositorio carece de `google-services.json`, ID de proyecto y cuentas de servicio; por ello no se desplegaron reglas ni se distribuyó un APK. El emulador/CI puede comprobar las reglas antes de usar un proyecto real. Estos estados son pendientes, no aprobaciones ni fallos funcionales medidos.
