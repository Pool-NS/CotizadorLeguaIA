# Limitaciones y evidencia visual

## Limitaciones conocidas

1. Room/SQLite vive dentro de cada instalación. No hay API para sincronizar cotizaciones, tarifas o stock entre teléfonos; el dashboard también es local.
2. FastAPI interpreta solicitudes y conecta Gemini, pero no almacena datos, no autentica trabajadores ni aplica cuotas/rate limit. Render se configura en el repositorio, pero esta revisión no verificó un host desplegado.
3. Room incluye entidades de producción, requerimientos y predicción, aunque no hay flujo completo de todas ellas ni predictor conectado. No inferir que su presencia en el esquema sea una funcionalidad terminada.
4. No hay receta aprobada de consumo; la app no promete suficiencia, no reserva material y no descuenta stock automáticamente al cotizar o vender.
5. El dashboard no mide exactitud independiente. La marca de comunicación del precio es declarada por operador. Las métricas del dashboard no son aún el análisis pretest/posttest de la tesis.
6. Pretest sin app, fase experimental, modalidad física de atención, errores atribuibles y exactitud necesitan definición y observación con el instrumento aprobado.
7. `exportSchema=false` y la falta de migración v1→2 dificultan certificar actualización desde esquemas muy antiguos.
8. No se ejecutó pentest, DAST, escaneo Sonar contra servidor, Quality Gate remoto ni análisis Qodana en esta revisión. Los marcos ISO/IEC son referencias de calidad/seguridad, no certificación lograda.
9. No se realizó una prueba visual de escalado, teclado, TalkBack o contraste en Android físico/emulador.

Android documenta que `allowBackup=false` puede no impedir transferencias directas entre dispositivos en ciertos fabricantes si la app apunta a Android 12 o posterior; por eso el proyecto declara también exclusiones explícitas en reglas de respaldo en nube y transferencia. Debe confirmarse en los dispositivos objetivo. Referencia: [Android Developers — Auto Backup](https://developer.android.com/identity/data/autobackup).

## Capturas de interfaces verificadas

**Estado:** pendiente. Al momento de la auditoría `adb devices -l` no mostró teléfonos y no se encontró ningún AVD configurado. Por esa razón esta carpeta no contiene capturas inventadas ni imágenes de previews como si fueran pantallas ejecutadas.

Para completar la evidencia visual, compilar e instalar el APK Debug en un emulador configurado o un dispositivo de prueba autorizado y capturar, como mínimo: Inicio, Formulario por servicio, Resumen, Panel, Tarifas y Stock. Anotar dispositivo, Android, `versionName`, estado de backend y si los datos son de prueba. No incluir datos reales de clientes.

## Uso en sustentación

Presentar por separado: (A) verificación técnica del software; (B) evaluación estadística de solicitudes reales pre/post, cuando se hayan recolectado conforme al protocolo; (C) limitaciones del prototipo. El éxito de compilación y las pruebas unitarias no equivalen a un efecto causal ni a mejora empresarial observada.
