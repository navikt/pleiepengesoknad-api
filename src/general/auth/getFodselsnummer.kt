package no.nav.pleiepenger.api.general.auth

import io.ktor.application.ApplicationCall
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.principal

fun getFodselsnummer(call: ApplicationCall) : String {
    val principal: JWTPrincipal = call.principal() ?: throw UnauthorizedException("No principal sett on call")
    return principal.payload.subject
}