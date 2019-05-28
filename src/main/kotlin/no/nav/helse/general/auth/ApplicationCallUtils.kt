package no.nav.helse.general.auth

import io.ktor.application.ApplicationCall
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.principal
import no.nav.helse.aktoer.NorskIdent
import no.nav.helse.aktoer.tilNorskIdent

fun ApplicationCall.getNorskIdent() : NorskIdent {
    val principal: JWTPrincipal = principal() ?: throw IllegalStateException("Principal == null")
    return principal.payload.subject.tilNorskIdent()
}
