# Preparación para la evaluación automatizada de Quantum

Este documento adapta el manual del estudiante de evaluación automatizada de calidad al repositorio Cotizador Leguía. Seguir el manual no significa que el pipeline o la nota hayan sido aprobados.

## Archivos y estado

| Requisito del manual | Estado en este repositorio | Acción necesaria |
|---|---|---|
| `.gitlab-ci.yml` en la raíz, incluyendo `root/laboratorio-quality` | Preparado | Coincide con el archivo `gitlab-ci.yml` adjunto; validar el primer pipeline en el GitLab asignado. |
| `project.yml` en la raíz con curso y datos académicos | Preparado con los datos confirmados | Se usó la estructura del `project.yml` adjunto y se reemplazaron los datos de ejemplo por los del Cotizador Leguía y los datos académicos proporcionados. |
| `.gitignore` excluye dependencias, build y secretos | Preparado | Revisar `git status` antes de cada commit; no subir `.env`, tokens ni claves. |
| Reporte JaCoCo para Kotlin/Android | Configurado | Ejecutar `gradlew.bat testDebugUnitTestCoverage`; revisar `qa/coverage/jacoco.xml` y registrar el reporte en Git. |
| Reporte de cobertura Python | Pendiente de habilitar | Agregar `pytest-cov` como dependencia de desarrollo y generar un XML separado si el evaluador analiza el backend Python. |
| SonarQube/Quality Gate | No verificado en Quantum | Consultar el pipeline y el reporte real. La configuración local de Sonar no demuestra una nota ni un Quality Gate aprobado. |
| Push al repositorio académico GitLab | Pendiente | Crear el proyecto en Quantum, conectar su URL HTTPS y autenticar Git usando el gestor de credenciales del equipo. |

## Medición local actual

En la última ejecución local se ejecutaron 22 pruebas unitarias Android: 22 aprobadas y 0 fallidas. JaCoCo midió 214 de 2,091 líneas: 10.23 %. Según la rúbrica del manual, este valor está bajo 30 % y obtiene 0/4 puntos en cobertura. El reporte está en `qa/coverage/jacoco.xml`. Este resultado no representa la nota total ni un resultado de Quantum; conviene priorizar pruebas adicionales de lógica de negocio antes de presentar.

## Generar cobertura Android

Desde PowerShell en la raíz del proyecto:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat testDebugUnitTestCoverage
```

El XML se escribe en `qa/coverage/jacoco.xml`, fuera de `build/`, para que el archivo de cobertura pueda incluirse en el commit. No cambies los umbrales ni excluyas carpetas de código para inflar la nota. Si la cobertura real no llega a 80 %, amplía pruebas sobre lógica de negocio y reporta el valor observado.

## Datos aún necesarios

`project.yml` está rellenado con los datos proporcionados: curso “Taller de Investigación II”, docente Maglioni Arana Caparachin y correo institucional, e integrante Piero Pool Chaupis Leguia. El código 75177414 se tomó del inicio del correo institucional, ya que indicaste que podía identificarse de esa forma. La ponderación está en 100 %, como confirmaste. El archivo descargado de ejemplo tenía datos de otro proyecto, otra persona, otro curso y 80 %; no se copiaron esos valores.

## Token y remoto

Un token de GitLab publicado en el chat debe revocarse desde Access Tokens y no reutilizarse. No se almacena en URL remota, archivos del proyecto, scripts ni historial de shell. Cuando se cree el repositorio académico, configura su URL HTTPS como remoto separado y usa Git Credential Manager para pegar un token nuevo en el diálogo local de contraseña. Los permisos deben limitarse a los scopes necesarios y con expiración.

El remoto actual `origin` continúa siendo GitHub (`Pool-NS/CotizadorLeguaIA`). No se reemplaza hasta tener la URL del proyecto Quantum para evitar afectar ese repositorio.

