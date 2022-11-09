package no.nav.helse.soknad.domene.arbeid

import no.nav.helse.general.kreverIkkeNull
import no.nav.helse.soknad.Periode
import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriodeType.ARBEIDER_PROSENT_AV_NORMALT
import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriodeType.ARBEIDER_TIMER_I_SNITT_PER_UKE
import no.nav.helse.soknad.domene.arbeid.ArbeidIPeriodeType.ARBEIDER_ULIKE_UKER_TIMER
import java.time.Duration

class ArbeidIPeriode(
    val type: ArbeidIPeriodeType,
    val arbeiderIPerioden: ArbeiderIPeriodenSvar,
    val prosentAvNormalt: Double? = null,
    val timerPerUke: Duration? = null,
    val arbeidsuker: List<ArbeidsUke>? = null
) {

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        when(type){
            ARBEIDER_PROSENT_AV_NORMALT -> kreverIkkeNull(prosentAvNormalt, "$felt.prosentAvNormalt må være satt dersom type=ARBEIDER_PROSENT_AV_NORMALT")
            ARBEIDER_TIMER_I_SNITT_PER_UKE -> kreverIkkeNull(timerPerUke, "$felt.timerPerUke må være satt dersom type=ARBEIDER_TIMER_I_SNITT_PER_UKE")
            ARBEIDER_ULIKE_UKER_TIMER -> kreverIkkeNull(arbeidsuker, "$felt.arbeidsuker må være satt dersom type=ARBEIDER_ULIKE_UKER_TIMER")
            else -> {}
        }
    }

    override fun equals(other: Any?) = this === other || other is ArbeidIPeriode && this.equals(other)
    private fun equals(other: ArbeidIPeriode) = this.type == other.type
            && this.arbeiderIPerioden == other.arbeiderIPerioden
            && this.prosentAvNormalt == other.prosentAvNormalt
            && this.timerPerUke == other.timerPerUke
            && this.arbeidsuker == other.arbeidsuker

}

data class ArbeidsUke(val periode: Periode, val timer: Duration)
