# Evidencia técnica para el capítulo IV

**Alcance:** auditoría del código de este repositorio y mejoras de instrumentación, interfaz y documentación.  
**Rama inicial de auditoría:** `codex/tesis-audit`.
**Commit base de auditoría:** `cc07eab` (`feat: improve thesis evidence and cotizador UX`).
**Versión base observada:** 1.5.0, `versionCode` 8. **Versión local actual:** 1.5.1, `versionCode` 9.
**Fecha de auditoría inicial:** 2026-11-19; actualización Firebase y eficiencia: 2026-11-26.

La auditoría inicial corresponde a los commits `72801f8` y `cc07eab`; sus resultados no son resultados de compilación de la rama Firebase. En 1.5.0 se confirmó una escritura Android en Firestore. La versión 1.5.1 añade lectura remota y espera ser instalada/verificada. La nueva prueba instrumentada de UI compila, pero Android canceló su instalación; no se reporta como aprobada.

## Documentos

- [Informe de auditoría](01_informe_auditoria.md): arquitectura real, hallazgos y alcance de las etapas.
- [Diagramas de arquitectura](02_arquitectura.md): contexto, componentes Android, flujo Gemini y datos Room.
- [Datos e indicadores](03_datos_e_indicadores.md): qué se captura, fórmulas, denominadores y propuesta de registro.
- [Matriz de requisitos, pruebas y evidencia](04_matriz_pruebas_evidencia.md): trazabilidad verificable, resultados y pendientes.
- [Limitaciones y capturas](05_limitaciones_y_capturas.md): límites conocidos y estado de capturas visuales.
- [Auditoría de eficiencia ISO/IEC 9126](06_auditoria_eficiencia_9126.md): alcance de mediciones, propuestas de umbral y estado verificable.
- [Firebase: configuración y despliegue](../firebase/CONFIGURACION_Y_DESPLIEGUE.md): servicios conectados por código y pasos para usar un proyecto real.
- [Plan de medición de eficiencia](../../qa/firebase/plan-medicion-eficiencia.md): medidas, condiciones y umbrales candidatos pendientes de aprobación.

## Cómo interpretar esta evidencia

Las compilaciones y pruebas de software evalúan propiedades técnicas del prototipo. No demuestran por sí mismas que el proceso comercial haya mejorado. El pretest y posttest deben provenir de observaciones reales autorizadas y del instrumento aprobado. No se generan registros de campo ni resultados estadísticos sintéticos.

La escritura cloud está demostrada para una cotización de prueba. La consulta desde la nueva versión, CPU/RAM/red en Profiler y el release 1.5.1 en App Distribution siguen pendientes de evidencia directa. La configuración del proyecto Firebase no equivale por sí sola a validar esos pasos.
