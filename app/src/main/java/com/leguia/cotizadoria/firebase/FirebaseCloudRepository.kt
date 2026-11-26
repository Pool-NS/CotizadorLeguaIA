package com.leguia.cotizadoria.firebase

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.perf.FirebasePerformance
import com.leguia.cotizadoria.data.CotizacionDao
import com.leguia.cotizadoria.data.CotizacionEntity
import kotlinx.coroutines.tasks.await

data class CloudSyncSummary(val uploaded: Int, val failed: Int)

/** Sends only a minimized operational record; free-text requirements and operator names stay on-device. */
class FirebaseCloudRepository(
    private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val quoteDao: CotizacionDao,
    private val organizationId: String,
    private val appVersion: String
) {
    init {
        require(organizationId.isNotBlank()) { "Configura firebaseOrganizationId antes de habilitar la nube." }
    }

    val currentUserId: String? get() = auth.currentUser?.uid

    suspend fun signIn(email: String, password: String) {
        require(email.isNotBlank() && password.isNotBlank())
        val user = auth.signInWithEmailAndPassword(email.trim(), password).await().user
            ?: error("Firebase Authentication no devolvió una cuenta.")
        try {
            val membership = firestore.collection("organizations")
                .document(organizationId)
                .collection("members")
                .document(user.uid)
                .get()
                .await()
            check(membership.getBoolean("active") == true) { "La cuenta no tiene acceso activo a la organización." }
        } catch (failure: Exception) {
            auth.signOut()
            throw failure
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    suspend fun synchronizePendingQuotes(): CloudSyncSummary {
        val userId = currentUserId ?: return CloudSyncSummary(uploaded = 0, failed = 0)
        val trace = FirebasePerformance.getInstance().newTrace("cloud_quote_sync")
        trace.start()
        try {
        val pending = quoteDao.obtenerPendientesFirebase(userId)
        var uploaded = 0
        var failed = 0

        pending.forEach { quote ->
            try {
                uploadOnce(quote, userId)
                quoteDao.actualizarEstadoFirebase(quote.id, SYNCED)
                uploaded++
            } catch (failure: Exception) {
                quoteDao.actualizarEstadoFirebase(quote.id, FAILED)
                if (failure !is FirebaseFirestoreException || failure.code !in TRANSIENT_FIRESTORE_ERRORS) {
                    com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().recordException(
                        IllegalStateException("Firebase quote synchronization failed (${failure.javaClass.simpleName}).")
                    )
                }
                failed++
            }
        }
        if (pending.isNotEmpty()) {
            trace.putMetric("quotes_attempted", pending.size.toLong())
            trace.putMetric("quotes_uploaded", uploaded.toLong())
            trace.putMetric("quotes_failed", failed.toLong())
        }
        return CloudSyncSummary(uploaded, failed)
        } finally {
            trace.stop()
        }
    }

    private suspend fun uploadOnce(quote: CotizacionEntity, userId: String) {
        check(quote.firebaseOwnerUid == userId) { "La cuenta actual no es propietaria del registro." }
        val document = firestore.collection("organizations")
            .document(organizationId)
            .collection("quotes")
            .document(quote.firebaseDocumentId)

        val existing = document.get().await()
        if (existing.exists()) {
            check(existing.getString("ownerUid") == userId) { "El identificador remoto ya pertenece a otra cuenta." }
            return
        }

        document.set(quote.toCloudRecord(userId, networkCategory())).await()
    }

    private fun CotizacionEntity.toCloudRecord(userId: String, network: String): Map<String, Any?> = mapOf(
        "recordId" to firebaseDocumentId,
        "ownerUid" to userId,
        "serviceId" to servicioId,
        "materialId" to materialId,
        "lengthM" to finiteOrNull(medidaLargo),
        "widthM" to finiteOrNull(medidaAncho),
        "heightM" to finiteOrNull(medidaAlto),
        "quoteStatus" to estado,
        "assistanceMode" to if (esModoOffline) "manual" else "ai_assisted",
        "startedAtEpochMs" to fechaHoraInicio,
        "priceCommunicatedAtEpochMs" to fechaHoraPrecioComunicado,
        "durationMillis" to fechaHoraPrecioComunicado?.minus(fechaHoraInicio)?.takeIf { it >= 0L },
        "communicationConfirmed" to (capturaComunicacionCompleta && fechaHoraPrecioComunicado != null),
        "appVersion" to appVersion,
        "deviceModel" to Build.MODEL.take(80),
        "androidApiLevel" to Build.VERSION.SDK_INT,
        "networkTransport" to network,
        "source" to "android_room",
        "uploadedAt" to FieldValue.serverTimestamp()
    )

    private fun networkCategory(): String {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return "unknown"
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork) ?: return "offline"
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
            else -> "other"
        }
    }

    private fun finiteOrNull(value: Double?): Double? = value?.takeIf(Double::isFinite)

    companion object {
        private const val SYNCED = "SYNCED"
        private const val FAILED = "FAILED"
        private val TRANSIENT_FIRESTORE_ERRORS = setOf(
            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
            FirebaseFirestoreException.Code.CANCELLED
        )
    }
}
