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
    fun `Skal ikke feile når harHattInntektSomFrilanser er satt til true og det finnes frilansobjekt`() {
        soknadMedFrilans(true).validate()
    }

    @Test(expected = Throwblem::class)
    fun `Skal feile når harHattInntektSomFrilanser er satt til false mens det finnes frilansobjekt`() {
        soknadMedFrilans(false).validate()
    }

    private fun soknadMedFrilans(harHattInntektSomFrilanser: Boolean) = Soknad(
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
                skalJobbe = "redusert"
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
        harHattInntektSomFrilanser = harHattInntektSomFrilanser,
        frilans = Frilans(
            jobberFortsattSomFrilans = true,
            startdato = LocalDate.now().minusDays(1))
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
