package no.nav.helse.soknad

import no.nav.helse.k9format.DAGER_PER_UKE
import java.time.Duration

internal val NormalArbeidsdag = Duration.ofHours(7).plusMinutes(30)

internal fun Duration.somTekst(): String {
    val timer = seconds / 3600
    val minutter = (seconds % 3600) / 60
    val timerTeskst = when (timer) {
        0L -> ""
        1L -> "$timer time"
        else -> "$timer timer"
    }
    val minutterTekst = when (minutter) {
        0L -> ""
        1L -> "$minutter minutt"
        else -> "$minutter minutter"
    }

    val mellomTekst = if (timerTeskst.isNotBlank() && minutterTekst.isNotBlank()) " og " else ""
    val avkortetTekst = if (this > NormalArbeidsdag) " (avkortet til 7 timer og 30 minutter)" else ""

    return "$timerTeskst$mellomTekst$minutterTekst$avkortetTekst"
}

fun TilsynsordningJa.snittTilsynsTimerPerDag(): Duration = summerTilsynsdager().dividedBy(DAGER_PER_UKE.toLong())

private fun TilsynsordningJa.summerTilsynsdager() = Duration.ZERO
    .plusOmIkkeNullOgAvkortTilNormalArbeidsdag(mandag)
    .plusOmIkkeNullOgAvkortTilNormalArbeidsdag(tirsdag)
    .plusOmIkkeNullOgAvkortTilNormalArbeidsdag(onsdag)
    .plusOmIkkeNullOgAvkortTilNormalArbeidsdag(torsdag)
    .plusOmIkkeNullOgAvkortTilNormalArbeidsdag(fredag)

fun Duration.plusOmIkkeNullOgAvkortTilNormalArbeidsdag(duration: Duration?): Duration {
    return when {
        duration == null -> this
        duration > NormalArbeidsdag -> plus(NormalArbeidsdag)
        else -> plus(duration)
    }
}
