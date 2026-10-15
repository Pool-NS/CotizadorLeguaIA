# Mejoras de interfaz, métricas y acceso administrativo

Fecha: 2026-10-15.

## Experiencia de uso

- Las pantallas secundarias comparten un encabezado compacto con una acción de regreso accesible, en lugar de botones “Volver” grandes y diferentes entre sí.
- El formulario conserva campos editables para cualquier medida y presenta un selector con medidas frecuentes del catálogo informado. Largo y ancho se pueden corregir después de elegir; altura sigue siendo opcional.
- Los campos de dimensiones usan teclado decimal. La selección de tamaño no aprueba tarifas: la app solo cotiza si existe una regla exacta activa para el servicio, material, variante y medida.
- El dashboard de prueba reduce textos repetidos y deja inventario, tarifas demo y casos de muestra dentro de secciones que se expanden bajo demanda.

## Gráficos y significado

| Vista | Visualización | Interpretación |
|---|---|---|
| Dashboard del jefe | Línea de 7 días | Cotizaciones locales por fecha de registro; si no hay actividad reciente, muestra valores cero. |
| Dashboard del jefe | Dona de estados | Pendientes, concretadas y no concretadas del teléfono actual. |
| Dashboard del jefe | Barras por servicio | Cantidad de solicitudes locales por servicio. |
| Demo de 200 registros | Dona de factibilidad | Recuento de resultados generados con reglas sintéticas. |
| Demo de 200 registros | Barras por servicio | Distribución de los 200 escenarios simulados. |

El dashboard real se calcula solo con la base Room de este dispositivo. No representa a todos los trabajadores y no sincroniza con la nube. Las gráficas sintéticas están rotuladas como demo; no deben reportarse como ventas o resultados empíricos de la empresa.

La conversión se calcula como `concretadas / (concretadas + no concretadas)`. Las solicitudes pendientes y las que no tienen resultado final quedan fuera del denominador. El “valor final registrado” suma cotizaciones que no están pendientes de precio; no se interpreta como ingreso o venta.

## Política de acceso

El acceso actual es un PIN numérico local de jefe, no una cuenta de identidad. Los nuevos PIN tienen entre 8 y 12 dígitos, quedan derivados con PBKDF2 y sal aleatoria, y el formulario bloquea durante 60 segundos tras cinco intentos fallidos. No exige mayúscula ni símbolo: ISO/IEC 27001 no impone una combinación universal de caracteres. Su control A.5.17 trata de la gestión organizacional de información de autenticación. ISO 9001 es una norma de gestión de calidad, no define reglas de caracteres para credenciales. Ninguna de estas referencias certifica la app.

Los PIN previos no se invalidan automáticamente. La app tampoco provee usuarios individuales ni autorización central; el backend y los datos Room continúan sin autenticación multiusuario compartida. Para ponerla en producción con trabajadores se requiere diseñar cuentas, roles, recuperación, protección de sesión, API autenticada y almacenamiento central. El PIN local no basta para proteger administración en una flota compartida.

## Lista de validación manual

1. Abrir Panel del jefe y volver al inicio usando la flecha; verificar que el control sea claro y accesible.
2. Abrir demo; comprobar que cada sección de inventario, tarifas y muestras se pueda expandir y contraer.
3. Revisar que todos los gráficos muestren sus etiquetas y que el demo identifique explícitamente datos sintéticos.
4. Registrar cotizaciones con medidas frecuentes, medida personalizada, coma decimal y altura vacía/indicada.
5. Crear un nuevo PIN de 8 dígitos, confirmar coincidencia, ingresar con el PIN correcto y comprobar el bloqueo tras cinco fallos.
6. Validar en dispositivo la densidad, lectura, contraste y accesibilidad; la captura de pantalla o compilación no sustituye esta prueba.

## Alcance de calidad

La interfaz se diseñó con criterios de usabilidad y la seguridad se documenta tomando ISO/IEC 27001:2022 A.5.17 como referencia. Esto no supone conformidad, auditoría ni certificación ISO, ni una aprobación de Qodana/SonarQube. Estado de análisis estático y pendientes: [revisión local de seguridad](REVISION_SEGURIDAD_LOCAL.md) y [calidad](QUALITY.md).

## Verificación de esta versión

El 2026-10-15 se ejecutaron `:app:testDebugUnitTest`, `:app:lintDebug` y `:app:assembleDebug`: las 21 pruebas unitarias pasaron, lint terminó con 0 errores y 2 avisos (target API 35 y Kapt en Room), y se generó `app/build/outputs/apk/debug/app-debug.apk` como versión 1.3.0. Falta validar el diseño en el teléfono.

Para publicar una app nueva o actualizarla en Google Play, desde el 31 de agosto de 2026 se requiere target API 36. Este prototipo sigue en target 35 porque el proyecto aún usa AGP 8.7.2 y SDK 35; se requiere actualizar la cadena Android, probar los cambios de comportamiento de Android 16 y volver a generar el artefacto antes de una publicación. Referencia oficial: https://developer.android.com/google/play/requirements/target-sdk y https://developer.android.com/about/versions/16/setup-sdk.
