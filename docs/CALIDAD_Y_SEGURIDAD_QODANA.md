# Revisión de calidad y seguridad (Qodana)

## Alcance

Este documento registra la respuesta del proyecto a las observaciones de Qodana recibidas el 26 de octubre de 2026. Qodana es análisis estático: sus resultados no prueban por sí solos que exista una vulnerabilidad explotable ni certifican conformidad con ISO/IEC 27001, ISO/IEC 25010 u otra norma.

## Decisiones de seguridad

- La variante `debug` permite HTTP para conectar el emulador o una red local al backend de desarrollo. Esta configuración se encuentra en `app/src/debug/AndroidManifest.xml`; el manifiesto principal no habilita tráfico sin cifrar. La compilación `release` exige una URL `https://` mediante `aiBackendUrlRelease`.
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

## Interpretación de los hallazgos restantes

Qodana puede presentar tráfico HTTP de `debug`, copias de seguridad y uso de PRNG como *security hotspots*. Se deben revisar en la interfaz del análisis y dejar constancia de la justificación anterior; no se debe ocultar una alerta con supresiones sin revisar su contexto.

Las alertas de complejidad cognitiva en `MainActivity`, formulario, login, resumen y tarifas requieren refactorizaciones graduales con revisión funcional. La eliminación de una alerta por bajar el umbral o excluir archivos no constituye una mejora de calidad.

## Criterio de cierre

La corrección se considera revisada cuando el pipeline ejecuta Qodana sobre el commit actualizado, no quedan errores de compilación, los *security hotspots* están revisados y las funciones de cotización, autenticación, inventario y modo offline se verifican manualmente. El resultado de una ejecución anterior no valida cambios posteriores.
