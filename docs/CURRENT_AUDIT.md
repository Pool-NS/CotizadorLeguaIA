# Auditoría del proyecto actual

Fecha: 2026-10-05. Revisión estática inicial del ZIP `CotizadorLeguaIA-main`.

## Arquitectura observada

- Aplicación Android nativa, Kotlin, Jetpack Compose, MVVM parcial y Room 2.6.1.
- `MainActivity` coordina navegación y calcula el precio; `CotizadorViewModel` y `CotizadorRepository` separan parte del acceso a datos.
- Room versión 4 conserva Servicio, Material, ParametroPrecio, Cotizacion, EventoTrazabilidad y RegistroError, y añade proforma, producción, requerimiento de materiales, stock, movimientos, necesidades y predicción.
- Gradle usa AGP 8.7.2, Kotlin 2.0.21 y compile/target SDK 35. No hay cliente de IA, servidor ni código predictivo.

## Funcionalidad implementada vs. incompleta

Hay pantallas de inicio, selección de servicio, formulario, resumen y panel de historial. Las cotizaciones se persisten y se crea un evento de trazabilidad básico. Los servicios demo se insertan al crear la base de datos.

El precio heredado estaba fijado en `MainActivity` como largo × ancho × alto × 15; se retiró del flujo. Sin parámetros aprobados, la app ahora guarda una solicitud marcada `PENDIENTE_PRECIO` y no presenta cero como precio. El formulario valida requerimiento y dimensiones positivas. La bandera offline ahora refleja el control de la pantalla. El campo IA permanece nulo. Se agregó captura por voz con Android y parser determinista de servicio/medidas etiquetadas, con 4 pruebas unitarias nuevas y confirmación manual obligatoria. El PIN `1234` original era público en el código; se sustituyó por autenticación del bloqueo de pantalla de Android. La exportación, estados comerciales, proforma, planificación, materiales/stock, simulación, dashboard y predicción no estaban implementados.

## Riesgos y deuda

- Precio potencialmente engañoso por reglas inventadas y unidades ambiguas.
- Antes estaba en versión 3 con migración 2→3. Se agregó migración explícita 3→4 para nuevas tablas de proforma, producción, inventario y predicción. Instalaciones versión 1 siguen sin ruta verificable; se requiere conocer esquema real y prueba de migración.
- Base local sin sincronización multiusuario; autenticación del dispositivo no sustituye control de acceso de servidor.
- La semilla incluye Corte y Confección, Bordado Personalizado y Estampado Textil, que no son los servicios del negocio indicado.
- No hay plan de pruebas funcional ni documentación de ejecución. Solo existen pruebas de plantilla.
- Los nombres de operador son datos de interfaz, no identidad autenticada.

## Plan de migración

1. Retirar tarifas y catálogos demo ambiguos; bloquear cálculo si faltan reglas validadas.
2. Extraer dominio determinista testeable y separar presentación, datos, integración IA y predicción.
3. Versionar cada cambio Room mediante migraciones no destructivas y pruebas.
4. Persistir estados comerciales con eventos atómicos, proforma y planificación; completar sus pantallas y pruebas.
5. Incorporar materiales, inventario y simulación sin reserva ni compra automática.
6. Desarrollar artefactos sintéticos y reportar sus métricas solo como verificación técnica.
7. Ejecutar Gradle, lint, pruebas y análisis estático; anotar explícitamente bloqueos del entorno.

## Validación inicial

`gradlew.bat` heredado pasaba un classpath vacío y se corrigió al lanzador estándar. El daemon JDK se ajustó de 21 a 17 porque este entorno tiene JDK 17 (compatible con AGP 8.7.2) y no 21. Se añadió `local.properties` local ignorado por Git para señalar el Android SDK instalado. Resultado final: `gradlew test` exitoso (7/7 unit tests) y `gradlew lint` exitoso con 37 advertencias; informes en `app/build/`. No se ejecutaron pruebas instrumentadas ni migración Room. No se observaron credenciales de API; sí el PIN local original mencionado arriba, ya reemplazado por bloqueo Android. La auditoría no equivale a una evaluación formal de conformidad ISO. Se verificó que ISO publica la edición ISO 9001:2026; referencia oficial: https://www.iso.org/standard/88464.html.

## Avance posterior

La auditoría anterior registra el estado inicial; el flujo actual solicita largo y ancho sin mostrar altura. El catálogo actual usa `Carpas`, `Toldos` y `Tapizado de moto`. Las carpas guardan tipo abierta/cerrada y, para las cerradas, cantidad de ventanas y puertas. Las tarifas se registran por servicio, tamaño exacto y tipo de carpa. La migración Room 4→5 agrega los atributos opcionales y renombra el servicio anterior `Tapizado`; la verificación instrumentada de esta migración queda pendiente.
