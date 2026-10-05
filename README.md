# Cotizador Leguía

Aplicación Android en Kotlin/Compose con persistencia local Room. El repositorio recibido es un prototipo; los cálculos y decisiones comerciales requieren reglas verificadas por Leguía antes del uso operativo.

## Estado actual

La app incluye cotización local, tarifas, inventario, seguimiento, captura de voz y revisión humana de campos. El módulo de IA generativa requiere el backend local y una clave API configurada fuera del repositorio; no se entrega una clave ni backend público. Consulta [docs/AI.md](docs/AI.md), [docs/CURRENT_AUDIT.md](docs/CURRENT_AUDIT.md) y [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md). Guion: [docs/DEMO_GUIDE.md](docs/DEMO_GUIDE.md). APK Debug: `app/build/outputs/apk/debug/app-debug.apk`.

## Requisitos y ejecución

Android Studio compatible con AGP 8.7.2, JDK 17 y Android SDK 35. Abre el directorio raíz en Android Studio. El backend se ejecuta aparte; instrucciones en `backend/api/README.md`. La última ejecución de test/lint anterior al módulo IA se documenta en `qa/FINAL_QUALITY_REPORT.md`; falta volver a validar este cambio y probarlo en dispositivo. SonarQube está configurado, pero el análisis y Quality Gate requieren una instancia y credenciales.

En `Panel de Administración` el jefe puede guardar precios aprobados por servicio, tamaño exacto, material y tipo de carpa (abierta/cerrada), y registrar ingresos de materiales. Las carpas cerradas capturan ventanas y puertas; el tapizado se especifica como `Tapizado de moto` con tipo/modelo. La app no solicita altura y solo aplica coincidencias exactas de servicio, tamaño, material y variante; si falta un material o su tarifa, la solicitud queda pendiente para revisión.

## Datos y modelo

La base local es Room. Migraciones están en `app/src/main/.../data/AppDatabase.kt`. No desinstale la app para ocultar errores de migración. El esquema todavía no cubre todo el flujo comercial descrito; vea [docs/DATA_MODEL.md](docs/DATA_MODEL.md) y [docs/DATABASE_DESIGN.md](docs/DATABASE_DESIGN.md).

## IA y predicción

La captura por voz usa el reconocedor de Android. El texto puede enviarse al backend generativo local para proponer campos estructurados y pedir revisión humana. No está configurado el servicio sin una clave API local. El predictor logístico es un experimento separado entrenado con datos sintéticos; no está conectado a la app y no representa resultados de campo de Leguía.

## Calidad y seguridad

Use [docs/TESTING.md](docs/TESTING.md), [docs/QUALITY.md](docs/QUALITY.md) y `qa/`. Las evaluaciones normativas son marcos de evaluación y gestión; no implican certificación ni aprobación.
