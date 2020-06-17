package no.nav.helse

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.soknad.*
import java.net.URL
import java.time.LocalDate
import kotlin.test.Test

class SoknadValidationTest {

    @Test(expected = Throwblem::class)
    fun `Feiler på søknad dersom utenlandsopphold har til og fra dato som ikke kommer i rett rekkefølge`() {
        soknad(
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
            ).validate()
    }

    @Test(expected = Throwblem::class)
    fun `Feiler på søknad dersom utenlandsopphold mangler landkode`() {
        soknad(
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
        ).validate()
    }

    @Test
    fun `Skal ikke feile ved opphold på en dag`() {
        soknad(
           harMedsoker = false, skalJobbeProsent = 0.0, skalJobbe = "nei"
        ).validate()
    }

    @Test
    fun `Søknad hvor perioden er 40 virkedager, skal ikke feile`(){
        soknadMedFrilans(fraOgMed = LocalDate.parse("2020-01-01"), tilOgMed = LocalDate.parse("2020-02-26")).validate()
    }

    @Test(expected = Throwblem::class)
    fun `Søknad hvor perioden er 41 virkedager hvor det ikke er bekreftet, skal feile`(){
        soknadMedFrilans(fraOgMed = LocalDate.parse("2020-01-01"), tilOgMed = LocalDate.parse("2020-02-27")).validate()
    }

    @Test
    fun `Søknad hvor perioden er 41 virkedager hvor det er bekreftet, skal ikke feile`(){
        soknadMedFrilans(
            bekrefterPeriodeOver8Uker = true,
            fraOgMed = LocalDate.parse("2020-01-01"),
            tilOgMed = LocalDate.parse("2020-02-27")
        ).validate()
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
            fødselsdato = null,
            navn = null
        ),
        relasjonTilBarnet = "far",
        arbeidsgivere = ArbeidsgiverDetaljer(listOf(
            OrganisasjonDetaljer(
                navn = "Org",
                organisasjonsnummer = "917755736",
                skalJobbeProsent = 10.0,
                jobberNormaltTimer = 10.0,
                skalJobbe = "redusert"
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
            startdato = LocalDate.now().minusDays(1))
        )

    private fun soknad(
        harMedsoker: Boolean,
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
            fødselsdato = null,
            navn = null
        ),
        relasjonTilBarnet = "far",
        arbeidsgivere = ArbeidsgiverDetaljer(
            listOf(
                OrganisasjonDetaljer(
                    navn = "Org",
                    organisasjonsnummer = "917755736",
                    skalJobbeProsent = skalJobbeProsent,
                    jobberNormaltTimer = jobberNormalTimer,
                    vetIkkeEkstrainfo = vetIkkeEkstrainfo,
                    skalJobbe = skalJobbe
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
        ferieuttakIPerioden = FerieuttakIPerioden(skalTaUtFerieIPerioden = false, ferieuttak = listOf())
        // harHattInntektSomFrilanser = false, default == false
    )
}
