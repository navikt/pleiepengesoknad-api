package no.nav.helse.arbeidsgiver

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.general.auth.IdTokenProvider
import no.nav.helse.general.getCallId
import no.nav.helse.soknad.FraOgMedTilOgMedValidator
import java.time.LocalDate

private const val fraOgMedQueryName = "fra_og_med"
private const val tilOgMedQueryName = "til_og_med"

fun Route.arbeidsgiverApis(
    arbeidsgivereService: ArbeidsgivereService,
    idTokenProvider: IdTokenProvider
) {

    get("/arbeidsgiver") {
        val violations = FraOgMedTilOgMedValidator.validate(
            fraOgMed = call.request.queryParameters[fraOgMedQueryName],
            tilOgMed = call.request.queryParameters[tilOgMedQueryName],
            parameterType = ParameterType.QUERY
        )

        if (violations.isNotEmpty()) {
            throw Throwblem(ValidationProblemDetails(violations))
        } else {
            call.respond(
                Arbeidsgivere(
                    arbeidsgivereService.getArbeidsgivere(
                        idToken = idTokenProvider.getIdToken(call),
                        callId = call.getCallId(),
                        fraOgMed = LocalDate.parse(call.request.queryParameters[fraOgMedQueryName]),
                        tilOgMed = LocalDate.parse(call.request.queryParameters[tilOgMedQueryName])
                    )
                )
            )
        }
    }
}