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
            harMedsoker = false, skalJobbeProsent = null,medlemskap =  Medlemskap(
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
            harMedsoker = false, skalJobbeProsent = null, medlemskap = Medlemskap(
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
           harMedsoker = false, skalJobbeProsent = null
        ).validate()
    }

    @Test
    fun `Håndterer søknad med frilans med oppdrag som ikke er pågående som er korrekt`(){
        soknadMedFrilans(true, listOf(Oppdrag(arbeidsgivernavn = "BariBar", fraOgMed = LocalDate.now().minusDays(1), tilOgMed = LocalDate.now(), erPagaende = false))).validate()
    }

    @Test
    fun `Håndterer søknad med frilans med oppdrag som er pågående som er korrekt`(){
        soknadMedFrilans(true, listOf(Oppdrag(arbeidsgivernavn = "BariBar", fraOgMed = LocalDate.now().minusDays(1), erPagaende = true))).validate()
    }

    @Test(expected = Throwblem::class)
    fun `Feiler på søknad med frilans med oppdrag som har sluttdato men også er pågående`(){
        soknadMedFrilans(false, listOf(Oppdrag(arbeidsgivernavn = "BariBar", fraOgMed = LocalDate.now().minusDays(1), tilOgMed = LocalDate.now(), erPagaende = true))).validate()
    }

    @Test(expected = Throwblem::class)
    fun `Feiler på søknad med frilans med oppdrag som har sluttdato nyere enn startdato`(){
        soknadMedFrilans(false, listOf(Oppdrag(arbeidsgivernavn = "BariBar", fraOgMed = LocalDate.now().minusDays(1), tilOgMed = LocalDate.now().minusDays(2), erPagaende = true))).validate()
    }

    @Test(expected = Throwblem::class)
    fun `Feiler på søknad med frilans med oppdrag som har blank arbeidsgivernavn`(){
        soknadMedFrilans(false, listOf(Oppdrag(arbeidsgivernavn = " ", fraOgMed = LocalDate.now().minusDays(1), tilOgMed = LocalDate.now(), erPagaende = true))).validate()
    }




    private fun soknadMedFrilans(jobberFortsattSomFrilans: Boolean, listeAvOppdrag: List<Oppdrag>) = Soknad(
        newVersion = null,
        sprak = Sprak.nb,
        barn = BarnDetaljer(
            aktoerId = null,
            fodselsnummer = null,
            fodselsdato = null,
            navn = null
        ),
        relasjonTilBarnet = "far",
        arbeidsgivere = ArbeidsgiverDetaljer(listOf(
            OrganisasjonDetaljer(
                navn = "Org",
                organisasjonsnummer = "917755736",
                skalJobbeProsent = 10.0,
                jobberNormaltTimer = 10.0,
                skalJobbe = "ja"
            )
        )),
        vedlegg = listOf(URL("http://localhost:8080/vedlegg/1")),
        fraOgMed = LocalDate.now(),
        tilOgMed = LocalDate.now(),
        medlemskap = Medlemskap(
            harBoddIUtlandetSiste12Mnd = false,
            skalBoIUtlandetNeste12Mnd = true
        ),
        harMedsoker = true,
        samtidigHjemme = true,

        harBekreftetOpplysninger = true,
        harForstattRettigheterOgPlikter = true,
        tilsynsordning = null,
        utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(skalOppholdeSegIUtlandetIPerioden = false, opphold = listOf()),
        ferieuttakIPerioden = FerieuttakIPerioden(skalTaUtFerieIPerioden = false, ferieuttak = listOf()),
        harHattInntektSomFrilanser = true,
        frilans = Frilans(
            harHattInntektSomFosterforelder = true,
            harHattOppdragForFamilie = true,
            startdato = LocalDate.now().minusDays(1),
            jobberFortsattSomFrilans = jobberFortsattSomFrilans,
            oppdrag = listeAvOppdrag)
        )

    private fun soknad(
        harMedsoker: Boolean,
        samtidigHjemme: Boolean? = false,
        skalJobbeProsent: Double?,
        vetIkkeEkstrainfo: String? = null,
        jobberNormalTimer: Double? = null,
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
    ) = Soknad(
        newVersion = null,
        sprak = Sprak.nb,
        barn = BarnDetaljer(
            aktoerId = null,
            fodselsnummer = null,
            fodselsdato = null,
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
                    skalJobbe = "ja"
                )
            )
        ),
        vedlegg = listOf(URL("http://localhost:8080/vedlegg/1")),
        fraOgMed = LocalDate.now(),
        tilOgMed = LocalDate.now(),
        medlemskap = medlemskap,
        harMedsoker = harMedsoker,
        samtidigHjemme = samtidigHjemme,

        harBekreftetOpplysninger = true,
        harForstattRettigheterOgPlikter = true,
        tilsynsordning = null,
        utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(skalOppholdeSegIUtlandetIPerioden = false, opphold = listOf()),
        ferieuttakIPerioden = FerieuttakIPerioden(skalTaUtFerieIPerioden = false, ferieuttak = listOf())
        // harHattInntektSomFrilanser = false, default == false

    )
}
