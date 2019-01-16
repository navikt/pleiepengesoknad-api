package no.nav.helse.monitorering

interface Readiness {
    suspend fun getResult() : ReadinessResult
}