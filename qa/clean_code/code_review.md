# Revisión de código

Prácticas aplicadas en el avance: parser de voz determinista y testeable fuera de Compose; reglas de precio, transición comercial y simulación separadas de pantallas; pares de dimensiones interpretados como largo×ancho; valores contradictorios enviados a revisión; validación obligatoria de largo/ancho, sin captura de altura; tarifas persistidas por servicio, tipo de carpa y combinación dimensional exacta; atributos de carpa y moto guardados en columnas tipadas; ajuste de stock y movimiento registrados en transacción; no se guarda un precio inventado.

Pendiente: dividir `MainActivity` y DI/ViewModel factory, `Flow` de errores para errores de persistencia, control por roles/cuentas en backend, proforma y producción UI, consumos y stock mínimo, snapshots de esquema Room, pruebas de migración, reducir las 37 advertencias Android Lint y ejecutar el análisis/Quality Gate de SonarQube. No se afirma “Clean Code aprobado” ni una ISO de código limpio.
