package no.nav.helse.aktor

import no.nav.helse.general.auth.Fodselsnummer

class AktorService(
    private val aktorGateway: AktorGateway
){
    suspend fun getAktorId(fnr: Fodselsnummer) : AktorId {
        return aktorGateway.getAktorId(fnr)
    }
}