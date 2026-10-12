# Abrir y analizar el dataset en jamovi

## Archivo

Abre `qa/data/synthetic_quotes_200.csv` desde el repositorio. El archivo contiene 200 filas, identificadores únicos y 12 columnas. No es una base de datos de producción ni un registro histórico real.

## Importar

1. Abre jamovi y elige **Abrir → Este equipo** (o el botón de menú y luego **Abrir**).
2. Selecciona `synthetic_quotes_200.csv` en `CotizadorLeguaIA/qa/data/`.
3. Comprueba que la primera fila se use como encabezado y que el separador sea coma. Los decimales están escritos con punto.
4. En la pestaña **Datos**, revisa el tipo de medida:
   - Nominal: `quote_id`, `service_type`, `status`, `synthetic_flag`.
   - Continua: `quote_amount`, `discount_percentage`, `quotation_time_minutes`, `days_to_requested_date`, `customer_response_days`.
   - Nominal binaria: `requirement_complete`, `ai_interpretation_reviewed`, `converted_to_sale` (0 = no, 1 = sí).
5. Guarda el archivo de jamovi como `.omv` en una carpeta de resultados; conserva intacto el CSV original.

## Análisis exploratorio para practicar

- **Exploración → Descriptivos**: analiza importe, descuento, tiempo de cotización y días de respuesta. Puedes separar por `service_type`.
- **Frecuencias**: resume `service_type`, `status` y las variables binarias.
- **Tablas de contingencia**: cruza `service_type` con `converted_to_sale`; activa chi-cuadrado solo como ejercicio sobre los datos simulados.
- Guarda tablas, gráficos y el archivo `.omv` como evidencias de práctica, identificándolas como resultados sintéticos.

## Límite para la tesis

Todos los registros tienen `synthetic_flag = true`; las ventas, tiempos, importes y respuestas fueron simulados. Sus estadísticos no demuestran que la aplicación mejore el proceso real de Leguía, no reemplazan datos históricos y no deben presentarse como observaciones de personas o ventas reales. Si el Capítulo IV evalúa una intervención, recopila datos observados con autorización, anonimización y un diseño aprobado por el docente. El CSV actual sirve para practicar la importación y el flujo de Jamovi.
