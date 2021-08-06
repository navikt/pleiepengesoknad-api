package no.nav.helse

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.k9format.tilK9Format
import no.nav.helse.soknad.Arbeidsform
import no.nav.helse.soknad.ArbeidsgiverDetaljer
import no.nav.helse.soknad.BarnDetaljer
import no.nav.helse.soknad.BarnRelasjon
import no.nav.helse.soknad.Bosted
import no.nav.helse.soknad.FerieuttakIPerioden
import no.nav.helse.soknad.Medlemskap
import no.nav.helse.soknad.OrganisasjonDetaljer
import no.nav.helse.soknad.SkalJobbe
import no.nav.helse.soknad.Språk
import no.nav.helse.soknad.Søknad
import no.nav.helse.soknad.UtenlandsoppholdIPerioden
import no.nav.helse.soknad.validate
import org.junit.jupiter.api.Assertions
import java.net.URL
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.test.Test

class SoknadValidationTest {

    @Test
    fun `Feiler på søknad dersom utenlandsopphold har til og fra dato som ikke kommer i rett rekkefølge`() {
        Assertions.assertThrows(Throwblem::class.java) {
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
    }

    @Test
    fun `Feiler på søknad dersom utenlandsopphold mangler landkode`() {
        Assertions.assertThrows(Throwblem::class.java) {
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
    }

    @Test
    fun `Skal ikke feile ved opphold på en dag`() {
        Assertions.assertDoesNotThrow {
            val søknad = soknad(
                harMedsoker = false, skalJobbeProsent = 0.0, skalJobbe = SkalJobbe.NEI
            )
            val k9Format = søknad.tilK9Format(ZonedDateTime.now(), SøknadUtils.søker)

            søknad.validate(k9Format)
        }
    }

    @Test
    fun `Skal feile dersom barnRelasjon er ANNET men barnRelasjonBeskrivelse er tom`() {
        Assertions.assertThrows(Throwblem::class.java) {
            val søknad = soknad(
                harMedsoker = false,
                skalJobbe = SkalJobbe.NEI
            ).copy(
                barnRelasjon = BarnRelasjon.ANNET,
                barnRelasjonBeskrivelse = null
            )
            val k9Format = søknad.tilK9Format(ZonedDateTime.now(), SøknadUtils.søker)

            søknad.validate(k9Format)
        }
    }

    private fun soknad(
        harMedsoker: Boolean = true,
        samtidigHjemme: Boolean? = false,
        skalJobbeProsent: Double = 0.0,
        vetIkkeEkstrainfo: String? = null,
        jobberNormalTimer: Double = 0.0,
        skalJobbe: SkalJobbe = SkalJobbe.JA,
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
            fødselsnummer = "02119970078",
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
        utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(skalOppholdeSegIUtlandetIPerioden = false, opphold = listOf()),
        ferieuttakIPerioden = FerieuttakIPerioden(skalTaUtFerieIPerioden = false, ferieuttak = listOf()),
        barnRelasjon = null,
        barnRelasjonBeskrivelse = null,
        harVærtEllerErVernepliktig = true
        // harHattInntektSomFrilanser = false, default == false
    )
}
