package no.nav.helse.ansettelsesforhold

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import no.nav.helse.general.auth.getFodselsnummer

@KtorExperimentalLocationsAPI
fun Route.ansettelsesforholdApis(
    service: AnsettelsesforholdService
) {

    @Location("/ansettelsesforhold")
    class getAnsettelsesforhold

    get { _: getAnsettelsesforhold ->
        call.respond(
            AnsettelsesforholdResponse(service.getAnsettelsesforhold(getFodselsnummer(call)))
        )
    }
}