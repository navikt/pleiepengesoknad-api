package no.nav.helse.soknad

import no.nav.helse.dusseldorf.ktor.core.*
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarnValidator
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val MAX_VEDLEGG_SIZE = 24 * 1024 * 1024 // 3 vedlegg på 8 MB
private val vedleggTooLargeProblemDetails = DefaultProblemDetails(
    title = "attachments-too-large",
    status = 413,
    detail = "Totale størreslsen på alle vedlegg overstiger maks på 24 MB."
)
private const val MAX_FRITEKST_TEGN = 1000

class FraOgMedTilOgMedValidator {
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
                        parameterName = "fraOgMed",
                        parameterType = parameterType,
                        reason = "Må settes og være på gyldig format (YYYY-MM-DD)",
                        invalidValue = fraOgMed
                    )
                )
            }
            if (parsedTilOgMed == null) {
                violations.add(
                    Violation(
                        parameterName = "tilOgMed",
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
                        parameterName = "fraOgMed",
                        parameterType = parameterType,
                        reason = "Fra og med må være før eller lik til og med.",
                        invalidValue = DateTimeFormatter.ISO_DATE.format(fraOgMed)
                    )
                )
                violations.add(
                    Violation(
                        parameterName = "tilOgMed",
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

internal fun Søknad.validate(k9FormatSøknad: no.nav.k9.søknad.Søknad) {
    val violations = barn.validate()

    violations.addAll(arbeidsgivere.organisasjoner.validate())
    violations.addAll(validerSelvstendigVirksomheter(selvstendigVirksomheter, selvstendigArbeidsforhold))

    omsorgstilbud?.apply { violations.addAll(this.validate()) }

    violations.addAll(validerBarnRelasjon())

    // Datoer
    violations.addAll(
        FraOgMedTilOgMedValidator.validate(
            fraOgMed = fraOgMed,
            tilOgMed = tilOgMed,
            parameterType = ParameterType.ENTITY
        )
    )

    /*// Vedlegg
    if (vedlegg.isEmpty()) {
        violations.add(
            Violation(
                parameterName = "vedlegg",
                parameterType = ParameterType.ENTITY,
                reason = "Det må sendes minst et vedlegg.",
                invalidValue = vedlegg
            )
        )
    } TODO: Sett på validering igjen når det er påkrevd igjen*/

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

    violations.addAll(nullSjekk(medlemskap.harBoddIUtlandetSiste12Mnd, "medlemskap.harBoddIUtlandetSiste12Mnd"))
    violations.addAll(validerBosted(medlemskap.utenlandsoppholdSiste12Mnd))

    violations.addAll(nullSjekk(medlemskap.skalBoIUtlandetNeste12Mnd, "medlemskap.skalBoIUtlandetNeste12Mnd"))
    violations.addAll(validerBosted(medlemskap.utenlandsoppholdNeste12Mnd))

    if (utenlandsoppholdIPerioden != null) {
        violations.addAll(
            nullSjekk(
                utenlandsoppholdIPerioden.skalOppholdeSegIUtlandetIPerioden,
                "utenlandsoppholdIPerioden.skalOppholdeSegIUtlandetIPerioden"
            )
        )
    }

    violations.addAll(validerUtenladsopphold(utenlandsoppholdIPerioden?.opphold))
    violations.addAll(validerFerieuttakIPerioden(ferieuttakIPerioden))
    violations.addAll(nullSjekk(harMedsøker, "harMedsøker"))

    if (!harBekreftetOpplysninger) {
        violations.add(
            Violation(
                parameterName = "harBekreftetOpplysninger",
                parameterType = ParameterType.ENTITY,
                reason = "Opplysningene må bekreftes for å sende inn søknad.",
                invalidValue = false

            )
        )
    }
    if (!harForståttRettigheterOgPlikter) {
        violations.add(
            Violation(
                parameterName = "harForstattRettigheterOgPlikter",
                parameterType = ParameterType.ENTITY,
                reason = "Må ha forstått rettigheter og plikter for å sende inn søknad.",
                invalidValue = false

            )
        )
    }

    beredskap?.apply {
        violations.addAll(nullSjekk(beredskap, "beredskap.beredskap"))

        tilleggsinformasjon?.apply {
            if (length > MAX_FRITEKST_TEGN) {
                violations.add(
                    Violation(
                        parameterName = "beredskap.tilleggsinformasjon",
                        parameterType = ParameterType.ENTITY,
                        reason = "Kan maks være $MAX_FRITEKST_TEGN tegn, var $length.",
                        invalidValue = length
                    )
                )
            }
        }
    }

    nattevåk?.apply {
        violations.addAll(nullSjekk(nattevåk.harNattevåk, "nattevåk.harNattevåk"))
        tilleggsinformasjon?.apply {
            if (length > MAX_FRITEKST_TEGN) {
                violations.add(
                    Violation(
                        parameterName = "nattevaak.tilleggsinformasjon",
                        parameterType = ParameterType.ENTITY,
                        reason = "Kan maks være $MAX_FRITEKST_TEGN tegn, var $length.",
                        invalidValue = length
                    )
                )
            }
        }
    }

    violations.addAll(validerK9Format(k9FormatSøknad))

    if (violations.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(violations))
    }
}

private fun validerK9Format(k9FormatSøknad: no.nav.k9.søknad.Søknad): MutableSet<Violation> =
    PleiepengerSyktBarnValidator().valider(k9FormatSøknad.getYtelse<PleiepengerSyktBarn>()).map {
        Violation(
            parameterName = it.felt,
            parameterType = ParameterType.ENTITY,
            reason = it.feilmelding,
            invalidValue = "K9-format feilkode: ${it.feilkode}"
        )
    }.sortedBy { it.reason }.toMutableSet()

private fun validerSelvstendigVirksomheter(
    selvstendigVirksomheter: List<Virksomhet>, selvstendigArbeidsforhold: Arbeidsforhold?
): MutableSet<Violation> = mutableSetOf<Violation>().apply {
    if (selvstendigVirksomheter.isNotEmpty()) {
        selvstendigVirksomheter.mapIndexed { index, virksomhet ->
            addAll(virksomhet.validate(index))
        }

        if (selvstendigArbeidsforhold == null) {
            add(
                Violation(
                    parameterName = "selvstendigArbeidsforhold",
                    parameterType = ParameterType.ENTITY,
                    reason = "selvstendigArbeidsforhold kan ikke være null dersom selvstendigVirksomheter ikke er tom.",
                    invalidValue = selvstendigArbeidsforhold
                )
            )
        }

    } else {
        if (selvstendigArbeidsforhold != null) {
            add(
                Violation(
                    parameterName = "selvstendigArbeidsforhold",
                    parameterType = ParameterType.ENTITY,
                    reason = "selvstendigVirksomheter kan ikke være tom dersom selvstendigArbeidsforhold er satt.",
                    invalidValue = selvstendigVirksomheter
                )
            )
        }
    }
}

private fun validerBosted(list: List<Bosted>): MutableSet<Violation> {
    val violations = mutableSetOf<Violation>()
    list.mapIndexed { index, bosted ->
        val fraDataErEtterTilDato = bosted.fraOgMed.isAfter(bosted.tilOgMed)
        if (fraDataErEtterTilDato) {
            violations.add(
                Violation(
                    parameterName = "Utenlandsopphold[$index]",
                    parameterType = ParameterType.ENTITY,
                    reason = "Til dato kan ikke være før fra dato",
                    invalidValue = "fraOgMed eller tilOgMed"
                )
            )
        }
        if (bosted.landkode.isEmpty()) {
            violations.add(
                Violation(
                    parameterName = "Utenlandsopphold[$index]",
                    parameterType = ParameterType.ENTITY,
                    reason = "Landkode er ikke satt",
                    invalidValue = "landkode"
                )
            )
        }
        if (bosted.landnavn.isEmpty()) {
            violations.add(
                Violation(
                    parameterName = "Utenlandsopphold[$index]",
                    parameterType = ParameterType.ENTITY,
                    reason = "Landnavn er ikke satt",
                    invalidValue = "landnavn"
                )
            )
        }
    }
    return violations
}

private fun validerUtenladsopphold(list: List<Utenlandsopphold>?): MutableSet<Violation> {
    val violations = mutableSetOf<Violation>()
    list?.mapIndexed { index, utenlandsopphold ->
        val fraDataErEtterTilDato = utenlandsopphold.fraOgMed.isAfter(utenlandsopphold.tilOgMed)
        if (fraDataErEtterTilDato) {
            violations.add(
                Violation(
                    parameterName = "Utenlandsopphold[$index]",
                    parameterType = ParameterType.ENTITY,
                    reason = "Til dato kan ikke være før fra dato",
                    invalidValue = "fraOgMed eller tilOgMed"
                )
            )
        }
        if (utenlandsopphold.landkode.isEmpty()) {
            violations.add(
                Violation(
                    parameterName = "Utenlandsopphold[$index]",
                    parameterType = ParameterType.ENTITY,
                    reason = "Landkode er ikke satt",
                    invalidValue = "landkode"
                )
            )
        }
        if (utenlandsopphold.landnavn.isEmpty()) {
            violations.add(
                Violation(
                    parameterName = "Utenlandsopphold[$index]",
                    parameterType = ParameterType.ENTITY,
                    reason = "Landnavn er ikke satt",
                    invalidValue = "landnavn"
                )
            )
        }
        if (utenlandsopphold.årsak != null && utenlandsopphold.erBarnetInnlagt == false) {
            violations.add(
                Violation(
                    parameterName = "Utenlandsopphold[$index]",
                    parameterType = ParameterType.ENTITY,
                    reason = "Attributten årsak settes til null når erBarnetInnlagt er false",
                    invalidValue = "årsak eller erBarnetInnlagt"
                )
            )
        }
        if (utenlandsopphold.erBarnetInnlagt == true && utenlandsopphold.perioderBarnetErInnlagt.isEmpty()) {
            violations.add(
                Violation(
                    parameterName = "Utenlandsopphold[$index]",
                    parameterType = ParameterType.ENTITY,
                    reason = "Hvis erBarnetInnlagt er true så må perioderBarnetErInnlagt inneholde minst en periode",
                    invalidValue = "perioderBarnetErInnlagt"
                )
            )
        }
    }
    return violations
}

private fun validerFerieuttakIPerioden(ferieuttakIPerioden: FerieuttakIPerioden?): MutableSet<Violation> {
    val violations = mutableSetOf<Violation>()
    ferieuttakIPerioden?.ferieuttak?.mapIndexed { index, ferieuttak ->
        val fraDataErEtterTilDato = ferieuttak.fraOgMed.isAfter(ferieuttak.tilOgMed)
        if (fraDataErEtterTilDato) {
            violations.add(
                Violation(
                    parameterName = "Utenlandsopphold[$index]",
                    parameterType = ParameterType.ENTITY,
                    reason = "Til dato kan ikke være før fra dato",
                    invalidValue = "fraOgMed eller tilOgMed"
                )
            )
        }
    }
    return violations
}

fun Omsorgstilbud.validate() = mutableSetOf<Violation>().apply {
    when(val vet = vetOmsorgstilbud) {
        VetOmsorgstilbud.VET_IKKE -> {
            if (fasteDager != null || (enkeltDager != null && enkeltDager.isNotEmpty())) {
                add(
                    Violation(
                        parameterName = "omsorgstilbud.fasteDager eller omsorgstilbud.enkeltDager",
                        parameterType = ParameterType.ENTITY,
                        reason = "Dersom vetOmsorgstilbud er '$vet', så kan verken 'fasteDager' eller 'enkeltDager' være satt.",
                        invalidValue = "enkeltDager = $enkeltDager, fasteDager = $fasteDager"
                    )
                )
            }
        }

        else -> {
            if (fasteDager == null && enkeltDager.isNullOrEmpty()) {
                add(
                    Violation(
                        parameterName = "omsorgstilbud.fasteDager eller omsorgstilbud.enkeltDager",
                        parameterType = ParameterType.ENTITY,
                        reason = "Dersom vetOmsorgstilbud er '$vet', så må enten 'fasteDager' eller 'enkeltDager' være satt.",
                        invalidValue = "enkeltDager = $enkeltDager, fasteDager = $fasteDager"
                    )
                )
            }

            if (fasteDager != null && (enkeltDager != null && enkeltDager.isNotEmpty())) {
                add(
                    Violation(
                        parameterName = "omsorgstilbud.fasteDager og omsorgstilbud.enkeltDager",
                        parameterType = ParameterType.ENTITY,
                        reason = "Både 'fasteDager' og 'enkeltDager' kan ikke være satt samtidig.",
                        invalidValue = "enkeltDager = $enkeltDager, fasteDager = $fasteDager"
                    )
                )
            }
        }
    }
}

private fun Søknad.validerBarnRelasjon(): MutableSet<Violation> {
    val violations = mutableSetOf<Violation>()

    if (barnRelasjon == BarnRelasjon.ANNET && barnRelasjonBeskrivelse.isNullOrBlank()) {
        violations.add(
            Violation(
                parameterName = "barnRelasjonBeskrivelse",
                parameterType = ParameterType.ENTITY,
                reason = "Når barnRelasjon er ANNET, kan ikke barnRelasjonBeskrivelse være tom",
                invalidValue = barnRelasjonBeskrivelse
            )
        )
    }

    return violations
}

internal fun nullSjekk(verdi: Boolean?, navn: String): MutableSet<Violation> {
    val mangler: MutableSet<Violation> = mutableSetOf()

    if (verdi == null) {
        mangler.add(
            Violation(
                parameterName = navn,
                parameterType = ParameterType.ENTITY,
                reason = "$navn kan ikke være null",
                invalidValue = verdi
            )
        )
    }

    return mangler
}