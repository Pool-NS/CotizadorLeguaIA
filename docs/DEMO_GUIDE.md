# Guion para presentar el primer avance

## Demostración de voz

1. En Inicio, pulsa `Iniciar Nueva Cotización` y `Hablar requerimiento`.
2. Acepta el reconocedor de voz de Android. Ejemplo: “Necesito una carpa, largo 3 metros y ancho dos metros”.
3. Si se reconoce un solo servicio, el formulario abre Carpas y propone largo 3, ancho 2; también interpreta “3 por 2”. El reconocedor puede depender de internet.
4. Revisa y corrige transcripción, medidas y unidades antes de continuar. Parser local y reconocimiento son asistencia; la persona confirma la solicitud.
5. Si nombras dos servicios, el sistema solicita selección manual. Largo y ancho son obligatorios antes de guardar.
6. El jefe puede abrir `Tarifas` y guardar precios por tamaño exacto (por ejemplo, Carpas 3×2 y 2×2 con precios distintos). La cotización aplica solo una tarifa que coincida con el servicio y largo×ancho. Si no existe ese tamaño, no muestra un precio inventado y la solicitud queda pendiente.
7. Para Carpas, prueba abierta y cerrada. En cerrada registra cuántas ventanas y puertas solicita; la tarifa distingue tamaño y tipo abierto/cerrado. Para Tapizado de moto, registra tipo/modelo y largo×ancho.

## Panel del jefe

Abre Panel de Administración. En cada instalación, el jefe debe configurar su PIN privado antes de entregar el teléfono a un trabajador; el PIN queda local en ese teléfono y no se sincroniza entre equipos. El panel muestra conteos, permite configurar una tarifa por servicio y abrir Materiales y stock para registrar ingresos. El equipo no tiene cuentas empresariales sincronizadas.

## Límites que explicar durante la presentación

- No está integrada IA generativa; el servicio y medidas se detectan por reglas a partir de la transcripción.
- El precio automático requiere una tarifa aprobada para el servicio y la combinación exacta de largo×ancho. No aplica interpolaciones para tamaños cercanos, ni suma conceptos de materiales, desperdicio, altura, descuentos o validaciones comerciales adicionales.
- Inventario permite registrar ingresos y ver saldos. Todavía no descuenta consumos por producción ni gestiona mínimos, compras, proformas o el flujo de producción completo.
- Sonar Gradle está configurado, pero la instancia requiere `SONAR_HOST_URL` y `SONAR_TOKEN`; no se ejecutó análisis ni Quality Gate.
- Las métricas de predicción son sintéticas y no describen resultados del negocio.
