# Evidencia técnica para el capítulo IV

**Alcance:** auditoría del código de este repositorio y mejoras de instrumentación, interfaz y documentación.  
**Rama de auditoría documentada:** `codex/tesis-audit`.
**Commit de implementación auditado:** `cc07eab` (`feat: improve thesis evidence and cotizador UX`).
**Versión Android observada:** 1.5.0, `versionCode` 8.  
**Esquema Room en la auditoría original:** versión 9; la rama `codex/firebase-integration` añade migración a versión 10, aún pendiente de compilación.
**Fecha de auditoría:** 2026-11-19.

La auditoría inicial corresponde al commit base `72801f8` y a `cc07eab`; esos resultados no son resultados de compilación de la rama Firebase. El estado de integración, despliegue y pruebas cloud se informa por separado en los documentos enlazados abajo. No hay confirmación de despliegue Firebase en un proyecto real.

## Documentos

- [Informe de auditoría](01_informe_auditoria.md): arquitectura real, hallazgos y alcance de las etapas.
- [Diagramas de arquitectura](02_arquitectura.md): contexto, componentes Android, flujo Gemini y datos Room.
- [Datos e indicadores](03_datos_e_indicadores.md): qué se captura, fórmulas, denominadores y propuesta de registro.
- [Matriz de requisitos, pruebas y evidencia](04_matriz_pruebas_evidencia.md): trazabilidad verificable, resultados y pendientes.
- [Limitaciones y capturas](05_limitaciones_y_capturas.md): límites conocidos y estado de capturas visuales.
- [Firebase: configuración y despliegue](../firebase/CONFIGURACION_Y_DESPLIEGUE.md): servicios conectados por código y pasos para usar un proyecto real.
- [Plan de medición de eficiencia](../../qa/firebase/plan-medicion-eficiencia.md): medidas, condiciones y umbrales pendientes de aprobación.

## Cómo interpretar esta evidencia

Las compilaciones y pruebas de software evalúan propiedades técnicas del prototipo. No demuestran por sí mismas que el proceso comercial haya mejorado. El pretest y posttest deben provenir de observaciones reales autorizadas y del instrumento aprobado. No se generan registros de campo ni resultados estadísticos sintéticos.
