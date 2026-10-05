# Índice de evidencias para tesis

| Funcionalidad | Requisito | Caso de uso | Entidad/capa actual | Prueba | Indicador | Evidencia | Estado
|---|---|---|---|---|---|---|---|
| Formulario de requerimiento | Captura y validación | Registrar solicitud | `CotizacionEntity`, Compose | TC-001…TC-060; unit UI pendiente | tiempo (inicio/fin parcial) | `PantallaFormularioServicio.kt` | Parcial
| Cálculo configurable | Sin reglas inventadas | Calcular referencial | `QuotePricingCalculator` | `PricingWorkflowTest` escrito, no ejecutado | exactitud por validar con negocio | `domain/Pricing.kt` | Parcial
| Revisión IA | IA sugiere, humano decide | Interpretar requerimiento | `GenerativeAiClient` interface/fake | TC-061…TC-090 no ejecutados | correcciones humanas pendientes | `ai/GenerativeAiClient.kt` | Interfaz únicamente
| Estado comercial | Mantener resultado trazable | Concretar/no concretar | `QuoteWorkflow`; Room pendiente | TC-091…TC-115 no ejecutados | tasa de conversión futura | `domain/QuoteWorkflow.kt` | Lógica aislada
| Proforma/producción/materiales | Continuidad operativa | Venta a trabajo | Sin entidades nuevas persistidas | TC-116…TC-175 no ejecutados | no calculado | Diseño en `docs/DATA_MODEL.md` | Pendiente
| Predictor | Estimar conversión en sintéticos | Evaluación offline | pipeline sklearn sintético | script ejecutado, no test Android | métricas de test sintético | `qa/prediction/metrics.json` | Técnico sintético
| Calidad del software | Evaluación objetiva | Revisión/ejecución | Documentación y configuración | QA pendiente | no determinado | `qa/FINAL_QUALITY_REPORT.md` | Parcial

Separación obligatoria: (A) resultados de software: cambios y comandos con evidencia; (B) modelo predictivo: solo los datos sintéticos documentados; (C) resultados experimentales reales de la investigación: no proporcionados ni generados aquí. No inferir mejora del proceso de Leguía a partir de datos sintéticos.
