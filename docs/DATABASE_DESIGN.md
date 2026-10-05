# Diseño de datos Room v4

| Entidad | PK | Campos principales | FK/cardinalidad | Índices / razón |
|---|---|---|---|---|
| Servicio | id | nombre, descripción | padre de parámetros y cotizaciones | —; catálogo |
| Material | id | nombre, tipo | padre de parámetros, cotización opcional, requerimiento y stock | —; catálogo |
| ParametroPrecio | id | servicioId, concepto, precioUnitario, unidadMedida, activo | Servicio 1:N | servicioId; recuperar reglas por servicio |
| Cotizacion | id | código, servicioId, operador, requerimiento, medidas opcionales, precio/descuento, estado, fechas, offline | Servicio N:1; Material opcional | código único, servicio, material, estado, fecha |
| EventoTrazabilidad | id | cotizacionId opcional, evento, descripción, timestamp, operador | Cotización 1:N | cotización, timestamp |
| RegistroError | id | cotizacionId, código, detalle, atribuible, timestamp | Cotización 1:N | cotización, código |
| Proforma | id | cotizacionId, código, fecha, responsable, observación | Cotización 1:0..1 (cotizacionId único) | cotización único y fecha |
| TrabajoProduccion | id | cotización, servicio, fecha prevista, prioridad, estado, responsable, observaciones | Cotización 1:N; Servicio 1:N | cotización, servicio, fecha, estado |
| RequerimientoMaterial | id | trabajo, material, cantidad, unidad, origen | Trabajo N:M Material mediante entidad puente | trabajo y material |
| Stock | id | material único, cantidad disponible, unidad, origen, actualización | Material 1:0..1 | material único |
| MovimientoStock | id | material, cambio, tipo, fecha, responsable, observación, origen | Material 1:N | material, fecha, origen |
| NecesidadAbastecimiento | id | material, trabajo opcional, cantidad, unidad, estado, creador, responsable | Material 1:N; Trabajo opcional | material, trabajo, estado |
| Prediccion | id | cotización, probabilidad, clase, fecha, versión, sintético | Cotización 1:N | cotización y fecha |

Migración implementada: 3→4, conserva filas previas y crea las tablas nuevas. La migración anterior 2→3 se conserva. No se implementó una ruta 1→2 porque el ZIP no aporta el esquema v1: esa instalación requiere identificar y probar su esquema real antes de actualizar; no se usa fallback destructivo. `exportSchema=false` heredado limita la verificación automatizada y debe corregirse al incorporar snapshots de esquema versionados.

Las referencias de consumo/cantidad deben ingresarse con unidad y origen explícitos; entidades que representan inventario no tienen origen REAL por defecto. No se agrega stock demo al catálogo real. Los DAOs permiten lectura/registro manual, sin reserva ni descuento automático desde cotizaciones. La compra/abastecimiento requiere decisión humana.
