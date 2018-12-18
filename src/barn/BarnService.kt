package no.nav.pleiepenger.api.barn

import no.nav.pleiepenger.api.general.auth.Fodselsnummer

class BarnService(val barnGateway: BarnGateway) {
    suspend fun getBarn(fnr: Fodselsnummer) : List<Barn> {
        return barnGateway.getBarn(fnr)
    }
}