# Cotizador Leguía

Aplicación Android en Kotlin/Compose con persistencia local Room. El repositorio recibido es un prototipo; los cálculos y decisiones comerciales requieren reglas verificadas por Leguía antes del uso operativo.

## Estado actual

La app contiene navegación básica, formulario, resumen, almacenamiento de cotizaciones e historial. Consulte [docs/CURRENT_AUDIT.md](docs/CURRENT_AUDIT.md) y [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) para límites y riesgos. Hay captura de voz del reconocedor Android y extracción determinista con revisión humana; IA generativa y sincronización reales no están integradas. Guion: [docs/DEMO_GUIDE.md](docs/DEMO_GUIDE.md). APK: `app/build/outputs/apk/debug/app-debug.apk`. No incluya claves en la app.

## Requisitos y ejecución

Android Studio compatible con AGP 8.7.2, JDK 17 y Android SDK 35. Abra el directorio raíz en Android Studio o ejecute `./gradlew test` y `./gradlew lint` en un entorno con dependencias Gradle disponibles. Última validación: test Debug/Release aprobados (15 pruebas), lint aprobado con 37 advertencias, y APK Debug generado. SonarQube está configurado, pero el análisis y Quality Gate están pendientes porque falta una instancia y credenciales. Reportes en `app/build/reports/` y `app/build/test-results/`.

En `Panel de Administración` el jefe puede guardar precios aprobados por servicio, tamaño exacto y tipo de carpa (abierta/cerrada), y registrar ingresos de materiales. Las carpas cerradas capturan ventanas y puertas; el tapizado se especifica como `Tapizado de moto` con tipo/modelo. La app no solicita altura y solo aplica coincidencias de tarifa exactas; si no existe una tarifa, la solicitud queda pendiente para revisión.

## Datos y modelo

La base local es Room. Migraciones están en `app/src/main/.../data/AppDatabase.kt`. No desinstale la app para ocultar errores de migración. El esquema todavía no cubre todo el flujo comercial descrito; vea [docs/DATA_MODEL.md](docs/DATA_MODEL.md) y [docs/DATABASE_DESIGN.md](docs/DATABASE_DESIGN.md).

## IA y predicción

La captura por voz usa el reconocedor de Android y un parser local determinista para servicio/medidas nombradas; verifica manualmente antes de guardar. IA generativa real no está integrada. El predictor no está conectado a la app. El material predictivo sintético no representa resultados de campo de Leguía.

## Calidad y seguridad

Use [docs/TESTING.md](docs/TESTING.md), [docs/QUALITY.md](docs/QUALITY.md) y `qa/`. Las evaluaciones normativas son marcos de evaluación y gestión; no implican certificación ni aprobación.
