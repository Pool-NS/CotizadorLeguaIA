# Dataset de 200 cotizaciones para SPSS

## Archivos

- `synthetic_quotes_200.csv`: los 200 registros en formato CSV.
- `Importar_dataset_SPSS.sps`: sintaxis de SPSS para importar, etiquetar variables y ejecutar resúmenes iniciales.

En SPSS, abre `Importar_dataset_SPSS.sps` y ejecútalo. Si el repositorio está en otra carpeta, cambia la ruta indicada en `/FILE` por la ruta de `synthetic_quotes_200.csv` en tu equipo.

## Diccionario de variables

| Variable | Tipo | Descripción |
|---|---|---|
| `quote_id` | Texto | Identificador artificial, de `SYN-001` a `SYN-200`. |
| `service_type` | Categórica | Servicio simulado: Carpas, Toldos o Tapizado. |
| `quote_amount` | Numérica | Importe simulado; el dataset no especifica moneda ni tarifa real. |
| `discount_percentage` | Numérica | Descuento porcentual simulado. |
| `requirement_complete` | Binaria (0/1) | Indica si el requerimiento se marcó como completo. |
| `ai_interpretation_reviewed` | Binaria (0/1) | Indicador simulado de revisión de interpretación; no significa que la app tenga IA integrada. |
| `quotation_time_minutes` | Numérica | Tiempo simulado para preparar una cotización, en minutos. |
| `days_to_requested_date` | Numérica | Días entre la cotización y la fecha solicitada. |
| `customer_response_days` | Numérica | Días simulados hasta la respuesta del cliente. |
| `status` | Categórica | `CONCRETADA` o `NO_CONCRETADA`, asignado por el proceso sintético. |
| `converted_to_sale` | Binaria (0/1) | Variable objetivo sintética: 1 convertida, 0 no convertida. |
| `synthetic_flag` | Texto | `true` en todos los registros; marca el origen sintético. |

## Limitaciones para el informe

Los 200 registros fueron generados artificialmente con una semilla fija. No son ventas observadas de Leguía ni una muestra representativa de clientes reales. Los análisis y métricas calculados con ellos sirven para demostrar el flujo de importación y análisis en SPSS, no para afirmar desempeño real, causalidad ni precisión operativa. Para validar el modelo se necesitarán datos reales, autorizados, anonimizados y con definiciones aprobadas por el negocio y el docente.

Los valores de importe y descuento son unidades simuladas; no deben presentarse como precios vigentes. La variable `ai_interpretation_reviewed` es únicamente un campo de demostración y no evidencia que la app integre IA generativa.
