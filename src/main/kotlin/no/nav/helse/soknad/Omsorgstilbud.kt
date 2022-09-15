package no.nav.helse.soknad

import no.nav.helse.k9format.ukedagerTilOgMed
import no.nav.helse.soknad.OmsorgstilbudSvarFremtid.*
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.tilsyn.TilsynPeriodeInfo
import no.nav.k9.søknad.ytelse.psb.v1.tilsyn.Tilsynsordning
import java.time.DayOfWeek
import java.time.DayOfWeek.*
import java.time.Duration
import java.time.Duration.ZERO
import java.time.LocalDate

data class Omsorgstilbud(
    val svarFortid: OmsorgstilbudSvarFortid? = null,
    val svarFremtid: OmsorgstilbudSvarFremtid? = null,
    val erLiktHverUke: Boolean? = null,
    val enkeltdager: List<Enkeltdag>? = null,
    val ukedager: PlanUkedager? = null
) {

    internal fun tilK9Tilsynsordning(
        periode: Periode,
        iGår: LocalDate = LocalDate.now().minusDays(1),
        iDag: LocalDate = LocalDate.now()
    ): Tilsynsordning {
        val tilsynsordning = Tilsynsordning()

        if(enkeltdager == null && ukedager == null) return tilsynsordning.medNullTimer(periode)

        enkeltdager?.let { beregnEnkeltdager(tilsynsordning) }

        if(ukedager != null && svarFortid != null) beregnUkedagerFortid(tilsynsordning, periode, iGår)
        if(ukedager != null && svarFremtid != null) beregnUkedagerFremtid(tilsynsordning, periode, iDag)

        return tilsynsordning
    }

    private fun beregnUkedagerFortid(tilsynsordning: Tilsynsordning, periode: Periode, iGår: LocalDate) {
        require(ukedager != null) { "Ukedager må være satt." }
        //Kan være at søknadsperiode kun er i fortiden. Derfor må vi sjekke om periode.tilOgMed er før eller etter i går.
        val gjeldendeTilOgMed = if(periode.tilOgMed.isBefore(iGår)) periode.tilOgMed else iGår
        when(svarFortid){
            OmsorgstilbudSvarFortid.JA -> beregnFraPlanUkedager(tilsynsordning, periode.fraOgMed, gjeldendeTilOgMed)
            OmsorgstilbudSvarFortid.NEI -> tilsynsordning.medNullTimer(Periode(periode.fraOgMed, gjeldendeTilOgMed))
            null -> null
        }
    }

    private fun beregnUkedagerFremtid(tilsynsordning: Tilsynsordning, periode: Periode, iDag: LocalDate){
        require(ukedager != null) { "Ukedager må være satt." }
        //Kan være at søknadsperiode kun er i fremtiden. Derfor må vi sjekke om periode.fraOgMed er før eller etter i dag.
        val gjeldendeFraOgMed = if(periode.fraOgMed.isBefore(iDag)) iDag else periode.fraOgMed
        when(svarFremtid){
            JA -> beregnFraPlanUkedager(tilsynsordning, gjeldendeFraOgMed, periode.tilOgMed)
            NEI, USIKKER -> tilsynsordning.medNullTimer(Periode(gjeldendeFraOgMed, periode.tilOgMed))
            null -> null
        }
    }

    private fun beregnEnkeltdager(tilsynsordning: Tilsynsordning) {
        require(enkeltdager != null) { "Enkeltdager må være satt" }
        enkeltdager.forEach { enkeltdag ->
            tilsynsordning.leggeTilPeriode(
                Periode(enkeltdag.dato, enkeltdag.dato),
                TilsynPeriodeInfo().medEtablertTilsynTimerPerDag(ZERO.plusOmIkkeNullOgAvkortTilNormalArbeidsdag(enkeltdag.tid))
            )
        }
    }

    private fun beregnFraPlanUkedager(tilsynsordning: Tilsynsordning, fraOgMed: LocalDate, tilOgMed: LocalDate) {
        require(ukedager != null) { "ukedager må være satt." }
        fraOgMed.ukedagerTilOgMed(tilOgMed).forEach { dato ->
            val tilsynslengde = ukedager.timerGittUkedag(dato.dayOfWeek)
            tilsynsordning.leggeTilPeriode(
                Periode(dato, dato),
                TilsynPeriodeInfo().medEtablertTilsynTimerPerDag(ZERO.plusOmIkkeNullOgAvkortTilNormalArbeidsdag(tilsynslengde)))
        }
    }

    private fun Tilsynsordning.medNullTimer(periode: Periode) = leggeTilPeriode(periode, TilsynPeriodeInfo().medEtablertTilsynTimerPerDag(ZERO))
}

enum class OmsorgstilbudSvarFortid { JA, NEI }
enum class OmsorgstilbudSvarFremtid { JA, NEI, USIKKER }

data class Enkeltdag(
    val dato: LocalDate,
    val tid: Duration
)

data class PlanUkedager(
    val mandag: Duration? = null,
    val tirsdag: Duration? = null,
    val onsdag: Duration? = null,
    val torsdag: Duration? = null,
    val fredag: Duration? = null
) {
    companion object{
        private val NULL_ARBEIDSTIMER = ZERO
    }

    internal fun timerGittUkedag(ukedag: DayOfWeek): Duration {
        return when(ukedag){
            MONDAY -> mandag ?: NULL_ARBEIDSTIMER
            TUESDAY -> tirsdag ?: NULL_ARBEIDSTIMER
            WEDNESDAY -> onsdag ?: NULL_ARBEIDSTIMER
            THURSDAY -> torsdag ?: NULL_ARBEIDSTIMER
            FRIDAY -> fredag ?: NULL_ARBEIDSTIMER
            SATURDAY, SUNDAY -> NULL_ARBEIDSTIMER
        }
    }
}