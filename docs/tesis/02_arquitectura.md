# Diagramas de arquitectura

Los diagramas describen componentes del código. En `codex/firebase-integration` la integración Firebase es opcional: solo se activa con `app/google-services.json` y `firebaseOrganizationId` local. No existe un proyecto Firebase real configurado ni se afirma que la nube esté desplegada. Ver [configuración y despliegue Firebase](../firebase/CONFIGURACION_Y_DESPLIEGUE.md).

## Contexto del sistema

```mermaid
flowchart LR
    U[Trabajador o responsable] --> APP[App Android Cotizador Leguía]
    APP <--> ROOM[(Room / SQLite local por teléfono)]
    APP -->|Auth y cotizaciones minimizadas; solo sesión autorizada| FB[(Cloud Firestore opcional)]
    APP --> PERF[Performance Monitoring opcional]
    APP --> CRASH[Crashlytics opcional]
    CI[GitHub Actions manual] --> DIST[App Distribution opcional]
    APP -->|POST /v1/interpret cuando está habilitado| API[FastAPI]
    API -->|HTTPS con GEMINI_API_KEY del servidor| GEM[Gemini Developer API]
    API -. configuración de servicio .-> R[Render Web Service opcional]
    APP -. Debug: emulador / URL de desarrollo
    Release: solo endpoint HTTPS .-> API
    API -. No persiste cotizaciones .-> ROOM
```

## Capas Android observadas

```mermaid
flowchart TD
    MA[MainActivity: composición raíz, dependencias y destinos] --> S[Jetpack Compose + Material 3]
    S --> VM[CotizadorViewModel: estados Flow y acciones]
    VM --> REP[CotizadorRepository]
    REP --> DAO[Room DAOs]
    DAO --> DB[(SQLite: cotizador_leguia_db)]
    VM --> PRICE[Dominio: parser, flujo y cálculo de tarifas]
    VM --> AI[BackendGenerativeAiClient]
    AI --> NET[HttpURLConnection / JSON]
    S --> CSV[SAF: guardar CSV elegido por el usuario]
    S --> CLOUD[Acceso Firebase opcional]
    CLOUD --> AUTH[Firebase Authentication]
    CLOUD --> SYNC[WorkManager: sincronización con red]
    SYNC --> FSTORE[Firestore Rules: miembro activo de organización]
```

La inyección de dependencias y navegación son manuales desde `MainActivity`; no se encontró una librería DI ni un `NavController`. `rememberSaveableStateHolder` conserva estado Compose de destinos durante la navegación. Persistencia comercial y reglas de precios se resuelven en Room y dominio local.

En la rama Firebase, Room sigue siendo el almacén local. Los registros nuevos con una cuenta activa se sincronizan de forma diferida; registros ya existentes no se suben automáticamente. La nube recibe un subconjunto operativo, no el texto libre ni el precio. La versión Room pasa a 10 mediante migración 9→10.

## Flujo de interpretación IA

```mermaid
sequenceDiagram
    actor W as Trabajador
    participant A as Android Compose
    participant V as ViewModel
    participant F as FastAPI
    participant G as Gemini
    W->>A: Escribe o dicta requerimiento
    A->>V: Solicitar interpretación
    V->>F: POST /v1/interpret {requirement}
    F->>F: Validar longitud 3..2000
    F->>G: Enviar prompt + esquema JSON + clave del servidor
    G-->>F: Campos estructurados
    F-->>V: Respuesta Pydantic validada
    V-->>A: Propuesta, faltantes y ambigüedades
    A-->>W: Permite revisar y corregir manualmente
    Note over A,G: IA no cotiza precio ni decide suficiencia de stock
```

Si el dispositivo está en modo sin conexión, la llamada IA no está habilitada y la persona completa los campos manualmente. Fallos del servicio deben permitir reintento o captura manual.

## Modelo de datos Room simplificado

```mermaid
erDiagram
    SERVICIO ||--o{ PARAMETRO_PRECIO : define
    SERVICIO ||--o{ COTIZACION : clasifica
    MATERIAL ||--o{ COTIZACION : referencia
    MATERIAL ||--o| STOCK : saldo
    MATERIAL ||--o{ MOVIMIENTO_STOCK : registra
    COTIZACION ||--o{ EVENTO_TRAZABILIDAD : traza
    COTIZACION ||--o{ REGISTRO_ERROR : observa
    COTIZACION ||--o| PROFORMA : puede_generar
    COTIZACION ||--o{ TRABAJO_PRODUCCION : puede_originar
    TRABAJO_PRODUCCION ||--o{ REQUERIMIENTO_MATERIAL : requiere
    MATERIAL ||--o{ REQUERIMIENTO_MATERIAL : asigna
    COTIZACION ||--o{ PREDICCION : prediccion_futura
    MATERIAL ||--o{ NECESIDAD_ABASTECIMIENTO : puede_requerir
    TRABAJO_PRODUCCION ||--o{ NECESIDAD_ABASTECIMIENTO : puede_vincular
```

`Prediccion`, `Proforma`, producción y abastecimiento están en el esquema, pero no todos tienen una pantalla o flujo operativo completo. La presencia de una entidad no demuestra que la funcionalidad esté implementada de extremo a extremo.
