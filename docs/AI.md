# IA generativa en la cotización

## Alcance implementado

La captura por voz de Android convierte voz en texto. Después, el cotizador puede enviar el texto a `backend/api` para que un modelo generativo proponga servicio, largo, ancho, material y características de carpa o moto. La versión Android Debug llama al backend local configurado y carga la propuesta en el formulario. El cotizador revisa y corrige los campos antes de continuar; se guarda la salida estructurada y si hubo corrección humana.

El modelo no calcula precios, descuentos, ventas, consumo de materiales ni reservas. El precio permanece bajo reglas deterministas con tarifas aprobadas por el negocio. Si falta un dato, hay ambigüedad, no hay conexión o el backend no tiene clave, la app permite continuar manualmente y no presenta una interpretación automática como confirmada.

## Configuración

La app nunca se conecta directamente a la API del proveedor ni incluye la clave. Sigue [backend/api/README.md](../backend/api/README.md), configura `OPENAI_API_KEY` en un archivo `.env` local que Git ignora y ejecuta el servidor local. `OPENAI_MODEL` se puede sobrescribir según los modelos habilitados en la cuenta API.

Para teléfono físico, define `aiBackendUrl` en `local.properties` para una instancia de desarrollo accesible. La compilación Release solo acepta URL HTTPS mediante `aiBackendUrlRelease`. Un backend público aún requiere autenticación, límites de uso, protección de datos, monitoreo y políticas de retención; no está listo para producción multiusuario.

## Datos y evaluación

No se debe enviar nombre, teléfono, dirección u otro dato identificable. La llamada a OpenAI usa salida estructurada con un esquema JSON y `store: false`. OpenAI indica que, por defecto, los datos de API no se usan para entrenar sus modelos; pueden aplicarse registros de supervisión y retención temporal. Revisa sus condiciones vigentes antes de usar solicitudes reales.

El CSV sintético de 200 cotizaciones trata sobre conversión comercial y no entrena esta capacidad de interpretación generativa. La evaluación de la IA requiere un conjunto separado de frases de prueba con campos esperados validados por expertos: servicio, dimensiones, material, tipo de carpa/moto, faltantes y ambigüedades. No uses frases sintéticas como evidencia de desempeño en Leguía.
