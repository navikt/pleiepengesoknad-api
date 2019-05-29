package no.nav.helse.soknad

import no.nav.helse.dusseldorf.ktor.core.*
import no.nav.helse.vedlegg.Vedlegg
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val MAX_VEDLEGG_SIZE = 24 * 1024 * 1024 // 3 vedlegg på 8 MB
private val vedleggTooLargeProblemDetails = DefaultProblemDetails(title = "attachments-too-large", status = 413, detail = "Totale størreslsen på alle vedlegg overstiger maks på 24 MB.")
private const val MIN_GRAD = 20
private const val MAX_GRAD = 100


class FraOgMedTilOgMedValidator {
    companion object {
        internal fun validate(
            fraOgMed : String?,
            tilOgMed : String?,
            parameterType: ParameterType
        ) : Set<Violation> {
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
        ) : Set<Violation> {
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

        private fun parseDate(date: String?) : LocalDate? {
            if (date == null) return null
            return try { LocalDate.parse(date) } catch (cause : Throwable) { null }
        }
    }
}

internal fun Soknad.validate() {
    val violations = barn.validate(relasjonTilBarnet)

    // Arbeidsgivere
    arbeidsgivere.organisasjoner.mapIndexed { index, organisasjon ->
        if (!organisasjon.organisasjonsnummer.erGyldigOrganisasjonsnummer()) {
            violations.add(
                Violation(
                    parameterName = "arbeidsgivere.organisasjoner[$index].organisasjonsnummer",
                    parameterType = ParameterType.ENTITY,
                    reason = "Ikke gyldig organisasjonsnummer.",
                    invalidValue = organisasjon.organisasjonsnummer
                )
            )
        }
        if (organisasjon.navn != null && organisasjon.navn.erBlankEllerLengreEnn(100)) {
            violations.add(
                Violation(
                    parameterName = "arbeidsgivere.organisasjoner[$index].navn",
                    parameterType = ParameterType.ENTITY,
                    reason = "Navnet på organisasjonen kan ikke være tomt, og kan maks være 100 tegn.",
                    invalidValue = organisasjon.navn
                )
            )
        }
    }

    // Datoer
    violations.addAll(FraOgMedTilOgMedValidator.validate(
        fraOgMed = fraOgMed,
        tilOgMed = tilOgMed,
        parameterType = ParameterType.ENTITY
    ))

    // Vedlegg
    if (vedlegg.isEmpty()) {
        violations.add(
            Violation(
                parameterName = "vedlegg",
                parameterType = ParameterType.ENTITY,
                reason = "Det må sendes minst et vedlegg.",
                invalidValue = vedlegg
            )
        )
    }

    vedlegg.mapIndexed { index, url ->
        // Kan oppstå url = null etter Jackson deserialisering
        if (url == null || !url.path.matches(Regex("/vedlegg/.*"))) {
            violations.add(
                Violation(
                    parameterName = "vedlegg[$index]",
                    parameterType = ParameterType.ENTITY,
                    reason = "Ikke gyldig vedlegg URL.",
                    invalidValue = url
                )
            )
        }
    }

    // Grad
    if (grad < MIN_GRAD || grad > MAX_GRAD) {
        violations.add(
            Violation(
                parameterName = "grad",
                parameterType = ParameterType.ENTITY,
                reason = "Grad må være mellom $MIN_GRAD og $MAX_GRAD.",
                invalidValue = grad

        ))
    }

    // Booleans (For å forsikre at de er satt og ikke blir default false)
    fun booleanIkkeSatt(parameterName: String) {
        violations.add(
            Violation(
                parameterName = parameterName,
                parameterType = ParameterType.ENTITY,
                reason = "Må settes til true eller false.",
                invalidValue = null

        ))
    }
    if (medlemskap.harBoddIUtlandetSiste12Mnd == null) booleanIkkeSatt("medlemskap.har_bodd_i_utlandet_siste_12_mnd")
    if (medlemskap.skalBoIUtlandetNeste12Mnd == null) booleanIkkeSatt("medlemskap.skal_bo_i_utlandet_neste_12_mnd")
    if (harMedsoker == null) booleanIkkeSatt("har_medsoker")
    if (!harBekreftetOpplysninger) {
        violations.add(
            Violation(
                parameterName = "har_bekreftet_opplysninger",
                parameterType = ParameterType.ENTITY,
                reason = "Opplysningene må bekreftes for å sende inn søknad.",
                invalidValue = false

            ))
    }
    if (!harForstattRettigheterOgPlikter) {
        violations.add(
            Violation(
                parameterName = "har_forstatt_rettigheter_og_plikter",
                parameterType = ParameterType.ENTITY,
                reason = "Må ha forstått rettigheter og plikter for å sende inn søknad.",
                invalidValue = false

            ))
    }

    // Ser om det er noen valideringsfeil
    if (violations.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(violations))
    }

}

internal fun BarnDetaljer.validate(relasjonTilBarnet: String?) : MutableSet<Violation> {

    val violations = mutableSetOf<Violation>()

    if (!gyldigAntallIder()) {
        violations.add(
            Violation(
                parameterName = "barn",
                parameterType = ParameterType.ENTITY,
                reason = "Kan kun sette en av 'aktoer_id', 'fodselsnummer' eller 'alternativ_id' på barnet.",
                invalidValue = null
            )
        )
    }

    if (fodselsnummer != null && !fodselsnummer.erGyldigFodselsnummer()) {
        violations.add(
            Violation(
                parameterName = "barn.fodselsnummer",
                parameterType = ParameterType.ENTITY,
                reason = "Ikke gyldig fødselsnummer.",
                invalidValue = fodselsnummer
            )
        )
    }

    if (alternativId != null && (alternativId.erBlankEllerLengreEnn(50) || !alternativId.erKunSiffer())) {
        violations.add(
            Violation(
                parameterName = "barn.alternativ_id",
                parameterType = ParameterType.ENTITY,
                reason = "Ikke gyldig alternativ id. Kan kun inneholde tall og være maks 50 lang.",
                invalidValue = alternativId
            )
        )
    }


    val kreverNavnPaaBarnet = fodselsnummer != null
    if ((kreverNavnPaaBarnet || navn != null) && (navn == null || navn.erBlankEllerLengreEnn(100))) {
        violations.add(
            Violation(
                parameterName = "barn.navn",
                parameterType = ParameterType.ENTITY,
                reason = "Navn på barnet kan ikke være tomt, og kan maks være 100 tegn.",
                invalidValue = navn
            )
        )
    }

    val kreverRelasjonPaaBarnet = aktoerId == null
    if ((kreverRelasjonPaaBarnet || relasjonTilBarnet != null) && (relasjonTilBarnet == null || relasjonTilBarnet.erBlankEllerLengreEnn(100))) {
        violations.add(
            Violation(
                parameterName = "relasjon_til_barnet",
                parameterType = ParameterType.ENTITY,
                reason = "Relasjon til barnet kan ikke være tom og være mindre enn 100 tegn.",
                invalidValue = relasjonTilBarnet
            )
        )
    }


    return violations
}

private fun BarnDetaljer.gyldigAntallIder() : Boolean {
    val antallIderSatt = listOfNotNull(aktoerId, fodselsnummer, alternativId).size
    return antallIderSatt == 0 || antallIderSatt == 1
}

internal fun List<Vedlegg>.validerVedlegg(vedleggUrler: List<URL>) {
    if (size != vedleggUrler.size) {
        throw Throwblem(
            ValidationProblemDetails(
                violations = setOf(
                    Violation(
                        parameterName = "vedlegg",
                        parameterType = ParameterType.ENTITY,
                        reason = "Mottok referanse til ${vedleggUrler.size} vedlegg, men fant kun $size vedlegg.",
                        invalidValue = vedleggUrler
                    )
                )
            )
        )
    }
    validerTotalStorresle()
}

private fun List<Vedlegg>.validerTotalStorresle() {
    val totalSize = sumBy { it.content.size }
    if (totalSize > MAX_VEDLEGG_SIZE) {
        throw Throwblem(vedleggTooLargeProblemDetails)
    }
}


private fun String.erBlankEllerLengreEnn(maxLength: Int): Boolean = isBlank() || length > maxLength
