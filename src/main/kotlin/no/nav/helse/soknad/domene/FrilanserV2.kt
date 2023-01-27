package no.nav.helse.soknad.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.general.krever
import no.nav.helse.general.kreverIkkeTom
import no.nav.helse.soknad.domene.arbeid.Arbeidsforhold
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import java.time.LocalDate
import no.nav.k9.søknad.felles.type.Periode as K9Periode

data class FrilanserV2(
    val harInntektSomFrilanser: Boolean,
    val oppdrag: List<FrilanserOppdrag>,
) {

    internal fun valider(felt: String, tilOgMed: LocalDate) = mutableListOf<String>().apply {
        if (harInntektSomFrilanser) {
            kreverIkkeTom(oppdrag, "$felt.oppdrag kan ikke være tom dersom $felt.harInntektSomFrilanser=true")
        }
        oppdrag.forEachIndexed { index, frilanserOppdrag ->
            addAll(frilanserOppdrag.valider("${felt}.oppdrag[$index]", tilOgMed))
        }
    }

    fun k9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        return if (!harInntektSomFrilanser) Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(fraOgMed, tilOgMed)
        else {
            oppdrag
                .map {
                    val k9ArbeidstidInfo = it.k9ArbeidstidInfo(fraOgMed, tilOgMed)
                    k9ArbeidstidInfo
                }
                .reduce { acc: ArbeidstidInfo, arbeidstidInfo: ArbeidstidInfo ->
                    arbeidstidInfo.perioder.forEach { (p: no.nav.k9.søknad.felles.type.Periode, periodeInfo: ArbeidstidPeriodeInfo) ->
                        if (p == null || periodeInfo == null) {
                            println("NULLLLLLLLL")
                        } else acc.leggeTilPeriode(p, periodeInfo)
                    }
                    acc
                }
        }
    }
}

data class FrilanserOppdrag(
    val navn: String,
    val organisasjonsnummer: String? = null,
    val offentligIdent: String? = null,
    val manuellOppføring: Boolean,
    val oppdragType: FrilanserOppdragType,
    val harOppdragIPerioden: FrilanserOppdragIPerioden,
    @JsonFormat(pattern = "yyyy-MM-dd") val ansattFom: LocalDate? = null,
    @JsonFormat(pattern = "yyyy-MM-dd") val ansattTom: LocalDate? = null,
    val styremedlemHeleInntekt: Boolean? = null,
    val arbeidsforhold: Arbeidsforhold? = null,
) {
    internal fun valider(felt: String, tilOgMed: LocalDate) = mutableListOf<String>().apply {
        if (arbeidsforhold != null) addAll(arbeidsforhold.valider("$felt.arbeidsforhold"))
        if (ansattFom != null && ansattTom != null) {
            krever(
                ansattFom.isBefore(ansattTom) || ansattFom.isEqual(ansattTom),
                "$felt.ansattTom kan ikke være etter ansattFom"
            )
        }
        when (harOppdragIPerioden) {
            FrilanserOppdragIPerioden.JA -> {
                krever(ansattTom == null, "$felt.ansattTom må være null dersom harOppdragIPerioden=JA")
            }
            FrilanserOppdragIPerioden.JA_MEN_AVSLUTTES_I_PERIODEN -> {
                krever(
                    ansattTom != null,
                    "$felt.ansattTom kan ikke være null dersom harOppdragIPerioden=JA_MEN_AVSLUTTES_I_PERIODEN"
                )
                kotlin.runCatching {
                    krever(
                        ansattTom!!.isBefore(tilOgMed),
                        "$felt.ansattTom må være før søknadsperiodens sluttdato dersom harOppdragIPerioden=JA_MEN_AVSLUTTES_I_PERIODEN"
                    )
                }
            }
            FrilanserOppdragIPerioden.NEI -> {
                krever(ansattFom == null, "$felt.ansattFom må være null dersom harOppdragIPerioden=NEI")
                krever(ansattTom == null, "$felt.ansattTom må være null dersom harOppdragIPerioden=NEI")
            }
        }

        when (oppdragType) {
            FrilanserOppdragType.STYREMELEM_ELLER_VERV -> {
                krever(
                    styremedlemHeleInntekt != null,
                    "Dersom $felt.oppdragType=STYREMELEM_ELLER_VERV, så kan ikke styremedlemHeleInntekt være null"
                )
                if (styremedlemHeleInntekt != null && styremedlemHeleInntekt) {
                    krever(
                        arbeidsforhold != null,
                        "Dersom $felt.oppdragType=STYREMELEM_ELLER_VERV og styremedlemHeleInntekt=true, så kan ikke arbeidsforhold være null"
                    )
                }
            }
            FrilanserOppdragType.FOSTERFORELDER -> krever(
                arbeidsforhold == null,
                "Dersom $felt.oppdragType=FOSTERFORELDER, må arbeidsforhold være null"
            )
            else -> {}
        }
    }

    fun k9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        return when (oppdragType) {

            // Fosterhjemsgodtgjørelse: blir ikke spurt om normaltid eller faktisk arbeidstid, sendes inn 0/0.
            FrilanserOppdragType.FOSTERFORELDER -> Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(fraOgMed, tilOgMed)

            // Omsorgsstønad: fører timer som alle andre, ekstra info om hva de skal ta utgangspunkt i.
            FrilanserOppdragType.OMSORGSSTØNAD -> {
                if (styremedlemHeleInntekt != null && styremedlemHeleInntekt == false)
                    Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(fraOgMed, tilOgMed)
                else håndterArbeidsforhold(fraOgMed, tilOgMed, arbeidsforhold)
            }

            // Styreverv og lignende: spm om de taper inntekten, hvis ja = føre timer, hvis nei = settes til 0/0.
            FrilanserOppdragType.STYREMELEM_ELLER_VERV -> {
                requireNotNull(styremedlemHeleInntekt) { "styremedlemHeleInntekt kan ikke være null" }
                when (styremedlemHeleInntekt) {
                    true -> håndterArbeidsforhold(fraOgMed, tilOgMed, arbeidsforhold)
                    false -> Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(fraOgMed, tilOgMed)
                }
            }

            FrilanserOppdragType.FRILANSER -> håndterArbeidsforhold(fraOgMed, tilOgMed, arbeidsforhold)
        }
    }

    private fun håndterArbeidsforhold(
        fraOgMed: LocalDate,
        tilOgMed: LocalDate,
        arbeidsforhold: Arbeidsforhold?,
    ) = when {
        (arbeidsforhold == null) -> Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(fraOgMed, tilOgMed)
        startetOgSluttetISøknadsperioden(fraOgMed, tilOgMed) -> k9ArbeidstidInfoMedStartOgSluttIPerioden(
            fraOgMed,
            tilOgMed
        )
        sluttetISøknadsperioden(tilOgMed) -> k9ArbeidstidInfoMedSluttIPerioden(fraOgMed, tilOgMed)
        startetISøknadsperioden(fraOgMed) -> k9ArbeidstidInfoMedStartIPerioden(fraOgMed, tilOgMed)
        else -> arbeidsforhold.tilK9ArbeidstidInfo(fraOgMed, tilOgMed)
    }

    private fun k9ArbeidstidInfoMedStartOgSluttIPerioden(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        requireNotNull(arbeidsforhold)
        requireNotNull(ansattFom)
        requireNotNull(ansattTom)
        val arbeidsforholdFørStart = Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(fraOgMed, ansattFom.minusDays(1))
        val arbeidsforholdMedArbeid = arbeidsforhold.tilK9ArbeidstidInfo(ansattFom, ansattTom)
        val arbeidsforholdEtterSlutt = Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(ansattTom.plusDays(1), tilOgMed)
        return slåSammenArbeidstidInfo(arbeidsforholdFørStart, arbeidsforholdMedArbeid, arbeidsforholdEtterSlutt)
    }

    private fun k9ArbeidstidInfoMedSluttIPerioden(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        requireNotNull(arbeidsforhold)
        requireNotNull(ansattTom)
        val arbeidsforholdFørSlutt = arbeidsforhold.tilK9ArbeidstidInfo(fraOgMed, ansattTom)
        val arbeidsforholdEtterSlutt = Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(ansattTom.plusDays(1), tilOgMed)
        return slåSammenArbeidstidInfo(arbeidsforholdFørSlutt, arbeidsforholdEtterSlutt)
    }

    private fun k9ArbeidstidInfoMedStartIPerioden(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        requireNotNull(arbeidsforhold)
        requireNotNull(ansattFom)
        val arbeidsforholdFørStart = Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(fraOgMed, ansattFom.minusDays(1))
        val arbeidsforholdEtterStart = arbeidsforhold.tilK9ArbeidstidInfo(ansattFom, tilOgMed)
        return slåSammenArbeidstidInfo(arbeidsforholdFørStart, arbeidsforholdEtterStart)
    }

    private fun slåSammenArbeidstidInfo(vararg arbeidstidInfo: ArbeidstidInfo): ArbeidstidInfo {
        return ArbeidstidInfo().apply {
            arbeidstidInfo.forEach { arbeidstidInfo: ArbeidstidInfo ->
                arbeidstidInfo.perioder.forEach { (periode, arbeidstidPeriodeInfo): Map.Entry<K9Periode, ArbeidstidPeriodeInfo> ->
                    this.leggeTilPeriode(
                        periode,
                        arbeidstidPeriodeInfo
                    )
                }
            }
        }
    }

    private fun sluttetISøknadsperioden(tilOgMed: LocalDate?) = (ansattTom != null && ansattTom.isBefore(tilOgMed))
    private fun startetISøknadsperioden(fraOgMed: LocalDate) = ansattFom?.isAfter(fraOgMed) ?: false
    private fun startetOgSluttetISøknadsperioden(fraOgMed: LocalDate, tilOgMed: LocalDate?) =
        sluttetISøknadsperioden(tilOgMed) && startetISøknadsperioden(fraOgMed)
}

enum class FrilanserOppdragIPerioden { JA, JA_MEN_AVSLUTTES_I_PERIODEN, NEI }
enum class FrilanserOppdragType { STYREMELEM_ELLER_VERV, FOSTERFORELDER, FRILANSER, OMSORGSSTØNAD }
