package no.nav.helse.soknad.domene

import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.helse.soknad.Land
import java.time.LocalDate

data class OpptjeningIUtlandet(
    val navn: String,
    val opptjeningType: OpptjeningType,
    val land: Land,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate
)

enum class OpptjeningType {
    ARBEIDSTAKER,
    FRILANSER
}

internal fun List<OpptjeningIUtlandet>.valider() = mutableSetOf<Violation>().apply {
    this@valider.forEachIndexed { index, opptjeningIUtlandet -> addAll(opptjeningIUtlandet.valider("opptjeningIUtlandet[$index]")) }
}

private fun OpptjeningIUtlandet.valider(felt: String) = mutableSetOf<Violation>().apply {
    this.addAll(land.valider("$felt.land"))
}