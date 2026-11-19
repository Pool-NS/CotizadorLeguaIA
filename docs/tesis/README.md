# Evidencia técnica para el capítulo IV

**Alcance:** auditoría del código de este repositorio y mejoras de instrumentación, interfaz y documentación.  
**Rama de trabajo:** `codex/tesis-audit`.  
**Commit base al iniciar:** `72801f8` (`ci: retain CodeQL SARIF without GitHub Code Scanning`).  
**Versión Android observada:** 1.5.0, `versionCode` 8.  
**Esquema Room observado:** versión 9.  
**Fecha de auditoría:** 2026-11-19.

El commit indicado es la base de la auditoría. Los archivos de esta rama se modifican durante el trabajo; el commit final solo podrá consignarse después de confirmar esos cambios. No se afirma que el repositorio remoto esté actualizado.

## Documentos

- [Informe de auditoría](01_informe_auditoria.md): arquitectura real, hallazgos y alcance de las etapas.
- [Diagramas de arquitectura](02_arquitectura.md): contexto, componentes Android, flujo Gemini y datos Room.
- [Datos e indicadores](03_datos_e_indicadores.md): qué se captura, fórmulas, denominadores y propuesta de registro.
- [Matriz de requisitos, pruebas y evidencia](04_matriz_pruebas_evidencia.md): trazabilidad verificable, resultados y pendientes.
- [Limitaciones y capturas](05_limitaciones_y_capturas.md): límites conocidos y estado de capturas visuales.

## Cómo interpretar esta evidencia

Las compilaciones y pruebas de software evalúan propiedades técnicas del prototipo. No demuestran por sí mismas que el proceso comercial haya mejorado. El pretest y posttest deben provenir de observaciones reales autorizadas y del instrumento aprobado. No se generan registros de campo ni resultados estadísticos sintéticos.
