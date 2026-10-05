# Registro real de cotizaciones para pretest y posttest

Este formato sigue los indicadores y categorías del instrumento descrito en la tesis. No incluye observaciones inventadas. Completa una fila únicamente después de observar una solicitud real y anonimízala con un código; no registres nombre, teléfono, dirección ni otros datos identificables del cliente.

## Condición de medición

- `PRETEST`: proceso habitual, antes de usar la app como intervención.
- `POSTTEST`: proceso con la versión de la app que se evaluará, después de la familiarización del personal.
- Si no existen cotizaciones antiguas con marcas de tiempo y detalle suficientes, levanta la línea base de manera prospectiva observando el proceso habitual. No reconstruyas horas ni errores de memoria.
- Registra los periodos y criterios de inclusión/exclusión aprobados por el asesor. Usa los mismos criterios antes y después.

## Campos

| Campo | Registro |
|---|---|
| `request_id` | Código anónimo único, por ejemplo `PRE-001` o `POST-001`. No reutilizarlo. |
| `observation_date` | Fecha `AAAA-MM-DD`. |
| `condition` | `PRETEST` o `POSTTEST`. |
| `service` | `Carpas`, `Toldos` o `Tapizado de moto`, según el alcance aprobado. |
| `operator_code` | Código anónimo del cotizador, no su nombre. Mantener códigos consistentes. |
| `start_timestamp`, `end_timestamp` | Fecha y hora local ISO `AAAA-MM-DD HH:MM:SS`. Inicio: el cliente formula la solicitud; fin: se comunica el precio final. |
| `duration_minutes` | Diferencia entre fin e inicio, en minutos. Mantener las interrupciones ocurridas durante la atención y describirlas en `incident_notes`. |
| `e1_record_error` | 1 si se registró mal una medida/característica comunicada correctamente; en otro caso 0. |
| `e2_selection_interpretation_error` | 1 si servicio, material, medida o característica se seleccionó/interpretó mal; en otro caso 0. |
| `e3_calculation_error` | 1 si el cálculo no coincide con los datos y reglas válidas; en otro caso 0. |
| `e4_parameter_error` | 1 si se aplicó una tarifa/regla distinta de la validada; en otro caso 0. |
| `e5_required_information_omitted` | 1 si se omitió información necesaria; en otro caso 0. |
| `e6_requests_mixed` | 1 si se mezcló información entre solicitudes; en otro caso 0. |
| `error_count` | Suma de E1 a E6. E7, E8 y E9 no se cuentan como errores del proceso. |
| `quote_correct` | 1 si no hubo errores E1-E6; 0 si hubo uno o más. |
| `price_delivered_during_attention` | 1 si se comunicó el precio final durante la misma atención; 0 si no. |
| `e7_customer_source_error` | 1 si el cliente dio una medida/dato incorrecto y fue registrado tal como lo comunicó; no cuenta como error del proceso. |
| `e8_commercial_adjustment` | 1 si hubo descuento/ajuste voluntario; no cuenta como error del proceso. |
| `e9_later_requirement_change` | 1 si el cliente modificó la solicitud después; no cuenta como error del proceso. |
| `incident_notes` | Incidencias relevantes sin datos personales. |

Codifica las variables binarias únicamente con `0` o `1`. Si no se pudo observar un campo, consulta al asesor antes de usar un valor especial; no conviertas automáticamente los faltantes en cero.

## Cálculos y análisis

- `duration_minutes = (end_timestamp - start_timestamp) × 1440` si Excel/Calc guarda ambos campos como fecha-hora.
- `error_count = E1 + E2 + E3 + E4 + E5 + E6`.
- `quote_correct = 1` cuando `error_count = 0`, de lo contrario `0`.
- Cumplimiento porcentual por condición: cotizaciones con `price_delivered_during_attention = 1` / cotizaciones observadas × 100.
- La prueba estadística depende de si pretest y posttest son pares legítimos o grupos independientes y de los supuestos observados. No combines estas observaciones reales con `qa/data/synthetic_quotes_200.csv`.

El instrumento de tesis requiere juicio de expertos y aplicación piloto antes de la recolección definitiva. Esta plantilla facilita el registro; no constituye por sí sola validación metodológica ni autorización para recoger datos.
