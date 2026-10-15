# Cotizador Leguía

Aplicación Android en Kotlin/Compose con persistencia local Room. El repositorio recibido es un prototipo; los cálculos y decisiones comerciales requieren reglas verificadas por Leguía antes del uso operativo.

## Estado actual

La app incluye cotización local, tarifas, inventario, seguimiento, captura de voz y revisión humana de campos. El módulo de IA generativa usa un backend FastAPI que consulta Gemini y requiere una clave configurada fuera del repositorio; no se entrega una clave ni backend público. La gratuidad de Gemini depende de la cuenta, del modelo y de límites sujetos a cambio. Consulta [docs/AI.md](docs/AI.md), [docs/CURRENT_AUDIT.md](docs/CURRENT_AUDIT.md), [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) y el [enfoque metodológico de IA, DevSecOps y métricas](docs/METODOLOGIA_IA_DEVSECOPS.md). Guion: [docs/DEMO_GUIDE.md](docs/DEMO_GUIDE.md). APK Debug: `app/build/outputs/apk/debug/app-debug.apk`.

La versión Android de este avance es `1.1.0` (version code `2`). La app muestra su identidad y versión en la pantalla de inicio y usa un ícono vectorial propio de Leguía. Las métricas y visualizaciones recomendadas con base en la operacionalización disponible están en [docs/METRICAS_APP_TESIS.md](docs/METRICAS_APP_TESIS.md); la revisión estática limitada de seguridad está en [docs/REVISION_SEGURIDAD_LOCAL.md](docs/REVISION_SEGURIDAD_LOCAL.md).

El alcance específico de la app, su diferencia frente a un ERP general como Odoo y los criterios pendientes antes de declararla lista para operación están en [docs/DIFERENCIACION_ODOO_Y_ALCANCE.md](docs/DIFERENCIACION_ODOO_Y_ALCANCE.md). La app complementa un ERP en el flujo especializado de cotización; no pretende sustituirlo.

## Requisitos y ejecución

Android Studio compatible con AGP 8.7.2, JDK 17 y Android SDK 35. Abre el directorio raíz en Android Studio. El backend se ejecuta aparte; instrucciones en `backend/api/README.md`. La última ejecución de test/lint anterior al módulo IA se documenta en `qa/FINAL_QUALITY_REPORT.md`; falta volver a validar este cambio y probarlo en dispositivo. SonarQube está configurado, pero el análisis y Quality Gate requieren una instancia y credenciales.

Para desplegar el backend de demostración en Render consulta [docs/DEPLOY_RENDER.md](docs/DEPLOY_RENDER.md). El plan Free puede suspenderse por inactividad y el backend aún no incluye autenticación ni límites de uso para un despliegue operativo multiusuario.

En `Panel de Administración` el jefe puede guardar precios aprobados por servicio, tamaño exacto, material y tipo de carpa (abierta/cerrada), y registrar ingresos de materiales. El formulario ofrece lino económico/liviano/pesado para carpas y Tarflex/lona orillo azul/rojo para toldos; permite seleccionar medidas frecuentes, ingresar otras medidas y añadir altura opcional. Las carpas cerradas capturan ventanas y puertas. Si se ingresa altura, queda registrada, pero las reglas actuales cotizan largo × ancho; confírmalo con el jefe si cambia precio o consumo. Estas opciones no crean tarifas ni reglas de consumo por sí solas: el jefe debe registrar los precios aprobados, y el consumo real de materiales requiere una receta validada. El demo de 200 filas está claramente separado del historial real; [documentación de la demo](qa/data/README_STOCK_DEMO.md).

## Datos y modelo

La base de datos actual es Room local en cada instalación Android. FastAPI recibe peticiones y llama a Gemini: no es la base de datos ni sincroniza varios trabajadores. Para compartir cotizaciones entre dispositivos hará falta alojar una base central y añadir autenticación/autorización al backend. Migraciones están en `app/src/main/.../data/AppDatabase.kt`. No desinstale la app para ocultar errores de migración. El esquema todavía no cubre todo el flujo comercial descrito; vea [docs/DATA_MODEL.md](docs/DATA_MODEL.md) y [docs/DATABASE_DESIGN.md](docs/DATABASE_DESIGN.md).

## Dataset de 200 registros

El archivo `qa/data/synthetic_quotes_200.csv` se abre directamente en jamovi o SPSS; no se carga en Room. Sus 200 registros son sintéticos y sirven para practicar, no como datos históricos reales ni evidencia empírica de Leguía. El demo con tarifas y stock simulados está en [qa/data/README_STOCK_DEMO.md](qa/data/README_STOCK_DEMO.md) y se abre desde Panel de Administración. Para recolectar observaciones reales pretest/posttest, usa la plantilla `qa/data/observed_quotes_template.csv` y sigue [docs/THESIS_DATA_GUIDE.md](docs/THESIS_DATA_GUIDE.md).

El libro de práctica [synthetic_quotes_stock_smartpls_demo.xlsx](qa/data/synthetic_quotes_stock_smartpls_demo.xlsx) contiene dashboard con barras, base de 200 filas, inventario/tarifas sintéticas y hoja de precios por validar. No promete un alfa de Cronbach; la confiabilidad debe estimarse con el instrumento real aprobado.

## IA y predicción

La captura por voz usa el reconocedor de Android. El texto puede enviarse al backend generativo local para proponer campos estructurados y pedir revisión humana. No está configurado el servicio sin una clave API local. El predictor logístico es un experimento separado entrenado con datos sintéticos; no está conectado a la app y no representa resultados de campo de Leguía.

## Calidad y seguridad

Use [docs/TESTING.md](docs/TESTING.md), [docs/QUALITY.md](docs/QUALITY.md), [docs/QODANA.md](docs/QODANA.md) y `qa/`. Las evaluaciones normativas son marcos de evaluación y gestión; no implican certificación ni aprobación.
