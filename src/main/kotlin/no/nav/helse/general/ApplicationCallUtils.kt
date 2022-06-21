package no.nav.helse.general

import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*

data class CallId(
    val value: String
)

fun ApplicationCall.getCallId(): CallId {
    return CallId(callId!!)
}

data class Metadata(
    val version: Int,
    val correlationId: String
)

fun ApplicationCall.getMetadata() = Metadata(
    version = 1,
    correlationId = getCallId().value
)
