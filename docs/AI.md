# Integración de IA generativa y voz

No existe cliente de IA generativa real ni backend. El requerimiento dictado usa el reconocedor de Android para convertir voz a texto; después `SpokenRequirementParser` aplica reglas locales explícitas para servicio y largo×ancho, incluyendo tamaños expresados como “3 por 2”. Este parser no es IA generativa. El operador revisa y edita la transcripción y las medidas antes de guardar; si hay varios servicios o medidas contradictorias, se solicita revisión manual. El formulario registra largo y ancho y no solicita altura. El proveedor de reconocimiento de Android puede requerir conectividad y tiene su propia política de privacidad; el ingreso manual sigue disponible.

Un cliente generativo futuro debe implementar `GenerativeAiClient` detrás de backend autenticado y nunca decidir precio/descuento/venta. Ninguna clave se almacena en Kotlin o Git.
