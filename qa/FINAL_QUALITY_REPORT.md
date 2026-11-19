# Informe de calidad (estado parcial)

Última verificación local: 2026-11-19. Este documento registra comprobaciones ejecutadas; no afirma certificación ni conformidad ISO.

## Resultado de la verificación

| Control | Resultado actual | Pendiente |
|---|---|---|
| Pruebas unitarias Android | `testDebugUnitTest`: 34 pruebas aprobadas, 0 fallidas y 0 errores | Pruebas UI e instrumentadas en dispositivo/emulador |
| Compilación Debug | `assembleDebug`: BUILD SUCCESSFUL; APK 1.5.0/code 8 generado | Instalación y recorrido visual manual |
| Android Lint | `lintDebug`: BUILD SUCCESSFUL, 0 errores y 32 advertencias | Avisos sobre `targetSdk = 35`, AGP/dependencias con versiones recientes y KAPT; no es una evaluación Sonar/Qodana |
| IA generativa | La app valida el JSON esperado, limita su tamaño y oculta IP/detalles técnicos en errores de red | No se hizo llamada externa a Gemini ni se verificó disponibilidad del backend en Render |
| Stock | Ingresos, salidas y ajustes con motivo, responsable, fecha, transacción y validación de unidad/saldo | Prueba Room instrumentada; no hay reglas aprobadas para descontar consumo al cotizar |
| Tarifas | Solo las reglas activas entran al cotizador; el panel consulta historial activo/inactivo | Cargar/revisar precios comerciales con el responsable |
| Exportación | CSV del periodo seleccionado, sin texto del cliente, operador ni código comercial; fase, exactitud y errores en blanco si no se verificaron | Revisar el archivo exportado y cotejar definiciones con el protocolo |
| Métricas de tesis | Tiempo de app y comunicación autorreportada; se distingue dato faltante histórico | Exactitud auditada, E1–E9 y comparación pretest/posttest según protocolo |
| ISO/IEC / SQuaRE | Prácticas y evidencias documentadas como referencias de evaluación | Auditoría formal, evidencia operativa suficiente y certificación si se pretende declarar conformidad |
| Backend Python | El intento local de `unittest discover` no finalizó ni produjo salida reproducible; no se marca como aprobado | Diagnosticar el entorno local y obtener reporte de pruebas |
| SonarQube / Qodana / CodeQL remoto | No se ejecutaron en esta verificación local | Ejecutar en el servicio con permisos y configuración habilitados |

## Alcance de medición

La base Room es local a cada teléfono; no consolida el negocio. El tablero aplica periodos 7 días, 30 días y todo el historial, y no presenta gráficos sin registros. Conversión comercial se calcula como concretadas / (concretadas + no concretadas). El tiempo mediano actual mide desde “Nueva cotización” hasta guardar; no reemplaza el cronometraje aprobado. “Precio comunicado” es una confirmación del operador, no evidencia independiente de que el cliente lo recibió. La exactitud no se infiere de ventas o correcciones de IA.

No se fabrican registros para SmartPLS, SPSS o jamovi. `qa/data/observed_quotes_template.csv` permanece como plantilla vacía para observaciones autorizadas; las simulaciones de 200 filas fueron retiradas y no son resultados de campo.

## Artefacto

- Debug: `app/build/outputs/apk/debug/app-debug.apk` (versión 1.5.0, `versionCode` 8).
- Reporte Lint: `app/build/reports/lint-results-debug.html`.
- Pruebas unitarias: `app/build/test-results/testDebugUnitTest/`.

La APK Debug es para instalación de prueba y no está firmada para publicación en una tienda.
