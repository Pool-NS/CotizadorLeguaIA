* Plantilla para importar observaciones reales anonimizadas.
* Sustituye la ruta por la ubicación local del CSV ya completado.
GET DATA
  /TYPE=TXT
  /FILE='C:\RUTA\observaciones_reales.csv'
  /ENCODING='UTF8'
  /DELCASE=LINE
  /DELIMITERS="," 
  /QUALIFIER='"'
  /ARRANGEMENT=DELIMITED
  /FIRSTCASE=2
  /VARIABLES=
    record_id A32
    phase A12
    observation_date A12
    service_type A32
    ai_used F1.0
    ai_interpretation_reviewed F1.0
    start_time A8
    final_price_time A8
    quotation_time_minutes F8.2
    quote_correct F1.0
    error_count F4.0
    error_codes A32
    final_price_communicated_same_visit F1.0
    exclusion_reason A120.
CACHE.
EXECUTE.

VARIABLE LABELS
  record_id 'Código anónimo de observación'
  phase 'Fase definida en el diseño aprobado'
  service_type 'Servicio observado'
  quotation_time_minutes 'Minutos hasta comunicar precio final'
  quote_correct 'Cotización correcta según instrumento aprobado'
  final_price_communicated_same_visit 'Precio comunicado durante la misma visita'.

VALUE LABELS ai_used ai_interpretation_reviewed quote_correct final_price_communicated_same_visit
  0 'No' 1 'Sí'.
VARIABLE LEVEL phase service_type (NOMINAL)
  ai_used ai_interpretation_reviewed quote_correct final_price_communicated_same_visit (NOMINAL)
  quotation_time_minutes error_count (SCALE).
EXECUTE.
