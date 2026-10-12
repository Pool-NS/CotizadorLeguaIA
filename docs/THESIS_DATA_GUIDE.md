# Guía de datos para el Capítulo IV

## Qué establece el diseño de la tesis

La tesis compartida plantea un estudio cuantitativo, aplicado y preexperimental de un grupo con medición antes y después de implementar la aplicación (pretest → intervención → posttest). La unidad de análisis es cada solicitud real de cotización atendida en Leguía. Las dimensiones principales de la variable dependiente son tiempo de cotización, exactitud y cumplimiento u oportunidad.

Por ello, la fuente para contrastar las hipótesis debe ser el registro de solicitudes reales observadas en el negocio. El CSV `synthetic_quotes_200.csv` contiene 200 filas simuladas y se conserva para practicar la importación, descriptivos y el flujo de jamovi/SPSS. `synthetic_quote_stock_pricing_200.csv` añade supuestos ficticios de consumo, precios y stock para demostrar el dashboard y los estados de factibilidad. Ninguno es historial de Leguía, ninguno entrena el componente de interpretación y ninguno puede respaldar resultados empíricos ni valores p de la tesis. Consulta [README_STOCK_DEMO.md](../qa/data/README_STOCK_DEMO.md) para ver las limitaciones de esos escenarios.

## Cómo levantar los registros

1. Confirma con el docente y el responsable del negocio el tamaño objetivo, los periodos y los criterios de inclusión. La tesis define una muestra por conveniencia y periodos pretest/posttest de igual duración; el tamaño final depende de las solicitudes reales elegibles. Si el docente exige 200 observaciones, acuerda el reparto entre fases antes de recolectar. No fabriques registros para completar la cuota.
2. Observa cotizaciones reales de carpas, toldos y tapizado de moto, siguiendo el alcance de la tesis. Ropa de moto está fuera de ese alcance.
3. Usa el mismo criterio de inicio y fin, instrumento, alcance y reglas de registro en ambas fases. Pretest: sin la app. Posttest: con la app después de su implementación y familiarización.
4. Asigna un código aleatorio por solicitud. No registres nombres, teléfonos, direcciones ni texto que identifique al cliente. Conserva cualquier archivo de correspondencia por separado, con acceso limitado, solo si fuese imprescindible y estuviera autorizado.
5. Registra la hora de inicio y la hora de comunicación del precio final. La duración en minutos debe poder reconstruirse a partir de esos momentos.
6. Evalúa exactitud con los criterios de error E1–E6 definidos en el instrumento de tesis. E7–E9 son situaciones que no deben contarse como errores atribuibles al proceso.
7. Marca cumplimiento cuando el precio final se comunica durante la misma atención. No inventes un umbral de minutos.
8. Revisa integridad y consistencia sin cambiar valores para favorecer la hipótesis. Documenta exclusiones con su motivo.

## Plantilla del registro observacional

`qa/data/observed_quotes_template.csv` es una plantilla vacía para un registro por solicitud real. Está separada del CSV sintético. Completa `phase` con `PRETEST` o `POSTTEST`; usa `1` para sí y `0` para no. `error_codes` acepta códigos como `E1;E4`; si no hubo errores, déjalo vacío y registra `error_count` como `0`. La duración debe ser coherente con las horas observadas. No reemplaces celdas faltantes con datos inventados.

Columnas:

| Campo | Uso |
|---|---|
| `record_id` | Código aleatorio, sin datos identificables. |
| `phase` | Condición PRETEST o POSTTEST. |
| `observation_date` | Fecha de la observación. |
| `service_type` | Carpas, Toldos o Tapizado de moto. |
| `ai_used` | Si se usó la interpretación generativa durante la cotización. |
| `ai_interpretation_reviewed` | Si la persona cotizadora revisó la propuesta de IA. |
| `start_time`, `final_price_time` | Horas observadas con formato `HH:MM:SS`. |
| `quotation_time_minutes` | Duración calculada entre inicio y comunicación del precio final. |
| `quote_correct` | Resultado de la verificación con criterios definidos en la tesis. |
| `error_count`, `error_codes` | Número y categorías E1–E6 atribuibles. |
| `final_price_communicated_same_visit` | Indicador de cumplimiento u oportunidad. |
| `exclusion_reason` | Motivo, si el registro se excluye; vacío si se incluye. |

Guarda la base de trabajo en un lugar privado y con acceso autorizado. No subas observaciones de clientes a un repositorio público. Antes de usar jamovi, SPSS o cualquier otra herramienta, elimina identificadores y verifica que la codificación sea coherente.

## Análisis estadístico y uso de SmartPLS

El diseño descrito no plantea un modelo de constructos latentes medidos mediante una encuesta. Sus indicadores son observaciones de eventos: minutos, errores/cotizaciones correctas y cumplimiento sí/no. Por eso, SmartPLS no es la herramienta que se desprende directamente de esta operacionalización. Para el plan descrito, jamovi o SPSS permiten estadística descriptiva y pruebas de comparación pretest-posttest.

El libro [synthetic_quotes_stock_smartpls_demo.xlsx](../qa/data/synthetic_quotes_stock_smartpls_demo.xlsx) contiene `PLS_solo_practica` para comprobar que SmartPLS importe una tabla numérica y `SmartPLS_guia` con sus límites. Las filas son simuladas: no miden constructos de tesis y no sirven para fiabilidad, validez, hipótesis ni resultados del Capítulo IV. SmartPLS informa alfa de Cronbach ≥ 0.70 como criterio usual de consistencia interna, pero el valor depende de indicadores múltiples del instrumento real; no se debe fabricar ni garantizar con 200 filas sintéticas. Consulta la [referencia oficial de umbrales de SmartPLS](https://www.smartpls.com/documentation/functionalities/thresholds/) y confirma con el docente si el modelo de tesis realmente requiere PLS-SEM.

- Si las observaciones de ambas fases son independientes, selecciona la prueba según el tipo de indicador y los supuestos: tiempo (t para muestras independientes o Mann–Whitney); proporciones de exactitud y cumplimiento (chi-cuadrado o Fisher cuando corresponda).
- Solo trates mediciones como pareadas cuando exista una correspondencia válida y verificable de las mismas unidades entre fases; entonces usa pruebas para datos relacionados.
- Presenta descriptivos, prueba elegida, valor p, tamaño de efecto e interpretación vinculada a cada hipótesis. La elección final debe seguir el plan aprobado y la estructura real de la muestra.
- Considera SmartPLS únicamente si el docente cambia o amplía el modelo hacia constructos latentes con indicadores de encuesta y valida previamente hipótesis, instrumento, muestra y plan de análisis. No conviertas columnas de eventos en indicadores PLS solo para usar el programa.

## Papel de la IA, precios y stock

En el prototipo actual, Gemini interpreta texto y propone campos estructurados; una persona revisa la propuesta. La IA no determina la tarifa final, no verifica por sí sola el stock y no declara que una cotización sea comercialmente aceptable. El precio se calcula mediante reglas y tarifas aprobadas configuradas en la aplicación. Room es una base local por instalación y el backend FastAPI no es una base de datos compartida.

La evaluación técnica de la interpretación de IA requiere un conjunto separado de frases de prueba con etiquetas esperadas verificadas por personas conocedoras del proceso. Debe medirse, por ejemplo, la proporción de campos interpretados correctamente y los errores por campo; las frases no sustituyen las solicitudes reales de pretest/posttest.

Un análisis futuro de factibilidad de materiales debería consultar inventario estructurado, consumos o lista de materiales y trabajos ya confirmados. Las reglas deterministas podrían devolver estados como `suficiente`, `faltante` o `requiere revisión`, mostrando cantidades y fecha de actualización. Una cotización no confirmada debe ser solo una simulación: no reservar stock ni iniciar compras. Este componente requiere reglas de consumo validadas, inventario compartido confiable y permisos; no está implementado por desplegar la API de Gemini en Render.

## Privacidad, autorización y reproducibilidad

Obtén la autorización que corresponda antes de observar y procesar información del negocio. Limita la base a las variables necesarias, conserva los datos en almacenamiento autorizado y documenta la versión del instrumento, las fechas de observación, las reglas de depuración y el software utilizado. Publica únicamente agregados anonimizados autorizados. El CSV sintético debe identificarse como sintético en tablas, gráficos y anexos.
