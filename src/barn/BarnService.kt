package no.nav.pleiepenger.api.barn

import no.nav.pleiepenger.api.general.auth.Fodselsnummer
import no.nav.pleiepenger.api.id.IdService

class BarnService(val barnGateway: BarnGateway,
                  val idService: IdService) {
    suspend fun getBarn(fnr: Fodselsnummer) : List<Barn> {
        return barnGateway.getBarn(idService.getId(fnr))
    }
}