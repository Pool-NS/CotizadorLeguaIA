# Revisión de calidad y seguridad (Qodana)

## Alcance

Este documento registra la respuesta del proyecto a las observaciones de Qodana recibidas el 26 de octubre de 2026. Qodana es análisis estático: sus resultados no prueban por sí solos que exista una vulnerabilidad explotable ni certifican conformidad con ISO/IEC 27001, ISO/IEC 25010 u otra norma.

## Decisiones de seguridad

- El manifiesto principal declara `android:usesCleartextTraffic="false"` y usa `network_security_config.xml` con HTTP desactivado. Solo `debug` agrega una excepción de texto claro para `10.0.2.2`, el alias del emulador Android hacia el host; Release y teléfonos físicos requieren HTTPS. Esta excepción de desarrollo debe revisarse como hotspot, no ampliarse a todos los dominios.
- Las copias de seguridad Android permanecen deshabilitadas con `android:allowBackup="false"`. Las reglas XML están vacías para no incluir datos personales, cotizaciones, credenciales ni preferencias si en el futuro cambia esa política.
- La compilación `release` activa R8 (`isMinifyEnabled = true`) y usa las reglas ProGuard del módulo. El artefacto de producción debe compilarse y probarse en un dispositivo antes de distribuirse.
- Las claves de Gemini, Sonar/Qodana, GitLab y otros servicios son secretos. Deben residir en el almacén de secretos del proveedor o en archivos locales ignorados por Git; si se compartieron públicamente, se revocan y se rotan.

## Correcciones aplicadas a partir del reporte

- Se retiró el `TODO` de las reglas de extracción de datos.
- Se centralizaron formatos numéricos y monetarios para evitar llamadas marcadas como obsoletas por el analizador.
- Se extrajeron validación y construcción de la cotización del callback de la pantalla de formulario.
- Se reemplazaron textos repetidos por constantes y se retiró un parámetro sin uso.
- Se declaró funcional la interfaz de un solo método de IA y se construye la URL del backend mediante `URI`.
- Se documentaron respuestas HTTP del endpoint y se simplificó el mapeo de errores del proveedor.
- Se activó minificación de `release` y se describió la tarea de cobertura JaCoCo.
- Los generadores de datos usan aleatoriedad reproducible porque producen demostraciones sintéticas; no generan contraseñas, tokens, claves ni valores criptográficos.
- Se inyectó el dispatcher de IO en el cliente HTTP, se agrupó el formulario de tarifa en un objeto para reducir parámetros y se descompuso el generador del dashboard para bajar complejidad cognitiva sin cambiar la semilla ni las filas producidas.
- La prueba de configuración sin clave crea el objeto de solicitud fuera de `assertRaises`, dejando dentro del bloque solo la operación que se espera que lance la excepción.

## Interpretación de los hallazgos restantes

Qodana/Sonar pueden presentar la excepción HTTP de `debug` y el uso de PRNG en los generadores de datos sintéticos como *security hotspots*. El PRNG conserva una semilla fija para que el dataset sea reproducible; no se usa para secretos ni autenticación. Revisa y marca el hotspot como revisado con esta justificación en la interfaz del análisis; no cambies a aleatoriedad criptográfica ni alteres la semilla para forzar una nota.

SonarQube for IDE también puede incluir `.gradle/8.9/dependencies-accessors/**/sources/LibrariesForLibsInPluginsBlock.java` cuando se analiza toda la raíz del proyecto. Es código generado por Gradle, no código fuente mantenido; no lo edites. En modo independiente, agrega `.gradle/**` en **Settings > Tools > SonarQube for IDE > File Exclusions**. En modo Connected, las exclusiones locales no se aplican; el análisis del servidor debe limitar el alcance al código fuente y excluir solo cachés/artefactos generados, sin excluir carpetas de la aplicación.

La configuración de SonarScanner del proyecto declara `sonar.sources=app/src/main/java` y excluye explícitamente `.gradle/**`; la exclusión no reduce el alcance de código fuente de la app porque la caché ya está fuera de `sonar.sources`. Esta configuración aplica a los análisis que usen el plugin Gradle o `sonar-project.properties`, no cambia los ajustes de SonarQube for IDE ni los de una instancia remota configurada por separado.

## Diagnóstico de GitHub Actions

El correo de Actions del 26 de octubre de 2026 reportó dos causas independientes: el build Android falló por conflicto entre los atributos `networkSecurityConfig` del manifiesto principal y el de `debug` (corregido con `tools:replace` en el manifiesto `debug`); CodeQL terminó el análisis pero no pudo subir SARIF porque GitHub indica que Code Scanning no está habilitado en el repositorio. La captura de Settings muestra que GitHub ofrece Wiki solo con una mejora de plan o haciendo público el repo, lo que indica que el repositorio es privado y el plan actual no habilita Code Scanning. Para no cambiar la visibilidad ni esconder errores, el workflow conserva el análisis CodeQL, guarda los SARIF como artefactos descargables por 14 días y no intenta subirlos a la vista Security. Si en el futuro se habilita un plan compatible o el repo se hace público deliberadamente, se puede cambiar `upload: never` y retirar la carga de artefactos para publicar los resultados en Code Scanning.

El fallo del CodeQL Java/Kotlin al compilar estaba relacionado con el mismo conflicto del manifiesto. El job CodeQL de Python analizó los archivos y falló al cargar resultados por la disponibilidad de Code Scanning; el correo no muestra vulnerabilidades detectadas. Con la nueva salida como artefacto, los SARIF se descargan desde **Actions > ejecución > Artifacts** y pueden abrirse con un visor compatible con SARIF.

Las alertas de complejidad cognitiva en `MainActivity`, formulario, login, resumen y tarifas requieren refactorizaciones graduales con revisión funcional. La eliminación de una alerta por bajar el umbral o excluir archivos no constituye una mejora de calidad.

## Criterio de cierre

La corrección se considera revisada cuando el pipeline ejecuta Qodana sobre el commit actualizado, no quedan errores de compilación, los *security hotspots* están revisados y las funciones de cotización, autenticación, inventario y modo offline se verifican manualmente. El resultado de una ejecución anterior no valida cambios posteriores.
