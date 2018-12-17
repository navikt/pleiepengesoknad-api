package no.nav.pleiepenger.api.general.auth

import io.ktor.application.ApplicationCall
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.principal

fun getFodselsnummer(call: ApplicationCall) : Fodselsnummer {
    val principal: JWTPrincipal = call.principal() ?: throw UnauthorizedException("No principal sett on call")
    return Fodselsnummer(principal.payload.subject)
}