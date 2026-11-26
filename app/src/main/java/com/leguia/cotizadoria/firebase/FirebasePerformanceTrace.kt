package com.leguia.cotizadoria.firebase

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

fun interface TraceSession {
    fun stop()
}

fun interface TraceRecorder {
    fun start(name: String): TraceSession
}

object NoOpTraceRecorder : TraceRecorder {
    override fun start(name: String) = TraceSession { }
}

class FirebaseTraceRecorder(
    private val performance: FirebasePerformance = FirebasePerformance.getInstance()
) : TraceRecorder {
    override fun start(name: String): TraceSession {
        val trace: Trace = performance.newTrace(name)
        trace.start()
        return TraceSession { trace.stop() }
    }
}
