package no.nav.helse.soknad

import no.nav.helse.dusseldorf.ktor.core.*
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
internal val vekttallProviderFnr1: (Int) -> Int = { arrayOf(3, 7, 6, 1, 8, 9, 4, 5, 2).reversedArray()[it] }
internal val vekttallProviderFnr2: (Int) -> Int = { arrayOf(5, 4, 3, 2, 7, 6, 5, 4, 3, 2).reversedArray()[it] }
private val fnrDateFormat = DateTimeFormatter.ofPattern("ddMMyy")

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

internal fun Søknad.validate() {
    val violations = barn.validate()

    violations.addAll(arbeidsgivere.organisasjoner.validate())
    violations.addAll(validerSelvstendigVirksomheter(selvstendigVirksomheter))

    tilsynsordning?.apply {
        violations.addAll(this.validate())
    }

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
    if (bekrefterPeriodeOver8Uker != null) {
        val antallVirkedagerIPerioden = antallVirkedagerIEnPeriode(fraOgMed, tilOgMed)

        if (antallVirkedagerIPerioden > ANTALL_VIRKEDAGER_8_UKER && !bekrefterPeriodeOver8Uker) {
            violations.add(
                Violation(
                    parameterName = "bekrefterPeriodeOver8Uker",
                    parameterType = ParameterType.ENTITY,
                    reason = "Hvis perioden er over 8 uker(40 virkedager) må bekrefterPeriodeOver8Uker være true"
                )
            )
        }
    }

    if (medlemskap.harBoddIUtlandetSiste12Mnd == null) booleanIkkeSatt("medlemskap.harBoddIUtlandetSiste12Mnd")
    violations.addAll(validerBosted(medlemskap.utenlandsoppholdSiste12Mnd))

    if (medlemskap.skalBoIUtlandetNeste12Mnd == null) booleanIkkeSatt("medlemskap.skalBoIUtlandetNeste12Mnd")
    violations.addAll(validerBosted(medlemskap.utenlandsoppholdNeste12Mnd))

    if (utenlandsoppholdIPerioden != null &&
        utenlandsoppholdIPerioden.skalOppholdeSegIUtlandetIPerioden == null
    ) {
        booleanIkkeSatt("utenlandsoppholdIPerioden.skalOppholdeSegIUtlandetIPerioden")
    }

    violations.addAll(validerUtenladsopphold(utenlandsoppholdIPerioden?.opphold))
    violations.addAll(validerFerieuttakIPerioden(ferieuttakIPerioden))

    if (harMedsøker == null) booleanIkkeSatt("harMedsøker")
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
        if (beredskap == null) booleanIkkeSatt("beredskap.beredskap")
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
        if (harNattevåk == null) booleanIkkeSatt("nattevåk.harNattevåk")
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

private fun validerSelvstendigVirksomheter(
    selvstendigVirksomheter: List<Virksomhet>
): MutableSet<Violation> = mutableSetOf<Violation>().apply {
    if (selvstendigVirksomheter.isNotEmpty()) {
        selvstendigVirksomheter.mapIndexed { index, virksomhet ->
            addAll(virksomhet.validate(index))
        }
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
        if(utenlandsopphold.erBarnetInnlagt == true && utenlandsopphold.perioderBarnetErInnlagt.isEmpty()){
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

    if (svar != TilsynsordningSvar.vetIkke && vetIkke != null) {
        violations.add(
            Violation(
                parameterName = "tilsynsordning.vetIkke",
                parameterType = ParameterType.ENTITY,
                reason = "Skal kun settes om svar er 'vetIkke'",
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
                    parameterName = "tilsynsordning.vetIkke.annet",
                    parameterType = ParameterType.ENTITY,
                    reason = "Skal kun settes om svar er 'annet''",
                    invalidValue = svar
                )
            )
        }

        if (svar == TilsynsordningVetIkkeSvar.annet && annet.isNullOrBlank()) {
            violations.add(
                Violation(
                    parameterName = "tilsynsordning.vetIkke.annet",
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
                        parameterName = "tilsynsordning.vetIkke.annet",
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

        organisasjon.skalJobbeProsent.apply {
            if (this !in 0.0..100.0) {
                violations.add(
                    Violation(
                        parameterName = "arbeidsgivere.organisasjoner[$index].skalJobbeProsent",
                        parameterType = ParameterType.ENTITY,
                        reason = "Skal jobbe prosent må være mellom 0 og 100.",
                        invalidValue = this
                    )
                )
            }
        }

        when (organisasjon.skalJobbe) {
            "ja" -> {
                organisasjon.skalJobbeProsent.let {
                    if (it != 100.0) {
                        violations.add(
                            Violation(
                                parameterName = "arbeidsgivere.organisasjoner[$index].skalJobbeProsent && arbeidsgivere.organisasjoner[$index].skalJobbe",
                                parameterType = ParameterType.ENTITY,
                                reason = "skalJobbeProsent er ulik 100%. Dersom skalJobbe = 'ja', så må skalJobbeProsent være 100%",
                                invalidValue = this
                            )
                        )
                    } else {
                    }
                }
            }
            "nei" -> {
                organisasjon.skalJobbeProsent.let {
                    if (it != 0.0) {
                        violations.add(
                            Violation(
                                parameterName = "arbeidsgivere.organisasjoner[$index].skalJobbeProsent && arbeidsgivere.organisasjoner[$index].skalJobbe",
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
                organisasjon.skalJobbeProsent.let {
                    if (it !in 1.0..99.9) {
                        violations.add(
                            Violation(
                                parameterName = "arbeidsgivere.organisasjoner[$index].skalJobbeProsent && arbeidsgivere.organisasjoner[$index].skalJobbe",
                                parameterType = ParameterType.ENTITY,
                                reason = "skalJobbeProsent ligger ikke mellom 1% og 99%. Dersom skalJobbe = 'redusert', så må skalJobbeProsent være mellom 1% og 99%",
                                invalidValue = this
                            )
                        )
                    } else {
                    }
                }
            }
            "vetIkke" -> {
                organisasjon.skalJobbeProsent.let {
                    if (it != 0.0) {
                        violations.add(
                            Violation(
                                parameterName = "arbeidsgivere.organisasjoner[$index].skalJobbeProsent && arbeidsgivere.organisasjoner[$index].skalJobbe",
                                parameterType = ParameterType.ENTITY,
                                reason = "skalJobbeProsent er ikke 0%. Dersom skalJobbe = 'vet ikke', så må skalJobbeProsent være 0%",
                                invalidValue = this
                            )
                        )
                    } else {
                    }
                }
            }
            else -> violations.add(
                Violation(
                    parameterName = "arbeidsgivere.organisasjoner[$index].skalJobbe",
                    parameterType = ParameterType.ENTITY,
                    reason = "Skal jobbe har ikke riktig verdi. Gyldige verdier er: ja, nei, redusert, vetIkke",
                    invalidValue = organisasjon.skalJobbe
                )
            )
        }
    }
    return violations
}

private fun Søknad.validerBarnRelasjon() : MutableSet<Violation> {
    val violations = mutableSetOf<Violation>()

    if(barnRelasjon == BarnRelasjon.ANNET && barnRelasjonBeskrivelse.isNullOrBlank()){
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

private fun String.erBlankEllerLengreEnn(maxLength: Int): Boolean = isBlank() || length > maxLength

fun String.erGyldigNorskIdentifikator(): Boolean {
    if (length != 11 || !erKunSiffer() || !starterMedFodselsdato()) return false

    val forventetKontrollsifferEn = get(9)

    val kalkulertKontrollsifferEn = Mod11.kontrollsiffer(
        number = substring(0, 9),
        vekttallProvider = vekttallProviderFnr1
    )

    if (kalkulertKontrollsifferEn != forventetKontrollsifferEn) return false

    val forventetKontrollsifferTo = get(10)

    val kalkulertKontrollsifferTo = Mod11.kontrollsiffer(
        number = substring(0, 10),
        vekttallProvider = vekttallProviderFnr2
    )

    return kalkulertKontrollsifferTo == forventetKontrollsifferTo
}

/**
 * https://github.com/navikt/helse-sparkel/blob/2e79217ae00632efdd0d4e68655ada3d7938c4b6/src/main/kotlin/no/nav/helse/ws/organisasjon/Mod11.kt
 * https://www.miles.no/blogg/tema/teknisk/validering-av-norske-data
 */
internal object Mod11 {
    private val defaultVekttallProvider: (Int) -> Int = { 2 + it % 6 }

    internal fun kontrollsiffer(
        number: String,
        vekttallProvider: (Int) -> Int = defaultVekttallProvider
    ): Char {
        return number.reversed().mapIndexed { i, char ->
            Character.getNumericValue(char) * vekttallProvider(i)
        }.sum().let(Mod11::kontrollsifferFraSum)
    }


    private fun kontrollsifferFraSum(sum: Int) = sum.rem(11).let { rest ->
        when (rest) {
            0 -> '0'
            1 -> '-'
            else -> "${11 - rest}"[0]
        }
    }
}

fun String.starterMedFodselsdato(): Boolean {
    // Sjekker ikke hvilket århundre vi skal tolket yy som, kun at det er en gyldig dato.
    // F.eks blir 290990 parset til 2090-09-29, selv om 1990-09-29 var ønskelig.
    // Kunne sett på individsifre (Tre første av personnummer) for å tolke århundre,
    // men virker unødvendig komplekst og sårbart for ev. endringer i fødselsnummeret.
    return try {
        var substring = substring(0, 6)
        val førsteSiffer = (substring[0]).toString().toInt()
        if (førsteSiffer in 4..7) {
            substring = (førsteSiffer - 4).toString() + substring(1, 6)
        }
        fnrDateFormat.parse(substring)

        true
    } catch (cause: Throwable) {
        false
    }
}

internal fun antallVirkedagerIEnPeriode(fraOgMed: LocalDate, tilOgMed: LocalDate) : Int {
    var antallDagerIPerioden = fraOgMed.until(tilOgMed, ChronoUnit.DAYS)
    var dagSomSkalSjekkes: LocalDate = fraOgMed;

    while (!dagSomSkalSjekkes.isAfter(tilOgMed)) {
        if (dagSomSkalSjekkes.dayOfWeek == DayOfWeek.SATURDAY || dagSomSkalSjekkes.dayOfWeek == DayOfWeek.SUNDAY) antallDagerIPerioden--
        dagSomSkalSjekkes = dagSomSkalSjekkes.plusDays(1)
    }

    return antallDagerIPerioden.toInt()
}