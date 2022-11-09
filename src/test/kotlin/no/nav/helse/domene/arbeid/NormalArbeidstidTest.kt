package no.nav.helse.domene.arbeid

import no.nav.helse.soknad.domene.arbeid.NormalArbeidstid
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals

class NormalArbeidstidTest {

    @Test
    fun `Regner ut riktig timerPerDag fra timerPerUkeISnitt - 37,5 timer per uke gir 7,5 per dag`(){
        val normalarbeidstid = NormalArbeidstid(
            timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
        )

        assertEquals(Duration.ofHours(7).plusMinutes(30), normalarbeidstid.timerPerDagFraSnitt())
    }
}
