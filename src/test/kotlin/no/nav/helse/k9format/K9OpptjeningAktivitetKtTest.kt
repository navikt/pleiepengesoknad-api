package no.nav.helse.k9format

import no.nav.helse.soknad.domene.FrilanserOppdrag
import no.nav.helse.soknad.domene.FrilanserOppdragIPerioden
import no.nav.helse.soknad.domene.FrilanserOppdragType
import no.nav.helse.soknad.domene.FrilanserV2
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class K9OpptjeningAktivitetKtTest {

    internal companion object {
        val mandag = LocalDate.parse("2022-11-15")
        val tirsdag = LocalDate.parse("2022-11-16")
        val onsdag = LocalDate.parse("2022-11-17")
        val torsdag = LocalDate.parse("2022-11-18")
        val fredag = LocalDate.parse("2022-11-19")
    }

    @Test
    fun `mapping av frilanserV2`() {
        val k9Frilanser = FrilanserV2(
            harInntektSomFrilanser = true,
            oppdrag = listOf(
                FrilanserOppdrag(
                    navn = "Oppdrag 1",
                    ansattFom = mandag,
                    ansattTom = tirsdag,
                    harOppdragIPerioden = FrilanserOppdragIPerioden.JA,
                    oppdragType = FrilanserOppdragType.FRILANSER,
                    manuellOppføring = false
                ),
                FrilanserOppdrag(
                    navn = "Oppdrag 2",
                    ansattFom = tirsdag,
                    ansattTom = onsdag,
                    harOppdragIPerioden = FrilanserOppdragIPerioden.JA,
                    oppdragType = FrilanserOppdragType.FRILANSER,
                    manuellOppføring = false
                ),
                FrilanserOppdrag(
                    navn = "Oppdrag 3",
                    ansattFom = onsdag,
                    ansattTom = torsdag,
                    harOppdragIPerioden = FrilanserOppdragIPerioden.JA,
                    oppdragType = FrilanserOppdragType.FRILANSER,
                    manuellOppføring = false
                ),
                FrilanserOppdrag(
                    navn = "Oppdrag 4",
                    ansattFom = torsdag,
                    ansattTom = null,
                    harOppdragIPerioden = FrilanserOppdragIPerioden.JA,
                    oppdragType = FrilanserOppdragType.FRILANSER,
                    manuellOppføring = false
                )
            )
        ).tilK9Frilanser()

        assertEquals(mandag, k9Frilanser.startdato)
        assertEquals(torsdag, k9Frilanser.sluttdato)
    }
}
