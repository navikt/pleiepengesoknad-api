package no.nav.helse.soknad

import no.nav.helse.dusseldorf.ktor.core.*
import no.nav.helse.vedlegg.Vedlegg
import java.net.URL
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private const val MAX_VEDLEGG_SIZE = 24 * 1024 * 1024 // 3 vedlegg på 8 MB
private const val ANTALL_VIRKEDAGER_8_UKER = 40
private val vedleggTooLargeProblemDetails = DefaultProblemDetails(
    title = "attachments-too-large",
    status = 413,
    detail = "Totale størreslsen på alle vedlegg overstiger maks på 24 MB."
)
private const val MIN_GRAD = 20
private const val MAX_GRAD = 100
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

internal fun Soknad.validate() {
    val violations = barn.validate(relasjonTilBarnet)
    violations.addAll(arbeidsgivere.organisasjoner.validate())
    tilsynsordning?.apply {
        violations.addAll(this.validate())
    }

    if(harHattInntektSomSelvstendigNaringsdrivende){
        if(selvstendigVirksomheter != null && selvstendigVirksomheter.isNotEmpty()){
            selvstendigVirksomheter.forEach { virksomhet ->
                violations.addAll(virksomhet.validate())
            }
        }
    }

    if(harHattInntektSomSelvstendigNaringsdrivende){
        if(selvstendigVirksomheter != null && selvstendigVirksomheter.isEmpty()){
            violations.add(
                Violation(
                    parameterName = "harHattInntektSomSelvstendigNaringsdrivende",
                    parameterType = ParameterType.ENTITY,
                    reason = "Hvis harHattInntektSomSelvstendigNaringsdrivende er true så kan ikke listen over virksomehter være tom",
                    invalidValue = harHattInntektSomSelvstendigNaringsdrivende
                )
            )
        }
    }

    // Datoer
    violations.addAll(
        FraOgMedTilOgMedValidator.validate(
            fraOgMed = fraOgMed,
            tilOgMed = tilOgMed,
            parameterType = ParameterType.ENTITY
        )
    )

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

    // Booleans (For å forsikre at de er satt og ikke blir default false)
    fun booleanIkkeSatt(parameterName: String) {
        violations.add(
            Violation(
                parameterName = parameterName,
                parameterType = ParameterType.ENTITY,
                reason = "Må settes til true eller false.",
                invalidValue = null

            )
        )
    }

    //Validerer at brukeren bekrefter dersom perioden er over 8 uker (40 virkedager)
    if(bekrefterPeriodeOver8Uker != null){
        var antallDagerIPerioden = fraOgMed.until(tilOgMed, ChronoUnit.DAYS)
        var dagSomSkalSjekkes: LocalDate = fraOgMed;

        while(!dagSomSkalSjekkes.isAfter(tilOgMed)){
            if(dagSomSkalSjekkes.dayOfWeek == DayOfWeek.SATURDAY || dagSomSkalSjekkes.dayOfWeek == DayOfWeek.SUNDAY) antallDagerIPerioden--
            dagSomSkalSjekkes = dagSomSkalSjekkes.plusDays(1)
        }

        if(antallDagerIPerioden > ANTALL_VIRKEDAGER_8_UKER && !bekrefterPeriodeOver8Uker){
            violations.add(
                Violation(
                    parameterName = "bekrefterPeriodeOver8Uker",
                    parameterType = ParameterType.ENTITY,
                    reason = "Hvis perioden er over 8 uker(40 virkedager) må bekrefterPeriodeOver8Uker være true"
                )
            )
        }
    }

    if (medlemskap.harBoddIUtlandetSiste12Mnd == null) booleanIkkeSatt("medlemskap.har_bodd_i_utlandet_siste_12_mnd")
    violations.addAll(validerBosted(medlemskap.utenlandsoppholdSiste12Mnd))
    if (medlemskap.skalBoIUtlandetNeste12Mnd == null) booleanIkkeSatt("medlemskap.skal_bo_i_utlandet_neste_12_mnd")
    violations.addAll(validerBosted(medlemskap.utenlandsoppholdNeste12Mnd))
    if (utenlandsoppholdIPerioden != null &&
        utenlandsoppholdIPerioden.skalOppholdeSegIUtlandetIPerioden == null
    ) {
        booleanIkkeSatt("utenlandsopphold_i_perioden.skal_oppholde_seg_i_utlandet_i_perioden")
    }
    violations.addAll(validerUtenladsopphold(utenlandsoppholdIPerioden?.opphold))
    violations.addAll(validerFerieuttakIPerioden(ferieuttakIPerioden))

    if (harMedsoker == null) booleanIkkeSatt("har_medsoker")
    if (!harBekreftetOpplysninger) {
        violations.add(
            Violation(
                parameterName = "har_bekreftet_opplysninger",
                parameterType = ParameterType.ENTITY,
                reason = "Opplysningene må bekreftes for å sende inn søknad.",
                invalidValue = false

            )
        )
    }
    if (!harForstattRettigheterOgPlikter) {
        violations.add(
            Violation(
                parameterName = "har_forstatt_rettigheter_og_plikter",
                parameterType = ParameterType.ENTITY,
                reason = "Må ha forstått rettigheter og plikter for å sende inn søknad.",
                invalidValue = false

            )
        )
    }

    if (harHattInntektSomFrilanser) {
        if (frilans == null) {
            violations.add(
                Violation(
                    parameterName = "har_hatt_inntekt_som_frilanser",
                    parameterType = ParameterType.ENTITY,
                    reason = "Dersom søkeren har hatt inntekter som frilanser, skal frilans objektet ikke være null"
                )
            )
        }
    }

    if (!harHattInntektSomFrilanser) {
        if (frilans != null) {
            violations.add(
                Violation(
                    parameterName = "har_hatt_inntekt_som_frilanser",
                    parameterType = ParameterType.ENTITY,
                    reason = "Dersom søkeren IKKE har hatt inntekter som frilanser, skal frilans objektet være null"
                )
            )
        }
    }

    beredskap?.apply {
        if (beredskap == null) booleanIkkeSatt("beredskap.i_beredskap")
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

    nattevaak?.apply {
        if (harNattevaak == null) booleanIkkeSatt("nattevaak.har_nattevaak")
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

    // Ser om det er noen valideringsfeil
    if (violations.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(violations))
    }
}

private fun validerBosted(
    list: List<Bosted>
): MutableSet<Violation> {
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

private fun validerUtenladsopphold(
    list: List<Utenlandsopphold>?
): MutableSet<Violation> {
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
        if (utenlandsopphold.arsak != null && utenlandsopphold.erBarnetInnlagt == false) {
            violations.add(
                Violation(
                    parameterName = "Utenlandsopphold[$index]",
                    parameterType = ParameterType.ENTITY,
                    reason = "Attributten arsak settes til null når er_barnet_innlagt er false",
                    invalidValue = "arsak eller erBarnetInnlagt"
                )
            )
        }
    }
    return violations
}

private fun validerFerieuttakIPerioden(
    ferieuttakIPerioden: FerieuttakIPerioden?
): MutableSet<Violation> {
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

internal fun Tilsynsordning.validate(): MutableSet<Violation> {
    val violations = mutableSetOf<Violation>()

    if (svar != TilsynsordningSvar.ja && ja != null) {
        violations.add(
            Violation(
                parameterName = "tilsynsordning.ja",
                parameterType = ParameterType.ENTITY,
                reason = "Skal kun settes om svar er 'ja'",
                invalidValue = ja.toString()
            )
        )
    }

    if (svar != TilsynsordningSvar.vet_ikke && vetIkke != null) {
        violations.add(
            Violation(
                parameterName = "tilsynsordning.vet_ikke",
                parameterType = ParameterType.ENTITY,
                reason = "Skal kun settes om svar er 'vet_ikke'",
                invalidValue = vetIkke.toString()
            )
        )
    }

    ja?.apply {
        tilleggsinformasjon?.apply {
            if (length > MAX_FRITEKST_TEGN) {
                violations.add(
                    Violation(
                        parameterName = "tilsynsordning.ja.tilleggsinformasjon",
                        parameterType = ParameterType.ENTITY,
                        reason = "Kan maks være $MAX_FRITEKST_TEGN tegn, var $length.",
                        invalidValue = length
                    )
                )
            }
        }
    }

    vetIkke?.apply {
        if (svar != TilsynsordningVetIkkeSvar.annet && annet != null) {
            violations.add(
                Violation(
                    parameterName = "tilsynsordning.vet_ikke.annet",
                    parameterType = ParameterType.ENTITY,
                    reason = "Skal kun settes om svar er 'annet''",
                    invalidValue = svar
                )
            )
        }

        if (svar == TilsynsordningVetIkkeSvar.annet && annet.isNullOrBlank()) {
            violations.add(
                Violation(
                    parameterName = "tilsynsordning.vet_ikke.annet",
                    parameterType = ParameterType.ENTITY,
                    reason = "Må settes når svar er 'annet",
                    invalidValue = svar
                )
            )
        }

        annet?.apply {
            if (length > MAX_FRITEKST_TEGN) {
                violations.add(
                    Violation(
                        parameterName = "tilsynsordning.vet_ikke.annet",
                        parameterType = ParameterType.ENTITY,
                        reason = "Kan maks være $MAX_FRITEKST_TEGN tegn, var $length.",
                        invalidValue = length
                    )
                )
            }
        }
    }

    return violations
}

internal fun BarnDetaljer.validate(relasjonTilBarnet: String?): MutableSet<Violation> {

    val violations = mutableSetOf<Violation>()

    if (!gyldigAntallIder()) {
        violations.add(
            Violation(
                parameterName = "barn",
                parameterType = ParameterType.ENTITY,
                reason = "Kan kun sette en av 'aktoer_id', 'fodselsnummer' på barnet.",
                invalidValue = null
            )
        )
    }

    if (fodselsnummer != null && !fodselsnummer.erKunSiffer()) {
        violations.add(
            Violation(
                parameterName = "barn.fodselsnummer",
                parameterType = ParameterType.ENTITY,
                reason = "Ikke gyldig fødselsnummer.",
                invalidValue = fodselsnummer
            )
        )
    }

    if (fodselsdato != null && (fodselsdato.isAfter(LocalDate.now()))) {
        violations.add(
            Violation(
                parameterName = "barn.fodselsdato",
                parameterType = ParameterType.ENTITY,
                reason = "Fødselsdato kan ikke være in fremtiden",
                invalidValue = fodselsdato
            )
        )
    }

    return violations
}

internal fun List<OrganisasjonDetaljer>.validate(
): MutableSet<Violation> {
    val violations = mutableSetOf<Violation>()

    mapIndexed { index, organisasjon ->
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

        // TODO: Fjern etter at dette er merget inn i master og er i prod.

        organisasjon.skalJobbeProsent?.apply {
            if (this !in 0.0..100.0) {
                violations.add(
                    Violation(
                        parameterName = "arbeidsgivere.organisasjoner[$index].skal_jobbe_prosent",
                        parameterType = ParameterType.ENTITY,
                        reason = "Skal jobbe prosent må være mellom 0 og 100.",
                        invalidValue = this
                    )
                )
            }
        }

        when (organisasjon.skalJobbe) {
            "ja" -> {
                //TODO: Fjern wrap med ?.let{ } når dette er prodsatt. skalJobbeProsent skal ikke lenger være optional.
                organisasjon.skalJobbeProsent?.let {
                    if (it !== 100.0) {
                        violations.add(
                            Violation(
                                parameterName = "arbeidsgivere.organisasjoner[$index].skal_jobbe_prosent && arbeidsgivere.organisasjoner[$index].skal_jobbe",
                                parameterType = ParameterType.ENTITY,
                                reason = "skalJobbeProsent er ulik 100%. Dersom skalJobbe = 'ja', så må skalJobbeProsent være 100%",
                                invalidValue = this
                            )
                        )
                    } else {}
                }
            }
            "nei" -> {
                //TODO: Fjern wrap med ?.let{ } når dette er prodsatt. skalJobbeProsent skal ikke lenger være optional.
                organisasjon.skalJobbeProsent?.let {
                    if (it !== 0.0) {
                        violations.add(
                            Violation(
                                parameterName = "arbeidsgivere.organisasjoner[$index].skal_jobbe_prosent && arbeidsgivere.organisasjoner[$index].skal_jobbe",
                                parameterType = ParameterType.ENTITY,
                                reason = "skalJobbeProsent er ulik 0%. Dersom skalJobbe = 'nei', så må skalJobbeProsent være 0%",
                                invalidValue = this
                            )
                        )
                    } else {
                    }
                }
            }
            "redusert" -> {
                //TODO: Fjern wrap med ?.let{ } når dette er prodsatt. skalJobbeProsent skal ikke lenger være optional.
                organisasjon.skalJobbeProsent?.let {
                    if (it !in 1.0..99.9) {
                        violations.add(
                            Violation(
                                parameterName = "arbeidsgivere.organisasjoner[$index].skal_jobbe_prosent && arbeidsgivere.organisasjoner[$index].skal_jobbe",
                                parameterType = ParameterType.ENTITY,
                                reason = "skalJobbeProsent ligger ikke mellom 1% og 99%. Dersom skalJobbe = 'redusert', så må skalJobbeProsent være mellom 1% og 99%",
                                invalidValue = this
                            )
                        )
                    } else {
                    }
                }
            }
            "vet_ikke" -> {
                //TODO: Fjern wrap med ?.let{ } når dette er prodsatt. skalJobbeProsent skal ikke lenger være optional.
                organisasjon.skalJobbeProsent?.let {
                    if (it != 0.0) {
                        violations.add(
                            Violation(
                                parameterName = "arbeidsgivere.organisasjoner[$index].skal_jobbe_prosent && arbeidsgivere.organisasjoner[$index].skal_jobbe",
                                parameterType = ParameterType.ENTITY,
                                reason = "skalJobbeProsent er ikke 0%. Dersom skalJobbe = 'vet ikke', så må skalJobbeProsent være mellom 0%",
                                invalidValue = this
                            )
                        )
                    } else {
                    }
                }
            }
            else -> violations.add(
                Violation(
                    parameterName = "arbeidsgivere.organisasjoner[$index].skal_jobbe",
                    parameterType = ParameterType.ENTITY,
                    reason = "Skal jobbe har ikke riktig verdi. Gyldige verdier er: ja, nei, redusert, vet_ikke",
                    invalidValue = organisasjon.skalJobbe
                )
            )
        }
    }
    return violations
}

private fun BarnDetaljer.gyldigAntallIder(): Boolean {
    val antallIderSatt = listOfNotNull(aktoerId, fodselsnummer).size
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
