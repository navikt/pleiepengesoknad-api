package no.nav.helse.barn

import no.nav.helse.general.auth.Fodselsnummer

class BarnService(private val barnGateway: BarnGateway) {
    suspend fun getBarn(fnr: Fodselsnummer) : List<KomplettBarn> {
        return barnGateway.getBarn(fnr)
    }
}