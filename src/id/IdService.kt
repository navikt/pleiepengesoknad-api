package no.nav.pleiepenger.api.id

import no.nav.pleiepenger.api.general.auth.Fodselsnummer

class IdService(private val idGateway: IdGateway) {
    private val fnrIdMap = mutableMapOf<Fodselsnummer, Id>()

    suspend fun getId(fnr: Fodselsnummer) : Id {
        if (fnrIdMap.containsKey(fnr)) {
            return fnrIdMap[fnr]!!
        }
        val id = idGateway.getId(fnr)
        fnrIdMap[fnr] = id
        return id
    }

    suspend fun refreshAndGetId(fnr: Fodselsnummer) : Id {
        val id = idGateway.getId(fnr)
        fnrIdMap[fnr] = id
        return id
    }
}