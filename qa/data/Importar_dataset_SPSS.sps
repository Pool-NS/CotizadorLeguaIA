* Importa el dataset sintético de 200 cotizaciones en SPSS.
* Si guardaste el repositorio en otra carpeta, actualiza la ruta de FILE.
GET DATA
  /TYPE=TXT
  /FILE='C:\Users\ns62\Documents\ChatGPT\CotizadorLeguaIA.git\qa\data\synthetic_quotes_200.csv'
  /ENCODING='UTF8'
  /DELCASE=LINE
  /DELIMITERS="," 
  /QUALIFIER='"'
  /ARRANGEMENT=DELIMITED
  /FIRSTCASE=2
  /VARIABLES=
    quote_id A8
    service_type A20
    quote_amount F8.2
    discount_percentage F5.2
    requirement_complete F1.0
    ai_interpretation_reviewed F1.0
    quotation_time_minutes F3.0
    days_to_requested_date F3.0
    customer_response_days F2.0
    status A20
    converted_to_sale F1.0
    synthetic_flag A5.
CACHE.
EXECUTE.

VARIABLE LABELS
  quote_id 'Identificador sintético de cotización'
  service_type 'Tipo de servicio'
  quote_amount 'Importe de cotización (unidades monetarias no especificadas)'
  discount_percentage 'Descuento porcentual simulado'
  requirement_complete 'Indica si los datos del requerimiento están completos'
  ai_interpretation_reviewed 'Indica revisión simulada de una interpretación; no implica IA integrada'
  quotation_time_minutes 'Tiempo simulado para preparar la cotización, en minutos'
  days_to_requested_date 'Días entre cotización y fecha solicitada'
  customer_response_days 'Días simulados hasta respuesta del cliente'
  status 'Estado sintético de la cotización'
  converted_to_sale 'Indica si la cotización se convirtió en venta según los datos sintéticos'
  synthetic_flag 'Marca de registro sintético'.

VALUE LABELS
  requirement_complete 0 'No' 1 'Sí'
  ai_interpretation_reviewed 0 'No revisada' 1 'Revisada'
  converted_to_sale 0 'No convertida' 1 'Convertida'.

VALUE LABELS service_type
  'Carpas' 'Carpas'
  'Toldos' 'Toldos'
  'Tapizado' 'Tapizado'.

VALUE LABELS status
  'CONCRETADA' 'Concretada'
  'NO_CONCRETADA' 'No concretada'.

VARIABLE LEVEL
  quote_id service_type status synthetic_flag (NOMINAL)
  requirement_complete ai_interpretation_reviewed converted_to_sale (NOMINAL)
  quote_amount discount_percentage quotation_time_minutes
  days_to_requested_date customer_response_days (SCALE).
FORMATS quote_amount (F8.2) discount_percentage (F5.2).
EXECUTE.

* Comprobaciones descriptivas iniciales.
FREQUENCIES VARIABLES=service_type status converted_to_sale synthetic_flag.
DESCRIPTIVES VARIABLES=quote_amount discount_percentage quotation_time_minutes
  days_to_requested_date customer_response_days.
