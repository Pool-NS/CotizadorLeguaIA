# Informe de ejecución

- `gradlew.bat test --console=plain --no-daemon`: BUILD SUCCESSFUL. Compiló Debug y Release; 11 pruebas unitarias ejecutadas, 0 fallidas/omitidas (10 de dominio y 1 plantilla preexistente). Evidencia: `app/build/test-results/testDebugUnitTest/`. No hay pruebas instrumentadas de migración/UI. APK Debug generado en `app/build/outputs/apk/debug/app-debug.apk`, aún no probado en dispositivo.
- `gradlew.bat lint --console=plain --no-daemon`: BUILD SUCCESSFUL sobre el formulario de voz actual. Reporte `app/build/reports/lint-results-debug.html`; 37 advertencias y 0 errores: 21 GradleDependency, 7 UnusedResources, 3 AndroidGradlePluginVersion, 3 UseTomlInstead, 1 OldTargetApi, 1 RedundantLabel y 1 KaptUsageInsteadOfKsp.
- Pipeline predictivo sintético: ejecutado con `python backend/model/generate_and_train.py`; split 140/30/30. Métricas en `qa/prediction/metrics.json`. Dataset y casos contabilizados: 200 cada uno.
- Tarea Gradle `sonar` verificada con `help --task sonar`, plugin 7.5.0.8588 configurado; análisis real pendiente por ausencia de `SONAR_HOST_URL` y `SONAR_TOKEN`. Pruebas Android instrumentadas y prueba de migración Room: pendientes.

El build mostró además una advertencia del SDK acerca de SDK XML versión 4 y Kapt que retrocede el análisis de Kotlin 2.0 a lenguaje 1.9. La llamada Android de confirmación de credencial está obsoleta.
