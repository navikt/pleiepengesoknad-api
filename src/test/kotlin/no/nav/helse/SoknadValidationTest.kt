package no.nav.helse

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.soknad.*
import java.net.URL
import java.time.LocalDate
import kotlin.test.Test

class SoknadValidationTest {

    @Test
    fun `Håndterer søknader med grad`() {
        soknad(grad = 50, harMedsoker = true, dagerPerUkeBorteFraJobb = null, skalJobbeProsent = null).validate()
    }

    @Test
    fun `Håndterer søknad uten grad og med medsøker`() {
        soknad(grad = null, harMedsoker = true, dagerPerUkeBorteFraJobb = 5.0, skalJobbeProsent = 22.00).validate()
    }

    @Test
    fun `Håndterer søknad uten grad og uten medsøker`() {
        soknad(grad = null, harMedsoker = false, dagerPerUkeBorteFraJobb = null, skalJobbeProsent = 22.00).validate()
    }

    @Test(expected = Throwblem::class)
    fun `Feiler på søknad uten grad og uten medsøker, men med dager per uke borte fra jobb satt`() {
        soknad(grad = null, harMedsoker = false, dagerPerUkeBorteFraJobb = 2.5, skalJobbeProsent = 22.00).validate()
    }

    @Test(expected = Throwblem::class)
    fun `Feiler på søknad uten grad, hvor skal jobbe prosent ikke er satt`() {
        soknad(grad = null, harMedsoker = false, dagerPerUkeBorteFraJobb = null, skalJobbeProsent = null).validate()
    }

    private fun soknad(
        grad: Int?,
        harMedsoker: Boolean,
        dagerPerUkeBorteFraJobb: Double?,
        skalJobbeProsent: Double?,
        jobberNormalTimer: Double?= null
    ) = Soknad(
        sprak = Sprak.nb,
        barn = BarnDetaljer(
            aktoerId = null,
            fodselsnummer = null,
            alternativId = null,
            navn = null
        ),
        relasjonTilBarnet = "far",
        arbeidsgivere = ArbeidsgiverDetaljer(listOf(
            OrganisasjonDetaljer(
                navn = "Org",
                organisasjonsnummer = "917755736",
                skalJobbeProsent = skalJobbeProsent,
                jobberNormaltTimer = jobberNormalTimer
            )
        )),
        vedlegg = listOf(URL("http://localhost:8080/vedlegg/1")),
        fraOgMed = LocalDate.now(),
        tilOgMed = LocalDate.now(),
        medlemskap = Medlemskap(
            harBoddIUtlandetSiste12Mnd = false,
            skalBoIUtlandetNeste12Mnd = true
        ),
        harMedsoker = harMedsoker,
        harBekreftetOpplysninger = true,
        harForstattRettigheterOgPlikter = true,
        dagerPerUkeBorteFraJobb =dagerPerUkeBorteFraJobb,
        grad = grad,
        tilsynsordning = null
    )
}