# Informe de auditoría

## Alcance y método

Se inspeccionaron los manifiestos, módulos Kotlin, entidades y migraciones Room, DAOs, repositorio, ViewModel, pantallas Compose, API FastAPI, configuración de Render, dependencias, pruebas y documentación existentes. Se ejecutaron las verificaciones enumeradas en la matriz de evidencia. No se tuvo acceso a una instancia Android conectada ni se realizó una llamada real al proveedor Gemini.

Esta revisión es de código y configuración local. No es pentest, evaluación independiente ISO, certificación, auditoría de nube desplegada ni aprobación de tarifas comerciales. No se atribuye TDD al trabajo previo porque el historial disponible no demuestra ese proceso.

## Arquitectura confirmada

La app es Android nativa. `MainActivity` construye Room, `CotizadorRepository`, `CotizadorViewModel` y el cliente HTTP de IA; también mantiene la navegación entre pantallas. Las pantallas Compose capturan la solicitud, permiten revisar la interpretación, calculan el precio con parámetros aprobados almacenados en Room y presentan resumen, solicitudes, tarifas, stock y panel. El flujo principal es:

`Compose → ViewModel → Repository → DAO → Room/SQLite en el teléfono`.

La interpretación generativa es opcional: Android envía el texto a una URL configurable de FastAPI; FastAPI valida el tamaño y llama a Gemini usando `GEMINI_API_KEY` guardada en el servidor. La propuesta se valida y se revisa por una persona. Gemini no calcula precios, no consume stock ni decide la venta. La API no persiste cotizaciones y no es la base de datos.

## Arquitectura de datos

Room declara 13 entidades en versión 9: servicio, material, parámetros de precio, cotización, evento de trazabilidad, error, proforma, trabajo de producción, requerimiento de material, stock, movimiento de stock, necesidad de abastecimiento y predicción. Las cotizaciones, tarifas y saldos se guardan localmente por instalación. Los movimientos de stock conservan fecha, responsable, cantidad y motivo; el saldo no se descuenta automáticamente desde una cotización porque no hay recetas de consumo validadas.

Hay migraciones explícitas de 2→3 hasta 8→9 en el código. No se encontró una migración 1→2; una instalación antigua que realmente use ese esquema necesita identificarlo y probarlo antes de actualizar. `exportSchema=false` impide conservar snapshots de esquema Room en el repositorio. No existe sincronización ni base de datos multiusuario central.

## Seguridad y conectividad

- Android declara `allowBackup=false`, tráfico claro deshabilitado y configuración de red estricta en Release; el manifiesto Debug contiene una excepción limitada a desarrollo. Se añadieron exclusiones explícitas de respaldo en reglas antiguas y actuales, que deben verificarse en un dispositivo objetivo.
- La clave Gemini no forma parte de Android; se lee desde `GEMINI_API_KEY` en FastAPI y los archivos `.env` se excluyen de Git.
- El panel del responsable se protege con una credencial local derivada mediante PBKDF2 y limitación de intentos; no identifica a trabajadores ni autoriza solicitudes al servidor.
- Room y las preferencias se almacenan en el sandbox de Android, pero no se implementa cifrado propio de base de datos. Los respaldos/restauraciones se excluyen explícitamente; transferir a otro teléfono no está implementado.
- El endpoint FastAPI carece de autenticación, autorización por usuario, límites de tasa y control de cuotas. No debe exponerse a trabajadores ni tratarse como producción multiusuario hasta añadir protección del lado servidor.
- Render está descrito en `render.yaml` como un servicio gratuito. El repositorio no prueba que exista un despliegue activo, estable o con cuota Gemini disponible. En Debug se puede apuntar al emulador o a una URL temporal; Release requiere URL HTTPS.
- El backend recibe texto libre y lo remite al proveedor. No se garantiza que el texto esté libre de nombres, teléfonos o direcciones; deben minimizarse los datos y obtenerse las autorizaciones aplicables.

## Estado de las tres dimensiones del proceso

1. **Tiempo:** Android guarda hora inicial cuando se inicia la atención desde la app y hora de comunicación cuando el operador marca esa confirmación al guardar. No es un cronómetro independiente ni incluye atenciones pretest sin app. El dashboard muestra promedio y mediana, con tamaño `n`, solo para intervalos no negativos registrados.
2. **Exactitud:** no se infiere de ventas, de una corrección de IA ni de que el formulario haya pasado validaciones. Falta incorporar la evaluación independiente usando los criterios del instrumento aprobado. El panel indica “Sin datos” y el CSV deja esos campos vacíos.
3. **Cumplimiento/oportunidad:** se registra la declaración del operador sobre comunicación del precio y se muestran numerador y denominador de capturas completas. No es una validación independiente del mismo acto ni reemplaza el criterio del instrumento.

La conversión de cotización a venta es una métrica operativa adicional; no sustituye las tres dimensiones de la tesis.

## Prioridades de mejora realizadas en esta etapa

- Distinguir en el panel métricas observadas por la app de exactitud aún no medida.
- Corregir la mediana para muestras pares y agregar el promedio.
- Ampliar el CSV con ID estable dentro de Room, fecha/hora UTC, minutos, versión Android, fuente y modo de conexión. Fase, modalidad de atención, exactitud y errores quedan en blanco para completar solo con el protocolo aprobado.
- Cerrar la ambigüedad de reglas vacías de respaldo añadiendo exclusiones para nube y transferencia entre dispositivos, conservando `allowBackup=false`.
- Mantener la navegación y funciones existentes, y documentar los límites de stock, IA y medición.

## Alcance de la etapa siguiente

Priorizar pruebas instrumentadas de Room/migraciones, pruebas UI Compose para navegación y formularios, captura observacional independiente de exactitud/errores según el instrumento validado, accesibilidad en dispositivo (TalkBack, escalado y teclado) y análisis Sonar/Qodana sobre el commit final. Añadir backend multiusuario o recetas de consumo requiere decisiones y reglas del negocio; esta auditoría no las inventa.
