package no.nav.helse.soknad.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.general.krever
import no.nav.helse.general.kreverIkkeNull
import no.nav.helse.soknad.domene.arbeid.Arbeidsforhold
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import java.time.LocalDate

data class Frilans(
    val harInntektSomFrilanser: Boolean,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val startdato: LocalDate? = null,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val sluttdato: LocalDate? = null,
    val jobberFortsattSomFrilans: Boolean? = null,
    val arbeidsforhold: Arbeidsforhold? = null
) {

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        if(arbeidsforhold != null) addAll(arbeidsforhold.valider("$felt.arbeidsforhold"))
        if(sluttdato != null && startdato != null){
            krever(startdato.isBefore(sluttdato) || startdato.isEqual(sluttdato), "$felt.sluttdato kan ikke være etter startdato")
        }
        if(harInntektSomFrilanser){
            kreverIkkeNull(startdato, "$felt.startdao kan ikke være null dersom harInntektSomFrilanser=true")
            kreverIkkeNull(jobberFortsattSomFrilans, "$felt.jobberFortsattSomFrilans kan ikke være null dersom harInntektSomFrilanser=true")
        }
    }

    fun k9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        return when{
            (arbeidsforhold == null) -> Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(fraOgMed, tilOgMed)
            startetOgSluttetISøknadsperioden(fraOgMed, tilOgMed) -> k9ArbeidstidInfoMedStartOgSluttIPerioden(fraOgMed, tilOgMed)
            sluttetISøknadsperioden(tilOgMed) -> k9ArbeidstidInfoMedSluttIPerioden(fraOgMed, tilOgMed)
            startetISøknadsperioden(fraOgMed) -> k9ArbeidstidInfoMedStartIPerioden(fraOgMed, tilOgMed)
            else -> arbeidsforhold.tilK9ArbeidstidInfo(fraOgMed, tilOgMed)
        }
    }

    private fun k9ArbeidstidInfoMedStartOgSluttIPerioden(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        requireNotNull(arbeidsforhold)
        requireNotNull(startdato)
        requireNotNull(sluttdato)
        val arbeidsforholdFørStart = Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(fraOgMed, startdato.minusDays(1))
        val arbeidsforholdMedArbeid = arbeidsforhold.tilK9ArbeidstidInfo(startdato, sluttdato)
        val arbeidsforholdEtterSlutt = Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(sluttdato.plusDays(1), tilOgMed)
        return slåSammenArbeidstidInfo(arbeidsforholdFørStart, arbeidsforholdMedArbeid, arbeidsforholdEtterSlutt)

    }

    private fun k9ArbeidstidInfoMedStartIPerioden(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        requireNotNull(arbeidsforhold)
        requireNotNull(startdato)
        val arbeidsforholdFørStart = Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(fraOgMed, startdato.minusDays(1))
        val arbeidsforholdEtterStart = arbeidsforhold.tilK9ArbeidstidInfo(startdato, tilOgMed)
        return slåSammenArbeidstidInfo(arbeidsforholdFørStart, arbeidsforholdEtterStart)
    }

    private fun k9ArbeidstidInfoMedSluttIPerioden(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        requireNotNull(arbeidsforhold)
        requireNotNull(sluttdato)
        val arbeidsforholdFørSlutt = arbeidsforhold.tilK9ArbeidstidInfo(fraOgMed, sluttdato)
        val arbeidsforholdEtterSlutt = Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(sluttdato.plusDays(1), tilOgMed)
        return slåSammenArbeidstidInfo(arbeidsforholdFørSlutt, arbeidsforholdEtterSlutt)
    }


    private fun slåSammenArbeidstidInfo(vararg arbeidstidInfo: ArbeidstidInfo): ArbeidstidInfo {
        return ArbeidstidInfo().apply {
            arbeidstidInfo.forEach { arbeidstidInfo: ArbeidstidInfo ->
                arbeidstidInfo.perioder.forEach { (periode, arbeidstidPeriodeInfo): Map.Entry<no.nav.k9.søknad.felles.type.Periode, ArbeidstidPeriodeInfo> ->
                    this.leggeTilPeriode(
                        periode,
                        arbeidstidPeriodeInfo
                    )
                }
            }
        }
    }

    private fun sluttetISøknadsperioden(tilOgMed: LocalDate?) = (sluttdato != null && sluttdato.isBefore(tilOgMed))
    private fun startetISøknadsperioden(fraOgMed: LocalDate) = startdato?.isAfter(fraOgMed) ?: false
    private fun startetOgSluttetISøknadsperioden(fraOgMed: LocalDate, tilOgMed: LocalDate?) = sluttetISøknadsperioden(tilOgMed) && startetISøknadsperioden(fraOgMed)
}