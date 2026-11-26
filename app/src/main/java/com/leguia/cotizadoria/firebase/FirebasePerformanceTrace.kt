package com.leguia.cotizadoria.firebase

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

interface TraceSession {
    fun putMetric(name: String, value: Long)
    fun stop()
}

fun interface TraceRecorder {
    fun start(name: String): TraceSession
}

object NoOpTraceRecorder : TraceRecorder {
    override fun start(name: String) = object : TraceSession {
        override fun putMetric(name: String, value: Long) = Unit
        override fun stop() = Unit
    }
}

class FirebaseTraceRecorder(
    private val performance: FirebasePerformance = FirebasePerformance.getInstance()
) : TraceRecorder {
    override fun start(name: String): TraceSession {
        val trace: Trace = performance.newTrace(name)
        trace.start()
        return object : TraceSession {
            override fun putMetric(name: String, value: Long) = trace.putMetric(name, value)
            override fun stop() = trace.stop()
        }
    }
}
