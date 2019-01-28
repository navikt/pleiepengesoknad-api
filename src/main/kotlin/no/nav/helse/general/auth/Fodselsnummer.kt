package no.nav.helse.general.auth

import io.ktor.application.ApplicationCall
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.principal

data class Fodselsnummer(val value : String)

fun ApplicationCall.getFodselsnummer() : Fodselsnummer {
    val principal: JWTPrincipal = principal() ?: throw IllegalStateException("Principal == null")
    return Fodselsnummer(principal.payload.subject)
}
