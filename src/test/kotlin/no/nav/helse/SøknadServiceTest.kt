package no.nav.helse

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import no.nav.helse.barn.BarnService
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.dusseldorf.testsupport.jws.Azure
import no.nav.helse.general.CallId
import no.nav.helse.general.Metadata
import no.nav.helse.kafka.KafkaProducer
import no.nav.helse.soker.Søker
import no.nav.helse.soker.SøkerService
import no.nav.helse.soknad.Ferieuttak
import no.nav.helse.soknad.FerieuttakIPerioden
import no.nav.helse.soknad.MeldingRegistreringFeiletException
import no.nav.helse.soknad.SøknadService
import no.nav.helse.vedlegg.DokumentEier
import no.nav.helse.vedlegg.Vedlegg
import no.nav.helse.vedlegg.VedleggService
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.Test

internal class SøknadServiceTest{
    @RelaxedMockK
    lateinit var kafkaProducer: KafkaProducer

    @RelaxedMockK
    lateinit var søkerService: SøkerService

    @RelaxedMockK
    lateinit var barnService: BarnService

    @RelaxedMockK
    lateinit var vedleggService: VedleggService

    lateinit var søknadService: SøknadService

    @BeforeEach
    internal fun setUp() {
        MockKAnnotations.init(this)
        søknadService = SøknadService(
            kafkaProducer = kafkaProducer,
            vedleggService = vedleggService,
            søkerService = søkerService,
            barnService = barnService
        )
        assertNotNull(kafkaProducer)
        assertNotNull(søknadService)
    }

    @Test
    internal fun `Tester at den fjerner hold på persistert vedlegg dersom kafka feiler`() {
        assertThrows<MeldingRegistreringFeiletException> {
            runBlocking {
                coEvery {søkerService.getSoker(any(), any()) } returns Søker(
                    aktørId = "123",
                    fødselsdato = LocalDate.parse("2000-01-01"),
                    fødselsnummer = "02119970078"
                )

                coEvery {vedleggService.hentVedlegg(vedleggUrls = any(), any(), any(), any()) } returns listOf(Vedlegg("bytearray".toByteArray(), "vedlegg", "vedlegg", DokumentEier("290990123456")))

                every { kafkaProducer.produserPleiepengerMelding(any(), any()) } throws Exception("Mocket feil ved kafkaProducer")

                søknadService.registrer(
                    søknad = SøknadUtils.defaultSøknad().copy(
                        fraOgMed = LocalDate.now().minusDays(3),
                        tilOgMed = LocalDate.now().plusDays(4),
                        ferieuttakIPerioden = FerieuttakIPerioden(
                            skalTaUtFerieIPerioden = true,
                            ferieuttak = listOf(
                                Ferieuttak(
                                    fraOgMed = LocalDate.now(),
                                    tilOgMed = LocalDate.now().plusDays(2),
                                )
                            )
                        ),
                    ),
                    metadata = Metadata(
                        version = 1,
                        correlationId = "123"
                    ),
                    idToken = IdToken(Azure.V2_0.generateJwt(clientId = "ikke-authorized-client", audience = "omsorgsdager-melding-api")),
                    callId = CallId("abc")
                )
            }
        }

        coVerify(exactly = 1) { vedleggService.fjernHoldPåPersistertVedlegg(any(), any(), any()) }
    }
}
