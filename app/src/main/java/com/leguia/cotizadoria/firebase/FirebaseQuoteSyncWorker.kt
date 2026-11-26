package com.leguia.cotizadoria.firebase

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.leguia.cotizadoria.BuildConfig
import com.leguia.cotizadoria.data.AppDatabase

class FirebaseQuoteSyncWorker(
    appContext: Context,
    parameters: WorkerParameters
) : CoroutineWorker(appContext, parameters) {
    override suspend fun doWork(): Result {
        if (!BuildConfig.FIREBASE_ENABLED) return Result.success()
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) return Result.success()
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = FirebaseCloudRepository(
            context = applicationContext,
            auth = auth,
            firestore = FirebaseFirestore.getInstance(),
            quoteDao = database.cotizacionDao(),
            organizationId = BuildConfig.FIREBASE_ORGANIZATION_ID,
            appVersion = BuildConfig.VERSION_NAME
        )
        val summary = runCatching { repository.synchronizePendingQuotes() }.getOrElse { return Result.retry() }
        return when {
            summary.failed == 0 -> Result.success()
            runAttemptCount < MAX_RETRIES -> Result.retry()
            else -> Result.failure()
        }
    }

    private companion object {
        const val MAX_RETRIES = 8
    }
}
