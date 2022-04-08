package no.nav.helse.arbeidsgiver

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.ARBEIDSGIVER_URL
import no.nav.helse.Configuration
import no.nav.helse.ORGANISASJONER_URL
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.dusseldorf.ktor.core.erGyldigOrganisasjonsnummer
import no.nav.helse.general.getCallId
import no.nav.helse.general.oppslag.TilgangNektetException
import no.nav.helse.soker.respondTilgangNektetProblemDetail
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val fraOgMedQueryName = "fra_og_med"
private const val tilOgMedQueryName = "til_og_med"
const val orgQueryName = "org"
private const val privateArbeidsgivereQueryName = "private_arbeidsgivere"
private const val frilansoppdragQueryName = "frilansoppdrag"

fun Route.arbeidsgiverApis(
    arbeidsgivereService: ArbeidsgivereService,
    idTokenProvider: IdTokenProvider,
    miljø: Configuration.Miljø
) {

    get(ARBEIDSGIVER_URL) {
        val violations = FraOgMedTilOgMedValidatorArbeidsgiver.validate(
            fraOgMed = call.request.queryParameters[fraOgMedQueryName],
            tilOgMed = call.request.queryParameters[tilOgMedQueryName],
            parameterType = ParameterType.QUERY
        )

        if (violations.isNotEmpty()) {
            throw Throwblem(ValidationProblemDetails(violations))
        } else {
            try {
                val arbeidsgivere = arbeidsgivereService.getArbeidsgivere(
                    idToken = idTokenProvider.getIdToken(call),
                    callId = call.getCallId(),
                    fraOgMed = LocalDate.parse(call.request.queryParameters[fraOgMedQueryName]),
                    tilOgMed = LocalDate.parse(call.request.queryParameters[tilOgMedQueryName]),
                    skalHentePrivateArbeidsgivere = call.request.queryParameters[privateArbeidsgivereQueryName].toBoolean(),
                    skalHenteFrilansoppdrag  = call.request.queryParameters[frilansoppdragQueryName].toBoolean()
                )

                val unikeOrganisasjoner = arbeidsgivere.organisasjoner.distinctBy { it.organisasjonsnummer }

                call.respond(arbeidsgivere.copy(organisasjoner = unikeOrganisasjoner))
            } catch (e: Exception) {
                when (e) {
                    is TilgangNektetException -> call.respondTilgangNektetProblemDetail(e)
                    else -> throw e
                }
            }
        }
    }

    get(ORGANISASJONER_URL) {
        if(miljø == Configuration.Miljø.PROD) return@get call.respond(HttpStatusCode.NotImplemented)

        val org = ((call.request.queryParameters.getAll(orgQueryName)?.toSet()))
        val violations = validate(org)

        if (violations.isNotEmpty()) {
            throw Throwblem(ValidationProblemDetails(violations))
        } else {
            call.respond(
                arbeidsgivereService.hentOrganisasjoner(
                    idToken = idTokenProvider.getIdToken(call),
                    callId = call.getCallId(),
                    organisasjoner = org!!.map { Organisasjonsnummer.of(it) }.toSet()
                )
            )
        }
    }
}

private fun validate(organisasjonsnummere: Set<String>?): Set<Violation> = mutableSetOf<Violation>().apply {
    if (organisasjonsnummere.isNullOrEmpty()) {
        add(
            Violation(
                parameterName = orgQueryName,
                parameterType = ParameterType.QUERY,
                reason = "Påkrevd query parameter '$orgQueryName' er ikke satt.",
                invalidValue = organisasjonsnummere
            )
        )
    } else {
        organisasjonsnummere.forEachIndexed { index, it ->
            if (!it.erGyldigOrganisasjonsnummer()) {
                add(
                    Violation(
                        parameterName = "$orgQueryName[$index]",
                        parameterType = ParameterType.QUERY,
                        reason = "Query parameter $orgQueryName[$index] er av ugyldig format",
                        invalidValue = it
                    )
                )
            }
        }
    }
}

internal class FraOgMedTilOgMedValidatorArbeidsgiver {
    companion object {
        internal fun validate(
            fraOgMed: String?,
            tilOgMed: String?,
            parameterType: ParameterType
        ): Set<Violation> {
            val violations = mutableSetOf<Violation>()
            val parsedFraOgMed = parseDate(fraOgMed)
            val parsedTilOgMed = parseDate(tilOgMed)

            if (parsedFraOgMed == null) {
                violations.add(
                    Violation(
                        parameterName = "fra_og_med",
                        parameterType = parameterType,
                        reason = "Må settes og være på gyldig format (YYYY-MM-DD)",
                        invalidValue = fraOgMed
                    )
                )
            }
            if (parsedTilOgMed == null) {
                violations.add(
                    Violation(
                        parameterName = "til_og_med",
                        parameterType = parameterType,
                        reason = "Må settes og være på og gyldig format (YYYY-MM-DD)",
                        invalidValue = tilOgMed
                    )
                )
            }

            if (violations.isNotEmpty()) return violations
            return validate(
                fraOgMed = parsedFraOgMed!!,
                tilOgMed = parsedTilOgMed!!,
                parameterType = parameterType
            )

        }

        internal fun validate(
            fraOgMed: LocalDate,
            tilOgMed: LocalDate,
            parameterType: ParameterType
        ): Set<Violation> {
            val violations = mutableSetOf<Violation>()
            if (fraOgMed.isEqual(tilOgMed)) return violations

            if (!tilOgMed.isAfter(fraOgMed)) {
                violations.add(
                    Violation(
                        parameterName = "fra_og_med",
                        parameterType = parameterType,
                        reason = "Fra og med må være før eller lik til og med.",
                        invalidValue = DateTimeFormatter.ISO_DATE.format(fraOgMed)
                    )
                )
                violations.add(
                    Violation(
                        parameterName = "til_og_med",
                        parameterType = parameterType,
                        reason = "Til og med må være etter eller lik fra og med.",
                        invalidValue = DateTimeFormatter.ISO_DATE.format(tilOgMed)
                    )
                )
            }

            return violations
        }


        private fun parseDate(date: String?): LocalDate? {
            if (date == null) return null
            return try {
                LocalDate.parse(date)
            } catch (cause: Throwable) {
                null
            }
        }
    }
}
