package no.nav.helse

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.k9format.tilK9Format
import no.nav.helse.soknad.*
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
                harMedsoker = false, medlemskap =  Medlemskap(
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
                harMedsoker = false, medlemskap = Medlemskap(
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
    fun `Skal feile dersom barnRelasjon er ANNET men barnRelasjonBeskrivelse er tom`() {
        Assertions.assertThrows(Throwblem::class.java) {
            val søknad = soknad(
                harMedsoker = false
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
        ansatt = listOf(
            ArbeidsforholdAnsatt(
                navn = "Org",
                organisasjonsnummer = "917755736",
                arbeidsforhold = Arbeidsforhold(
                    arbeidsform = Arbeidsform.FAST,
                    jobberNormaltTimer = 40.0,
                    erAktivtArbeidsforhold = null,
                    historisk = null,
                    planlagt = null
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
