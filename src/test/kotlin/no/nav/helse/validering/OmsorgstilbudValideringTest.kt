package no.nav.helse.validering

import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.soknad.Enkeltdag
import no.nav.helse.soknad.Omsorgstilbud
import no.nav.helse.soknad.PlanUkedager
import no.nav.helse.soknad.validate
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OmsorgstilbudValideringTest {
    val gyldigOmsorgstilbud = Omsorgstilbud(
        erLiktHverDag = true,
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
    fun `Skal gi feil dersom både ukedager og enkeldager er satt`() {
        gyldigOmsorgstilbud.copy(
            ukedager = PlanUkedager(
                mandag = Duration.ofHours(3)
            ),
            enkeltdager = listOf(Enkeltdag(LocalDate.now(), Duration.ofHours(3)))
        ).validate().assertFeilPå(
            listOf(
                "Kan ikke ha både enkeltdager og ukedager satt, må velge en av de.",
                "Hvis erLiktHverDag er true må enkeldager være null."
            )
        )
    }

    @Test
    fun `Skal gi feil dersom både ukedager og enkeldager er null`() {
        gyldigOmsorgstilbud.copy(
            ukedager = null,
            enkeltdager = null
        ).validate().assertFeilPå(
            listOf(
                "Kan ikke ha både enkeldager og ukedager som null, en må være satt.",
                "Hvis erLiktHverDag er true må ukedager være satt."
            )
        )
    }

    @Test
    fun `Skal gi feil dersom erLiktHverDag er true og ukedager er null`() {
        gyldigOmsorgstilbud.copy(
            erLiktHverDag = true,
            ukedager = null,
            enkeltdager = null
        ).validate().assertFeilPå(
            listOf(
                "Hvis erLiktHverDag er true må ukedager være satt.",
                "Kan ikke ha både enkeldager og ukedager som null, en må være satt."
            )
        )
    }

    @Test
    fun `Skal gi feil dersom erLiktHverDag er true og enkeldager er satt`() {
        gyldigOmsorgstilbud.copy(
            erLiktHverDag = true,
            enkeltdager = listOf(Enkeltdag(LocalDate.now(), Duration.ofHours(3)))
        ).validate().assertFeilPå(
            listOf(
                "Hvis erLiktHverDag er true må enkeldager være null.",
                "Kan ikke ha både enkeltdager og ukedager satt, må velge en av de."
            )
        )
    }

    @Test
    fun `Skal gi feil dersom erLiktHverDag er false og ukedager er satt`() {
        gyldigOmsorgstilbud.copy(
            erLiktHverDag = false,
            ukedager = PlanUkedager(mandag = Duration.ofHours(2))
        ).validate().assertFeilPå(
            listOf(
                "Hvis erLiktHverDag er false kan ikke ukedager være satt.",
                "Hvis erLiktHverDag er false kan ikke enkeltdager være null."
            )
        )
    }

    @Test
    fun `Skal gi feil dersom erLiktHverDag er false og enkeltdager er null`() {
        gyldigOmsorgstilbud.copy(
            erLiktHverDag = false,
            enkeltdager = null
        ).validate().assertFeilPå(
            listOf(
                "Hvis erLiktHverDag er false kan ikke enkeltdager være null.",
                "Hvis erLiktHverDag er false kan ikke ukedager være satt."
            )
        )
    }

}

private fun MutableSet<Violation>.assertIngenFeil() {
    assertTrue(size == 0)
}

private fun MutableSet<Violation>.assertFeilPå(reason: List<String> = emptyList()) {
    println(this)
    assertEquals(size, reason.size)

    forEach {
        assertTrue(reason.contains(it.reason))
    }

}