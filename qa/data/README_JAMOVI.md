# Análisis de observaciones reales con jamovi

## Preparar la base

Registra una fila por solicitud observada en `observed_quotes_template.csv`, con autorización, anonimización y el mismo criterio de medición en ambas fases. La plantilla no contiene observaciones ni resultados. El repositorio ya no distribuye los 200 registros sintéticos.

## Importar

1. Abre jamovi y elige **Abrir → Este equipo**.
2. Selecciona el CSV que completaste a partir de `observed_quotes_template.csv`.
3. Confirma que la primera fila contiene los encabezados y el separador es coma.
4. Configura `phase` como nominal; `service_type` como nominal; `quotation_time_minutes` como continua; y los indicadores binarios como nominales.
5. Conserva una copia `.omv` en un lugar autorizado y protege el archivo con datos observados.

## Análisis

- Descriptivos de tiempo por fase y servicio.
- Frecuencias y proporciones para cotizaciones correctas y precio comunicado en la visita.
- Compara fases usando la prueba que corresponda al diseño y distribución de los datos; confirma el plan con el docente.
- Reporta el tamaño realmente observado, datos faltantes, exclusiones, prueba, valor p y tamaño de efecto.

No fabriques filas para alcanzar 200 casos ni modifiques respuestas para lograr un resultado estadístico deseado. Si necesitas una muestra de 200, consulta cómo recolectarla de manera autorizada.
