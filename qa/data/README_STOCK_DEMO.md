# Dataset sintético de cotizaciones, precios e inventario

## Generación

Desde la raíz del repositorio ejecuta:

```powershell
python qa/data/generate_synthetic_stock_demo.py
```

El script usa una semilla fija y la base sintética existente para generar exactamente 200 escenarios reproducibles. Produce:

- `synthetic_quote_stock_pricing_200.csv`: cotizaciones, dimensiones simuladas, tarifa, descuento, costo directo del material, saldo comparado, faltante y clasificación de factibilidad.
- `synthetic_material_inventory.csv`: saldos y costos unitarios de inventario ficticio.
- `synthetic_price_rules.csv`: supuestos de tarifas de demostración.
- `synthetic_stock_dashboard.json`: métricas resumidas y ejemplos; el mismo JSON se copia a los assets Android para el panel de demostración.
- `synthetic_quotes_stock_smartpls_demo.xlsx`: libro con Dashboard (gráficos de barras), los 200 registros, inventario/tarifas demo, una hoja de precios pendientes de confirmación y una guía de uso de SmartPLS.

El generador mantiene intacto `synthetic_quotes_200.csv`. Los materiales que muestra el formulario son: carpas — lino económico, liviano y pesado; toldos — Tarflex, lona orillo azul y lona orillo rojo. La opción abierta/cerrada de carpa permite indicar número de ventanas y puertas. Las medidas rápidas incluyen 2×2, 3×2, 3×3, 4×3, 5×4 y 6×4 m, con medidas personalizadas disponibles.

Todos los importes son soles simulados. Factores de consumo, tarifas, costos y saldos no fueron proporcionados ni validados por Leguía. Los precios manuscritos de las fotos no se cargaron porque no se puede asociar cada importe con seguridad a material, medida y tipo. La hoja `Precios_por_validar` del Excel es una plantilla para que el jefe confirme esas asociaciones, tarifas, saldos y consumos. Para carpas cerradas el dataset aplica un multiplicador ficticio; ventanas y puertas no tienen consumo adicional validado. El tapizado usa una unidad equivalente simulada, no una regla de taller.

## Cómo leer la factibilidad

- `FACTIBLE_CON_REGLAS_SINTETICAS`: dimensiones completas, saldo del escenario suficiente y aporte directo positivo bajo los supuestos del demo.
- `FALTA_STOCK`: la cantidad teórica de material supera el saldo ficticio inicial.
- `REVISAR_DATOS`: el registro de origen indica que faltan requisitos.
- `REVISAR_MARGEN_DIRECTO`: el precio final simulado no cubriría el costo directo simulado del material.

El aporte directo se calcula como precio simulado menos costo del material estimado. No incluye mano de obra, desperdicio real validado, gastos indirectos, impuestos, transporte ni otros costos, por lo que no equivale a utilidad ni determina una cotización óptima. Cada fila compara un escenario con el mismo saldo inicial de referencia; no descuenta inventario ni modela el consumo acumulado de las 200 cotizaciones.

## En la aplicación Android

Entra a **Panel de Administración**: las solicitudes guardadas localmente tienen indicadores y gráficos de barras por estado y conversión. El botón **Dashboard demo · 200 datos sintéticos** abre otra vista con barras por factibilidad y servicio, tarifas demo, saldos de prueba y ejemplos de faltantes. En **Nueva cotización**, el menú permite elegir material y medidas frecuentes; la altura es opcional y las carpas cerradas piden ventanas y puertas. Para aplicar precios reales, el jefe debe crear tarifas aprobadas por combinación exacta de servicio, medida, variante y material en el panel. El catálogo inicial de materiales no implica que esos precios o consumos estén aprobados.

El dataset vive en el paquete de la app y no se inserta en Room; los registros reales del teléfono permanecen separados. El resumen de una cotización real solo puede afirmar que alcanza el stock cuando exista una regla de consumo validada. Para un material normal sin esa regla, informa que se requiere revisión humana.

La IA generativa del proyecto interpreta y estructura el texto de la solicitud. El cálculo de precio y disponibilidad se realiza con reglas explícitas; no se le pide a Gemini inventar precios ni decidir stock. El demo de 200 registros no entrena la IA, no representa resultados históricos y no debe utilizarse para contrastar hipótesis de la tesis.

## Uso académico

Puedes abrir el Excel o importar el CSV enriquecido en jamovi o SPSS para practicar frecuencias, descriptivos, tablas y gráficos. La pestaña `PLS_solo_practica` contiene columnas numéricas para probar la importación a SmartPLS; no es un modelo de medición ni una base empírica. La hoja `SmartPLS_guia` explica esta limitación. SmartPLS reporta alfa de Cronbach ≥ 0.70 como umbral convencional de consistencia interna, pero eso no puede prometerse con datos sintéticos ni debe lograrse alterando respuestas; requiere indicadores múltiples de un instrumento real y un modelo de medición aprobado. Consulta la [documentación de SmartPLS sobre umbrales](https://www.smartpls.com/documentation/functionalities/thresholds/).

La tesis describe tiempo, exactitud y cumplimiento en solicitudes observadas, no automáticamente constructos latentes de encuesta. Consulta [THESIS_DATA_GUIDE.md](../../docs/THESIS_DATA_GUIDE.md) al definir el análisis con el docente. Los 200 registros sintéticos no son observaciones empíricas, no calculan confiabilidad y no deben usarse en el Capítulo IV como resultados de campo.
