package no.nav.helse.general

import io.prometheus.client.Histogram

object Operation {
    suspend fun <T>monitoredOperation(
        operation: suspend () -> T,
        histogram: Histogram) : T {
        val timer = histogram.startTimer()
        try {
            return operation.invoke()
        } finally {
            val elapsed = timer.observeDuration()
        }
    }
}