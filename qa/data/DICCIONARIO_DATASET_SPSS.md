# Diccionario de la plantilla de observación

La plantilla `observed_quotes_template.csv` está vacía intencionalmente. Una fila corresponde a una cotización realmente observada; no es el dataset de 200 ni genera registros automáticamente.

| Campo | Tipo sugerido | Significado |
|---|---|---|
| `record_id` | Texto nominal | Código sin información personal. |
| `phase` | Texto nominal | `PRETEST` o `POSTTEST`, según diseño aprobado. |
| `observation_date` | Fecha | Día de observación. |
| `service_type` | Texto nominal | Carpas, Toldos o Tapizado de moto. |
| `ai_used`, `ai_interpretation_reviewed` | Binaria | 0 = no, 1 = sí; registra solo lo observado. |
| `start_time`, `final_price_time` | Hora | Horas observadas para reconstruir la duración. |
| `quotation_time_minutes` | Numérica continua | Minutos entre inicio y comunicación del precio final. |
| `quote_correct` | Binaria | 1 = correcta según los criterios aprobados; 0 = no correcta. |
| `error_count`, `error_codes` | Numérico y texto | Cantidad/códigos de error definidos en el instrumento. |
| `final_price_communicated_same_visit` | Binaria | 1 = comunicado durante la misma atención; 0 = no. |
| `exclusion_reason` | Texto | Motivo documentado de exclusión; vacío si se incluye. |

No importes columnas de respuesta Likert ni calcules alfa de Cronbach con esta plantilla: la matriz actual es observacional. Si el docente solicita SmartPLS, valida primero un instrumento de constructos e ítems antes de recolectar esa clase de datos.
