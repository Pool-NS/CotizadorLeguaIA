# Arquitectura

Estado presente: presentación Compose → ViewModel → Repository → Room para cotizaciones y datos locales. `BackendGenerativeAiClient` llama a `backend/api`; el servidor valida el texto, llama al proveedor con salida JSON estructurada y devuelve solo sugerencias para revisión humana. Las claves se mantienen en el servidor. `MainActivity` todavía crea dependencias y coordina pantallas; la separación a casos de uso y DI queda pendiente. La predicción logística entrenada con datos sintéticos no está conectada al flujo Android.
