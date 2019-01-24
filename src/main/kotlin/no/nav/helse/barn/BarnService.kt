package no.nav.helse.barn

import no.nav.helse.general.auth.Fodselsnummer

class BarnService {
    suspend fun getBarn(fnr: Fodselsnummer) : List<KomplettBarn> {
        //return barnGateway.getBarn(fnr)
        return listOf()
    }
}