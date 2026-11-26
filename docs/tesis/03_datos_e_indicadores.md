# Datos e indicadores para el estudio

## Registros actuales

Room guarda `codigoCotizacion`, servicio, requerimiento libre, operador local, marcas de inicio y estado, modo offline, dimensiones, material, resultado comercial, indicador de corrección humana de campos IA y (desde Room 9) marca de precio comunicado y estado de completitud de esa captura.

En la rama Firebase (Room 10), cada cotización obtiene un ID aleatorio estable y un estado de sincronización (`LOCAL_ONLY`, `PENDING`, `SYNCED` o `FAILED`). Firestore solo recibe las filas nuevas creadas con una sesión autenticada y membresía activa; las filas locales anteriores no se cargan retrospectivamente. El CSV sigue siendo una exportación del teléfono y agrega el estado/ID cloud. La columna `assistance_mode` identifica captura manual o asistida por IA; no representa conectividad.

El requerimiento del cliente y operador se excluyen de la exportación. El CSV Android contiene ahora `record_id` derivado de la PK local de Room, fecha y marcas UTC, servicio, fuente del registro, conectividad, versión, duración en minutos y confirmación del operador. Por minimización, no exporta texto, cliente, operador ni importe comercial.

Este CSV automático no reemplaza la plantilla manual `qa/data/observed_quotes_template.csv`: tienen propósitos y fuentes distintos. Antes de unirlos para análisis, normaliza formatos y documenta equivalencias sin cambiar la definición del instrumento.

## Comparación con la matriz de la tesis

| Indicador | Fuente actual | Estado / limitación |
|---|---|---|
| Tiempo de cotización | Inicio en app → marca de precio comunicado por operador | Calculable para cotizaciones con ambas marcas válidas. Es tiempo registrado por el flujo Android, no reemplaza el cronometraje aprobado. No produce pretest sin app. |
| Exactitud | No hay evaluación independiente almacenada | Pendiente. Requiere aplicar criterios de error/corrección del instrumento aprobado; CSV deja celdas vacías. |
| Cumplimiento/oportunidad | Checkbox de confirmación del operador | Porcentaje sobre capturas completas, con numerador y denominador expuestos. No es corroboración externa. |
| Errores atribuibles | `RegistroErrorEntity` existe y su DAO permite persistir/consultar | La inspección no encontró su captura conectada al flujo cotidiano de cotización. No usar como indicador poblado. |
| Corrección humana | `corregidoPorHumano` para cambios posteriores a la propuesta IA | Solo describe que hubo cambio de campos tras propuesta IA; no equivale a exactitud del precio ni a error atribuible. |
| Fase PRETEST/POSTTEST | Asignación en protocolo externo | No la decide la app; CSV queda vacío para evitar clasificación automática incorrecta. |
| Modalidad de atención | No consta en `CotizacionEntity` | Campo de exportación vacío; debe acordarse si se refiere a mostrador, llamada u otra modalidad y capturarse con el mismo criterio en ambas fases. |
| Versión de software | `BuildConfig.VERSION_NAME` en exportación | Captura automática (1.5.0 en el artefacto inspeccionado). |

## Estructura de registro propuesta

La exportación Android contiene las columnas automáticas y deja explícitos los campos que requieren observación independiente:

| Campo | Quién / cómo se obtiene | Uso |
|---|---|---|
| `record_id` | Android, PK local estable convertida a `OBS-######` | Clave de fila; única dentro de una instalación Room, no ID compartido entre dispositivos. |
| `observation_date_utc` | Derivada de inicio | Agrupación temporal. |
| `start_timestamp_utc` | Automático al iniciar flujo en app | Inicio del intervalo app. |
| `price_communication_timestamp_utc` | Checkbox/confirmación al guardar | Marca declarada por el operador. Vacío si no confirma comunicación. |
| `quotation_time_minutes` | Automático: diferencia de marcas ÷ 60 000 | Unidad compatible con la matriz. |
| `service_type` | Catálogo local | Estratificación por servicio. |
| `attention_modality` | Observador cuando exista definición aprobada | No se infiere del modo online/offline. |
| `phase_pre_post` | Observador siguiendo protocolo aprobado | El sistema no decide la fase del diseño preexperimental. |
| `record_source` / `assistance_mode` | Android | Distingue la captura de Android y si se usó asistencia manual o IA; no es tipo de conectividad ni clasifica pre/post. |
| `software_version` | Android `BuildConfig.VERSION_NAME` | Trazabilidad de la intervención. |
| `operator_confirmed_price_communication` | Checkbox del operador, `1`/`0` solo si se completó la captura | Aproximación de oportunidad declarada, no auditoría independiente. |
| `quote_correct_independent_audit` | Observador con instrumento aprobado | Vacío en exportación hasta captura/validación independiente. |
| `attributable_error_count`, `attributable_error_codes` | Observador con catálogo de errores aprobado | Vacíos hasta registrar eventos reales; no rellenar con ceros supuestos. |
| `ai_fields_corrected_by_operator` | Campo local de corrección tras propuesta IA | `1`/`0` solo cuando hubo interpretación; vacío sin IA. No mide exactitud global. |

Para el pretest sin app usa un registro manual independiente, basado en el mismo código anónimo, protocolo, instrumentos y criterios. No fabriques datos ni fusiones pretest manual y posttest automático sin documentar sus fuentes y diferencias.

## Definiciones, fórmulas y denominadores

No se cambian las definiciones aprobadas por la matriz:

- **Tiempo (min):** `comunicación de precio − inicio`. App calcula el intervalo cuando ambas marcas existen y son válidas. `n_tiempo` = número de intervalos válidos. Promedio = suma de duraciones ÷ `n_tiempo`; mediana = dato central ordenado o promedio de los dos centrales si `n_tiempo` es par.
- **Exactitud (%):** `(cotizaciones correctas ÷ cotizaciones evaluadas) × 100`. Requiere un criterio independiente de corrección establecido en el instrumento. Hoy no hay numerador/denominador registrados por la app.
- **Cumplimiento/oportunidad (%):** `(solicitudes con precio final comunicado en la atención ÷ solicitudes observadas elegibles) × 100`, de acuerdo con la definición operativa aprobada. El dashboard solo muestra `confirmación positiva del operador ÷ capturas completas`, y no debe presentarse como resultado definitivo de tesis hasta cotejarlo con el instrumento.

Firebase Performance registra agregados técnicos de la interpretación IA y sincronización cloud. Estos trazos no son registros unitarios de atención y no deben mezclarse con tiempo, exactitud u oportunidad del proceso. La exactitud independiente permanece en blanco y el importe de la cotización no se sincroniza; por tanto, su verificación requiere el instrumento de observación y la fuente local autorizada descrita en [`qa/firebase/plan-medicion-eficiencia.md`](../../qa/firebase/plan-medicion-eficiencia.md).

Solicitudes, conversión y monto cotizado son métricas operativas complementarias, no sustitutos de los indicadores de investigación. No reportar coeficientes de hipótesis, alfa de Cronbach ni resultados estadísticos con datos vacíos o simulados.
