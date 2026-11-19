# Índice de evidencias para tesis

| Funcionalidad | Requisito | Caso de uso | Entidad/capa actual | Prueba | Indicador | Evidencia | Estado
|---|---|---|---|---|---|---|---|
| Formulario de requerimiento | Captura y validación | Registrar solicitud | `CotizacionEntity`, Compose | TC-001…TC-060; unit UI pendiente | tiempo (inicio/fin parcial) | `PantallaFormularioServicio.kt` | Parcial
| Cálculo configurable | Sin reglas inventadas | Calcular referencial | `QuotePricingCalculator` | `PricingWorkflowTest` escrito, no ejecutado | exactitud por validar con negocio | `domain/Pricing.kt` | Parcial
| Revisión IA | IA sugiere, humano decide | Interpretar requerimiento | Android → FastAPI → Gemini; Room guarda evidencia local | 5 pruebas unitarias simuladas; prueba con API/dispositivo pendiente | correcciones humanas pendientes | `backend/api/app.py`, `BackendGenerativeAiClient.kt` | Integrada; servicio requiere clave y URL configuradas
| Estado comercial | Mantener resultado trazable | Concretar/no concretar | `QuoteWorkflow`; Room pendiente | TC-091…TC-115 no ejecutados | tasa de conversión futura | `domain/QuoteWorkflow.kt` | Lógica aislada
| Proforma/producción/materiales | Continuidad operativa | Venta a trabajo | Sin entidades nuevas persistidas | TC-116…TC-175 no ejecutados | no calculado | Diseño en `docs/DATA_MODEL.md` | Pendiente
| Predictor | No forma parte del alcance implementado | No hay evaluación predictiva | No hay modelo conectado | No aplica | No reportar métricas predictivas | `docs/PREDICTION.md` | No implementado
| Calidad del software | Evaluación objetiva | Revisión/ejecución | Documentación y configuración | QA pendiente | no determinado | `qa/FINAL_QUALITY_REPORT.md` | Parcial

Separación obligatoria: (A) resultados de software: cambios y comandos con evidencia; (B) no hay modelo predictivo en el producto; (C) resultados experimentales reales de la investigación: se deben recolectar y documentar con autorización. No inferir mejora del proceso de Leguía sin observaciones reales.
