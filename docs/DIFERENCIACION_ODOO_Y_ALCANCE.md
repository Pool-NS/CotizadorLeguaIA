# Diferenciación de Cotizador Leguía y alcance de la tesis

## Propósito

Este documento resume cómo se diferencia Cotizador Leguía de un ERP general como Odoo y qué significa completar el producto para el alcance de la tesis. La propuesta de investigación evalúa una aplicación Android con apoyo de IA generativa para mejorar el proceso de cotización del negocio Leguía. No propone reemplazar un ERP.

## Diferenciador del producto

La diferencia no consiste en tener más módulos que un ERP. Consiste en especializar el flujo de cotización para los servicios y reglas de Leguía:

1. **Captura contextual:** registra la solicitud hablada o escrita para carpas, toldos y tapizado de moto.
2. **Estructuración asistida por IA:** propone servicio, medidas, material y características desde lenguaje natural. La persona cotizadora revisa y corrige la propuesta; la IA no determina el precio final.
3. **Cotización configurable:** calcula solo cuando encuentra una tarifa aprobada para la combinación aplicable de servicio, medidas, material y variante. El precio final y los descuentos permanecen bajo decisión humana.
4. **Continuidad del trabajo:** conserva trazabilidad de la cotización. El modelo ya contempla entidades para proforma y producción, pero los flujos completos de esos módulos requieren validación funcional antes de presentarlos como terminados.
5. **Inventario operativo básico:** el jefe registra materiales e ingresos reales en el dispositivo. La app muestra el saldo registrado; sin receta de consumo aprobada no afirma que alcance ni descuenta stock.
6. **Evaluación alineada a la investigación:** el prototipo es la intervención tecnológica; el efecto debe medirse con observaciones pretest y posttest reales mediante el instrumento aprobado. El dashboard de demo aún no es un reporte analítico completo de tiempo, exactitud y oportunidad.

## Comparación de alcance

| Aspecto | Cotizador Leguía (alcance de tesis) | ERP general como Odoo |
|---|---|---|
| Objetivo principal | Apoyar y medir el proceso especializado de cotización de Leguía. | Gestionar procesos empresariales configurables de múltiples áreas. |
| Interacción principal | Capturar en lenguaje natural un requerimiento de fabricación y convertirlo en datos revisables. | Depende de módulos y flujos configurados; la IA generativa no define por sí sola la propuesta de Cotizador Leguía. |
| Precio | Reglas aprobadas por servicio, medida, material y variante; decisión final humana. | Configuración comercial más amplia que requiere implementación para el negocio. |
| Materiales | Comprobación de necesidades asociadas a los trabajos del alcance; la receta y saldo deben estar validados. | Gestión de inventario y operaciones con mayor amplitud y parametrización. |
| Tesis | Instrumentación del proceso pretest/posttest y evidencia del efecto en tiempo, exactitud y oportunidad. | No reemplaza el diseño ni los indicadores de esta tesis. |
| Límite explícito | No incluye contabilidad, facturación electrónica oficial, nómina ni ERP integral. | Puede cubrir módulos empresariales adicionales según edición y configuración. |

No se afirma que Cotizador Leguía sea mejor que Odoo en términos generales. Son productos con propósitos y alcances diferentes. El prototipo es una solución vertical para el proceso definido por la tesis.

## Estado verificable del prototipo

El repositorio incluye app Android, almacenamiento Room local, catálogo de tarifas, inventario local, flujo de cotización e interpretación generativa mediante backend FastAPI/Gemini. Los artefactos sintéticos y el dashboard de demostración se retiraron. Las tarifas/stock reales aún necesitan aprobación del jefe; el backend no es una base de datos central.

Esto permite presentar un **prototipo funcional para demostración y evaluación técnica**, pero no acredita operación multiusuario ni resultados de eficacia.

## Pendientes para declarar el producto listo para operación

Los siguientes puntos no se resuelven con más código inventado; requieren validación o información real:

- El jefe debe aprobar tarifas, unidades, descuentos permitidos y vigencia por servicio, medida, material y variante.
- El taller debe validar recetas/consumos por tipo de producto, dimensiones, ventanas, puertas y material. Sin receta, la app debe indicar “requiere revisión”, no “stock suficiente”.
- La base Room se guarda por instalación. Para que varios trabajadores compartan cotizaciones, precios y stock se necesita backend persistente, autenticación/autorización por rol, sincronización, respaldos y reglas de concurrencia.
- La clave del proveedor IA debe guardarse en el entorno de servidor y el endpoint público debe limitar abuso, frecuencia y tamaño de solicitud antes de uso compartido.
- Para el Capítulo IV se requieren observaciones reales y autorizadas en pretest y posttest, con el instrumento y criterios de exactitud aprobados.
- El dashboard de tesis debe calcular y presentar los indicadores reales completos (distribuciones de duración, exactitud, errores definidos por el instrumento, oportunidad, faltantes y N por fase); los gráficos operativos locales no sustituyen esos análisis.
- La medición de calidad no equivale a certificación ISO. El proyecto puede documentar controles y evidencias relacionados con calidad y seguridad, pero cualquier declaración de conformidad o certificación requiere auditoría competente.
- No se incluye predicción comercial. Su evaluación futura necesita datos históricos reales suficientes, revisión de sesgo y validación fuera de muestra.
- Los módulos de proforma, programación, producción y entrega deben tener pantallas, permisos, transiciones de estado y pruebas integradas antes de afirmarse como un flujo operativo completo.

## Criterios de aceptación para cerrar el alcance de tesis

1. Un responsable autorizado carga y aprueba tarifas verificadas para los escenarios que se probarán.
2. El personal registra una solicitud y revisa/corrige los campos propuestos por IA; ante desconexión puede completar manualmente.
3. El sistema calcula el precio referencial con reglas aprobadas, registra ajustes humanos y conserva la trazabilidad del resultado.
4. La comprobación de material usa recetas y existencias validadas; escenarios no confirmados no reservan ni descuentan stock.
5. Los datos de observación registran tiempos, exactitud y oportunidad según el instrumento aprobado, sin información identificable innecesaria.
6. CI, análisis estático y pruebas de la versión evaluada quedan vinculados al commit. Las pruebas con trabajadores se realizan en los dispositivos y el entorno definidos por la investigación.
7. El Capítulo IV informa únicamente resultados observados, con N, faltantes, prueba estadística y tamaño del efecto que correspondan al diseño aprobado.

La app puede considerarse terminada **para el prototipo y alcance de tesis** cuando estos criterios se verifiquen y las dependencias de negocio y despliegue estén resueltas. “100%” no se puede certificar solo por compilar o tener CI verde.
