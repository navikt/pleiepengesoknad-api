package no.nav.helse.general.auth

import io.ktor.application.ApplicationCall
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.principal

fun getFodselsnummer(call: ApplicationCall) : Fodselsnummer {
    val principal: JWTPrincipal = call.principal() ?: throw IllegalStateException("Principal == null")
    return Fodselsnummer(principal.payload.subject)
}