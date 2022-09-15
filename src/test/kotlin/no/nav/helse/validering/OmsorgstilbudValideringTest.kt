package no.nav.helse.validering

import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.soknad.*
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OmsorgstilbudValideringTest {
    val gyldigOmsorgstilbud = Omsorgstilbud(
        svarFortid = OmsorgstilbudSvarFortid.JA,
        svarFremtid = OmsorgstilbudSvarFremtid.JA,
        erLiktHverUke = true,
        ukedager = PlanUkedager(
            mandag = Duration.ofHours(3)
        ),
        enkeltdager = null
    )

    @Test
    fun `Gyldig omsorgstilbud gir ingen feil`() {
        gyldigOmsorgstilbud.validate().assertIngenFeil()
    }

    @Test
    fun `Skal gi feil dersom svarFortid=JA, svarFremtid=JA og både ukedager og enkeldager er null`() {
        gyldigOmsorgstilbud.copy(
            svarFortid = OmsorgstilbudSvarFortid.JA,
            svarFremtid = OmsorgstilbudSvarFremtid.JA,
            ukedager = null,
            enkeltdager = null
        ).validate().assertFeilPå(
            listOf(
                "Ved svarFortid=JA kan ikke både enkeltdager og ukedager være null.",
                "Ved svarFremtid=JA kan ikke både enkeltdager og ukedager være null.",
                "Hvis erLiktHverUke er true må ukedager være satt."
            )
        )
    }

    @Test
    fun `Skal gi feil dersom både ukedager og enkeldager er satt`() {
        gyldigOmsorgstilbud.copy(
            ukedager = PlanUkedager(
                mandag = Duration.ofHours(3)
            ),
            enkeltdager = listOf(Enkeltdag(LocalDate.now(), Duration.ofHours(3)))
        ).validate().assertFeilPå(
            listOf(
                "Kan ikke ha både enkeltdager og ukedager satt, må velge en av de.",
                "Hvis erLiktHverUke er true må enkeldager være null."
            )
        )
    }

    @Test
    fun `Skal gi feil dersom erLiktHverUke er true og ukedager er null`() {
        gyldigOmsorgstilbud.copy(
            svarFortid = OmsorgstilbudSvarFortid.JA,
            svarFremtid = OmsorgstilbudSvarFremtid.NEI,
            erLiktHverUke = true,
            ukedager = null,
            enkeltdager = listOf(Enkeltdag(LocalDate.now(), Duration.ofHours(3)))
        ).validate().assertFeilPå(
            listOf(
                "Hvis erLiktHverUke er true må ukedager være satt.",
                "Hvis erLiktHverUke er true må enkeldager være null."
            )
        )
    }

    @Test
    fun `Skal gi feil dersom erLiktHverUke er true og enkeldager er satt`() {
        gyldigOmsorgstilbud.copy(
            erLiktHverUke = true,
            enkeltdager = listOf(Enkeltdag(LocalDate.now(), Duration.ofHours(3)))
        ).validate().assertFeilPå(
            listOf(
                "Hvis erLiktHverUke er true må enkeldager være null.",
                "Kan ikke ha både enkeltdager og ukedager satt, må velge en av de."
            )
        )
    }

    @Test
    fun `Skal gi feil dersom erLiktHverUke er false og ukedager er satt`() {
        gyldigOmsorgstilbud.copy(
            erLiktHverUke = false,
            ukedager = PlanUkedager(mandag = Duration.ofHours(2))
        ).validate().assertFeilPå(
            listOf(
                "Hvis erLiktHverUke er false kan ikke ukedager være satt.",
                "Hvis erLiktHverUke er false kan ikke enkeltdager være null."
            )
        )
    }

    @Test
    fun `Skal gi feil dersom erLiktHverUke er false og enkeltdager er null`() {
        gyldigOmsorgstilbud.copy(
            erLiktHverUke = false,
            enkeltdager = null
        ).validate().assertFeilPå(
            listOf(
                "Hvis erLiktHverUke er false kan ikke enkeltdager være null.",
                "Hvis erLiktHverUke er false kan ikke ukedager være satt."
            )
        )
    }

}

internal fun MutableSet<Violation>.assertIngenFeil() {
    assertTrue(size == 0)
}

internal fun MutableSet<Violation>.assertFeilPå(reason: List<String> = emptyList()) {
    println(this)
    assertEquals(size, reason.size)

    forEach {
        assertTrue(reason.contains(it.reason))
    }

}