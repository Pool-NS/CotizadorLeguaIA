# Plan de medición: eficiencia y evidencia de Firebase

## Propósito y límites

Este plan define registros reproducibles; no contiene datos observados ni umbrales aprobados. No se inventan resultados del proceso comercial ni se generan cotizaciones sintéticas. El docente/asesor debe aprobar protocolo, umbrales y correspondencia con ISO/IEC 9126 antes de interpretar el porcentaje de cumplimiento.

ISO/IEC 9126-1:2001 está retirada. Si la matriz del curso exige esa edición, cítese como modelo histórico solicitado por el curso y no se declare certificación. Regístrese la edición exacta y la fuente institucional; no reemplazarla unilateralmente por ISO/IEC 25010. La norma no prescribe un umbral universal de segundos, CPU, RAM o porcentaje de cumplimiento para esta app.

## Variables y separación de mediciones

| Categoría | Medida | Fuente | Unidad / cálculo | Estado |
|---|---|---|---|---|
| Proceso de cotización | Duración total registrada con app | `startedAtEpochMs` → `priceCommunicatedAtEpochMs` en Room/Firestore | minutos = diferencia ms / 60 000 | Datos reales solo tras ejecutar protocolo; marcas no independientes. |
| Proceso de cotización | Exactitud | Auditoría humana independiente, instrumento aprobado | correctas / evaluadas × 100 | No la infiere Firebase ni la IA; falta captura independiente aprobada. |
| Proceso de cotización | Oportunidad | Registro de solicitudes elegibles y precio comunicado en el acto | comunicadas / elegibles × 100 | La casilla actual es declaración del operador y no prueba por sí sola el hecho. |
| Eficiencia técnica | Tiempo de interpretación IA | Firebase Performance `ai_interpretation` y trazas Android | ms agregados por Firebase, según muestreo/configuración | No equivale a duración completa de atención. |
| Eficiencia técnica | Duración del envío cloud | Firebase Performance `cloud_quote_sync` | duración de traza, conteos de intento/subida/fallo | No equivale al tiempo de cotización. |
| Eficiencia técnica | Memoria / CPU / red del teléfono | Android Studio Profiler, Perfetto y/o Macrobenchmark aprobados | según herramienta/protocolo | Firebase Performance no registra toda esta medición por fila. |
| Cumplimiento de requisitos | Pruebas dentro de metas acordadas | ejecución reproducible contra requisitos no funcionales aprobados | pruebas que cumplen / pruebas válidas × 100 | No calcular hasta aprobar umbrales y plan de prueba. |

`AUT` no se propone como variable mediadora. La matriz completa y la aprobación del asesor no están adjuntas al repositorio; este archivo no cambia objetivos, hipótesis, instrumento ni matriz oficial.

## Registro técnico exportable

La plantilla vacía `qa/data/technical_efficiency_runs_template.csv` tiene una fila por métrica medida en una repetición. Completa solo después de medir en dispositivo; no contiene datos simulados. Usa un `run_id` aleatorio por repetición, `phase` como `pre` o `post` solo cuando lo haya asignado el protocolo aprobado y un `operator_code_anonymous` no reversible.

Una misma repetición puede generar filas separadas `cpu_percent`, `memory_rss_kib`, `network_rx_bytes`, `network_tx_bytes` y `duration_ms`, cada una con su `source_tool` real. No copies memoria ni CPU desde Firebase Performance; deben venir de los registros Android Profiler/Perfetto. `threshold_value`, `threshold_id` y `outcome` se dejan vacíos hasta que el asesor apruebe criterios. La plantilla no debe rellenarse con valores de ejemplo para la tesis.

No almacenar email, UID de Firebase, número de teléfono, nombre de cliente, texto libre, token, precio real o identificadores publicitarios en el dataset analítico. Para registros comerciales se usa el exportador CSV local minimizado; el origen debe quedar etiquetado como Android y el período debe seguir el protocolo del estudio.

## Protocolo reproducible propuesto para aprobación

1. El asesor define la unidad observacional, muestra, servicios y criterios de exactitud/oportunidad, e indica la edición del modelo de calidad. Mantener el protocolo pretest/posttest consistente y etiquetar cada fase de forma externa; la app no asigna fase.
2. Definir y aprobar requisitos no funcionales con umbral, unidad, condiciones de prueba, percentil o estadístico, número de repeticiones, tolerancia y criterio de exclusión. Dejarlos en blanco hasta recibir aprobación.
3. Registrar versión, commit, dispositivo, nivel Android, red y condiciones. Usar solicitudes de ensayo no personales y tarifas controladas. No usar registros ficticios para resultados de tesis.
4. Repetir cada escenario bajo las condiciones acordadas; exportar resultados crudos sin redondeo, mantener el archivo original de solo lectura y documentar toda limpieza.
5. Para cada métrica técnica calcular estadísticos descriptivos solo con ejecuciones válidas. Reportar numerador, denominador, datos ausentes, exclusiones y limitaciones.
6. Calcular cumplimiento únicamente cuando exista una meta aprobada: `100 × cantidad de ejecuciones válidas que cumplen / cantidad total de ejecuciones válidas evaluadas`.
7. Evaluar exactitud con una persona revisora y regla definida antes de observar el resultado; cuando sea posible, conservar revisión doble y adjudicación. No transformar los cambios de IA en una medida de exactitud.
8. Guardar exportaciones y capturas dentro de `docs/tesis/evidencias/` sin datos identificables, junto al commit y configuración de prueba.

### Captura técnica con herramientas Android oficiales

1. Usa un teléfono físico, anota fabricante/modelo, versión Android/API, variante, commit, nivel de batería y tipo/condición de red. Para resultados comparables ejecuta un build `profileable` cercano a `release`; no presentes la depuración instrumentada como medición de producción. La guía oficial de Android recomienda perfilar una variante release con `profileable` cuando se busca bajo overhead.
2. Abre **View → Tool Windows → Profiler**, selecciona el proceso `com.leguia.cotizadoria` y registra una sesión que incluya una cotización completa. Usa CPU/System Trace para actividad de CPU e hilos y la vista de memoria/RSS para RAM; exporta el archivo de grabación o conserva una captura con dispositivo y escala legibles.
3. Para red, limita la selección temporal a la misma operación y registra bytes recibidos y enviados desde el Network Profiler/inspector compatible con la versión de Android Studio instalada. Guarda ambos sentidos por separado. El conteo de red no sustituye CPU/RAM ni el tiempo de cotización.
4. Repite el mismo caso y condiciones según el número de repeticiones aprobado. Deja que el profiler complete el parseo antes de guardar la evidencia. Anota cambios de red, temperatura, batería o actividad de fondo; no borres ejecuciones, márcalas como excluidas y documenta el motivo según el protocolo.
5. Exporta una fila por métrica/repetición a `technical_efficiency_runs_template.csv`, con referencia al archivo/captura del profiler. Evita incluir payloads, textos del cliente, correo, UID, tokens o datos de cuenta.

Fuentes de los instrumentos: [Android Studio Profiler](https://developer.android.com/studio/profile/), [vista de CPU y memoria en vivo](https://developer.android.com/studio/profile/inspect-app-live) y [sistema de trazas](https://developer.android.com/studio/profile/cpu-profiler). La consola Firebase Performance ofrece trazas agregadas y tiempos de red para el SDK; no sustituye estas mediciones de recursos en el dispositivo.

## Tabla de umbrales y evidencia

| ID | Requisito no funcional | Umbral | Condiciones | Aprobación | Evidencia |
|---|---|---|---|---|---|
| ENF-01 | Tiempo de interacción | Pendiente de aprobación | Dispositivo, red, percentil/repeticiones por definir | Asesor | CSV de ejecución |
| ENF-02 | Uso de RAM | Pendiente de aprobación | Dispositivo/escenario por definir | Asesor | Profiler/Perfetto |
| ENF-03 | Uso de CPU | Pendiente de aprobación | Dispositivo/escenario por definir | Asesor | Profiler/Perfetto |
| ENF-04 | Datos enviados por operación | Pendiente de aprobación | Red y escenario por definir | Asesor | Inspector de red/perfil |
| ENF-05 | Cumplimiento global de eficiencia | Pendiente de aprobación | Fórmula/protocolo por definir | Asesor | Resumen reproducible |

## Pruebas implementadas en esta rama

El directorio `firebase-tests/` usa Firebase Local Emulator Suite para comprobar que: (a) acceso anónimo es rechazado, (b) una persona autenticada sin membresía activa es rechazada, (c) el miembro activo puede crear y leer el registro mínimo permitido, (d) texto libre adicional y actualizaciones se rechazan, y (e) otra organización no puede leer los datos. Estas son pruebas de reglas de acceso; no demuestran el comportamiento de un proyecto cloud real ni la validez de las métricas de tesis.

## Referencias oficiales del producto

- [ISO/IEC 9126-1:2001, página de ISO y estado](https://www.iso.org/standard/22749.html)
- [ISO/IEC 25010:2023, página de ISO](https://www.iso.org/standard/78176.html)
- [Firebase Performance Monitoring](https://firebase.google.com/docs/perf-mon)
- [Android Studio Profiler](https://developer.android.com/studio/profile)
- [Perfetto](https://perfetto.dev/docs/)
- [Plantilla vacía de ejecuciones técnicas](../data/technical_efficiency_runs_template.csv)
