package no.nav.helse.domene

import no.nav.helse.soknad.Omsorgstilbud
import no.nav.helse.soknad.OmsorgstilbudSvarFortid.JA
import no.nav.helse.soknad.OmsorgstilbudSvarFortid.NEI
import no.nav.helse.soknad.OmsorgstilbudSvarFremtid
import no.nav.helse.soknad.PlanUkedager
import no.nav.helse.soknad.domene.arbeid.NULL_TIMER
import no.nav.k9.søknad.felles.type.Periode
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class OmsorgstilbudTest {

    private val mandag = LocalDate.parse("2022-09-19")
    private val tirsdag = mandag.plusDays(1)
    private val onsdag = tirsdag.plusDays(1)
    private val torsdag = onsdag.plusDays(1)
    private val fredag = torsdag.plusDays(1)
    private val femTimer = Duration.ofHours(5)

    @Test
    fun `Omsorgstilbud uten enkeltdager eller ukedager skal gi tilsynsording med 0 timer i hele perioden`(){
        Omsorgstilbud(ukedager = null, enkeltdager = null).tilK9Tilsynsordning(Periode(mandag, fredag)).also {
            assertEquals(NULL_TIMER, it.perioder[Periode(mandag, fredag)]!!.etablertTilsynTimerPerDag)
        }
    }

    @Test
    fun `Omsorgstilbud med fortid hvor perioden ikke overlapper med iDag - forventer at dato oppgitt i periode brukes`(){
        Omsorgstilbud(ukedager = PlanUkedager(mandag = femTimer, tirsdag = femTimer, onsdag = femTimer), svarFortid = JA)
            .tilK9Tilsynsordning(Periode(mandag, onsdag), iDag = fredag)
            .also {
                assertEquals(3, it.perioder.size)
                listOf(mandag, tirsdag, onsdag).forEach { tilsynsdag ->
                    assertEquals(femTimer, it.perioder[Periode(tilsynsdag, tilsynsdag)]!!.etablertTilsynTimerPerDag)
                }
            }
    }

    @Test
    fun `Omsorgstilbud med fortid hvor perioden overlapper med iDag - forventer at dato for i går brukes`(){
        Omsorgstilbud(ukedager = PlanUkedager(mandag = femTimer, tirsdag = femTimer, onsdag = femTimer), svarFortid = JA)
            .tilK9Tilsynsordning(Periode(mandag, onsdag), iDag = onsdag)
            .also {
                assertEquals(2, it.perioder.size)
                listOf(mandag, tirsdag).forEach { tilsynsdag ->
                    assertEquals(femTimer, it.perioder[Periode(tilsynsdag, tilsynsdag)]!!.etablertTilsynTimerPerDag)
                }
            }
    }

    @Test
    fun `Omsorgstilbud med svarFortid=NEI og svarFremtid=JA med overlapp med iDag- forventer 0 timer for fortid og 5 timer for fremtid`(){
        Omsorgstilbud(svarFortid = NEI, svarFremtid = OmsorgstilbudSvarFremtid.JA, ukedager = PlanUkedager(femTimer, femTimer, femTimer, femTimer, femTimer))
            .tilK9Tilsynsordning(Periode(mandag, fredag), iDag = onsdag)
            .also {
                assertEquals(4, it.perioder.size)
                assertEquals(NULL_TIMER, it.perioder[Periode(mandag, tirsdag)]!!.etablertTilsynTimerPerDag)
                listOf(onsdag, torsdag, fredag).forEach { tilsynsdag ->
                    assertEquals(femTimer, it.perioder[Periode(tilsynsdag, tilsynsdag)]!!.etablertTilsynTimerPerDag)
                }
            }
    }

    @Test
    fun `Omsorgstilbud med fremtid hvor perioden ikke overlapper med iDag- forventer at dato oppgitt i periode brukes`(){
        Omsorgstilbud(ukedager = PlanUkedager(onsdag = femTimer, torsdag = femTimer, fredag = femTimer), svarFremtid = OmsorgstilbudSvarFremtid.JA)
            .tilK9Tilsynsordning(Periode(onsdag, fredag), iDag = tirsdag)
            .also {
                assertEquals(3, it.perioder.size)
                listOf(onsdag, torsdag, fredag).forEach { tilsynsdag ->
                    assertEquals(femTimer, it.perioder[Periode(tilsynsdag, tilsynsdag)]!!.etablertTilsynTimerPerDag)
                }
            }
    }

    @Test
    fun `Omsorgstilbud med fremtid hvor perioden overlapper med iDag - forventer at dato for i dag brukes`(){
        Omsorgstilbud(ukedager = PlanUkedager(onsdag = femTimer, torsdag = femTimer, fredag = femTimer), svarFremtid = OmsorgstilbudSvarFremtid.JA)
            .tilK9Tilsynsordning(Periode(onsdag, fredag), iDag = torsdag)
            .also {
                assertEquals(2, it.perioder.size)
                listOf(torsdag, fredag).forEach { tilsynsdag ->
                    assertEquals(femTimer, it.perioder[Periode(tilsynsdag, tilsynsdag)]!!.etablertTilsynTimerPerDag)
                }
            }
    }

    @Test
    fun `Omsorgstilbud med svarFortid=JA og svarFremtid=NEI med overlapp med iDag - forventer 5 timer for fortid og 0 timer for fremtid`(){
        Omsorgstilbud(svarFortid = JA, svarFremtid = OmsorgstilbudSvarFremtid.NEI, ukedager = PlanUkedager(femTimer, femTimer, femTimer, femTimer, femTimer))
            .tilK9Tilsynsordning(Periode(mandag, fredag), iDag = onsdag)
            .also {
                assertEquals(3, it.perioder.size)
                assertEquals(NULL_TIMER, it.perioder[Periode(onsdag, fredag)]!!.etablertTilsynTimerPerDag)
                listOf(mandag, tirsdag).forEach { tilsynsdag ->
                    assertEquals(femTimer, it.perioder[Periode(tilsynsdag, tilsynsdag)]!!.etablertTilsynTimerPerDag)
                }
            }
    }

    @Test
    fun `Omsorgstilbud med nei som svar i fortid og fremtid - forventer 0 timer for hele perioden`(){
        Omsorgstilbud(svarFortid = NEI, svarFremtid = OmsorgstilbudSvarFremtid.NEI)
            .tilK9Tilsynsordning(Periode(mandag, fredag), iDag = torsdag)
            .also {
                assertEquals(1, it.perioder.size)
                assertEquals(NULL_TIMER, it.perioder[Periode(mandag, fredag)]!!.etablertTilsynTimerPerDag)
            }
    }
}