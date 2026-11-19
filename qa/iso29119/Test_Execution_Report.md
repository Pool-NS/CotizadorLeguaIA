# Informe de ejecución de pruebas

## Android — 2026-11-12

- Comando: `gradlew.bat testDebugUnitTest lintDebug assembleDebug --console=plain`.
- Resultado: **BUILD SUCCESSFUL**.
- Pruebas unitarias: 30 aprobadas, 0 fallidas, 0 omitidas. Reportes en `app/build/test-results/testDebugUnitTest/`.
- Lint Debug: completado. El reporte contiene advertencias de target SDK y actualizaciones de dependencias; no detuvieron el build. Archivo: `app/build/reports/lint-results-debug.html`.
- APK Debug generada: `app/build/outputs/apk/debug/app-debug.apk`, versión 1.5.0, `versionCode` 8.
- Se añadió cobertura unitaria para mensajes seguros de error de IA y reglas de movimiento de inventario.
- Se añadió cobertura unitaria para CSV anonimizado y campos de tesis que deben permanecer sin verificar.

## Límites de esta ejecución

- No se ejecutaron pruebas instrumentadas de Room, migración, navegación ni interfaz en dispositivo/emulador.
- No se hizo llamada externa a Gemini ni prueba de backend desplegado.
- SonarQube, Qodana y el workflow CodeQL de GitHub no se ejecutaron en esta verificación local.
- No se midió contraste/accesibilidad visual en una matriz de dispositivos.
- Los 200 casos de `qa/iso29119/test_cases_200.csv` son casos especificados y no deben confundirse con 200 ejecuciones realizadas.

## Ejecución histórica

Los registros anteriores del 2026-10-12 describían una versión previa del producto y conteos de pruebas distintos. Para la APK actual, usar el resultado del 2026-11-12 y los reportes generados en el directorio `app/build/`.
