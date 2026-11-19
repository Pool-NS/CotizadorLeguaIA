# Guía operativa: stock y tarifas

## Registrar existencias

1. Abre **Panel de Administración** y entra en **Stock**.
2. Pulsa **Registrar material o ingreso**.
3. Escribe el nombre exacto del material (puedes tocar una sugerencia), elige categoría, indica opcionalmente su medida/presentación (por ejemplo, ancho 1.50 m o rollo de 50 m), registra la cantidad y su unidad.
4. Pulsa **Guardar ingreso de stock**. Si el material ya existe, el ingreso se suma al saldo y queda anotado como movimiento.
5. Para un material ya registrado, usa **Reponer** en su ficha. La app precarga nombre, medida y unidad; ingresa solo la cantidad recibida.
6. Si se usó o vendió material, registra **Salida / ajuste** e indica la cantidad real y el motivo.

No hay cantidades precargadas. El responsable debe comprobar el conteo y mantener medida y unidad consistentes para cada material. Si necesita almacenar una presentación de tamaño distinto, regístrela con otro nombre de material para no mezclar saldos. Los datos Room están en ese teléfono y no se comparten entre trabajadores.

## Registrar precios aprobados

1. En el panel del jefe, abre **Tarifas**.
2. Selecciona servicio y, para carpas, tipo abierta/cerrada.
3. Toca un tamaño frecuente o elige medida propia; confirma largo y ancho en metros.
4. Escribe el material con el mismo nombre que se usa en la cotización y el precio aprobado en soles.
5. Guarda una tarifa por combinación. La app deja el precio pendiente si no encuentra coincidencia exacta.

Los precios manuscritos deben ser confirmados por el responsable antes de registrarlos. La app no completa tarifas ausentes ni calcula por interpolación.

## Interpretar el stock al cotizar

El resumen muestra el saldo conocido si ese material está registrado. Como todavía no existe una receta de consumo validada por material, servicio, medida y variante, la app no afirma que el stock alcance, no reserva ni descuenta existencias automáticamente al cotizar o marcar una venta. El jefe debe aprobar esas recetas antes de habilitar una conclusión automática.

## Datos académicos

Los registros sintéticos se quitaron de la app y del repositorio. Para el Capítulo IV, usa `observed_quotes_template.csv` solo como estructura para registrar solicitudes reales anonimizadas y autorizadas. Guía: `../../docs/THESIS_DATA_GUIDE.md`.
