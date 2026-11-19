# Mejoras de interfaz, métricas y acceso administrativo

Fecha de actualización: 2026-11-12.

## Experiencia de uso

- Las pantallas secundarias comparten un encabezado compacto con una acción “‹ Volver” de tamaño táctil accesible.
- El botón físico de regreso mueve selección → formulario → resumen sin perder campos; el panel captura regreso propio desde sus secciones.
- El inicio separa la acción operativa de cotizar del acceso autenticado a las herramientas del responsable. El modo sin conexión indica que la IA no estará disponible y conserva el ingreso manual.
- El formulario conserva campos editables para cualquier medida y presenta un selector con medidas frecuentes del catálogo informado. Largo y ancho se pueden corregir después de elegir; altura sigue siendo opcional.
- Los campos de dimensiones usan teclado decimal. La selección de tamaño no aprueba tarifas: la app solo cotiza si existe una regla exacta activa para el servicio, material, variante y medida.
- Se retiró el dashboard de prueba y el inventario/precio sintéticos. El panel solo muestra información de Room de este dispositivo.
- La captura de medidas ahora ofrece un carrusel horizontal de tamaños frecuentes `largo × ancho`; al tocar una opción rellena ambos campos y se pueden editar o escribir otros valores.
- Los materiales aprobados conocidos aparecen como opciones táctiles dentro del servicio seleccionado: lino para carpas y Tarflex/lona para toldos. Tapizado permite describir el material o acabado, sin inventar un catálogo o tarifa.
- El inventario permite buscar y filtrar por categoría, muestra cantidad, unidad y presentación, y registra ingresos, salidas y ajustes con motivo. Cada movimiento se confirma y deja fecha y responsable; no admite cantidades no finitas, unidades distintas ni saldo negativo.
- El registro de ingreso distingue presentación/medida de la cantidad disponible y exige motivo. Una reposición se suma al saldo del mismo material cuando la unidad y presentación son compatibles.
- Los movimientos recientes muestran hasta 50 operaciones locales. No se muestra un nivel bajo/adecuado sin mínimos aprobados; tampoco se descuenta stock al cotizar sin una receta de consumo aprobada.
- Tarifas permite buscar por material/medida, filtrar servicio/tamaño, consultar tarifas activas y abrir el formulario solo cuando se necesita. Antes de guardar muestra servicio, medida, material e importe. La sustitución de la tarifa exacta conserva la anterior inactiva en Room.

## Gráficos y significado

| Vista | Visualización | Interpretación |
|---|---|---|
| Dashboard del jefe | Línea de 7 días | Cotizaciones locales por fecha de registro; aparece cuando hay cotizaciones en el periodo seleccionado. |
| Dashboard del jefe | Dona de estados | Pendientes, concretadas y no concretadas del teléfono actual; aparece solo con registros. |
| Dashboard del jefe | Barras por servicio | Cantidad de solicitudes locales por servicio; omite categorías vacías. |
| Dashboard del jefe | KPI de tiempo mediano | Desde el botón Nueva cotización hasta guardar una confirmación de precio comunicado; no equivale por sí mismo al instrumento de observación. |
| Dashboard del jefe | KPI de precio comunicado | Autorregistro explícito del operador; muestra numerador/denominador de las solicitudes nuevas cuyo registro de comunicación fue capturado. |
El dashboard se calcula solo con la base Room de este dispositivo. No representa a todos los trabajadores y no sincroniza con la nube. No se muestran gráficos basados en filas generadas.

La conversión se calcula como `concretadas / (concretadas + no concretadas)`. Las solicitudes pendientes y las que no tienen resultado final quedan fuera del denominador. El “valor final registrado” suma cotizaciones que no están pendientes de precio; no se interpreta como ingreso o venta.

El panel aplica los filtros Todo, 30 días y 7 días a los KPI, gráficos y listado. La exactitud no se deduce de conversión, saldo ni correcciones de IA; se deja pendiente de revisión humana con el criterio que apruebe el asesor. Los tiempos y confirmaciones del panel son una medición operativa local, no reemplazan registros pretest/posttest.

El panel exporta el periodo seleccionado como CSV mediante el selector de documentos de Android. Se omiten identificadores comerciales, operador y texto de solicitud; fase, exactitud y errores quedan en blanco hasta que exista verificación aprobada.

## Política de acceso

El acceso del jefe es una contraseña local, no una cuenta de identidad. Las nuevas contraseñas deben tener 12–64 caracteres e incluir mayúscula, minúscula, número y símbolo; se derivan con PBKDF2 y sal aleatoria. La app bloquea durante 60 segundos tras cinco intentos fallidos. Esta es una política interna solicitada para el proyecto: A.5.17 de ISO/IEC 27001 aborda la gestión de información de autenticación y no prescribe por sí mismo esas clases de caracteres. ISO 9001 tampoco define reglas de caracteres. Ninguna de estas referencias certifica la app.

La primera vez que una contraseña previa no cumpla la nueva política, debe verificarse y luego actualizarse antes de entrar al panel. La app tampoco provee usuarios individuales ni autorización central; el backend y los datos Room continúan sin autenticación multiusuario compartida. Para ponerla en producción con trabajadores se requiere diseñar cuentas, roles, recuperación, protección de sesión, API autenticada y almacenamiento central. La contraseña local no basta para proteger administración en una flota compartida.

## Lista de validación manual

1. Abrir/cerrar inventario y tarifas con botón de pantalla y regreso del dispositivo; comprobar que el estado del panel se conserva.
2. En formulario/resumen, navegar atrás y confirmar que se mantengan transcripción, dimensiones, material y selección del usuario.
3. Registrar un material nuevo con categoría, presentación, cantidad, unidad y motivo; revisar el diálogo antes de confirmar.
4. Registrar reposición compatible, salida y ajuste positivo/negativo; probar motivo vacío, unidad distinta y salida mayor al saldo.
5. Consultar movimientos recientes y confirmar que muestran cantidad, unidad, fecha, responsable y motivo.
6. Filtrar inventario por nombre y categoría; comprobar mensaje sin resultados.
7. Buscar tarifa por material/medida y usar filtros de servicio/tamaño; confirmar el diálogo antes de actualizar una tarifa y revisar que la anterior siga inactiva en historial.
8. Probar rangos 7 días/30 días/Todo, gráficos sin datos, pendientes destacadas y cotización local.
9. Guardar un precio con y sin confirmación de comunicación; verificar que el tablero no obligue a declararlo comunicado.
10. Registrar cotizaciones con tamaños frecuentes, medida propia, coma decimal y altura vacía/indicada; volver desde resumen y comprobar que los campos se conservan.
11. Exportar el periodo seleccionado; confirmar que el CSV no contenga cliente, requerimiento, operador ni código comercial y que fase/exactitud/errores estén vacíos, no en cero.
6. Crear contraseña nueva, comprobar el bloqueo temporal tras intentos fallidos y validar el flujo en dispositivo.
7. Validar densidad, lectura, contraste y accesibilidad; la compilación no sustituye esta prueba.

## Alcance de calidad

La interfaz se diseñó con criterios de usabilidad y la seguridad se documenta tomando ISO/IEC 27001:2022 A.5.17 como referencia. Esto no supone conformidad, auditoría ni certificación ISO, ni una aprobación de Qodana/SonarQube. Estado de análisis estático y pendientes: [revisión local de seguridad](REVISION_SEGURIDAD_LOCAL.md) y [calidad](QUALITY.md).

La configuración Debug del backend usa `aiBackendUrl` en `local.properties` y por defecto apunta al emulador Android (`http://10.0.2.2:8000`). Para ngrok se debe copiar la URL HTTPS activa de la terminal de ngrok y reconstruir/reinstalar Debug; no se debe fijar una URL efímera del túnel en Git. `GET /` y `GET /health` son diagnósticos; la interpretación requiere `POST /v1/interpret`. El agente ngrok y Uvicorn deben permanecer ejecutándose.

## Verificación de esta versión

La versión 1.5.0 incorpora navegación de regreso, filtros de periodo, exportación CSV anonimizada, movimientos de inventario y captura explícita de comunicación del precio. La versión también guarda campos del formulario al volver atrás y permite ver versiones activas/inactivas de tarifas.

Verificación local del 2026-11-12: `testDebugUnitTest`, `lintDebug` y `assembleDebug` finalizaron con BUILD SUCCESSFUL; 30 pruebas unitarias pasaron. Lint informa avisos de dependencias desactualizadas y `targetSdk = 35`; no son fallos de compilación y requieren una actualización/probar la migración de plataforma por separado. La verificación visual en emulador/dispositivo y pruebas de instrumentación Room no se ejecutaron.

APK Debug: `app/build/outputs/apk/debug/app-debug.apk`. El APK es de depuración y no está firmado para publicación en una tienda.

Para publicar una app nueva o actualizarla en Google Play, desde el 31 de agosto de 2026 se requiere target API 36. Este prototipo sigue en target 35 porque el proyecto aún usa AGP 8.7.2 y SDK 35; se requiere actualizar la cadena Android, probar los cambios de comportamiento de Android 16 y volver a generar el artefacto antes de una publicación. Referencia oficial: https://developer.android.com/google/play/requirements/target-sdk y https://developer.android.com/about/versions/16/setup-sdk.
