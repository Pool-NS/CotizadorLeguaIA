# Modelo de datos actual y objetivo

Room actual: Servicio, Material, ParametroPrecio, Cotizacion, EventoTrazabilidad y RegistroError. Cotizacion referencia Servicio y opcionalmente Material; los eventos se vinculan opcionalmente con cotización.

Modelo objetivo a evaluar: Proforma (1:0..1 Cotizacion concretada), TrabajoProduccion (cotización 1:N según decisión del negocio), RequerimientoMaterial (trabajo N:M material), Stock y MovimientoStock (movimientos auditables), NecesidadAbastecimiento y Prediccion. Desde Room v4 se añadieron Proforma, TrabajoProduccion, RequerimientoMaterial, Stock, MovimientoStock, NecesidadAbastecimiento y Prediccion, con migración explícita 3→4 y DAOs. Aún faltan pantallas y pruebas de migración. Los requerimientos de materiales se registran solo para trabajos; no hay reserva/descuento automático. Antes de migrar, acordar cardinalidades, control de concurrencia e inventario inicial con Leguía. Índices actuales: código de cotización único y FK/estado/fecha. No se añade cantidad consumida ni regla de precio ficticia.

Diseño detallado, campos, claves foráneas y justificación de índices: [DATABASE_DESIGN.md](DATABASE_DESIGN.md).
