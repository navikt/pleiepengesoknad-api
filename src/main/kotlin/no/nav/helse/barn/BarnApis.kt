package no.nav.helse.barn

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import no.nav.helse.general.auth.getFodselsnummer
import no.nav.helse.general.getCallId


@KtorExperimentalLocationsAPI
fun Route.barnApis(
    barnService: BarnService
) {

    @Location("/barn")
    class getBarn

    get { _: getBarn ->

        val kompletteBarn = barnService.getBarn(
            fnr = call.getFodselsnummer(),
            callId = call.getCallId()
        )
        call.respond(
            toApiResponse(kompletteBarn)
        )
    }
}

private fun toApiResponse(kompletteBarn: List<KomplettBarn>) : BarnResponse {
    val barn = mutableListOf<Barn>()
    kompletteBarn.forEach {
        barn.add(Barn(
            fornavn = it.fornavn,
            mellomnavn = it.mellomnavn,
            etternavn = it.etternavn,
            fodselsdato = it.fodselsdato
        ))
    }
    return BarnResponse(barn = barn.toList())
}