package no.nav.helse.barn

import no.nav.helse.general.CallId
import no.nav.helse.general.auth.Fodselsnummer

class BarnService {
    suspend fun getBarn(
        fnr: Fodselsnummer,
        callId: CallId
    ) : List<KomplettBarn> {
        //return barnGateway.getBarn(fnr)
        return listOf()
    }
}