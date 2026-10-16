# SonarQube

El plugin Gradle oficial `org.sonarqube` 7.5.0.8588 está aplicado al proyecto raíz; el task `sonar` se verificó con `gradlew help --task sonar`. La tarea Android `testDebugUnitTestCoverage` genera JaCoCo XML en `qa/coverage/jacoco.xml`, referenciado desde `build.gradle.kts` y `sonar-project.properties`. El manual de Quantum indica que su pipeline no ejecuta pruebas; por eso el reporte debe generarse localmente e incluirse en el commit de entrega.

Para ejecutar contra un servidor real en PowerShell, configura sin versionar el token:

```powershell
$env:SONAR_HOST_URL = "https://sonarqube.ejemplo"
$env:SONAR_TOKEN = "<token personal>"
.\gradlew.bat testDebugUnitTestCoverage lintDebug sonar
```

La ejecución de análisis no se hizo: este entorno no tiene `SONAR_HOST_URL` ni `SONAR_TOKEN` y no hay instancia conocida. `BUILD SUCCESSFUL` de Gradle/lint no significa que el Quality Gate Sonar pase.

Objetivos solicitados por el proyecto: cero issues nuevas abiertas; 100% de Security Hotspots nuevos revisados; cobertura nueva ≥80%; duplicación nueva ≤3%. Configura un Quality Gate con esos umbrales en el servidor y, si la instancia soporta AI Code Quality Gate, evalúalo por separado. No se afirman métricas ni Quality Gate sin reporte del servidor.
