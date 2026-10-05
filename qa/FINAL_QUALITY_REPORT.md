# Informe final de calidad (parcial)

Fecha: 2026-10-05. Este reporte diferencia inspección, ejecución y trabajo pendiente. No representa certificación ni conformidad ISO.

| Control | Resultado | Evidencia | Estado |
|---|---|---|---|
| Auditoría del ZIP y arquitectura | Inspección estática completada | `docs/CURRENT_AUDIT.md` | COMPLETADO |
| Regla fija heredada y acceso del jefe | Fórmula fija retirada; PIN local configurable por instalación y guardado como hash | `MainActivity.kt`, `PantallaLoginAdmin.kt` | COMPILADO; falta validar operación de inicialización por equipo |
| Validación de formulario | Requerimiento obligatorio; largo y ancho > 0 obligatorios; no se solicita altura; regreso a selección | `PantallaFormularioServicio.kt` | CAMBIO HECHO; UI test pendiente |
| Dominio cotización | Calculadora de parámetros, estados e impacto read-only | `domain/`, `PricingWorkflowTest.kt` | 6 unit tests aprobados |
| Voz y extracción estructurada | Reconocimiento Android; detecta Carpas/Toldos/Tapizado de moto y largo×ancho; revisión humana; sin altura | `MainActivity.kt`, `SpokenRequirementParser.kt`, `SpokenRequirementParserTest.kt` | 7 parser tests dentro de la suite de 15; PASS; dispositivo pendiente |
| Dashboard administrativo | Conteos comerciales con tarjetas y navegación a tarifas/inventario | `PantallaPanelAdmin.kt` | COMPILADO; prueba de uso pendiente |
| Tarifas por tamaño, material y variante | El jefe registra tarifa por servicio, largo×ancho, material y tipo de carpa; acentos/mayúsculas se normalizan para buscar la misma tarifa | `PantallaTarifas.kt`, `DimensionPriceKey`, `ParametroPrecioDao.kt`, `MainActivity.kt` | COMPILADO; cargar tarifas comerciales reales antes de cotizar |
| Materiales e inventario | Registrar ingreso por material/unidad, acumular stock y persistir movimiento en transacción | `PantallaInventario.kt`, `WorkflowDaos.kt` | COMPILADO; no descuenta consumos de producción ni define stock mínimo |
| Room/proforma/producción/predicción | Migraciones hasta Room v6; 5→6 agrega el material descrito a cotización; UI de proforma/producción/predicción y prueba instrumentada de migración pendientes | `WorkflowEntities.kt`, `WorkflowDaos.kt`, `AppDatabase.kt` | PARCIAL |
| Dataset sintético | Exactamente 200 filas, IDs únicos SYN-001…SYN-200, todos sintéticos | `qa/data/synthetic_quotes_200.csv` | EJECUTADO/VERIFICADO POR SCRIPT |
| Modelo predictivo sintético | Regresión logística, split 140/30/30; test accuracy 0.6667, precision 0.6875, recall 0.6875, F1 0.6875, ROC-AUC 0.6964; accuracy de validación 0.5000; todo sintético | `qa/prediction/metrics.json` | EJECUTADO; solo evidencia técnica sintética |
| Casos de prueba | Exactamente 200 casos únicos especificados; todos NOT_EXECUTED | `qa/iso29119/test_cases_200.csv` | DOCUMENTADOS; NO EJECUTADOS |
| Pruebas unitarias Android | Debug y Release: 16 aprobadas, 0 fallidas, 0 errores; incluye separación de tarifa por material y normalización de acentos | `app/build/test-results/testDebugUnitTest/`, `testReleaseUnitTest/` | PASS |
| Room migration/UI instrumentadas | Sin ejecución | `qa/iso29119/Test_Execution_Report.md` | PENDIENTE |
| ISO/IEC 25010 / SQuaRE / ISO 9001 / 29119 | Plantillas y evaluación inicial documentadas; evaluación formal incompleta | `qa/` | PARCIAL |
| SonarQube | Plugin Gradle 7.5.0.8588 y tarea `sonar` registrados; host/token ausentes, análisis no ejecutado | `build.gradle.kts`, `qa/sonarqube/README.md` | CONFIGURADO; GATE PENDIENTE |
| APK demostración | `assembleDebug` BUILD SUCCESSFUL tras voz, validaciones, resumen, dashboard, tarifas e inventario | `app/build/outputs/apk/debug/app-debug.apk` | GENERADO; no instalado en dispositivo |
| Compilación | Debug y Release compilaron durante `gradlew test` | `app/build/` | PASS |
| Lint | BUILD SUCCESSFUL; 37 advertencias, 0 errores | `app/build/reports/lint-results-debug.html` | PASS CON ADVERTENCIAS |
| Dispositivo / connectedAndroidTest | No ejecutado | No hay dispositivo configurado | PENDIENTE |

No se afirma que estén completados dashboard integral, pantallas para proforma/producción, generación de proforma desde UI, predicción integrada con Android, migración desde Room v1 ni 200 ejecuciones de pruebas. El PIN del jefe es local a cada instalación: el jefe debe configurarlo antes de entregar cada teléfono; no hay cuentas/roles sincronizados ni una forma de verificar identidad empresarial sin backend. El precio solo coincide por servicio, material, variante de carpa y tamaño exacto largo×ancho; un material no especificado o sin coincidencia aprobada deja la cotización pendiente. Inventario registra ingresos, pero no consumos de producción, compras o mínimos. Las métricas sintéticas no describen a Leguía y no se deben usar como resultados de tesis.
