package no.nav.helse.domene.arbeid

import no.nav.helse.TestUtils.Companion.verifiserFeil
import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriode
import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriodeType
import no.nav.helse.soknad.domene.arbeid.ArbeiderIPeriodenSvar
import no.nav.helse.soknad.domene.arbeid.NormalArbeidstid
import java.time.Duration
import kotlin.test.Test

class ArbeidIPeriodeTest {

    companion object {
        private val normalArbeidstid = NormalArbeidstid(timerPerUkeISnitt = Duration.ofHours(40))
    }

    @Test
    fun `Skal gi feil dersom type=ARBEIDER_PROSENT_AV_NORMALT og prosentAvNormalt er null`() {
        ArbeidIPeriode(
            type = ArbeidIPeriodeType.ARBEIDER_PROSENT_AV_NORMALT,
            arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
            prosentAvNormalt = null
        ).valider("test").verifiserFeil(1)
    }

    @Test
    fun `Skal gi feil dersom type=ARBEIDER_TIMER_I_SNITT_PER_UKE og timerPerUke er null`() {
        ArbeidIPeriode(
            type = ArbeidIPeriodeType.ARBEIDER_TIMER_I_SNITT_PER_UKE,
            arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
            timerPerUke = null
        ).valider("test").verifiserFeil(1)
    }
}
