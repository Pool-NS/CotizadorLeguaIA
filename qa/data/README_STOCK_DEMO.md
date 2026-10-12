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

El generador mantiene intacto `synthetic_quotes_200.csv`. Todos los importes son soles simulados. Factores de consumo, tarifas, costos y saldos no fueron proporcionados ni validados por Leguía. Para carpas cerradas se aplica un multiplicador ficticio; el tapizado usa una unidad equivalente simulada, no una regla de taller.

## Cómo leer la factibilidad

- `FACTIBLE_CON_REGLAS_SINTETICAS`: dimensiones completas, saldo del escenario suficiente y aporte directo positivo bajo los supuestos del demo.
- `FALTA_STOCK`: la cantidad teórica de material supera el saldo ficticio inicial.
- `REVISAR_DATOS`: el registro de origen indica que faltan requisitos.
- `REVISAR_MARGEN_DIRECTO`: el precio final simulado no cubriría el costo directo simulado del material.

El aporte directo se calcula como precio simulado menos costo del material estimado. No incluye mano de obra, desperdicio real validado, gastos indirectos, impuestos, transporte ni otros costos, por lo que no equivale a utilidad ni determina una cotización óptima. Cada fila compara un escenario con el mismo saldo inicial de referencia; no descuenta inventario ni modela el consumo acumulado de las 200 cotizaciones.

## En la aplicación Android

Entra a **Panel de Administración → Dashboard demo · 200 datos sintéticos**. El panel muestra métricas, supuestos de tarifa, saldos de prueba y ejemplos de faltantes. El dataset vive en el paquete de la app y no se inserta en Room; los registros reales del teléfono permanecen separados. El resumen de una cotización real solo puede afirmar que alcanza el stock cuando exista una regla de consumo validada. Para un material normal sin esa regla, informa que se requiere revisión humana.

La IA generativa del proyecto interpreta y estructura el texto de la solicitud. El cálculo de precio y disponibilidad se realiza con reglas explícitas; no se le pide a Gemini inventar precios ni decidir stock. El demo de 200 registros no entrena la IA, no representa resultados históricos y no debe utilizarse para contrastar hipótesis de la tesis.

## Uso académico

Puedes importar el CSV enriquecido en jamovi o SPSS para practicar frecuencias, descriptivos, tablas y gráficos. SmartPLS no corresponde automáticamente a las hipótesis operacionales de la tesis, que miden tiempo, exactitud y cumplimiento en solicitudes observadas. Usa solicitudes reales para pretest/posttest y consulta al docente antes de cambiar el modelo o aplicar PLS-SEM a indicadores de eventos.
