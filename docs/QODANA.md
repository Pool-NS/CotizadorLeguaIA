# Análisis estático con Qodana

El repositorio incluye `qodana.yaml` para usar el perfil recomendado de Qodana Community for Android (`qodana-jvm-android`, versión 2026.2). El workflow `.github/workflows/qodana.yml` analiza `main`, pull requests y ejecuciones manuales en modo contenedor; guarda el reporte como artefacto `qodana-report` en la ejecución de GitHub Actions. Para ejecutar este linter desde la CLI local se necesita Docker; este equipo no lo tiene instalado.

## Ejecutar desde Android Studio

1. Abre la raíz del repositorio en Android Studio.
2. Selecciona **Tools → Qodana → Try Code Analysis with Qodana**.
3. Usa el perfil recomendado y ejecuta el análisis.
4. Revisa hallazgos por severidad en la pestaña Qodana de **Problems**; corrige problemas confirmados y vuelve a analizar.
5. Si se crea un `qodana.sarif.json`, revisa el contenido antes de agregarlo al repositorio.

## Revisar el análisis en GitHub

Ve a **Actions → Qodana → última ejecución → Artifacts → qodana-report** y descarga el reporte. Para publicarlo en Qodana Cloud se necesita enlazar el proyecto y configurar un `QODANA_TOKEN` como secreto de GitHub; ese token no debe escribirse en el YAML.

Qodana es análisis estático. No sustituye pruebas funcionales, pruebas en dispositivos, auditoría de dependencias, DAST/pentest, revisión de permisos ni un proceso de certificación ISO. El workflow está configurado, pero el resultado de la primera ejecución en GitHub debe revisarse antes de declarar aprobado el análisis o establecer un Quality Gate.

Referencias oficiales: [Qodana para Java/Kotlin/Android](https://www.jetbrains.com/help/qodana/jvm.html), [Qodana en GitHub Actions](https://www.jetbrains.com/help/qodana/github.html), [plugin de Qodana para IDEs JetBrains](https://www.jetbrains.com/help/qodana/qodana-ide-plugin.html).
