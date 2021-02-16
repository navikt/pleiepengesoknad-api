package no.nav.helse

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.k9format.tilK9Format
import no.nav.helse.soker.Søker
import no.nav.helse.soknad.*
import java.net.URL
import java.time.LocalDate
import kotlin.test.Ignore
import java.time.ZonedDateTime
import kotlin.test.Test

class SoknadValidationTest {

    @Test(expected = Throwblem::class)
    fun `Feiler på søknad dersom utenlandsopphold har til og fra dato som ikke kommer i rett rekkefølge`() {
        val søknad = soknad(
            harMedsoker = false, skalJobbeProsent = 0.0, medlemskap =  Medlemskap(
                harBoddIUtlandetSiste12Mnd = false,
                skalBoIUtlandetNeste12Mnd = true,
                utenlandsoppholdNeste12Mnd = listOf(
                    Bosted(
                        LocalDate.of(2022, 1, 4),
                        LocalDate.of(2022, 1, 3),
                        "US", "USA"
                    )
                )
            )
        )
        val k9Format = søknad.tilK9Format(ZonedDateTime.now(), SøknadUtils.søker)
        søknad.validate(k9Format)
    }

    @Test(expected = Throwblem::class)
    fun `Feiler på søknad dersom utenlandsopphold mangler landkode`() {
        val søknad = soknad(
            harMedsoker = false, skalJobbeProsent = 0.0, medlemskap = Medlemskap(
                harBoddIUtlandetSiste12Mnd = false,
                skalBoIUtlandetNeste12Mnd = true,
                utenlandsoppholdNeste12Mnd = listOf(
                    Bosted(
                        LocalDate.of(2022, 1, 2),
                        LocalDate.of(2022, 1, 3),
                        "", "USA"
                    )
                )
            )
        )
        val k9Format = søknad.tilK9Format(ZonedDateTime.now(), SøknadUtils.søker)
        søknad.validate(k9Format)
    }

    @Test
    fun `Skal ikke feile ved opphold på en dag`() {
        val søknad = soknad(
            harMedsoker = false, skalJobbeProsent = 0.0, skalJobbe = "nei"
        )
        val k9Format = søknad.tilK9Format(ZonedDateTime.now(), SøknadUtils.søker)

        søknad.validate(k9Format)
    }

    @Test
    fun `Søknad hvor perioden er 40 virkedager, skal ikke feile`(){
        val søknad = soknadMedFrilans(fraOgMed = LocalDate.parse("2020-01-01"), tilOgMed = LocalDate.parse("2020-02-26"))
        val k9Format = søknad.tilK9Format(ZonedDateTime.now(), SøknadUtils.søker)

        søknad.validate(k9Format)
    }

    @Test(expected = Throwblem::class)
    fun `Søknad hvor perioden er 41 virkedager hvor det ikke er bekreftet, skal feile`(){
        val søknad = soknadMedFrilans(fraOgMed = LocalDate.parse("2020-01-01"), tilOgMed = LocalDate.parse("2020-02-27"))
        val k9Format = søknad.tilK9Format(ZonedDateTime.now(), SøknadUtils.søker)

        søknad.validate(k9Format)
    }

    @Test
    fun `Søknad hvor perioden er 41 virkedager hvor det er bekreftet, skal ikke feile`(){
        val søknad = soknadMedFrilans(
            bekrefterPeriodeOver8Uker = true,
            fraOgMed = LocalDate.parse("2020-01-01"),
            tilOgMed = LocalDate.parse("2020-02-27")
        )
        val k9Format = søknad.tilK9Format(ZonedDateTime.now(), SøknadUtils.søker)

        søknad.validate(k9Format)
    }

    @Test(expected = Throwblem::class)
    fun `Skal feile dersom barnRelasjon er ANNET men barnRelasjonBeskrivelse er tom`() {
        val søknad = soknad(
            harMedsoker = false,
            skalJobbe = "nei"
        ).copy(
            barnRelasjon = BarnRelasjon.ANNET,
            barnRelasjonBeskrivelse = null
        )
        val k9Format = søknad.tilK9Format(ZonedDateTime.now(), SøknadUtils.søker)

        søknad.validate(k9Format)
    }

    @Ignore //TODO 09.02.2021 - Settes på når feltet er prodsatt
    @Test(expected = Throwblem::class)
    fun `Skal feile dersom "harVærtEllerErVernepliktig" blir satt til null`(){
        val søknad = soknad(skalJobbe = "nei").copy(harVærtEllerErVernepliktig = null)
        val k9Format = søknad.tilK9Format(ZonedDateTime.now(), SøknadUtils.søker)

        søknad.validate(k9Format)
    }

    private fun soknadMedFrilans(
        bekrefterPeriodeOver8Uker: Boolean = false,
        fraOgMed: LocalDate = LocalDate.now(),
        tilOgMed: LocalDate = LocalDate.now()
    ) = Søknad(
        newVersion = null,
        språk = Språk.nb,
        barn = BarnDetaljer(
            aktørId = null,
            fødselsnummer = null,
            fødselsdato = LocalDate.now(),
            navn = null
        ),
        arbeidsgivere = ArbeidsgiverDetaljer(listOf(
            OrganisasjonDetaljer(
                navn = "Org",
                organisasjonsnummer = "917755736",
                skalJobbeProsent = 10.0,
                jobberNormaltTimer = 10.0,
                skalJobbe = "redusert",
                arbeidsform = Arbeidsform.TURNUS
            )
        )),
        vedlegg = listOf(URL("http://localhost:8080/vedlegg/1")),
        fraOgMed = fraOgMed,
        tilOgMed = tilOgMed,
        bekrefterPeriodeOver8Uker = bekrefterPeriodeOver8Uker,
        medlemskap = Medlemskap(
            harBoddIUtlandetSiste12Mnd = false,
            skalBoIUtlandetNeste12Mnd = true
        ),
        harMedsøker = true,
        samtidigHjemme = true,

        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true,
        tilsynsordning = null,
        utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(skalOppholdeSegIUtlandetIPerioden = false, opphold = listOf()),
        ferieuttakIPerioden = FerieuttakIPerioden(skalTaUtFerieIPerioden = false, ferieuttak = listOf()),
        frilans = Frilans(
            jobberFortsattSomFrilans = true,
            startdato = LocalDate.now().minusDays(1)
        ),
        harVærtEllerErVernepliktig = true
        )

    private fun soknad(
        harMedsoker: Boolean = true,
        samtidigHjemme: Boolean? = false,
        skalJobbeProsent: Double = 0.0,
        vetIkkeEkstrainfo: String? = null,
        jobberNormalTimer: Double = 0.0,
        skalJobbe: String = "ja",
        medlemskap: Medlemskap = Medlemskap(
            harBoddIUtlandetSiste12Mnd = false,
            skalBoIUtlandetNeste12Mnd = true,
            utenlandsoppholdNeste12Mnd = listOf(
                Bosted(
                    LocalDate.of(2022, 1, 2),
                    LocalDate.of(2022, 1, 3),
                    "US", "USA"
                )
            )
        )
    ) = Søknad(
        newVersion = null,
        språk = Språk.nb,
        barn = BarnDetaljer(
            aktørId = null,
            fødselsnummer = null,
            fødselsdato = LocalDate.now(),
            navn = null
        ),
        arbeidsgivere = ArbeidsgiverDetaljer(
            listOf(
                OrganisasjonDetaljer(
                    navn = "Org",
                    organisasjonsnummer = "917755736",
                    skalJobbeProsent = skalJobbeProsent,
                    jobberNormaltTimer = jobberNormalTimer,
                    vetIkkeEkstrainfo = vetIkkeEkstrainfo,
                    skalJobbe = skalJobbe,
                    arbeidsform = Arbeidsform.TURNUS
                )
            )
        ),
        vedlegg = listOf(URL("http://localhost:8080/vedlegg/1")),
        fraOgMed = LocalDate.now(),
        tilOgMed = LocalDate.now(),
        medlemskap = medlemskap,
        harMedsøker = harMedsoker,
        samtidigHjemme = samtidigHjemme,
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true,
        tilsynsordning = null,
        utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(skalOppholdeSegIUtlandetIPerioden = false, opphold = listOf()),
        ferieuttakIPerioden = FerieuttakIPerioden(skalTaUtFerieIPerioden = false, ferieuttak = listOf()),
        barnRelasjon = null,
        barnRelasjonBeskrivelse = null,
        harVærtEllerErVernepliktig = true
        // harHattInntektSomFrilanser = false, default == false
    )
}
