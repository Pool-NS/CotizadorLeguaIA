# Datos:
Chaupis Leguia Piero Pool
correo:75177414@continental.edu.pe
correo personal u opcional;pieropoolc@gmail.com


# Cotizador Leguía

Aplicación Android en Kotlin/Compose con persistencia local Room. El repositorio recibido es un prototipo; los cálculos y decisiones comerciales requieren reglas verificadas por Leguía antes del uso operativo.

## Estado actual

La app incluye cotización local, tarifas, inventario, seguimiento, captura de voz y revisión humana de campos. El módulo de IA generativa usa un backend FastAPI que consulta Gemini y requiere una clave configurada fuera del repositorio; no se entrega una clave ni backend público. La gratuidad de Gemini depende de la cuenta, del modelo y de límites sujetos a cambio. Consulta [docs/AI.md](docs/AI.md), [docs/CURRENT_AUDIT.md](docs/CURRENT_AUDIT.md), [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) y el [enfoque metodológico de IA, DevSecOps y métricas](docs/METODOLOGIA_IA_DEVSECOPS.md). Guion: [docs/DEMO_GUIDE.md](docs/DEMO_GUIDE.md). APK Debug: `app/build/outputs/apk/debug/app-debug.apk`.

La versión Android de este avance es `1.5.0` (version code `8`). La app muestra su identidad y versión en la pantalla de inicio y usa un ícono vectorial propio de Leguía. La navegación conserva el formulario al volver del resumen; el inventario registra ingresos, salidas y ajustes trazables en Room. El panel aplica filtros por periodo, distingue confirmación de comunicación de precio y exporta CSV minimizado. Las métricas son locales a cada teléfono. Consulta [las métricas y visualizaciones](docs/METRICAS_APP_TESIS.md), [la guía de UX](docs/MEJORAS_UX_DASHBOARD.md) y [la revisión de seguridad](docs/REVISION_SEGURIDAD_LOCAL.md).

El alcance específico de la app, su diferencia frente a un ERP general como Odoo y los criterios pendientes antes de declararla lista para operación están en [docs/DIFERENCIACION_ODOO_Y_ALCANCE.md](docs/DIFERENCIACION_ODOO_Y_ALCANCE.md). La app complementa un ERP en el flujo especializado de cotización; no pretende sustituirlo.

## Requisitos y ejecución

Android Studio compatible con AGP 8.7.2, JDK 17 y Android SDK 35. Abre el directorio raíz en Android Studio. El backend se ejecuta aparte; instrucciones en `backend/api/README.md`. La última ejecución de test/lint anterior al módulo IA se documenta en `qa/FINAL_QUALITY_REPORT.md`; falta volver a validar este cambio y probarlo en dispositivo. SonarQube está configurado, pero el análisis y Quality Gate requieren una instancia y credenciales.

Para desplegar el backend de demostración en Render consulta [docs/DEPLOY_RENDER.md](docs/DEPLOY_RENDER.md). El plan Free puede suspenderse por inactividad y el backend aún no incluye autenticación ni límites de uso para un despliegue operativo multiusuario.

En `Panel de Administración` el jefe puede guardar precios aprobados por servicio, tamaño exacto, material y tipo de carpa (abierta/cerrada), y registrar ingresos de materiales. El formulario ofrece lino económico/liviano/pesado para carpas y Tarflex/lona orillo azul/rojo para toldos; permite seleccionar medidas frecuentes, ingresar otras medidas y añadir altura opcional. Las carpas cerradas capturan ventanas y puertas. Si se ingresa altura, queda registrada, pero las reglas actuales cotizan largo × ancho; confírmalo con el jefe si cambia precio o consumo. Estas opciones no crean tarifas ni reglas de consumo por sí solas: el jefe debe registrar los precios aprobados, y el consumo real de materiales requiere una receta validada. La guía operativa está en [qa/data/GUIA_STOCK_Y_TARIFAS.md](qa/data/GUIA_STOCK_Y_TARIFAS.md).

## Datos y modelo

La base de datos actual es Room local en cada instalación Android. FastAPI recibe peticiones y llama a Gemini: no es la base de datos ni sincroniza varios trabajadores. Para compartir cotizaciones entre dispositivos hará falta alojar una base central y añadir autenticación/autorización al backend. Migraciones están en `app/src/main/.../data/AppDatabase.kt`. No desinstale la app para ocultar errores de migración. El esquema todavía no cubre todo el flujo comercial descrito; vea [docs/DATA_MODEL.md](docs/DATA_MODEL.md) y [docs/DATABASE_DESIGN.md](docs/DATABASE_DESIGN.md).

## Dataset de 200 registros

La aplicación no incorpora cotizaciones, stock ni predicciones de demostración. Las plantillas vacías para registrar observaciones reales y su guía de análisis están en `qa/data/` y [docs/THESIS_DATA_GUIDE.md](docs/THESIS_DATA_GUIDE.md). No uses datos inventados como resultados de tesis; confirma instrumento y análisis con el docente.

El jefe registra existencias en Panel de Administración → Stock y tarifas por servicio, medida, material y variante en Tarifas. Se guardan localmente en Room en ese teléfono. El resumen muestra el saldo registrado y pide validación humana mientras no exista una regla de consumo real aprobada.

## IA y predicción

La captura por voz usa el reconocedor de Android. El texto puede enviarse al backend generativo local para proponer campos estructurados y pedir revisión humana. No está configurado el servicio sin una clave API local. No se incluye un predictor comercial.

## Calidad y seguridad

Use [docs/TESTING.md](docs/TESTING.md), [docs/QUALITY.md](docs/QUALITY.md), [docs/QODANA.md](docs/QODANA.md) y `qa/`. Las evaluaciones normativas son marcos de evaluación y gestión; no implican certificación ni aprobación.

La actualización de navegación, dashboard, gráficos y política local de contraseña está descrita en [docs/MEJORAS_UX_DASHBOARD.md](docs/MEJORAS_UX_DASHBOARD.md). La referencia de credenciales se documenta también en [docs/REVISION_SEGURIDAD_LOCAL.md](docs/REVISION_SEGURIDAD_LOCAL.md). La combinación de caracteres es política interna del proyecto; no se atribuye como mandato textual de ISO/IEC 27001.

Para preparar la evaluación de calidad automatizada de Quantum/GitLab, revisa [docs/GITLAB_QUANTUM_EVALUACION.md](docs/GITLAB_QUANTUM_EVALUACION.md); `project.yml` requiere completar los datos académicos reales antes de ejecutar el pipeline.
