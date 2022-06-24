package no.nav.helse.soknad.domene

import no.nav.helse.general.krever
import no.nav.helse.soknad.Land
import java.time.LocalDate

class UtenlandskNæring(
    val næringstype: Næringstyper,
    val navnPåVirksomheten: String,
    val land: Land,
    val organisasjonsnummer: String,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate? = null
) {
    companion object {
        internal fun List<UtenlandskNæring>.valider(felt: String) = this.flatMapIndexed { index, utenlandskNæring ->
            utenlandskNæring.valider("$felt[$index]")
        }
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        addAll(land.validerV2("$felt.land"))
        tilOgMed?.let { krever(tilOgMed.erLikEllerEtter(fraOgMed), "$felt.tilOgMed må være lik eller etter fraOgMed") }
    }

    override fun equals(other: Any?) = other === this || other is UtenlandskNæring && this.equals(other)
    private fun equals(other: UtenlandskNæring) = this.organisasjonsnummer == other.organisasjonsnummer && this.navnPåVirksomheten == other.navnPåVirksomheten
}

internal fun LocalDate.erLikEllerEtter(tilOgMedDato: LocalDate) = this.isEqual(tilOgMedDato) || this.isAfter(tilOgMedDato)