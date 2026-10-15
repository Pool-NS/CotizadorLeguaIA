# Guion para presentar el primer avance

## Demostración de voz

1. En Inicio, pulsa `Iniciar Nueva Cotización` y `Hablar requerimiento`.
2. Acepta el reconocedor de voz de Android. Ejemplo: “Necesito una carpa, largo 3 metros y ancho dos metros”.
3. Si se reconoce un solo servicio, el formulario abre el servicio y propone largo 3, ancho 2; también interpreta “3 por 2”. El reconocedor puede depender de internet.
4. Revisa y corrige transcripción, medidas y unidades antes de continuar. Parser local y reconocimiento son asistencia; la persona confirma la solicitud.
5. Si nombras dos servicios, el sistema solicita selección manual. Largo y ancho son obligatorios antes de guardar.
6. El jefe puede abrir `Tarifas` y guardar precios por servicio, tamaño exacto y material (por ejemplo, Carpas 3×2 en lona y en Oxford). La cotización aplica una tarifa solo si coinciden servicio, largo×ancho, material y variante, cuando aplica. Si falta esa tarifa, no muestra un precio inventado y la solicitud queda pendiente.
7. Para Carpas, prueba abierta y cerrada. En cerrada registra cuántas ventanas y puertas solicita; la tarifa distingue tamaño, material y tipo abierto/cerrado. Para Tapizado de moto, registra material, tipo/modelo y largo×ancho.

## Panel del jefe

Abre Panel de Administración. En cada instalación, el jefe debe configurar su PIN privado antes de entregar el teléfono a un trabajador; el PIN queda local en ese teléfono y no se sincroniza entre equipos. El panel muestra conteos, permite configurar una tarifa por servicio y abrir Materiales y stock para registrar ingresos. El equipo no tiene cuentas empresariales sincronizadas.

## Qué muestran las métricas y gráficos

- El panel real muestra barras horizontales de cotizaciones pendientes/concretadas/no concretadas y conversión, usando únicamente los registros guardados en ese dispositivo.
- `Dashboard demo · 200 datos sintéticos` muestra gráficos de barras para resultado de factibilidad y cantidad por servicio. Las tarjetas adicionales resumen tiempo, precio y costos simulados.
- En la exposición, indica que son registros sintéticos. No los presentes como historial ni evidencia de la empresa.
- Para el modelo metodológico y la definición de métricas, consulta [METODOLOGIA_IA_DEVSECOPS.md](METODOLOGIA_IA_DEVSECOPS.md).

## Límites que explicar durante la presentación

- Gemini puede proponer campos estructurados desde el texto cuando el backend y la clave están disponibles. El parser local también reconoce servicios/medidas; la IA no fija precios ni decide el estado comercial.
- El precio automático requiere una tarifa aprobada para servicio, material, combinación exacta de largo×ancho y tipo de carpa cuando aplica. No aplica interpolaciones ni estima reglas comerciales no configuradas.
- Inventario permite registrar ingresos y ver saldos. Todavía no descuenta consumos por producción ni gestiona mínimos, compras, proformas o el flujo de producción completo.
- Sonar Gradle está configurado, pero la instancia requiere `SONAR_HOST_URL` y `SONAR_TOKEN`; no se ejecutó análisis ni Quality Gate.
- Las métricas de predicción son sintéticas y no describen resultados del negocio.
- El módulo Gemini se prueba técnicamente con casos etiquetados separados; el dataset de cotizaciones de 200 filas no sirve como dataset de entrenamiento ni evaluación de interpretación.
- Room/SQLite es local a cada instalación. El backend no consolida cotizaciones de trabajadores y el login local del panel no es autenticación multiusuario para producción.
